package com.hyser.hysercore.waypoints;

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

public class LunarWaypoints {
    
    private HyserCore plugin;
    private FileConfiguration config;
    private Map<String, Map<UUID, WaypointData>> clanWaypoints; // clan -> player -> waypoint
    private Plugin ultimateClans;
    private BukkitRunnable updateTask;
    
    public LunarWaypoints(HyserCore plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.clanWaypoints = new HashMap<>();
        
        // Verificar si UltimateClans está disponible con mejor detección
        this.ultimateClans = Bukkit.getPluginManager().getPlugin("UltimateClans");
        if (ultimateClans == null) {
            plugin.getLogger().warning("UltimateClans no encontrado. LunarWaypoints funcionará sin integración de clanes.");
        } else {
            plugin.getLogger().info("UltimateClans detectado v" + ultimateClans.getDescription().getVersion() + ". Integración activada.");
        }
        
        // Iniciar actualización automática tipo F3 rally
        startRallyUpdates();
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
        
        // Verificar límite de waypoints por clan
        int maxWaypoints = config.getInt("lunar-waypoints.max-waypoints-per-clan", 3);
        Map<UUID, WaypointData> clanMap = clanWaypoints.computeIfAbsent(clanName, k -> new HashMap<>());
        
        if (clanMap.size() >= maxWaypoints && !clanMap.containsKey(player.getUniqueId())) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-waypoints.messages.max-waypoints", 
                "&cTu clan ya tiene el máximo de waypoints permitidos ({max}).")
                .replace("{max}", String.valueOf(maxWaypoints)));
            player.sendMessage(message);
            return false;
        }
        
        // Establecer waypoint
        WaypointData waypoint = new WaypointData(waypointName, location.clone(), System.currentTimeMillis());
        clanMap.put(player.getUniqueId(), waypoint);
        
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
            "&b&l                    WAYPOINTS - CLAN " + clanName.toUpperCase()));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        
        Map<UUID, WaypointData> clanMap = clanWaypoints.get(clanName);
        if (clanMap == null || clanMap.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                config.getString("lunar-waypoints.messages.no-waypoints", 
                "&7No hay waypoints establecidos en tu clan.")));
        } else {
            for (Map.Entry<UUID, WaypointData> entry : clanMap.entrySet()) {
                Player waypointOwner = Bukkit.getPlayer(entry.getKey());
                WaypointData waypoint = entry.getValue();
                
                if (waypointOwner != null) {
                    // Verificar que el waypoint esté en el mismo mundo
                    if (!waypoint.getLocation().getWorld().getName().equals(player.getWorld().getName())) {
                        continue; // No mostrar waypoints de otros mundos
                    }
                    
                    double distance = player.getLocation().distance(waypoint.getLocation());
                    String direction = getDirection(player.getLocation(), waypoint.getLocation());
                    
                    String waypointInfo = ChatColor.translateAlternateColorCodes('&', 
                        "&f● &b{name} &7({player}) - &e{distance}m &7{direction}")
                        .replace("{name}", waypoint.getName())
                        .replace("{player}", waypointOwner.getName())
                        .replace("{distance}", String.format("%.1f", distance))
                        .replace("{direction}", direction);
                    
                    player.sendMessage(waypointInfo);
                }
            }
        }
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
    }
    
    public boolean removeWaypoint(Player player) {
        String clanName = getClanName(player);
        if (clanName == null) {
            return false;
        }
        
        Map<UUID, WaypointData> clanMap = clanWaypoints.get(clanName);
        if (clanMap != null && clanMap.containsKey(player.getUniqueId())) {
            clanMap.remove(player.getUniqueId());
            
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
            // Método mejorado para obtener clan con múltiples intentos
            
            // Intento 1: API moderna de UltimateClans
            try {
                Class<?> ultimateClansAPI = Class.forName("me.ulrichbg.UltimateClans.API.UltimateClansAPI");
                Object apiInstance = ultimateClansAPI.getMethod("getInstance").invoke(null);
                Object clan = ultimateClansAPI.getMethod("getPlayerClan", UUID.class).invoke(apiInstance, player.getUniqueId());
                
                if (clan != null) {
                    return (String) clan.getClass().getMethod("getName").invoke(clan);
                }
            } catch (Exception e) {
                plugin.getLogger().fine("Intento 1 fallido para obtener clan: " + e.getMessage());
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
                plugin.getLogger().fine("Intento 2 fallido para obtener clan: " + e.getMessage());
            }
            
            // Intento 3: Comandos directo (último recurso)
            // Este método no se implementa para evitar spam de comandos
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error crítico al acceder a UltimateClans: " + e.getMessage());
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
    
    // NUEVO: Sistema tipo F rally con actualizaciones automáticas
    private void startRallyUpdates() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllWaypoints();
            }
        };
        updateTask.runTaskTimer(plugin, 60L, 60L); // Actualizar cada 3 segundos
    }
    
    private void updateAllWaypoints() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("hysercore.waypoints")) {
                updatePlayerWaypoints(player, false); // Actualización silenciosa
            }
        }
    }
    
    public void updatePlayerWaypoints(Player player, boolean showHeader) {
        String clanName = getClanName(player);
        if (clanName == null) return;
        
        Map<UUID, WaypointData> clanMap = clanWaypoints.get(clanName);
        if (clanMap == null || clanMap.isEmpty()) return;
        
        if (showHeader) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bWaypoints&8] &7Actualizando posiciones..."));
        }
        
        for (Map.Entry<UUID, WaypointData> entry : clanMap.entrySet()) {
            Player waypointOwner = Bukkit.getPlayer(entry.getKey());
            WaypointData waypoint = entry.getValue();
            
            if (waypointOwner != null && !waypointOwner.equals(player)) {
                // Solo mostrar si están en el mismo mundo
                if (waypoint.getLocation().getWorld().getName().equals(player.getWorld().getName())) {
                    double distance = player.getLocation().distance(waypoint.getLocation());
                    String direction = getDirection(player.getLocation(), waypoint.getLocation());
                    
                    // Mostrar en action bar si es posible (1.8+)
                    String info = ChatColor.translateAlternateColorCodes('&', 
                        "&b{name} &7- &e{distance}m &7{direction}")
                        .replace("{name}", waypoint.getName())
                        .replace("{distance}", String.format("%.0f", distance))
                        .replace("{direction}", direction);
                    
                    // En 1.8.8 usamos chat normal ya que action bar es limitado
                    if (showHeader) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8→ ") + info);
                    }
                }
            }
        }
    }
    
    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
        }
    }
    
    // Clase para datos del waypoint
    private static class WaypointData {
        private String name;
        private Location location;
        private long timestamp;
        
        public WaypointData(String name, Location location, long timestamp) {
            this.name = name;
            this.location = location;
            this.timestamp = timestamp;
        }
        
        public String getName() { return name; }
        public Location getLocation() { return location; }
        public long getTimestamp() { return timestamp; }
    }
}