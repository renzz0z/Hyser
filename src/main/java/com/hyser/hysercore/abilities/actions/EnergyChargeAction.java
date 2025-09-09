package com.hyser.hysercore.abilities.actions;

import com.hyser.hysercore.abilities.AbilityAction;
import com.hyser.hysercore.utils.LangManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import org.bukkit.Sound;
import org.bukkit.Effect;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnergyChargeAction extends AbilityAction {
    private static final Map<UUID, Integer> energyLevels = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitRunnable> chargeTasks = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> chargeStartTime = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> allowedToCharge = new ConcurrentHashMap<>();
    
    private int maxEnergy;
    private long chargeIntervalTicks;
    private int energyPerInterval;
    private Plugin plugin;
    private boolean showProgress;
    private String chargeMessage;
    
    public EnergyChargeAction(String type, ConfigurationSection config) {
        super(type, config);
        this.maxEnergy = config.getInt("max_energy", 100);
        this.chargeIntervalTicks = config.getLong("charge_interval_ticks", 20L); // 1 segundo por defecto
        this.energyPerInterval = config.getInt("energy_per_interval", 10);
        this.showProgress = config.getBoolean("show_progress", true);
        this.chargeMessage = config.getString("charge_message", "&e⚡ Energía: &f{energy}/{max_energy} &e⚡");
    }
    
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void execute(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Si ya está cargando, detener la tarea anterior
        BukkitRunnable existingTask = chargeTasks.get(playerId);
        if (existingTask != null) {
            existingTask.cancel();
            chargeTasks.remove(playerId);
        }
        
        // Inicializar energía si no existe
        energyLevels.putIfAbsent(playerId, 0);
        chargeStartTime.put(playerId, System.currentTimeMillis());
        
        // Verificar si ya tiene una habilidad activa
        if (hasActiveAbility(playerId)) {
            player.sendMessage(getConfigMessage("sequential.already-charging"));
            return;
        }
        
        // Marcar como permitido para cargar
        allowedToCharge.put(playerId, true);
        
        player.sendMessage(getConfigMessage("energy.charge.start"));
        
        // Crear nueva tarea de carga
        BukkitRunnable chargeTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Verificar que el jugador sigue en línea
                if (!player.isOnline()) {
                    chargeTasks.remove(playerId);
                    energyLevels.remove(playerId);
                    chargeStartTime.remove(playerId);
                    cancel();
                    return;
                }
                
                // MODIFICADO: Ya no requiere objeto en mano después de activar
                // Solo verificar durante los primeros 3 segundos
                long elapsedTime = System.currentTimeMillis() - chargeStartTime.get(playerId);
                if (elapsedTime < 3000 && !hasAbilityItemInHand(player)) {
                    player.sendMessage(getConfigMessage("energy.charge.interrupted"));
                    allowedToCharge.remove(playerId);
                    chargeTasks.remove(playerId);
                    cancel();
                    return;
                }
                
                int currentEnergy = energyLevels.get(playerId);
                
                if (currentEnergy >= maxEnergy) {
                    // Energía máxima alcanzada
                    player.sendMessage(getConfigMessage("energy.charge.max-reached"));
                    player.sendMessage(getConfigMessage("sequential.ability-ready"));
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                    player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 0);
                    chargeTasks.remove(playerId);
                    cancel();
                    return;
                }
                
                // Incrementar energía
                int newEnergy = Math.min(currentEnergy + energyPerInterval, maxEnergy);
                energyLevels.put(playerId, newEnergy);
                
                // Mostrar progreso
                if (showProgress && newEnergy % (energyPerInterval * 3) == 0) { // Cada 3 intervalos
                    String message = getConfigMessage("energy.charge.progress")
                        .replace("{energy}", String.valueOf(newEnergy))
                        .replace("{max_energy}", String.valueOf(maxEnergy));
                    player.sendMessage(message);
                    
                    // Efectos visuales y sonoros
                    player.playSound(player.getLocation(), Sound.FIREWORK_TWINKLE, 0.5f, 1.5f);
                    player.getWorld().playEffect(player.getLocation().add(0, 1, 0), Effect.MOBSPAWNER_FLAMES, 0);
                }
            }
        };
        
        chargeTasks.put(playerId, chargeTask);
        chargeTask.runTaskTimer(plugin, chargeIntervalTicks, chargeIntervalTicks);
    }
    
    private boolean hasAbilityItemInHand(Player player) {
        org.bukkit.inventory.ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null || !itemInHand.hasItemMeta()) {
            return false;
        }
        
        org.bukkit.inventory.meta.ItemMeta meta = itemInHand.getItemMeta();
        java.util.List<String> lore = meta.getLore();
        
        if (lore != null) {
            for (String line : lore) {
                if (line.contains("Usos restantes:")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // Métodos estáticos para acceder a la energía
    public static int getEnergyLevel(UUID playerId) {
        return energyLevels.getOrDefault(playerId, 0);
    }
    
    public static boolean hasMaxEnergy(UUID playerId, int maxEnergy) {
        return energyLevels.getOrDefault(playerId, 0) >= maxEnergy;
    }
    
    public static void consumeEnergy(UUID playerId) {
        energyLevels.remove(playerId);
        chargeStartTime.remove(playerId);
        allowedToCharge.remove(playerId);
        
        // Cancelar tarea de carga si existe
        BukkitRunnable task = chargeTasks.get(playerId);
        if (task != null) {
            task.cancel();
            chargeTasks.remove(playerId);
        }
    }
    
    public static void stopCharging(UUID playerId) {
        BukkitRunnable task = chargeTasks.get(playerId);
        if (task != null) {
            task.cancel();
            chargeTasks.remove(playerId);
        }
    }
    
    public static long getChargeTime(UUID playerId) {
        Long startTime = chargeStartTime.get(playerId);
        return startTime != null ? System.currentTimeMillis() - startTime : 0;
    }
    
    public static void clearPlayerData(UUID playerId) {
        energyLevels.remove(playerId);
        chargeStartTime.remove(playerId);
        allowedToCharge.remove(playerId);
        stopCharging(playerId);
    }
    
    public static boolean hasActiveAbility(UUID playerId) {
        return allowedToCharge.containsKey(playerId) || energyLevels.containsKey(playerId);
    }
    
    private String getConfigMessage(String key) {
        LangManager langManager = LangManager.getInstance();
        if (langManager != null) {
            return langManager.getMessage(key);
        }
        // Fallback messages
        switch (key) {
            case "energy.charge.start": return ChatColor.YELLOW + "⚡ ¡Comenzando carga de energía! Mantén el objeto en mano...";
            case "energy.charge.interrupted": return ChatColor.RED + "⚡ Carga interrumpida: necesitas mantener el objeto en mano";
            case "energy.charge.max-reached": return ChatColor.GREEN + "⚡ ¡ENERGÍA MÁXIMA ALCANZADA! Usa doble shift para liberar";
            case "energy.charge.progress": return "&e⚡ Energía: &f{energy}/{max_energy} &e⚡";
            case "sequential.ability-ready": return ChatColor.AQUA + "⚡ Habilidad cargada: Haz doble shift para activar";
            case "sequential.already-charging": return ChatColor.RED + "⚡ Ya tienes una habilidad activa. Úsala primero o espera a que expire";
            default: return ChatColor.GRAY + "Mensaje no encontrado";
        }
    }
}