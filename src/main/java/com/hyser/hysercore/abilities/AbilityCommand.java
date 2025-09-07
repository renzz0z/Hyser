package com.hyser.hysercore.abilities;

import com.hyser.hysercore.HyserCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbilityCommand implements CommandExecutor, TabCompleter {
    private final HyserCore plugin;
    private final AbilityManager abilityManager;
    
    public AbilityCommand(HyserCore plugin, AbilityManager abilityManager) {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = abilityManager.getPrefix();
        
        if (args.length == 0) {
            sender.sendMessage(prefix + ChatColor.YELLOW + "Comandos de Objetos de Abilities:");
            sender.sendMessage(ChatColor.GRAY + "  /abilities list - Listar todos los objetos");
            sender.sendMessage(ChatColor.GRAY + "  /abilities info <objeto> - Información de un objeto");
            sender.sendMessage(ChatColor.GRAY + "  /abilities give <jugador> <objeto> - Dar objeto a jugador");
            sender.sendMessage(ChatColor.GRAY + "  /abilities reload - Recargar configuración");
            return true;
        }
        
        String subcommand = args[0].toLowerCase();
        
        switch (subcommand) {
            case "list":
                return handleListCommand(sender, prefix);
                
            case "info":
                if (args.length < 2) {
                    sender.sendMessage(prefix + ChatColor.RED + "Uso: /abilities info <ability>");
                    return true;
                }
                return handleInfoCommand(sender, args[1], prefix);
                
            case "reload":
                return handleReloadCommand(sender, prefix);
                
            case "give":
                if (args.length < 3) {
                    sender.sendMessage(prefix + ChatColor.RED + "Uso: /abilities give <jugador> <objeto>");
                    return true;
                }
                return handleGiveCommand(sender, args[1], args[2], prefix);
                
            default:
                sender.sendMessage(prefix + ChatColor.RED + "Subcomando desconocido: " + subcommand);
                return true;
        }
    }
    
    private boolean handleListCommand(CommandSender sender, String prefix) {
        if (abilityManager.getAbilities().isEmpty()) {
            sender.sendMessage(prefix + ChatColor.YELLOW + "No hay objetos de ability cargados.");
            return true;
        }
        
        sender.sendMessage(prefix + ChatColor.YELLOW + "Objetos de Ability disponibles:");
        
        for (Ability ability : abilityManager.getAbilities().values()) {
            String status = ability.isEnabled() ? 
                ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
            
            sender.sendMessage(ChatColor.GRAY + "  " + status + " " + 
                ChatColor.WHITE + ability.getId() + 
                ChatColor.GRAY + " - " + ability.getName());
        }
        
        return true;
    }
    
    private boolean handleInfoCommand(CommandSender sender, String abilityId, String prefix) {
        Ability ability = abilityManager.getAbility(abilityId);
        
        if (ability == null) {
            sender.sendMessage(prefix + ChatColor.RED + "Ability '" + abilityId + "' no encontrada.");
            return true;
        }
        
        sender.sendMessage(prefix + ChatColor.YELLOW + "Información de: " + ability.getName());
        sender.sendMessage(ChatColor.GRAY + "ID: " + ChatColor.WHITE + ability.getId());
        List<String> descriptions = ability.getDescription();
        if (descriptions != null && !descriptions.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Descripción:");
            for (String desc : descriptions) {
                sender.sendMessage(ChatColor.GRAY + "  " + ChatColor.WHITE + desc);
            }
        }
        sender.sendMessage(ChatColor.GRAY + "Permiso: " + ChatColor.WHITE + ability.getPermission());
        sender.sendMessage(ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + ability.getCooldown() + "s");
        sender.sendMessage(ChatColor.GRAY + "Estado: " + 
            (ability.isEnabled() ? ChatColor.GREEN + "Activa" : ChatColor.RED + "Desactivada"));
        
        if (ability.getTriggers() != null && !ability.getTriggers().isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Triggers: " + ChatColor.WHITE + ability.getTriggers().size());
        }
        
        if (ability.getActions() != null && !ability.getActions().isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Acciones: " + ChatColor.WHITE + ability.getActions().size());
        }
        
        return true;
    }
    
    private boolean handleReloadCommand(CommandSender sender, String prefix) {
        if (!sender.hasPermission("hysercore.admin")) {
            sender.sendMessage(prefix + ChatColor.RED + "No tienes permisos para recargar abilities.");
            return true;
        }
        
        try {
            abilityManager.reload();
            sender.sendMessage(prefix + ChatColor.GREEN + "Sistema de abilities recargado exitosamente.");
            sender.sendMessage(prefix + ChatColor.GRAY + "Abilities cargadas: " + 
                abilityManager.getAbilities().size());
        } catch (Exception e) {
            sender.sendMessage(prefix + ChatColor.RED + "Error al recargar abilities: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleGiveCommand(CommandSender sender, String playerName, String abilityId, String prefix) {
        if (!sender.hasPermission("hysercore.admin")) {
            sender.sendMessage(prefix + ChatColor.RED + "No tienes permisos para dar objetos de ability.");
            return true;
        }
        
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(prefix + ChatColor.RED + "Jugador '" + playerName + "' no encontrado.");
            return true;
        }
        
        ItemStack abilityItem = abilityManager.getItemManager().createAbilityItem(abilityId);
        if (abilityItem == null) {
            sender.sendMessage(prefix + ChatColor.RED + "Objeto de ability '" + abilityId + "' no encontrado.");
            return true;
        }
        
        target.getInventory().addItem(abilityItem);
        sender.sendMessage(prefix + ChatColor.GREEN + "Objeto '" + abilityId + "' entregado a " + target.getName());
        target.sendMessage(prefix + ChatColor.GREEN + "Has recibido un objeto especial: " + abilityItem.getItemMeta().getDisplayName());
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("list", "info", "reload");
            for (String sub : subcommands) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && "info".equalsIgnoreCase(args[0])) {
            for (String abilityId : abilityManager.getAbilities().keySet()) {
                if (abilityId.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(abilityId);
                }
            }
        }
        
        return completions;
    }
}