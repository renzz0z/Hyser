package com.hyser.hysercore.enchantments.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class ExplosiveEnchantment extends SwordEnchantment {
    
    private FileConfiguration config;
    
    public ExplosiveEnchantment(FileConfiguration config) {
        super("explosive", "Explosive", "Knockback explosivo");
        this.config = config;
    }
    
    @Override
    public void onAttack(Player attacker, Player target) {
        double chance = config.getDouble("enchantments.explosive.activation-chance", 0.12);
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }
        
        double knockbackForce = config.getDouble("enchantments.explosive.knockback-force", 1.8);
        
        Vector direction = target.getLocation().subtract(attacker.getLocation()).toVector().normalize();
        direction.multiply(knockbackForce);
        direction.setY(0.5);
        
        target.setVelocity(direction);
        
        if (config.getBoolean("enchantments.explosive.play-effect", true)) {
            target.getWorld().createExplosion(target.getLocation(), 0.0f, false);
        }
    }
}