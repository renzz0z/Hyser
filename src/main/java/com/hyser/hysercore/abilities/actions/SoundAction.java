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
        
        String soundName = config.getString(\"sound\", \"ENTITY_EXPERIENCE_ORB_PICKUP\");\n        try {\n            this.sound = Sound.valueOf(soundName.toUpperCase());\n        } catch (IllegalArgumentException e) {\n            try {\n                this.sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP; // fallback\n            } catch (Exception ex) {\n                this.sound = null;\n            }\n        }\n        \n        this.volume = (float) config.getDouble(\"volume\", 1.0);\n        this.pitch = (float) config.getDouble(\"pitch\", 1.0);\n    }\n    \n    @Override\n    public void execute(Player player) {\n        if (sound != null) {\n            player.playSound(player.getLocation(), sound, volume, pitch);\n        }\n    }\n}"