package com.hyser.hysercore.teamviewer;

import com.hyser.hysercore.HyserCore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LunarTeamViewer {
    
    private HyserCore plugin;
    private FileConfiguration config;
    private Map<UUID, Boolean> teamViewerEnabled;
    private Plugin ultimateClans;
    private BukkitRunnable updateTask;
    
    public LunarTeamViewer(HyserCore plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.teamViewerEnabled = new HashMap<>();
        
        // Verificar si UltimateClans está disponible
        this.ultimateClans = Bukkit.getPluginManager().getPlugin("UltimateClans");
        if (ultimateClans == null) {
            plugin.getLogger().warning("UltimateClans no encontrado. LunarTeamViewer funcionará sin integración de clanes.");
        } else {
            plugin.getLogger().info("UltimateClans detectado. LunarTeamViewer integrado.");
        }
        
        // Iniciar actualizaciones automáticas
        startTeamViewerUpdates();
    }
    
    public boolean toggleTeamViewer(Player player) {
        if (!config.getBoolean("lunar-teamviewer.enabled", true)) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-teamviewer.messages.system-disabled", 
                "&cEl sistema de teamviewer está deshabilitado."));
            player.sendMessage(message);
            return false;
        }
        
        // Verificar permisos
        if (!player.hasPermission("hysercore.teamviewer") && !player.hasPermission("hysercore.admin")) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-teamviewer.messages.no-permission", 
                "&cNo tienes permisos para usar teamviewer."));
            player.sendMessage(message);
            return false;
        }
        
        String clanName = getClanName(player);
        if (clanName == null) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-teamviewer.messages.no-clan", 
                "&cDebes estar en un clan para usar teamviewer."));
            player.sendMessage(message);
            return false;
        }
        
        UUID playerId = player.getUniqueId();
        boolean currentStatus = teamViewerEnabled.getOrDefault(playerId, false);
        boolean newStatus = !currentStatus;
        
        teamViewerEnabled.put(playerId, newStatus);
        
        if (newStatus) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-teamviewer.messages.teamviewer-enabled", 
                "&a¡TeamViewer activado! Puedes ver a tus compañeros de clan."));
            player.sendMessage(message);
        } else {
            String message = ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-teamviewer.messages.teamviewer-disabled", 
                "&cTeamViewer desactivado."));
            player.sendMessage(message);
        }
        
        return newStatus;
    }
    
    public void showTeamMembers(Player player) {
        if (!isTeamViewerEnabled(player)) {
            return;
        }
        
        String clanName = getClanName(player);
        if (clanName == null) {
            return;
        }
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            "&d&l                    TEAMVIEWER - CLAN " + clanName.toUpperCase()));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        
        boolean foundMembers = false;
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) {
                continue; // No mostrarse a sí mismo
            }
            
            String memberClan = getClanName(onlinePlayer);
            if (memberClan != null && memberClan.equals(clanName)) {
                // CRÍTICO: Solo mostrar jugadores en el MISMO MUNDO
                if (!onlinePlayer.getWorld().getName().equals(player.getWorld().getName())) {
                    continue; // No mostrar jugadores de otros mundos
                }
                
                Location memberLocation = onlinePlayer.getLocation();
                double distance = player.getLocation().distance(memberLocation);
                String direction = getDirection(player.getLocation(), memberLocation);
                String worldName = onlinePlayer.getWorld().getName();
                
                // Información del miembro del clan
                String memberInfo = ChatColor.translateAlternateColorCodes('&', 
                    config.getString("lunar-teamviewer.messages.clan-member-info", 
                    "&f● &b{player} &7- &e{distance}m &7{direction} &8[{world}]")
                    .replace("{player}", onlinePlayer.getName())
                    .replace("{distance}", String.format("%.1f", distance))
                    .replace("{direction}", direction)
                    .replace("{world}", worldName));
                
                player.sendMessage(memberInfo);
                foundMembers = true;
            }
        }
        
        if (!foundMembers) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-teamviewer.messages.no-clan-members", 
                "&7No hay miembros de tu clan online en este mundo.")));
        }
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
    }
    
    public boolean isTeamViewerEnabled(Player player) {
        return teamViewerEnabled.getOrDefault(player.getUniqueId(), false);
    }
    
    private String getClanName(Player player) {
        if (ultimateClans == null || player == null) {
            return null;
        }
        
        try {
            // Usar el mismo método mejorado que LunarWaypoints
            
            // Intento 1: API moderna de UltimateClans
            try {
                Class<?> ultimateClansAPI = Class.forName("me.ulrichbg.UltimateClans.API.UltimateClansAPI");
                Object apiInstance = ultimateClansAPI.getMethod("getInstance").invoke(null);
                Object clan = ultimateClansAPI.getMethod("getPlayerClan", UUID.class).invoke(apiInstance, player.getUniqueId());
                
                if (clan != null) {
                    return (String) clan.getClass().getMethod("getName").invoke(clan);
                }
            } catch (Exception e) {
                plugin.getLogger().fine("Error al obtener clan (TeamViewer): " + e.getMessage());
            }
            
            // Intento 2: API alternativa
            try {
                Class<?> clanPlayer = Class.forName("me.ulrichbg.UltimateClans.ClanPlayer");
                Object playerObj = clanPlayer.getConstructor(UUID.class).newInstance(player.getUniqueId());
                Object clan = clanPlayer.getMethod("getClan").invoke(playerObj);
                
                if (clan != null) {
                    return (String) clan.getClass().getMethod("getName").invoke(clan);
                }
            } catch (Exception e) {
                plugin.getLogger().fine("Error al obtener clan método 2 (TeamViewer): " + e.getMessage());
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error crítico al acceder a UltimateClans (TeamViewer): " + e.getMessage());
        }
        
        return null;
    }
    
    private String getDirection(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        
        double angle = Math.atan2(dz, dx) * 180 / Math.PI;
        
        if (angle < 0) {
            angle += 360;
        }
        
        if (angle >= 337.5 || angle < 22.5) {
            return "→ Este";
        } else if (angle >= 22.5 && angle < 67.5) {
            return "↘ Sureste";
        } else if (angle >= 67.5 && angle < 112.5) {
            return "↓ Sur";
        } else if (angle >= 112.5 && angle < 157.5) {
            return "↙ Suroeste";
        } else if (angle >= 157.5 && angle < 202.5) {
            return "← Oeste";
        } else if (angle >= 202.5 && angle < 247.5) {
            return "↖ Noroeste";
        } else if (angle >= 247.5 && angle < 292.5) {
            return "↑ Norte";
        } else {
            return "↗ Noreste";
        }
    }
    
    // Actualizaciones automáticas del TeamViewer
    private void startTeamViewerUpdates() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllTeamViewers();
            }
        };
        updateTask.runTaskTimer(plugin, 100L, 100L); // Actualizar cada 5 segundos
    }
    
    private void updateAllTeamViewers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isTeamViewerEnabled(player)) {
                updatePlayerTeamViewer(player);
            }
        }
    }
    
    private void updatePlayerTeamViewer(Player player) {
        String clanName = getClanName(player);
        if (clanName == null) return;
        
        // Actualización silenciosa: contar miembros del clan en el mismo mundo
        int membersInWorld = 0;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) continue;
            
            String memberClan = getClanName(onlinePlayer);
            if (memberClan != null && memberClan.equals(clanName) && 
                onlinePlayer.getWorld().getName().equals(player.getWorld().getName())) {
                membersInWorld++;
            }
        }
        
        // Solo mostrar información si hay cambios significativos o cada cierto tiempo
        // En este caso, mantenemos la actualización silenciosa para mejor rendimiento
    }
    
    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        teamViewerEnabled.clear();
    }
}