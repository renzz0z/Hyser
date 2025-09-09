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
    private Material requiredMaterial;
    private int requiredData;
    private String requiredName;
    
    public ItemUseTrigger(String type, ConfigurationSection config) {
        super(type, config);
        this.consumeUse = config.getBoolean("consume_use", true);
        
        // Obtener información del item requerido de la configuración padre
        ConfigurationSection parentConfig = config.getParent();
        if (parentConfig != null && parentConfig.contains("item")) {
            ConfigurationSection itemSection = parentConfig.getConfigurationSection("item");
            if (itemSection != null) {
                String materialName = itemSection.getString("material", "STONE");
                try {
                    this.requiredMaterial = Material.valueOf(materialName);
                } catch (IllegalArgumentException e) {
                    this.requiredMaterial = Material.STONE;
                }
                this.requiredData = itemSection.getInt("data", 0);
            }
        }
        
        // Obtener nombre requerido
        if (parentConfig != null) {
            this.requiredName = parentConfig.getString("name", "");
        }
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
        
        // CRÍTICO: Verificar material primero
        if (requiredMaterial != null && item.getType() != requiredMaterial) {
            return false;
        }
        
        // CRÍTICO: Verificar data si es necesario
        if (requiredData > 0 && item.getDurability() != requiredData) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        
        // CRÍTICO: Verificar nombre del item si se especifica
        if (requiredName != null && !requiredName.isEmpty()) {
            String displayName = meta.getDisplayName();
            if (displayName == null || !displayName.equals(org.bukkit.ChatColor.translateAlternateColorCodes('&', requiredName))) {
                return false;
            }
        }
        
        // Por último, verificar si tiene lore de ability
        List<String> lore = meta.getLore();
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