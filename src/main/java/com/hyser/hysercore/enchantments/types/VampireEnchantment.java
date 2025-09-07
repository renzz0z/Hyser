package com.hyser.hysercore.enchantments.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class VampireEnchantment extends SwordEnchantment {
    
    private FileConfiguration config;
    
    public VampireEnchantment(FileConfiguration config) {
        super("vampire", "Vampire", "Regeneración");
        this.config = config;
    }
    
    @Override
    public void onAttack(Player attacker, Player target) {
        double chance = config.getDouble("enchantments.vampire.activation-chance", 0.20);
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }
        
        int duration = config.getInt("enchantments.vampire.regen-duration-seconds", 4) * 20;
        int amplifier = config.getInt("enchantments.vampire.regen-amplifier", 1);
        
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, amplifier));
    }
}