package com.hyser.hysercore.prison;

import com.hyser.hysercore.HyserCore;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Arrow;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manager SIMPLIFICADO para el sistema de PrisonPunch
 * Solo maneja el empuje de flechas, sin modificar knockback complejo
 */
public class PrisonPunchManager {
    
    private final HyserCore plugin;
    private FileConfiguration config;
    private final Map<UUID, Long> lastShotTime;
    
    public PrisonPunchManager(HyserCore plugin) {
        this.plugin = plugin;
        this.lastShotTime = new HashMap<>();
        loadConfig();
    }
    
    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "prisonpunch.yml");
        if (!configFile.exists()) {
            plugin.saveResource("prisonpunch.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    public void reloadConfig() {
        loadConfig();
        plugin.getLogger().info("Configuración de empuje de flechas recargada");
    }
    
    public boolean isSystemEnabled() {
        return config.getBoolean("enabled", true);
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * Verifica si se puede aplicar empuje de flecha
     */
    public boolean canApplyArrowPush(Player shooter, Player victim) {
        // Verificar si el sistema está habilitado en este mundo
        String worldName = shooter.getWorld().getName();
        if (!config.getStringList("worlds.enabled-worlds").contains(worldName)) {
            return false;
        }
        
        if (config.getStringList("worlds.disabled-worlds").contains(worldName)) {
            return false;
        }
        
        // Verificar distancia máxima
        double maxDistance = config.getDouble("prison.maxDistance", 50.0);
        if (shooter.getLocation().distance(victim.getLocation()) > maxDistance) {
            return false;
        }
        
        // Verificar zonas especiales (esto se puede integrar con WorldGuard en el futuro)
        if (config.getBoolean("prison.disableInSafeZones", true)) {
            // Aquí se podría verificar con WorldGuard si está en zona segura
        }
        
        return true;
    }
    
    /**
     * Aplica empuje simple de flecha (sin knockback complejo)
     */
    public boolean applyArrowPush(Player shooter, Player victim, Arrow arrow) {
        if (!isSystemEnabled()) {
            return false;
        }
        
        if (!canApplyArrowPush(shooter, victim)) {
            return false;
        }
        
        // Verificar delay entre disparos
        UUID shooterId = shooter.getUniqueId();
        long currentTime = System.currentTimeMillis();
        int delayTicks = config.getInt("prison.hitDelay", 5);
        long delayMs = delayTicks * 50; // Convertir ticks a milisegundos
        
        if (lastShotTime.containsKey(shooterId)) {
            long lastShot = lastShotTime.get(shooterId);
            if (currentTime - lastShot < delayMs) {
                sendMessage(shooter, "messages.arrow-delay-active");
                return false;
            }
        }
        
        lastShotTime.put(shooterId, currentTime);
        
        // Aplicar empuje visual básico (mantener el daño normal de la flecha)
        playArrowEffects(shooter, victim);
        
        // Debug logging
        if (config.getBoolean("debug.log-arrows", false)) {
            plugin.getLogger().info(String.format(
                "Empuje de flecha aplicado: %s -> %s",
                shooter.getName(), victim.getName()
            ));
        }
        
        return true;
    }
    
    private void playArrowEffects(Player shooter, Player victim) {
        // Sonidos
        if (config.getBoolean("sounds.punch-hit.enabled", true)) {
            try {
                Sound hitSound = Sound.valueOf(config.getString("sounds.punch-hit.sound", "SUCCESSFUL_HIT"));
                float volume = (float) config.getDouble("sounds.punch-hit.volume", 1.0);
                float pitch = (float) config.getDouble("sounds.punch-hit.pitch", 1.2);
                shooter.playSound(shooter.getLocation(), hitSound, volume, pitch);
            } catch (IllegalArgumentException e) {
                // Sonido no válido para esta versión
            }
        }
        
        // Partículas
        if (config.getBoolean("particles.enabled", true)) {
            try {
                // Efectos de partículas en 1.8.8
                victim.getWorld().playEffect(victim.getLocation().add(0, 1, 0), 
                    org.bukkit.Effect.valueOf(config.getString("particles.punch-effect", "CRIT")), 
                    config.getInt("particles.amount", 10));
            } catch (Exception e) {
                // Efecto no válido para esta versión
            }
        }
    }
    
    public void sendMessage(Player player, String path) {
        String message = config.getString(path, "&cMensaje no configurado: " + path);
        if (!message.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    
    public void disableSystem() {
        config.set("enabled", false);
        saveConfig();
    }
    
    public void enableSystem() {
        config.set("enabled", true);
        saveConfig();
    }
    
    public void saveConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "prisonpunch.yml");
            config.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("No se pudo guardar la configuración: " + e.getMessage());
        }
    }
    
    public void setSystemEnabled(boolean enabled) {
        if (enabled) {
            enableSystem();
        } else {
            disableSystem();
        }
    }
    
    public void shutdown() {
        // Limpiar recursos si es necesario
        lastShotTime.clear();
    }
}