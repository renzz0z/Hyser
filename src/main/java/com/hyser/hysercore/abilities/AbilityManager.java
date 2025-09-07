package com.hyser.hysercore.abilities;

import com.hyser.hysercore.HyserCore;
import com.hyser.hysercore.abilities.triggers.*;
import com.hyser.hysercore.abilities.actions.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AbilityManager {
    private final HyserCore plugin;
    private FileConfiguration abilityConfig;
    private File abilityConfigFile;
    private Map<String, Ability> abilities;
    private Map<UUID, Map<String, Long>> cooldowns;
    private boolean enabled;
    private String prefix;
    
    public AbilityManager(HyserCore plugin) {
        this.plugin = plugin;
        this.abilities = new HashMap<>();
        this.cooldowns = new HashMap<>();
        loadConfig();
        loadAbilities();
    }
    
    private void loadConfig() {
        abilityConfigFile = new File(plugin.getDataFolder(), "ability.yml");
        if (!abilityConfigFile.exists()) {
            plugin.saveResource("ability.yml", false);
        }
        
        abilityConfig = YamlConfiguration.loadConfiguration(abilityConfigFile);
        this.enabled = abilityConfig.getBoolean("ability-system.enabled", true);
        this.prefix = ChatColor.translateAlternateColorCodes('&',
            abilityConfig.getString("ability-system.prefix", "&8[&dAbilities&8]&r "));
        
        plugin.getLogger().info("Ability system: " + (enabled ? "ENABLED" : "DISABLED"));
    }
    
    private void loadAbilities() {
        abilities.clear();
        
        if (!enabled) {
            plugin.getLogger().info("Abilities disabled, skipping load.");
            return;
        }
        
        ConfigurationSection abilitiesSection = abilityConfig.getConfigurationSection("abilities");
        if (abilitiesSection == null) {
            plugin.getLogger().warning("No abilities section found in ability.yml");
            return;
        }
        
        int loadedCount = 0;
        for (String abilityId : abilitiesSection.getKeys(false)) {
            ConfigurationSection abilitySection = abilitiesSection.getConfigurationSection(abilityId);
            if (abilitySection == null) continue;
            
            try {
                Ability ability = new Ability(abilityId, abilitySection);
                
                // Cargar triggers
                List<AbilityTrigger> triggers = loadTriggers(abilitySection);
                ability.setTriggers(triggers);
                
                // Cargar actions
                List<AbilityAction> actions = loadActions(abilitySection);
                ability.setActions(actions);
                
                // Cargar conditions (si existen)
                List<AbilityCondition> conditions = loadConditions(abilitySection);
                ability.setConditions(conditions);
                
                abilities.put(abilityId, ability);
                loadedCount++;
                
                plugin.getLogger().info("Loaded ability: " + abilityId + " (" + ability.getName() + ")");
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load ability: " + abilityId + " - " + e.getMessage());
                if (abilityConfig.getBoolean("ability-system.debug", false)) {
                    e.printStackTrace();
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + loadedCount + " abilities successfully.");
    }
    
    private List<AbilityTrigger> loadTriggers(ConfigurationSection abilitySection) {
        List<AbilityTrigger> triggers = new ArrayList<>();
        
        if (!abilitySection.contains("triggers")) {
            return triggers;
        }
        
        List<Map<?, ?>> triggersList = abilitySection.getMapList("triggers");
        for (Map<?, ?> triggerMap : triggersList) {
            String type = (String) triggerMap.get("type");
            if (type == null) continue;
            
            // Convertir Map a ConfigurationSection
            ConfigurationSection triggerConfig = abilityConfig.createSection("temp");
            for (Map.Entry<?, ?> entry : triggerMap.entrySet()) {
                triggerConfig.set(entry.getKey().toString(), entry.getValue());
            }
            
            AbilityTrigger trigger = createTrigger(type, triggerConfig);
            if (trigger != null) {
                triggers.add(trigger);
            }
        }
        
        return triggers;
    }
    
    private List<AbilityAction> loadActions(ConfigurationSection abilitySection) {
        List<AbilityAction> actions = new ArrayList<>();
        
        if (!abilitySection.contains("actions")) {
            return actions;
        }
        
        List<Map<?, ?>> actionsList = abilitySection.getMapList("actions");
        for (Map<?, ?> actionMap : actionsList) {
            String type = (String) actionMap.get("type");
            if (type == null) continue;
            
            // Convertir Map a ConfigurationSection
            ConfigurationSection actionConfig = abilityConfig.createSection("temp");
            for (Map.Entry<?, ?> entry : actionMap.entrySet()) {
                actionConfig.set(entry.getKey().toString(), entry.getValue());
            }
            
            AbilityAction action = createAction(type, actionConfig);
            if (action != null) {
                actions.add(action);
            }
        }
        
        return actions;
    }
    
    private List<AbilityCondition> loadConditions(ConfigurationSection abilitySection) {
        List<AbilityCondition> conditions = new ArrayList<>();
        
        if (!abilitySection.contains("conditions")) {
            return conditions;
        }
        
        // Implementación similar a loadActions pero para condiciones
        return conditions;
    }
    
    private AbilityTrigger createTrigger(String type, ConfigurationSection config) {
        switch (type.toUpperCase()) {
            case "RIGHT_CLICK":
            case "LEFT_CLICK":
            case "SHIFT_CLICK":
            case "SHIFT_RIGHT_CLICK":
            case "SHIFT_LEFT_CLICK":
                return new ClickTrigger(type, config);
            case "DOUBLE_SHIFT":
                return new DoubleShiftTrigger(type, config);
            default:
                plugin.getLogger().warning("Unknown trigger type: " + type);
                return null;
        }
    }
    
    private AbilityAction createAction(String type, ConfigurationSection config) {
        switch (type.toUpperCase()) {
            case "POTION_EFFECT":
                return new PotionEffectAction(type, config);
            case "SOUND":
                return new SoundAction(type, config);
            case "MESSAGE":
                return new MessageAction(type, config);
            case "PARTICLE":
                return new ParticleAction(type, config);
            default:
                plugin.getLogger().warning("Unknown action type: " + type);
                return null;
        }
    }
    
    public boolean handleEvent(Event event, Player player) {
        if (!enabled || abilities.isEmpty()) {
            return false;
        }
        
        boolean handled = false;
        
        for (Ability ability : abilities.values()) {
            if (!ability.isEnabled() || !ability.canUse(player)) {
                continue;
            }
            
            // Verificar cooldown
            if (isOnCooldown(player, ability.getId())) {
                continue;
            }
            
            // Verificar si algún trigger coincide
            if (ability.getTriggers() != null) {
                for (AbilityTrigger trigger : ability.getTriggers()) {
                    if (trigger.matches(event, player)) {
                        // Ejecutar habilidad
                        ability.execute(player);
                        
                        // Aplicar cooldown
                        if (ability.getCooldown() > 0) {
                            setCooldown(player, ability.getId(), ability.getCooldown());
                        }
                        
                        handled = true;
                        break; // Solo ejecutar una vez por habilidad
                    }
                }
            }
        }
        
        return handled;
    }
    
    private boolean isOnCooldown(Player player, String abilityId) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return false;
        }
        
        Long cooldownTime = playerCooldowns.get(abilityId);
        if (cooldownTime == null) {
            return false;
        }
        
        return System.currentTimeMillis() < cooldownTime;
    }
    
    private void setCooldown(Player player, String abilityId, int seconds) {
        UUID playerId = player.getUniqueId();
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>())
            .put(abilityId, System.currentTimeMillis() + (seconds * 1000L));
    }
    
    public void reload() {
        loadConfig();
        loadAbilities();
        plugin.getLogger().info("Ability system reloaded.");
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public Map<String, Ability> getAbilities() {
        return new HashMap<>(abilities);
    }
    
    public Ability getAbility(String id) {
        return abilities.get(id);
    }
    
    public String getPrefix() {
        return prefix;
    }
}