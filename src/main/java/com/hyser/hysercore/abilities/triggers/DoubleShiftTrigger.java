package com.hyser.hysercore.abilities.triggers;

import com.hyser.hysercore.abilities.AbilityTrigger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DoubleShiftTrigger extends AbilityTrigger {
    private static Map<UUID, Long> lastShiftTime = new HashMap<>();
    private long maxInterval;
    
    public DoubleShiftTrigger(String type, ConfigurationSection config) {
        super(type, config);
        this.maxInterval = config.getLong("within", 500);
    }
    
    @Override
    public boolean matches(Event event, Player player) {
        if (!(event instanceof PlayerToggleSneakEvent)) {
            return false;
        }
        
        PlayerToggleSneakEvent sneakEvent = (PlayerToggleSneakEvent) event;
        if (!sneakEvent.isSneaking()) {
            return false;
        }
        
        // NUEVO: Solo activar si tiene un objeto de ability en mano
        if (!hasAbilityItemInHand(player)) {
            return false;
        }
        
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        if (lastShiftTime.containsKey(playerId)) {
            long timeDiff = currentTime - lastShiftTime.get(playerId);
            if (timeDiff <= maxInterval) {
                lastShiftTime.remove(playerId);
                return true;
            }
        }
        
        lastShiftTime.put(playerId, currentTime);
        return false;
    }
    
    private boolean hasAbilityItemInHand(Player player) {
        org.bukkit.inventory.ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null || !itemInHand.hasItemMeta()) {
            return false;
        }
        
        org.bukkit.inventory.meta.ItemMeta meta = itemInHand.getItemMeta();
        java.util.List<String> lore = meta.getLore();
        
        // Verificar si tiene el lore de ability
        if (lore != null) {
            for (String line : lore) {
                if (line.contains("Usos restantes:")) {
                    return true;
                }
            }
        }
        
        return false;
    }
}