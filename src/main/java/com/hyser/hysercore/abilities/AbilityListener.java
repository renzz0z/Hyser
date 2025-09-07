package com.hyser.hysercore.abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

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
            updateComboItemsInInventory(attacker);
        }
        
        // Para el jugador que recibe daño
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            abilityManager.handleEvent(event, victim);
            updateComboItemsInInventory(victim);
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
    
    private void updateComboItemsInInventory(Player player) {
        // Actualizar lore de combo en todos los objetos de abilities del inventario
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                boolean isAbilityItem = false;
                String abilityId = null;
                
                // Verificar si es un objeto de ability con combo
                for (String lore : item.getItemMeta().getLore()) {
                    if (lore.contains("» Estado de Combo «")) {
                        isAbilityItem = true;
                        break;
                    }
                }
                
                if (isAbilityItem) {
                    // Buscar el ID de la ability
                    for (String id : abilityManager.getAbilities().keySet()) {
                        ItemStack abilityItem = abilityManager.getItemManager().createAbilityItem(id);
                        if (abilityItem != null && abilityItem.getType() == item.getType() && 
                            abilityItem.getItemMeta().getDisplayName().equals(item.getItemMeta().getDisplayName())) {
                            abilityId = id;
                            break;
                        }
                    }
                    
                    if (abilityId != null) {
                        ItemStack updatedItem = abilityManager.getItemManager().updateComboLore(player, item, abilityId);
                        player.getInventory().setItem(i, updatedItem);
                    }
                }
            }
        }
    }
}