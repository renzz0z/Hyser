package com.hyser.hysercore.enchantments.commands;

import com.hyser.hysercore.HyserCore;
import com.hyser.hysercore.enchantments.managers.SwordEnchantmentManager;
import com.hyser.hysercore.enchantments.types.SwordEnchantment;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwordEnchantCommand implements CommandExecutor, TabCompleter {
    
    private HyserCore plugin;
    private SwordEnchantmentManager enchantmentManager;
    
    public SwordEnchantCommand(HyserCore plugin, SwordEnchantmentManager enchantmentManager) {
        this.plugin = plugin;
        this.enchantmentManager = enchantmentManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser usado por jugadores.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("hysercore.swordenchant")) {
            String noPermission = ChatColor.translateAlternateColorCodes('&', 
                enchantmentManager.getConfig().getString("messages.no-permission", "&cNo tienes permisos para usar este comando."));
            player.sendMessage(noPermission);
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                handleReload(player);
                break;
            case "list":
                handleList(player);
                break;
            case "dash":
                enchantmentManager.applySwordEnchantment(player, "dash");
                break;
            case "ice_aspect":
            case "iceaspect":
                enchantmentManager.applySwordEnchantment(player, "ice_aspect");
                break;
            case "sharpness":
                enchantmentManager.applySwordEnchantment(player, "sharpness");
                break;
            case "lifesteal":
                enchantmentManager.applySwordEnchantment(player, "lifesteal");
                break;
            case "vampire":
                enchantmentManager.applySwordEnchantment(player, "vampire");
                break;
            case "bleeding":
                enchantmentManager.applySwordEnchantment(player, "bleeding");
                break;
            case "explosive":
                enchantmentManager.applySwordEnchantment(player, "explosive");
                break;
            case "executioner":
                enchantmentManager.applySwordEnchantment(player, "executioner");
                break;
            case "help":
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("hysercore.admin")) {
            String noPermission = ChatColor.translateAlternateColorCodes('&', 
                enchantmentManager.getConfig().getString("messages.no-permission", "&cNo tienes permisos para usar este comando."));
            sender.sendMessage(noPermission);
            return;
        }
        
        enchantmentManager.reloadConfig();
        String reloadMessage = ChatColor.translateAlternateColorCodes('&', 
            enchantmentManager.getConfig().getString("messages.config-reloaded", "&aConfiguración de Sword Enchantments recargada correctamente."));
        sender.sendMessage(reloadMessage);
    }
    
    private void handleList(Player player) {
        String prefix = ChatColor.translateAlternateColorCodes('&', 
            enchantmentManager.getConfig().getString("general.prefix", "&8[&bSwordEnchant&8] "));
        
        player.sendMessage(prefix + ChatColor.GOLD + "=== Encantamientos Disponibles ===");
        
        for (SwordEnchantment enchant : enchantmentManager.getAllEnchantments().values()) {
            player.sendMessage(ChatColor.YELLOW + "• " + ChatColor.WHITE + enchant.getDisplayName() + 
                ChatColor.GRAY + " - " + enchant.getLoreText());
        }
        
        player.sendMessage(ChatColor.GRAY + "Uso: /swordenchant <tipo>");
    }
    
    private void sendHelp(Player player) {
        String prefix = ChatColor.translateAlternateColorCodes('&', 
            enchantmentManager.getConfig().getString("general.prefix", "&8[&bSwordEnchant&8] "));
        
        player.sendMessage(prefix + ChatColor.GOLD + "=== Comandos de Sword Enchantments ===");
        player.sendMessage(ChatColor.YELLOW + "/swordenchant dash" + ChatColor.GRAY + " - Aplicar encantamiento Dash");
        player.sendMessage(ChatColor.YELLOW + "/swordenchant iceaspect" + ChatColor.GRAY + " - Aplicar encantamiento Ice Aspect");
        player.sendMessage(ChatColor.YELLOW + "/swordenchant sharpness" + ChatColor.GRAY + " - Aplicar encantamiento Sharpness+");
        player.sendMessage(ChatColor.YELLOW + "/swordenchant lifesteal" + ChatColor.GRAY + " - Aplicar encantamiento Lifesteal");
        player.sendMessage(ChatColor.YELLOW + "/swordenchant vampire" + ChatColor.GRAY + " - Aplicar encantamiento Vampire");
        player.sendMessage(ChatColor.YELLOW + "/swordenchant bleeding" + ChatColor.GRAY + " - Aplicar encantamiento Bleeding");
        player.sendMessage(ChatColor.YELLOW + "/swordenchant explosive" + ChatColor.GRAY + " - Aplicar encantamiento Explosive");
        player.sendMessage(ChatColor.YELLOW + "/swordenchant executioner" + ChatColor.GRAY + " - Aplicar encantamiento Executioner");
        player.sendMessage(ChatColor.YELLOW + "/swordenchant list" + ChatColor.GRAY + " - Ver encantamientos disponibles");
        player.sendMessage(ChatColor.YELLOW + "/swordenchant reload" + ChatColor.GRAY + " - Recargar configuración");
        player.sendMessage(ChatColor.GRAY + "Necesitas tener una espada en la mano para aplicar encantamientos.");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("hysercore.swordenchant")) {
            return completions;
        }
        
        if (args.length == 1) {
            String[] subCommands = {"dash", "iceaspect", "ice_aspect", "sharpness", "lifesteal", "vampire", "bleeding", "explosive", "executioner", "list", "help"};
            
            // Agregar reload si tiene permisos de admin
            if (sender.hasPermission("hysercore.admin")) {
                List<String> allCommands = new ArrayList<>(Arrays.asList(subCommands));
                allCommands.add("reload");
                subCommands = allCommands.toArray(new String[0]);
            }
            
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        }
        
        return completions;
    }
}