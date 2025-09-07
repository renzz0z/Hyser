package com.hyser.hysercore.abilities.actions;

import com.hyser.hysercore.abilities.AbilityAction;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

public class HealAction extends AbilityAction {
    private double amount;
    private String target;
    
    public HealAction(String type, ConfigurationSection config) {
        super(type, config);
        this.amount = config.getDouble("amount", 2.0);
        this.target = config.getString("target", "SELF");
    }
    
    @Override
    public void execute(Player player) {
        if ("SELF".equals(target)) {
            double currentHealth = player.getHealth();
            double maxHealth = player.getMaxHealth();
            double newHealth = Math.min(currentHealth + amount, maxHealth);
            player.setHealth(newHealth);
        }
    }
}