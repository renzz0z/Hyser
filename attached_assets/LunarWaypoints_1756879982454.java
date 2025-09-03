package com.hyser.hysercore.waypoints;

import com.hyser.hysercore.HyserCore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LunarWaypoints {
    
    private HyserCore plugin;
    private FileConfiguration config;
    private Map<UUID, Location> clanWaypoints;
    private Plugin ultimateClans;
    
    public LunarWaypoints(HyserCore plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.clanWaypoints = new HashMap<>();
        
        // Verificar si UltimateClans está disponible
        this.ultimateClans = Bukkit.getPluginManager().getPlugin("UltimateClans");
        if (ultimateClans == null) {
            plugin.getLogger().warning("UltimateClans no encontrado. LunarWaypoints funcionará sin integración de clanes.");
        } else {
            plugin.getLogger().info("UltimateClans detectado. Integración activada.");
        }
    }
    
    public boolean setWaypoint(Player player, String waypointName, Location location) {
        if (!config.getBoolean("lunar-waypoints.enabled", true)) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-waypoints.messages.system-disabled", 
                "&cEl sistema de waypoints está deshabilitado."));
            player.sendMessage(message);
            return false;
        }
        
        // Verificar permisos
        if (!player.hasPermission("hysercore.waypoints") && !player.hasPermission("hysercore.admin")) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-waypoints.messages.no-permission", 
                "&cNo tienes permisos para usar waypoints."));
            player.sendMessage(message);
            return false;
        }
        
        String clanName = getClanName(player);
        if (clanName == null) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-waypoints.messages.no-clan", 
                "&cDebes estar en un clan para establecer waypoints."));
            player.sendMessage(message);
            return false;
        }
        
        // Generar clave única para el waypoint del clan
        String waypointKey = clanName + ":" + waypointName;
        
        // Verificar límite de waypoints por clan
        int maxWaypoints = config.getInt("lunar-waypoints.max-waypoints-per-clan", 3);
        long currentWaypoints = clanWaypoints.entrySet().stream()
            .filter(entry -> getClanName(Bukkit.getPlayer(entry.getKey())) != null && 
                            getClanName(Bukkit.getPlayer(entry.getKey())).equals(clanName))
            .count();
        
        if (currentWaypoints >= maxWaypoints && !clanWaypoints.containsKey(player.getUniqueId())) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-waypoints.messages.max-waypoints", 
                "&cTu clan ya tiene el máximo de waypoints permitidos ({max}).")
                .replace("{max}", String.valueOf(maxWaypoints)));
            player.sendMessage(message);
            return false;
        }
        
        // Establecer waypoint
        clanWaypoints.put(player.getUniqueId(), location.clone());
        
        String successMessage = ChatColor.translateAlternateColorCodes('&', 
            config.getString("lunar-waypoints.messages.waypoint-set", 
            "&a¡Waypoint '{name}' establecido para el clan {clan}!")
            .replace("{name}", waypointName)
            .replace("{clan}", clanName));
        player.sendMessage(successMessage);
        
        return true;
    }
    
    public void showWaypoints(Player player) {
        if (!config.getBoolean("lunar-waypoints.enabled", true)) {
            return;
        }
        
        String clanName = getClanName(player);
        if (clanName == null) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-waypoints.messages.no-clan", 
                "&cDebes estar en un clan para ver waypoints."));
            player.sendMessage(message);
            return;
        }
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            "&b&l                    WAYPOINTS DEL CLAN " + clanName.toUpperCase()));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        
        boolean foundWaypoints = false;
        for (Player clanMember : Bukkit.getOnlinePlayers()) {
            String memberClan = getClanName(clanMember);
            if (memberClan != null && memberClan.equals(clanName)) {
                Location waypoint = clanWaypoints.get(clanMember.getUniqueId());
                if (waypoint != null) {
                    // Verificar que el waypoint esté en el mismo mundo
                    if (!waypoint.getWorld().getName().equals(player.getWorld().getName())) {
                        continue; // No mostrar waypoints de otros mundos
                    }
                    
                    double distance = player.getLocation().distance(waypoint);
                    String direction = getDirection(player.getLocation(), waypoint);
                    
                    String waypointInfo = ChatColor.translateAlternateColorCodes('&', 
                        config.getString("lunar-waypoints.messages.waypoint-info", 
                        "&f● &b{player} &7- &e{distance}m &7{direction}")
                        .replace("{player}", clanMember.getName())
                        .replace("{distance}", String.format("%.1f", distance))
                        .replace("{direction}", direction));
                    
                    player.sendMessage(waypointInfo);
                    foundWaypoints = true;
                }
            }
        }
        
        if (!foundWaypoints) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-waypoints.messages.no-waypoints", 
                "&7No hay waypoints establecidos en tu clan.")));
        }
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
    }
    
    public boolean removeWaypoint(Player player) {
        if (clanWaypoints.containsKey(player.getUniqueId())) {
            clanWaypoints.remove(player.getUniqueId());
            
            String message = ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-waypoints.messages.waypoint-removed", 
                "&cWaypoint eliminado."));
            player.sendMessage(message);
            return true;
        }
        
        String message = ChatColor.translateAlternateColorCodes('&', 
            config.getString("lunar-waypoints.messages.no-waypoint", 
            "&cNo tienes ningún waypoint establecido."));
        player.sendMessage(message);
        return false;
    }
    
    private String getClanName(Player player) {
        if (ultimateClans == null || player == null) {
            return null;
        }
        
        try {
            // Usar reflexión para acceder a la API de UltimateClans
            Class<?> ultimateClansAPI = Class.forName("me.ulrichbg.UltimateClans.API.UltimateClansAPI");
            Object apiInstance = ultimateClansAPI.getMethod("getInstance").invoke(null);
            Object clan = ultimateClansAPI.getMethod("getPlayerClan", UUID.class).invoke(apiInstance, player.getUniqueId());
            
            if (clan != null) {
                return (String) clan.getClass().getMethod("getName").invoke(clan);
            }
        } catch (Exception e) {
            // Si falla la integración, intentar método alternativo
            plugin.getLogger().warning("Error al acceder a UltimateClans API: " + e.getMessage());
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
}