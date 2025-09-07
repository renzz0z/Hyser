package com.hyser.hysercore.enchantments.listeners;

import com.hyser.hysercore.enchantments.managers.SwordEnchantmentManager;
import com.hyser.hysercore.enchantments.types.SwordEnchantment;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class EnchantmentBookListener implements Listener {
    
    private SwordEnchantmentManager enchantmentManager;
    
    public EnchantmentBookListener(SwordEnchantmentManager enchantmentManager) {
        this.enchantmentManager = enchantmentManager;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!enchantmentManager.getConfig().getBoolean("advanced.enable-book-effects", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        
        // Verificar si es un libro de encantamiento
        if (!isEnchantmentBook(item)) {
            return;
        }
        
        // Verificar si el jugador tiene una espada en el inventario
        ItemStack sword = findSwordInInventory(player);
        if (sword == null) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                enchantmentManager.getConfig().getString("book-messages.need-sword-for-book", 
                "&cNecesitas tener una espada para aplicar este libro de encantamiento."));
            player.sendMessage(message);
            event.setCancelled(true);
            return;
        }
        
        // Obtener el tipo de encantamiento del libro
        String enchantmentType = getEnchantmentFromBook(item);
        if (enchantmentType == null) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                enchantmentManager.getConfig().getString("book-messages.invalid-book", 
                "&cEste no es un libro de encantamiento válido."));
            player.sendMessage(message);
            event.setCancelled(true);
            return;
        }
        
        SwordEnchantment enchantment = enchantmentManager.getEnchantment(enchantmentType);
        if (enchantment == null) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                enchantmentManager.getConfig().getString("book-messages.invalid-book", 
                "&cEste no es un libro de encantamiento válido."));
            player.sendMessage(message);
            event.setCancelled(true);
            return;
        }
        
        // Verificar si la espada ya tiene el encantamiento
        if (hasEnchantment(sword, enchantment)) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                enchantmentManager.getConfig().getString("book-messages.sword-already-has-enchant", 
                "&cEsta espada ya tiene el encantamiento &f{enchant}&c.")
                .replace("{enchant}", enchantment.getDisplayName()));
            player.sendMessage(message);
            event.setCancelled(true);
            return;
        }
        
        // Verificar tasa de éxito
        double successRate = enchantmentManager.getConfig().getDouble("advanced.book-success-rate", 1.0);
        if (Math.random() > successRate) {
            player.sendMessage(ChatColor.RED + "¡El libro falló al aplicar el encantamiento!");
            consumeBook(player, item);
            event.setCancelled(true);
            return;
        }
        
        // Aplicar el encantamiento a la espada
        addEnchantmentToSword(sword, enchantment);
        
        // Mensaje de éxito
        String successMessage = ChatColor.translateAlternateColorCodes('&', 
            enchantmentManager.getConfig().getString("book-messages.book-applied", 
            "&a¡Encantamiento &f{enchant} &aaplicado desde el libro!")
            .replace("{enchant}", enchantment.getDisplayName()));
        player.sendMessage(successMessage);
        
        // Consumir el libro
        consumeBook(player, item);
        
        String consumedMessage = ChatColor.translateAlternateColorCodes('&', 
            enchantmentManager.getConfig().getString("book-messages.book-consumed", 
            "&7El libro de encantamiento ha sido consumido."));
        player.sendMessage(consumedMessage);
        
        event.setCancelled(true);
    }
    
    private boolean isEnchantmentBook(ItemStack item) {
        if (item == null || item.getType() != Material.BOOK) {
            return false;
        }
        
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return displayName.contains("Libro de") && displayName.contains("Encantamiento");
    }
    
    private ItemStack findSwordInInventory(Player player) {
        // Primero verificar la mano
        ItemStack mainHand = player.getItemInHand();
        if (isSword(mainHand)) {
            return mainHand;
        }
        
        // Luego verificar el inventario
        for (ItemStack item : player.getInventory().getContents()) {
            if (isSword(item)) {
                return item;
            }
        }
        
        return null;
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
    
    private String getEnchantmentFromBook(ItemStack book) {
        if (!book.hasItemMeta() || !book.getItemMeta().hasLore()) {
            return null;
        }
        
        List<String> lore = book.getItemMeta().getLore();
        for (String line : lore) {
            String cleanLine = ChatColor.stripColor(line).toLowerCase();
            if (cleanLine.contains("⚡")) {
                // Extraer el nombre del encantamiento del lore
                if (cleanLine.contains("dash")) return "dash";
                if (cleanLine.contains("ice aspect")) return "ice_aspect";
                if (cleanLine.contains("sharpness")) return "sharpness";
                if (cleanLine.contains("lifesteal")) return "lifesteal";
                if (cleanLine.contains("vampire")) return "vampire";
                if (cleanLine.contains("bleeding")) return "bleeding";
                if (cleanLine.contains("explosive")) return "explosive";
                if (cleanLine.contains("executioner")) return "executioner";
            }
        }
        
        return null;
    }
    
    private boolean hasEnchantment(ItemStack sword, SwordEnchantment enchantment) {
        if (!sword.hasItemMeta() || !sword.getItemMeta().hasLore()) {
            return false;
        }
        
        List<String> lore = sword.getItemMeta().getLore();
        String enchantLore = ChatColor.translateAlternateColorCodes('&', 
            enchantmentManager.getConfig().getString("lore-format", "&b⚡ {enchant}")
            .replace("{enchant}", enchantment.getLoreText()));
        
        for (String line : lore) {
            if (ChatColor.stripColor(line).equals(ChatColor.stripColor(enchantLore))) {
                return true;
            }
        }
        
        return false;
    }
    
    private void addEnchantmentToSword(ItemStack sword, SwordEnchantment enchantment) {
        ItemMeta meta = sword.getItemMeta();
        if (meta == null) {
            meta = sword.getItemMeta();
        }
        
        List<String> lore = meta.hasLore() ? meta.getLore() : new java.util.ArrayList<>();
        
        String enchantLore = ChatColor.translateAlternateColorCodes('&', 
            enchantmentManager.getConfig().getString("lore-format", "&b⚡ {enchant}")
            .replace("{enchant}", enchantment.getLoreText()));
        lore.add(enchantLore);
        
        meta.setLore(lore);
        sword.setItemMeta(meta);
    }
    
    private void consumeBook(Player player, ItemStack book) {
        if (book.getAmount() > 1) {
            book.setAmount(book.getAmount() - 1);
        } else {
            player.setItemInHand(new ItemStack(Material.AIR));
        }
    }
}