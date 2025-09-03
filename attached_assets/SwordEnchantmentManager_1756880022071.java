package com.hyser.hysercore.enchantments.managers;

import com.hyser.hysercore.HyserCore;
import com.hyser.hysercore.enchantments.types.DashEnchantment;
import com.hyser.hysercore.enchantments.types.IceAspectEnchantment;
import com.hyser.hysercore.enchantments.types.SwordEnchantment;
import com.hyser.hysercore.enchantments.types.SharpnessEnchantment;
import com.hyser.hysercore.enchantments.types.LifestealEnchantment;
import com.hyser.hysercore.enchantments.types.VampireEnchantment;
import com.hyser.hysercore.enchantments.types.BleedingEnchantment;
import com.hyser.hysercore.enchantments.types.ExplosiveEnchantment;
import com.hyser.hysercore.enchantments.types.ExecutionerEnchantment;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwordEnchantmentManager {
    
    private HyserCore plugin;
    private FileConfiguration config;
    private Map<String, SwordEnchantment> enchantments;
    
    public SwordEnchantmentManager(HyserCore plugin) {
        this.plugin = plugin;
        this.enchantments = new HashMap<>();
        loadConfig();
        registerEnchantments();
    }
    
    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "swordenchants.yml");
        if (!configFile.exists()) {
            plugin.saveResource("swordenchants.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    public void reloadConfig() {
        loadConfig();
        enchantments.clear();
        registerEnchantments();
        plugin.getLogger().info("Configuración de Sword Enchantments recargada");
    }
    
    private void registerEnchantments() {
        // Registrar Dash
        if (config.getBoolean("enchantments.dash.enabled", true)) {
            DashEnchantment dash = new DashEnchantment(config);
            enchantments.put("dash", dash);
        }
        
        // Registrar Ice Aspect
        if (config.getBoolean("enchantments.ice-aspect.enabled", true)) {
            IceAspectEnchantment iceAspect = new IceAspectEnchantment(config, plugin);
            enchantments.put("ice_aspect", iceAspect);
        }
        
        // Registrar Sharpness+
        if (config.getBoolean("enchantments.sharpness.enabled", true)) {
            SharpnessEnchantment sharpness = new SharpnessEnchantment(config);
            enchantments.put("sharpness", sharpness);
        }
        
        // Registrar Lifesteal
        if (config.getBoolean("enchantments.lifesteal.enabled", true)) {
            LifestealEnchantment lifesteal = new LifestealEnchantment(config);
            enchantments.put("lifesteal", lifesteal);
        }
        
        // Registrar Vampire
        if (config.getBoolean("enchantments.vampire.enabled", true)) {
            VampireEnchantment vampire = new VampireEnchantment(config);
            enchantments.put("vampire", vampire);
        }
        
        // Registrar Bleeding
        if (config.getBoolean("enchantments.bleeding.enabled", true)) {
            BleedingEnchantment bleeding = new BleedingEnchantment(config, plugin);
            enchantments.put("bleeding", bleeding);
        }
        
        // Registrar Explosive
        if (config.getBoolean("enchantments.explosive.enabled", true)) {
            ExplosiveEnchantment explosive = new ExplosiveEnchantment(config);
            enchantments.put("explosive", explosive);
        }
        
        // Registrar Executioner
        if (config.getBoolean("enchantments.executioner.enabled", true)) {
            ExecutionerEnchantment executioner = new ExecutionerEnchantment(config);
            enchantments.put("executioner", executioner);
        }
        
        plugin.getLogger().info("Registrados " + enchantments.size() + " encantamientos de espada");
    }
    
    public boolean applySwordEnchantment(Player player, String enchantmentName) {
        ItemStack item = player.getItemInHand();
        
        // Verificar que tenga una espada
        if (!isSword(item)) {
            String noSwordMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("messages.no-sword", "&cDebes tener una espada en la mano."));
            player.sendMessage(noSwordMessage);
            return false;
        }
        
        // Verificar que el encantamiento existe
        SwordEnchantment enchantment = enchantments.get(enchantmentName.toLowerCase());
        if (enchantment == null) {
            String unknownEnchantMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("messages.unknown-enchantment", "&cEncantamiento desconocido: {enchant}")
                .replace("{enchant}", enchantmentName));
            player.sendMessage(unknownEnchantMessage);
            return false;
        }
        
        // Verificar que no tenga ya el encantamiento
        if (hasEnchantment(item, enchantment)) {
            String alreadyHasMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("messages.already-has-enchantment", "&cEsta espada ya tiene el encantamiento {enchant}.")
                .replace("{enchant}", enchantment.getDisplayName()));
            player.sendMessage(alreadyHasMessage);
            return false;
        }
        
        // Aplicar encantamiento
        addEnchantmentToSword(item, enchantment);
        
        String successMessage = ChatColor.translateAlternateColorCodes('&', 
            config.getString("messages.enchantment-applied", "&aEncantamiento {enchant} aplicado exitosamente!")
            .replace("{enchant}", enchantment.getDisplayName()));
        player.sendMessage(successMessage);
        
        return true;
    }
    
    private boolean isSword(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        Material type = item.getType();
        return type == Material.WOOD_SWORD || 
               type == Material.STONE_SWORD || 
               type == Material.IRON_SWORD || 
               type == Material.GOLD_SWORD || 
               type == Material.DIAMOND_SWORD ||
               type.toString().contains("SWORD");
    }
    
    private boolean hasEnchantment(ItemStack item, SwordEnchantment enchantment) {
        if (!item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) {
            return false;
        }
        
        List<String> lore = meta.getLore();
        String enchantLore = ChatColor.translateAlternateColorCodes('&', 
            config.getString("lore-format", "&b{enchant}").replace("{enchant}", enchantment.getLoreText()));
        
        for (String line : lore) {
            if (ChatColor.stripColor(line).equals(ChatColor.stripColor(enchantLore))) {
                return true;
            }
        }
        
        return false;
    }
    
    private void addEnchantmentToSword(ItemStack item, SwordEnchantment enchantment) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = item.getItemMeta(); // Crear meta si no existe
        }
        
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        String enchantLore = ChatColor.translateAlternateColorCodes('&', 
            config.getString("lore-format", "&b{enchant}").replace("{enchant}", enchantment.getLoreText()));
        lore.add(enchantLore);
        
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    
    public SwordEnchantment getEnchantment(String name) {
        return enchantments.get(name.toLowerCase());
    }
    
    public List<SwordEnchantment> getEnchantsOnSword(ItemStack sword) {
        List<SwordEnchantment> foundEnchants = new ArrayList<>();
        
        if (!isSword(sword) || !sword.hasItemMeta() || !sword.getItemMeta().hasLore()) {
            return foundEnchants;
        }
        
        List<String> lore = sword.getItemMeta().getLore();
        
        for (SwordEnchantment enchant : enchantments.values()) {
            String enchantLore = ChatColor.translateAlternateColorCodes('&', 
                config.getString("lore-format", "&b{enchant}").replace("{enchant}", enchant.getLoreText()));
            
            for (String line : lore) {
                if (ChatColor.stripColor(line).equals(ChatColor.stripColor(enchantLore))) {
                    foundEnchants.add(enchant);
                    break;
                }
            }
        }
        
        return foundEnchants;
    }
    
    public Map<String, SwordEnchantment> getAllEnchantments() {
        return new HashMap<>(enchantments);
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
}