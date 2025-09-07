package com.hyser.hysercore.abilities.actions;

import com.hyser.hysercore.abilities.AbilityAction;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.configuration.ConfigurationSection;

public class PotionEffectAction extends AbilityAction {
    private PotionEffectType effectType;
    private int amplifier;
    private int duration;
    
    public PotionEffectAction(String type, ConfigurationSection config) {
        super(type, config);
        
        String effectName = config.getString("effect", "SPEED");
        try {
            this.effectType = PotionEffectType.getByName(effectName.toUpperCase());
        } catch (Exception e) {
            this.effectType = PotionEffectType.SPEED;
        }
        
        this.amplifier = config.getInt("amplifier", 0);
        this.duration = config.getInt("duration", 200);
    }
    
    @Override
    public void execute(Player player) {
        if (effectType != null) {
            PotionEffect effect = new PotionEffect(effectType, duration, amplifier);
            player.addPotionEffect(effect);
        }
    }
}