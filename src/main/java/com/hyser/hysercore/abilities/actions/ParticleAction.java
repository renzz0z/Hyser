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
        // En Spigot 1.8.8 las particulas se manejan diferente
        // Se requiere implementacion especifica para 1.8.8
        if (particleName != null) {
            // Placeholder para particulas en 1.8.8
            player.sendMessage("§7[Particles] " + particleName + " spawned!");
        }
    }
}