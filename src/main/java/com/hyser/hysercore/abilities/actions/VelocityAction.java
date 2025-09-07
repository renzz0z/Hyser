package com.hyser.hysercore.abilities.actions;

import com.hyser.hysercore.abilities.AbilityAction;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public class VelocityAction extends AbilityAction {
    private String direction;
    private double strength;
    private double yVelocity;
    
    public VelocityAction(String type, ConfigurationSection config) {
        super(type, config);
        this.direction = config.getString("direction", "FORWARD");
        this.strength = config.getDouble("strength", 1.0);
        this.yVelocity = config.getDouble("y_velocity", 0.2);
    }
    
    @Override
    public void execute(Player player) {
        Vector velocity = null;
        
        switch (direction.toUpperCase()) {
            case "FORWARD":
                velocity = player.getLocation().getDirection().multiply(strength);
                velocity.setY(yVelocity);
                break;
            case "BACKWARD":
                velocity = player.getLocation().getDirection().multiply(-strength);
                velocity.setY(yVelocity);
                break;
            case "UP":
                velocity = new Vector(0, strength, 0);
                break;
            case "RANDOM":
                velocity = new Vector(
                    (Math.random() - 0.5) * strength,
                    yVelocity,
                    (Math.random() - 0.5) * strength
                );
                break;
        }
        
        if (velocity != null) {
            player.setVelocity(velocity);
        }
    }
}