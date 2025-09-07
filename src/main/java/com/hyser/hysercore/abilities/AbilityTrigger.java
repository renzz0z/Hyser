package com.hyser.hysercore.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.configuration.ConfigurationSection;

public abstract class AbilityTrigger {
    protected String type;
    protected ConfigurationSection config;
    
    public AbilityTrigger(String type, ConfigurationSection config) {
        this.type = type;
        this.config = config;
    }
    
    public abstract boolean matches(Event event, Player player);
    
    public String getType() {
        return type;
    }
    
    protected Material getMaterial(String key) {
        String materialName = config.getString(key);
        if (materialName != null) {
            try {
                return Material.valueOf(materialName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
    
    protected boolean isOffHand(String key) {
        String handName = config.getString(key);
        return "OFF_HAND".equalsIgnoreCase(handName);
    }
}