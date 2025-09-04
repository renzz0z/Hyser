package com.hyser.hysercore.enchantments.types;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class BleedingEnchantment extends SwordEnchantment {
    
    private FileConfiguration config;
    private Plugin plugin;
    
    public BleedingEnchantment(FileConfiguration config, Plugin plugin) {
        super("bleeding", "Bleeding", "Sangrado");
        this.config = config;
        this.plugin = plugin;
    }
    
    @Override
    public void onAttack(Player attacker, Player target) {
        double chance = config.getDouble("enchantments.bleeding.activation-chance", 0.18);
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }
        
        int duration = config.getInt("enchantments.bleeding.duration-seconds", 6);
        double damagePerTick = config.getDouble("enchantments.bleeding.damage-per-tick", 0.5);
        int tickInterval = config.getInt("enchantments.bleeding.tick-interval", 20);
        
        new BukkitRunnable() {
            int ticks = 0;
            int maxTicks = duration;
            
            @Override
            public void run() {
                if (ticks >= maxTicks || !target.isOnline()) {
                    cancel();
                    return;
                }
                
                target.damage(damagePerTick);
                ticks++;
            }
        }.runTaskTimer(plugin, tickInterval, tickInterval);
    }
}