package com.hyser.hysercore.enchantments.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class LifestealEnchantment extends SwordEnchantment {
    
    private FileConfiguration config;
    
    public LifestealEnchantment(FileConfiguration config) {
        super("lifesteal", "Lifesteal", "Roba vida");
        this.config = config;
    }
    
    @Override
    public void onAttack(Player attacker, Player target) {
        double chance = config.getDouble("enchantments.lifesteal.activation-chance", 0.25);
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }
        
        double healPercentage = config.getDouble("enchantments.lifesteal.heal-percentage", 0.25);
        double damage = 2.0; // Daño base estimado, se puede obtener del evento
        double healAmount = damage * healPercentage;
        
        double currentHealth = attacker.getHealth();
        double maxHealth = attacker.getMaxHealth();
        double newHealth = Math.min(currentHealth + healAmount, maxHealth);
        
        attacker.setHealth(newHealth);
    }
}