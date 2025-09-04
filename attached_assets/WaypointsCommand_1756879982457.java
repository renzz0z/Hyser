package com.hyser.hysercore.waypoints;

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

public class WaypointsCommand implements CommandExecutor, TabCompleter {
    
    private HyserCore plugin;
    private LunarWaypoints waypointManager;
    
    public WaypointsCommand(HyserCore plugin, LunarWaypoints waypointManager) {
        this.plugin = plugin;
        this.waypointManager = waypointManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser usado por jugadores.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "set":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /waypoints set <nombre>");
                    return true;
                }
                String waypointName = args[1];
                waypointManager.setWaypoint(player, waypointName, player.getLocation());
                break;
                
            case "list":
            case "show":
                waypointManager.showWaypoints(player);
                break;
                
            case "remove":
            case "delete":
                waypointManager.removeWaypoint(player);
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
            plugin.getConfig().getString("lunar-waypoints.messages.prefix", "&8[&bWaypoints&8] "));
        
        player.sendMessage(prefix + ChatColor.GOLD + "=== Comandos de LunarWaypoints ===");
        player.sendMessage(ChatColor.YELLOW + "/waypoints set <nombre>" + ChatColor.GRAY + " - Establecer waypoint");
        player.sendMessage(ChatColor.YELLOW + "/waypoints list" + ChatColor.GRAY + " - Ver waypoints del clan");
        player.sendMessage(ChatColor.YELLOW + "/waypoints remove" + ChatColor.GRAY + " - Eliminar tu waypoint");
        player.sendMessage(ChatColor.YELLOW + "/waypoints help" + ChatColor.GRAY + " - Ver esta ayuda");
        player.sendMessage(ChatColor.GRAY + "Los waypoints solo funcionan en el mismo mundo.");
        player.sendMessage(ChatColor.GRAY + "Requiere estar en un clan (UltimateClans).");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("hysercore.waypoints") && !sender.hasPermission("hysercore.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            String[] subCommands = {"set", "list", "show", "remove", "delete", "help"};
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        }
        
        return completions;
    }
}