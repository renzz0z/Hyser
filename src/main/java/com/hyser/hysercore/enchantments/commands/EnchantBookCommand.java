package com.hyser.hysercore.enchantments.commands;

import com.hyser.hysercore.enchantments.managers.SwordEnchantmentManager;
import com.hyser.hysercore.enchantments.types.SwordEnchantment;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnchantBookCommand implements CommandExecutor, TabCompleter {
    
    private SwordEnchantmentManager enchantmentManager;
    
    public EnchantBookCommand(SwordEnchantmentManager enchantmentManager) {
        this.enchantmentManager = enchantmentManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser usado por jugadores.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("hysercore.admin")) {
            String noPermission = ChatColor.translateAlternateColorCodes('&', 
                enchantmentManager.getConfig().getString("messages.no-permission", "&cNo tienes permisos para usar este comando."));
            player.sendMessage(noPermission);
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String enchantType = args[0].toLowerCase();
        SwordEnchantment enchantment = null;
        
        switch (enchantType) {
            case "dash":
                enchantment = enchantmentManager.getEnchantment("dash");
                break;
            case "iceaspect":
            case "ice_aspect":
                enchantment = enchantmentManager.getEnchantment("ice_aspect");
                break;
            case "sharpness":
                enchantment = enchantmentManager.getEnchantment("sharpness");
                break;
            case "lifesteal":
                enchantment = enchantmentManager.getEnchantment("lifesteal");
                break;
            case "vampire":
                enchantment = enchantmentManager.getEnchantment("vampire");
                break;
            case "bleeding":
                enchantment = enchantmentManager.getEnchantment("bleeding");
                break;
            case "explosive":
                enchantment = enchantmentManager.getEnchantment("explosive");
                break;
            case "executioner":
                enchantment = enchantmentManager.getEnchantment("executioner");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Tipo de encantamiento inválido: " + enchantType);
                sendHelp(player);
                return true;
        }
        
        if (enchantment == null) {
            player.sendMessage(ChatColor.RED + "El encantamiento " + enchantType + " no está disponible.");
            return true;
        }
        
        // Crear libro de encantamiento
        ItemStack book = createEnchantmentBook(enchantment);
        
        // Dar el libro al jugador
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), book);
            player.sendMessage(ChatColor.YELLOW + "Tu inventario está lleno. El libro se ha dejado caer en el suelo.");
        } else {
            player.getInventory().addItem(book);
        }
        
        String successMessage = ChatColor.translateAlternateColorCodes('&', 
            "&a¡Libro de encantamiento &f{enchant} &acreado exitosamente!")
            .replace("{enchant}", enchantment.getDisplayName());
        player.sendMessage(successMessage);
        
        return true;
    }
    
    private ItemStack createEnchantmentBook(SwordEnchantment enchantment) {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        
        String bookName = ChatColor.translateAlternateColorCodes('&', 
            "&6Libro de &e" + enchantment.getDisplayName());
        meta.setDisplayName(bookName);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Libro de encantamiento"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&8Haz click derecho con una espada"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&8en tu inventario para aplicar."));
        lore.add("");
        lore.add(ChatColor.translateAlternateColorCodes('&', "&b⚡ " + enchantment.getLoreText()));
        
        // Agregar descripción específica del encantamiento
        switch (enchantment.getName()) {
            case "dash":
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7Impulso aéreo hacia adelante"));
                break;
            case "ice_aspect":
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7Efectos de hielo al atacar"));
                break;
            case "sharpness":
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7Aumenta el daño de ataque"));
                break;
            case "lifesteal":
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7Roba vida de los enemigos"));
                break;
            case "vampire":
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7Regeneración al atacar"));
                break;
            case "bleeding":
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7Causa sangrado continuo"));
                break;
            case "explosive":
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7Knockback explosivo"));
                break;
            case "executioner":
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7Más daño a enemigos heridos"));
                break;
        }
        
        meta.setLore(lore);
        book.setItemMeta(meta);
        
        return book;
    }
    
    private void sendHelp(Player player) {
        String prefix = ChatColor.translateAlternateColorCodes('&', 
            enchantmentManager.getConfig().getString("general.prefix", "&8[&bSwordEnchant&8] "));
        
        player.sendMessage(prefix + ChatColor.GOLD + "=== Comandos de Libros de Encantamiento ===");
        player.sendMessage(ChatColor.YELLOW + "/enchantbook dash" + ChatColor.GRAY + " - Crear libro de Dash");
        player.sendMessage(ChatColor.YELLOW + "/enchantbook iceaspect" + ChatColor.GRAY + " - Crear libro de Ice Aspect");
        player.sendMessage(ChatColor.YELLOW + "/enchantbook sharpness" + ChatColor.GRAY + " - Crear libro de Sharpness+");
        player.sendMessage(ChatColor.YELLOW + "/enchantbook lifesteal" + ChatColor.GRAY + " - Crear libro de Lifesteal");
        player.sendMessage(ChatColor.YELLOW + "/enchantbook vampire" + ChatColor.GRAY + " - Crear libro de Vampire");
        player.sendMessage(ChatColor.YELLOW + "/enchantbook bleeding" + ChatColor.GRAY + " - Crear libro de Bleeding");
        player.sendMessage(ChatColor.YELLOW + "/enchantbook explosive" + ChatColor.GRAY + " - Crear libro de Explosive");
        player.sendMessage(ChatColor.YELLOW + "/enchantbook executioner" + ChatColor.GRAY + " - Crear libro de Executioner");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("hysercore.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            String[] enchantTypes = {"dash", "iceaspect", "sharpness", "lifesteal", "vampire", "bleeding", "explosive", "executioner"};
            for (String type : enchantTypes) {
                if (type.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(type);
                }
            }
        }
        
        return completions;
    }
}