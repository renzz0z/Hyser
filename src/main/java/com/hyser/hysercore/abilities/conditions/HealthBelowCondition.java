package com.hyser.hysercore.abilities.conditions;

import com.hyser.hysercore.abilities.AbilityCondition;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

public class HealthBelowCondition extends AbilityCondition {
    private double value;
    
    public HealthBelowCondition(String type, ConfigurationSection config) {
        super(type, config);
        this.value = getDouble("value", 10.0);
    }
    
    @Override
    public boolean check(Player player) {
        return player.getHealth() < value;
    }
}