package com.hyser.hysercore.abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class AbilityListener implements Listener {
    private final AbilityManager abilityManager;
    
    public AbilityListener(AbilityManager abilityManager) {
        this.abilityManager = abilityManager;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!abilityManager.isEnabled()) return;
        
        Player player = event.getPlayer();
        abilityManager.handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (!abilityManager.isEnabled()) return;
        
        Player player = event.getPlayer();
        abilityManager.handleEvent(event, player);
    }
}