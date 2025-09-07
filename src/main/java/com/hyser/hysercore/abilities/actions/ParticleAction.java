package com.hyser.hysercore.abilities.actions;

import com.hyser.hysercore.abilities.AbilityAction;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

public class ParticleAction extends AbilityAction {
    private Particle particle;
    private int amount;
    private double offsetX, offsetY, offsetZ;
    
    public ParticleAction(String type, ConfigurationSection config) {
        super(type, config);
        
        String particleName = config.getString(\"particle\", \"CLOUD\");\n        try {\n            this.particle = Particle.valueOf(particleName.toUpperCase());\n        } catch (IllegalArgumentException e) {\n            try {\n                this.particle = Particle.CLOUD; // fallback\n            } catch (Exception ex) {\n                this.particle = null;\n            }\n        }\n        \n        this.amount = config.getInt(\"amount\", 10);\n        this.offsetX = config.getDouble(\"offset_x\", 0.5);\n        this.offsetY = config.getDouble(\"offset_y\", 0.5);\n        this.offsetZ = config.getDouble(\"offset_z\", 0.5);\n    }\n    \n    @Override\n    public void execute(Player player) {\n        if (particle != null) {\n            player.getWorld().spawnParticle(particle, \n                player.getLocation().add(0, 1, 0), \n                amount, offsetX, offsetY, offsetZ);\n        }\n    }\n}"