package com.hyser.hysercore.abilities.actions;

import com.hyser.hysercore.abilities.AbilityAction;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

public class SoundAction extends AbilityAction {
    private Sound sound;
    private float volume;
    private float pitch;
    
    public SoundAction(String type, ConfigurationSection config) {
        super(type, config);
        
        String soundName = config.getString("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        try {
            this.sound = Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                this.sound = Sound.ORB_PICKUP;
            } catch (Exception ex) {
                this.sound = null;
            }
        }
        
        this.volume = (float) config.getDouble("volume", 1.0);
        this.pitch = (float) config.getDouble("pitch", 1.0);
    }
    
    @Override
    public void execute(Player player) {
        if (sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
}