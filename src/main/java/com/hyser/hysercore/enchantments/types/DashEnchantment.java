package com.hyser.hysercore.enchantments.types;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DashEnchantment extends SwordEnchantment {
    
    private FileConfiguration config;
    
    public DashEnchantment(FileConfiguration config) {
        super("dash", "Dash", "Impulso aéreo");
        this.config = config;
    }
    
    @Override
    public void onAttack(Player attacker, Player target) {
        // Dash se activa con click derecho, no en ataque
    }
    
    public void activateDash(Player player) {
        if (isOnCooldown(player)) {
            int remaining = getRemainingCooldown(player);
            String cooldownMessage = ChatColor.translateAlternateColorCodes('&',
                config.getString("messages.dash-cooldown", "&cDash en cooldown. Espera &f{time} &csegundos.")
                .replace("{time}", String.valueOf(remaining)));
            player.sendMessage(cooldownMessage);
            return;
        }
        
        double forwardForce = config.getDouble("enchantments.dash.forward-force", 1.8);
        double upwardForce = config.getDouble("enchantments.dash.upward-force", 1.2);
        int cooldown = config.getInt("enchantments.dash.cooldown-seconds", 15);
        
        Vector direction = player.getLocation().getDirection().normalize();
        direction.multiply(forwardForce);
        direction.setY(upwardForce);
        
        player.setVelocity(direction);
        setCooldown(player, cooldown);
        
        if (config.getBoolean("enchantments.dash.play-sound", true)) {
            try {
                String soundName = config.getString("enchantments.dash.sound", "ENDERDRAGON_WINGS");
                Sound sound = Sound.valueOf(soundName);
                player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (Exception e) {
                player.getWorld().playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1.0f, 1.0f);
            }
        }
        
        String activatedMessage = ChatColor.translateAlternateColorCodes('&',
            config.getString("messages.dash-activated", "&b⚡ &aDash activado!"));
        player.sendMessage(activatedMessage);
    }
}