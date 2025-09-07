package com.hyser.hysercore.enchantments.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiAutoArmorListener implements Listener {
    
    private FileConfiguration config;
    private Map<UUID, ArmorChangeTracker> playerTrackers;
    
    public AntiAutoArmorListener(FileConfiguration config) {
        this.config = config;
        this.playerTrackers = new HashMap<>();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!config.getBoolean("anti-autoarmor.enabled", true)) {
            return;
        }
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // Ignorar en modo creativo si está configurado
        if (config.getBoolean("anti-autoarmor.ignore-creative", true) && 
            player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        
        // Verificar si es un slot de armadura
        if (!isArmorSlot(event.getSlot())) {
            return;
        }
        
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        
        // Verificar si se está equipando armadura
        if (isArmorPiece(clickedItem) || isArmorPiece(cursorItem)) {
            trackArmorChange(player);
        }
    }
    
    private boolean isArmorSlot(int slot) {
        // Slots de armadura en el inventario del jugador (1.8.8)
        return slot >= 36 && slot <= 39; // Slots 36-39 son helmet, chestplate, leggings, boots
    }
    
    private boolean isArmorPiece(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        String typeName = item.getType().name();
        return typeName.contains("HELMET") || 
               typeName.contains("CHESTPLATE") || 
               typeName.contains("LEGGINGS") || 
               typeName.contains("BOOTS") ||
               typeName.contains("_HELMET") ||
               typeName.contains("_CHESTPLATE") ||
               typeName.contains("_LEGGINGS") ||
               typeName.contains("_BOOTS");
    }
    
    private void trackArmorChange(Player player) {
        UUID playerId = player.getUniqueId();
        ArmorChangeTracker tracker = playerTrackers.get(playerId);
        
        if (tracker == null) {
            tracker = new ArmorChangeTracker();
            playerTrackers.put(playerId, tracker);
        }
        
        long currentTime = System.currentTimeMillis();
        long timeWindow = config.getLong("anti-autoarmor.time-window-ms", 200);
        long rapidThreshold = config.getLong("anti-autoarmor.rapid-change-threshold", 150);
        int maxEquips = config.getInt("anti-autoarmor.max-equips-per-window", 3);
        
        // Limpiar cambios antiguos
        tracker.cleanOldChanges(currentTime, timeWindow);
        
        // Agregar nuevo cambio
        tracker.addChange(currentTime);
        
        // Verificar si hay demasiados cambios en la ventana de tiempo
        if (tracker.getChangeCount() > maxEquips) {
            handleAutoArmorDetection(player, tracker);
            return;
        }
        
        // NUEVO: Verificar cambios extremadamente rápidos
        if (tracker.getChangeCount() >= 2) {
            long lastChangeTime = tracker.getLastChangeTime();
            if (currentTime - lastChangeTime < rapidThreshold) {
                handleAutoArmorDetection(player, tracker);
                return;
            }
        }
        
        // NUEVO: Verificar patrones sospechosos (mismo tipo de armadura repetidamente)
        if (config.getBoolean("anti-autoarmor.ignore-same-armor", true)) {
            // Esta lógica se puede expandir para rastrear qué tipo de armadura se está equipando
        }
    }
    
    private void handleAutoArmorDetection(Player player, ArmorChangeTracker tracker) {
        boolean kickPlayer = config.getBoolean("anti-autoarmor.kick-player", true);
        
        if (kickPlayer) {
            String kickMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("anti-autoarmor.kick-message", "&cHas sido expulsado por usar AutoArmor"));
            player.kickPlayer(kickMessage);
        } else {
            String warningMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("anti-autoarmor.warning-message", "&cAdvertencia: No uses AutoArmor en este servidor"));
            player.sendMessage(warningMessage);
        }
        
        // Limpiar tracker después de la detección
        tracker.reset();
    }
    
    // Clase interna para rastrear cambios de armadura
    private static class ArmorChangeTracker {
        private java.util.List<Long> changeTimes;
        
        public ArmorChangeTracker() {
            this.changeTimes = new java.util.ArrayList<>();
        }
        
        public void addChange(long time) {
            changeTimes.add(time);
        }
        
        public void cleanOldChanges(long currentTime, long timeWindow) {
            changeTimes.removeIf(time -> currentTime - time > timeWindow);
        }
        
        public int getChangeCount() {
            return changeTimes.size();
        }
        
        public long getLastChangeTime() {
            if (changeTimes.isEmpty()) {
                return 0;
            }
            return changeTimes.get(changeTimes.size() - 1);
        }
        
        public void reset() {
            changeTimes.clear();
        }
    }
}