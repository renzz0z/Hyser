package com.hyser.hysercore.abilities.actions;

import com.hyser.hysercore.abilities.AbilityAction;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

public class ParticleAction extends AbilityAction {
    private String particleName;
    private int amount;
    private double offsetX, offsetY, offsetZ;
    
    public ParticleAction(String type, ConfigurationSection config) {
        super(type, config);
        
        this.particleName = config.getString("particle", "CLOUD");
        
        this.amount = config.getInt("amount", 10);
        this.offsetX = config.getDouble("offset_x", 0.5);
        this.offsetY = config.getDouble("offset_y", 0.5);
        this.offsetZ = config.getDouble("offset_z", 0.5);
    }
    
    @Override
    public void execute(Player player) {
        // Implementación para Spigot 1.8.8
        if (particleName != null) {
            try {
                // En 1.8.8 se usa playEffect en lugar de spawnParticle
                org.bukkit.Effect effect = getEffectFromName(particleName);
                if (effect != null) {
                    for (int i = 0; i < amount; i++) {
                        double x = player.getLocation().getX() + (Math.random() - 0.5) * offsetX * 2;
                        double y = player.getLocation().getY() + Math.random() * offsetY;
                        double z = player.getLocation().getZ() + (Math.random() - 0.5) * offsetZ * 2;
                        
                        org.bukkit.Location particleLoc = new org.bukkit.Location(player.getWorld(), x, y, z);
                        player.getWorld().playEffect(particleLoc, effect, 0);
                    }
                }
            } catch (Exception e) {
                // Fallback silencioso para partículas no soportadas
            }
        }
    }
    
    private org.bukkit.Effect getEffectFromName(String name) {
        switch (name.toUpperCase()) {
            case "CLOUD":
            case "EXPLOSION":
                return org.bukkit.Effect.CLOUD;
            case "SMOKE":
                return org.bukkit.Effect.SMOKE;
            case "FLAME":
            case "FIRE":
                return org.bukkit.Effect.MOBSPAWNER_FLAMES;
            case "HEART":
                return org.bukkit.Effect.HEART;
            case "PORTAL":
                return org.bukkit.Effect.ENDER_SIGNAL;
            case "REDSTONE":
                return org.bukkit.Effect.POTION_BREAK;
            default:
                return org.bukkit.Effect.CLOUD;
        }
    }
}