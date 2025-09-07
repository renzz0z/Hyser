package com.hyser.hysercore.abilities.actions;

import com.hyser.hysercore.abilities.AbilityAction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

public class MessageAction extends AbilityAction {
    private String message;
    
    public MessageAction(String type, ConfigurationSection config) {
        super(type, config);
        this.message = ChatColor.translateAlternateColorCodes('&', 
            config.getString("message", "&7Habilidad activada!"));
    }
    
    @Override
    public void execute(Player player) {
        player.sendMessage(message);
    }
}