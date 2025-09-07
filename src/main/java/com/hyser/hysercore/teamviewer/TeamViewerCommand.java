package com.hyser.hysercore.teamviewer;

import com.hyser.hysercore.HyserCore;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamViewerCommand implements CommandExecutor, TabCompleter {
    
    private HyserCore plugin;
    private LunarTeamViewer teamViewerManager;
    
    public TeamViewerCommand(HyserCore plugin, LunarTeamViewer teamViewerManager) {
        this.plugin = plugin;
        this.teamViewerManager = teamViewerManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser usado por jugadores.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            teamViewerManager.showTeamMembers(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "on":
            case "enable":
                teamViewerManager.toggleTeamViewer(player);
                break;
                
            case "off":
            case "disable":
                if (teamViewerManager.isTeamViewerEnabled(player)) {
                    teamViewerManager.toggleTeamViewer(player);
                } else {
                    String message = ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfig().getString("lunar-teamviewer.messages.teamviewer-disabled", 
                        "&cTeamViewer ya está desactivado."));
                    player.sendMessage(message);
                }
                break;
                
            case "toggle":
                teamViewerManager.toggleTeamViewer(player);
                break;
                
            case "list":
            case "show":
                teamViewerManager.showTeamMembers(player);
                break;
                
            case "help":
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        String prefix = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("lunar-teamviewer.messages.prefix", "&8[&dTeamViewer&8] "));
        
        player.sendMessage(prefix + ChatColor.GOLD + "=== Comandos de LunarTeamViewer ===");
        player.sendMessage(ChatColor.YELLOW + "/teamviewer" + ChatColor.GRAY + " - Ver miembros del clan");
        player.sendMessage(ChatColor.YELLOW + "/teamviewer on" + ChatColor.GRAY + " - Activar teamviewer");
        player.sendMessage(ChatColor.YELLOW + "/teamviewer off" + ChatColor.GRAY + " - Desactivar teamviewer");
        player.sendMessage(ChatColor.YELLOW + "/teamviewer toggle" + ChatColor.GRAY + " - Alternar estado");
        player.sendMessage(ChatColor.YELLOW + "/teamviewer list" + ChatColor.GRAY + " - Mostrar lista");
        player.sendMessage(ChatColor.YELLOW + "/teamviewer help" + ChatColor.GRAY + " - Ver esta ayuda");
        player.sendMessage(ChatColor.GRAY + "Solo muestra jugadores en el mismo mundo.");
        player.sendMessage(ChatColor.GRAY + "Requiere estar en un clan (UltimateClans).");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("hysercore.teamviewer") && !sender.hasPermission("hysercore.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            String[] subCommands = {"on", "off", "toggle", "list", "show", "enable", "disable", "help"};
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        }
        
        return completions;
    }
}