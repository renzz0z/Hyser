package com.hyser.hysercore.abilities;

import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import java.util.Map;

public class Ability {
    private String id;
    private String name;
    private String description;
    private String permission;
    private int cooldown;
    private boolean enabled;
    private List<AbilityTrigger> triggers;
    private List<AbilityAction> actions;
    private List<AbilityCondition> conditions;
    
    public Ability(String id, ConfigurationSection config) {
        this.id = id;
        this.name = config.getString("name", id);
        this.description = config.getString("description", "");
        this.permission = config.getString("permission", "hysercore.ability." + id);
        this.cooldown = config.getInt("cooldown", 0);
        this.enabled = config.getBoolean("enabled", true);
    }
    
    public boolean canUse(Player player) {
        if (!enabled) return false;
        if (permission != null && !player.hasPermission(permission)) return false;
        
        // Verificar condiciones
        if (conditions != null) {
            for (AbilityCondition condition : conditions) {
                if (!condition.check(player)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public void execute(Player player) {
        if (actions != null) {
            for (AbilityAction action : actions) {
                action.execute(player);
            }
        }
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPermission() { return permission; }
    public int getCooldown() { return cooldown; }
    public boolean isEnabled() { return enabled; }
    public List<AbilityTrigger> getTriggers() { return triggers; }
    public List<AbilityAction> getActions() { return actions; }
    public List<AbilityCondition> getConditions() { return conditions; }
    
    // Setters
    public void setTriggers(List<AbilityTrigger> triggers) { this.triggers = triggers; }
    public void setActions(List<AbilityAction> actions) { this.actions = actions; }
    public void setConditions(List<AbilityCondition> conditions) { this.conditions = conditions; }
}