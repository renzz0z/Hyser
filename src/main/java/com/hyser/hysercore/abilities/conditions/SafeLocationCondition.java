package com.hyser.hysercore.abilities.conditions;

import com.hyser.hysercore.abilities.AbilityCondition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

public class SafeLocationCondition extends AbilityCondition {
    private boolean required;
    
    public SafeLocationCondition(String type, ConfigurationSection config) {
        super(type, config);
        this.required = getBoolean("value", true);
    }
    
    @Override
    public boolean check(Player player) {
        if (!required) return true;
        
        Location loc = player.getLocation();
        // Verificar que el jugador no esté en lava o fuego
        Material blockType = loc.getBlock().getType();
        Material belowType = loc.clone().add(0, -1, 0).getBlock().getType();
        
        return blockType != Material.LAVA && 
               blockType != Material.STATIONARY_LAVA &&
               blockType != Material.FIRE &&
               belowType != Material.LAVA &&
               belowType != Material.STATIONARY_LAVA;
    }
}