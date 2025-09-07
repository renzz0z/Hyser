package com.hyser.hysercore.abilities.triggers;

import com.hyser.hysercore.abilities.AbilityTrigger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import java.util.List;

public class ItemUseTrigger extends AbilityTrigger {
    private boolean consumeUse;
    
    public ItemUseTrigger(String type, ConfigurationSection config) {
        super(type, config);
        this.consumeUse = config.getBoolean("consume_use", true);
    }
    
    @Override
    public boolean matches(Event event, Player player) {
        if (!(event instanceof PlayerInteractEvent)) {
            return false;
        }
        
        PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
        Action action = interactEvent.getAction();
        
        // Solo activar en right click
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return false;
        }
        
        ItemStack item = interactEvent.getItem();
        if (item == null) {
            return false;
        }
        
        // Verificar si es un objeto de ability válido
        return isAbilityItem(item);
    }
    
    private boolean isAbilityItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        
        // Verificar si tiene el lore de ability
        if (lore != null) {
            for (String line : lore) {
                if (line.contains("Usos restantes:")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean shouldConsumeUse() {
        return consumeUse;
    }
}