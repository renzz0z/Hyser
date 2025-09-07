package com.hyser.hysercore.abilities.triggers;

import com.hyser.hysercore.abilities.AbilityTrigger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;

public class ClickTrigger extends AbilityTrigger {
    private Action requiredAction;
    private Material requiredItem;
    private EquipmentSlot requiredHand;
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
        }\n        \n        this.requiredItem = getMaterial(\"item\");\n        this.requiredHand = getHand(\"hand\");\n    }\n    \n    @Override\n    public boolean matches(Event event, Player player) {\n        if (!(event instanceof PlayerInteractEvent)) {\n            return false;\n        }\n        \n        PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;\n        \n        // Verificar shift si es requerido\n        if (requireShift && !player.isSneaking()) {\n            return false;\n        }\n        \n        // Verificar acción si está especificada\n        if (requiredAction != null) {\n            Action action = interactEvent.getAction();\n            if (action != requiredAction && \n                !(action == Action.RIGHT_CLICK_BLOCK && requiredAction == Action.RIGHT_CLICK_AIR) &&\n                !(action == Action.LEFT_CLICK_BLOCK && requiredAction == Action.LEFT_CLICK_AIR)) {\n                return false;\n            }\n        }\n        \n        // Verificar mano si está especificada\n        if (requiredHand != null && interactEvent.getHand() != requiredHand) {\n            return false;\n        }\n        \n        // Verificar item si está especificado\n        if (requiredItem != null) {\n            ItemStack item = interactEvent.getItem();\n            if (item == null || item.getType() != requiredItem) {\n                return false;\n            }\n        }\n        \n        return true;\n    }\n}"