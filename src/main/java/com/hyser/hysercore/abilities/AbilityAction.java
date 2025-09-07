package com.hyser.hysercore.abilities;

import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

public abstract class AbilityAction {
    protected String type;
    protected ConfigurationSection config;
    
    public AbilityAction(String type, ConfigurationSection config) {
        this.type = type;
        this.config = config;
    }
    
    public abstract void execute(Player player);
    
    public String getType() {
        return type;
    }
}