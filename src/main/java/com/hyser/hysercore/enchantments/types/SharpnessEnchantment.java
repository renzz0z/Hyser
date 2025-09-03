package com.hyser.hysercore.enchantments.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SharpnessEnchantment extends SwordEnchantment {
    
    private FileConfiguration config;
    
    public SharpnessEnchantment(FileConfiguration config) {
        super("sharpness", "Sharpness+", "Daño aumentado");
        this.config = config;
    }
    
    @Override
    public void onAttack(Player attacker, Player target) {
        // El daño se maneja en el listener de daño
    }
    
    public double getBonusDamage() {
        return config.getDouble("enchantments.sharpness.bonus-damage", 1.5);
    }
}