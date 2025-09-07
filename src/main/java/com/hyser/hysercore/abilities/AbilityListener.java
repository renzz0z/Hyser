package com.hyser.hysercore.abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

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
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!abilityManager.isEnabled()) return;
        
        // Para el jugador que ataca
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            abilityManager.handleEvent(event, attacker);
        }
        
        // Para el jugador que recibe daño
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            abilityManager.handleEvent(event, victim);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (!abilityManager.isEnabled()) return;
        
        // Cancelar consumo de objetos de abilities para evitar que se coman
        if (abilityManager.getItemManager() != null) {
            if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasLore()) {
                for (String lore : event.getItem().getItemMeta().getLore()) {
                    if (lore.contains("Usos restantes:")) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}