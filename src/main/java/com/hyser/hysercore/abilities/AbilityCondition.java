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
    
    protected int getInt(String key, int defaultValue) {
        return config.getInt(key, defaultValue);
    }
    
    protected double getDouble(String key, double defaultValue) {
        return config.getDouble(key, defaultValue);
    }
    
    protected String getString(String key, String defaultValue) {
        return config.getString(key, defaultValue);
    }
    
    protected boolean getBoolean(String key, boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }
}