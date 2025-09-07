package com.hyser.hysercore.abilities;

import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

public abstract class AbilityCondition {
    protected String type;
    protected ConfigurationSection config;
    
    public AbilityCondition(String type, ConfigurationSection config) {
        this.type = type;
        this.config = config;
    }
    
    public abstract boolean check(Player player);
    
    public String getType() {
        return type;
    }
}