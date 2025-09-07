package com.hyser.hysercore.abilities.triggers;

import com.hyser.hysercore.abilities.AbilityTrigger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;

public class ClickTrigger extends AbilityTrigger {
    private Action requiredAction;
    private Material requiredItem;
    private boolean requireOffHand;
    private boolean requireShift;
    
    public ClickTrigger(String type, ConfigurationSection config) {
        super(type, config);
        
        switch (type.toUpperCase()) {
            case "RIGHT_CLICK":
                this.requiredAction = Action.RIGHT_CLICK_AIR;
                break;
            case "LEFT_CLICK":
                this.requiredAction = Action.LEFT_CLICK_AIR;
                break;
            case "SHIFT_RIGHT_CLICK":
                this.requiredAction = Action.RIGHT_CLICK_AIR;
                this.requireShift = true;
                break;
            case "SHIFT_LEFT_CLICK":
                this.requiredAction = Action.LEFT_CLICK_AIR;
                this.requireShift = true;
                break;
            case "SHIFT_CLICK":
                this.requireShift = true;
                break;
        }
        
        this.requiredItem = getMaterial("item");
        this.requireOffHand = isOffHand("hand");
    }
    
    @Override
    public boolean matches(Event event, Player player) {
        if (!(event instanceof PlayerInteractEvent)) {
            return false;
        }
        
        PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
        
        if (requireShift && !player.isSneaking()) {
            return false;
        }
        
        if (requiredAction != null) {
            Action action = interactEvent.getAction();
            if (action != requiredAction && 
                !(action == Action.RIGHT_CLICK_BLOCK && requiredAction == Action.RIGHT_CLICK_AIR) &&
                !(action == Action.LEFT_CLICK_BLOCK && requiredAction == Action.LEFT_CLICK_AIR)) {
                return false;
            }
        }
        
        // En 1.8.8 no hay getHand(), omitir verificacion de mano
        
        if (requiredItem != null) {
            ItemStack item = interactEvent.getItem();
            if (item == null || item.getType() != requiredItem) {
                return false;
            }
        }
        
        return true;
    }
}