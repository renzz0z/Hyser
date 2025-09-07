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
        
        String effectName = config.getString(\"effect\", \"SPEED\");\n        try {\n            this.effectType = PotionEffectType.getByName(effectName.toUpperCase());\n        } catch (Exception e) {\n            this.effectType = PotionEffectType.SPEED; // fallback\n        }\n        \n        this.amplifier = config.getInt(\"amplifier\", 0);\n        this.duration = config.getInt(\"duration\", 200);\n    }\n    \n    @Override\n    public void execute(Player player) {\n        if (effectType != null) {\n            PotionEffect effect = new PotionEffect(effectType, duration, amplifier);\n            player.addPotionEffect(effect);\n        }\n    }\n}"