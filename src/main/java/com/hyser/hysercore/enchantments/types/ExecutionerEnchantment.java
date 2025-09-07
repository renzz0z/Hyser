package com.hyser.hysercore.enchantments.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ExecutionerEnchantment extends SwordEnchantment {
    
    private FileConfiguration config;
    
    public ExecutionerEnchantment(FileConfiguration config) {
        super("executioner", "Executioner", "Más daño a heridos");
        this.config = config;
    }
    
    @Override
    public void onAttack(Player attacker, Player target) {
        // El daño se maneja en el listener de daño
    }
    
    public boolean shouldActivate(Player target) {
        double threshold = config.getDouble("enchantments.executioner.health-threshold", 0.30);
        double healthPercentage = target.getHealth() / target.getMaxHealth();
        return healthPercentage <= threshold;
    }
    
    public double getBonusMultiplier() {
        return config.getDouble("enchantments.executioner.bonus-multiplier", 1.5);
    }
}