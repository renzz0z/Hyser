package com.hyser.hysercore.abilities.actions;

import com.hyser.hysercore.abilities.AbilityAction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

public class HealRadiusAction extends AbilityAction {
    private double amount;
    private double radius;
    private String target;
    
    public HealRadiusAction(String type, ConfigurationSection config) {
        super(type, config);
        this.amount = config.getDouble("amount", 2.0);
        this.radius = config.getDouble("radius", 5.0);
        this.target = config.getString("target", "NEARBY_PLAYERS");
    }
    
    @Override
    public void execute(Player player) {
        if ("NEARBY_PLAYERS".equals(target)) {
            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    double currentHealth = target.getHealth();
                    double maxHealth = target.getMaxHealth();
                    double newHealth = Math.min(currentHealth + amount, maxHealth);
                    target.setHealth(newHealth);
                }
            }
        }
    }
}