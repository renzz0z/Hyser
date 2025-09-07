package com.hyser.hysercore.abilities;

import com.hyser.hysercore.HyserCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.ArrayList;
import java.util.List;

public class ItemAbilityManager {
    private final HyserCore plugin;
    private final AbilityManager abilityManager;
    
    public ItemAbilityManager(HyserCore plugin, AbilityManager abilityManager) {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
    }
    
    public ItemStack createAbilityItem(String abilityId) {
        Ability ability = abilityManager.getAbility(abilityId);
        if (ability == null) return null;
        
        // Obtener configuración del item desde el archivo ability.yml
        FileConfiguration abilityConfig = abilityManager.getAbilityConfigFile();
        ConfigurationSection config = abilityConfig.getConfigurationSection("ability-items." + abilityId);
        if (config == null) return null;
        
        // Obtener configuración del item
        String materialName = config.getString("item.material", "STICK");
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.STICK;
        }
        
        int data = config.getInt("item.data", 0);
        int maxUses = config.getInt("uses", 1);
        
        // Crear item
        ItemStack item = new ItemStack(material, 1, (short) data);
        ItemMeta meta = item.getItemMeta();
        
        // Configurar nombre
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', ability.getName()));
        
        // Configurar lore (description + usos)
        List<String> lore = new ArrayList<>();
        
        // Agregar descripción
        List<String> descriptions = ability.getDescription();
        if (descriptions != null) {
            for (String desc : descriptions) {
                lore.add(ChatColor.translateAlternateColorCodes('&', desc));
            }
        }
        
        // Agregar información de usos
        lore.add("");
        lore.add(ChatColor.GRAY + "Usos restantes: " + ChatColor.WHITE + maxUses + "/" + maxUses);
        lore.add(ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + ability.getCooldown() + "s");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click derecho para usar");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    public boolean useAbilityItem(Player player, ItemStack item, String abilityId) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        
        if (lore == null) return false;
        
        // Buscar línea de usos
        int usesLineIndex = -1;
        int currentUses = 0;
        int maxUses = 0;
        
        for (int i = 0; i < lore.size(); i++) {
            String line = ChatColor.stripColor(lore.get(i));
            if (line.startsWith("Usos restantes:")) {
                usesLineIndex = i;
                String[] parts = line.split(" ");
                if (parts.length >= 3) {
                    String[] usesParts = parts[2].split("/");
                    if (usesParts.length >= 2) {
                        try {
                            currentUses = Integer.parseInt(usesParts[0]);
                            maxUses = Integer.parseInt(usesParts[1]);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                }
                break;
            }
        }
        
        if (usesLineIndex == -1 || currentUses <= 0) {
            player.sendMessage(ChatColor.RED + "¡Este objeto no tiene más usos!");
            return false;
        }
        
        // Reducir usos
        currentUses--;
        
        if (currentUses <= 0) {
            // Eliminar item si no quedan usos
            player.getInventory().remove(item);
            player.sendMessage(ChatColor.GRAY + "El objeto se ha gastado completamente.");
        } else {
            // Actualizar lore con nuevos usos
            lore.set(usesLineIndex, ChatColor.GRAY + "Usos restantes: " + 
                ChatColor.WHITE + currentUses + "/" + maxUses);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return true;
    }
    
    public FileConfiguration getAbilityConfig() {
        return abilityManager.getAbilityConfigFile();
    }
}