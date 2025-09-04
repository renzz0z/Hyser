package com.hyser.hysercore.prison;

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

/**
 * Comando para gestionar el sistema de PrisonPunch
 * Basado en la configuración de AzuriteSpigot
 */
public class PrisonPunchCommand implements CommandExecutor, TabCompleter {
    
    private final HyserCore plugin;
    private final PrisonPunchManager punchManager;
    
    public PrisonPunchCommand(HyserCore plugin, PrisonPunchManager punchManager) {
        this.plugin = plugin;
        this.punchManager = punchManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hysercore.admin")) {
            sendMessage(sender, "messages.no-permission");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "enable":
                handleEnable(sender);
                break;
            case "disable":
                handleDisable(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            case "test":
                handleTest(sender, args);
                break;
            case "config":
                handleConfig(sender, args);
                break;
            case "help":
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void handleReload(CommandSender sender) {
        punchManager.reloadConfig();
        sendMessage(sender, "messages.config-reloaded");
        
        sender.sendMessage(ChatColor.GREEN + "=== PrisonPunch Recargado ===");
        sender.sendMessage(ChatColor.YELLOW + "Estado: " + 
            (punchManager.isSystemEnabled() ? ChatColor.GREEN + "HABILITADO" : ChatColor.RED + "DESHABILITADO"));
        sender.sendMessage(ChatColor.YELLOW + "Configuración basada en AzuriteSpigot cargada correctamente.");
    }
    
    private void handleEnable(CommandSender sender) {
        punchManager.setSystemEnabled(true);
        sendMessage(sender, "messages.system-enabled");
        sender.sendMessage(ChatColor.GREEN + "Sistema de PrisonPunch habilitado con configuración de AzuriteSpigot.");
    }
    
    private void handleDisable(CommandSender sender) {
        punchManager.setSystemEnabled(false);
        sendMessage(sender, "messages.system-disabled");
        sender.sendMessage(ChatColor.RED + "Sistema de PrisonPunch deshabilitado.");
    }
    
    private void handleStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Estado de PrisonPunch ===");
        sender.sendMessage(ChatColor.YELLOW + "Estado: " + 
            (punchManager.isSystemEnabled() ? ChatColor.GREEN + "HABILITADO" : ChatColor.RED + "DESHABILITADO"));
        
        // Mostrar configuración principal
        sender.sendMessage(ChatColor.GRAY + "--- Configuración AzuriteSpigot ---");
        sender.sendMessage(ChatColor.AQUA + "Knockback Horizontal: " + 
            ChatColor.WHITE + punchManager.getConfig().getDouble("knockback.simple.horizontal", 0.425));
        sender.sendMessage(ChatColor.AQUA + "Knockback Vertical: " + 
            ChatColor.WHITE + punchManager.getConfig().getDouble("knockback.simple.vertical", 0.5));
        sender.sendMessage(ChatColor.AQUA + "Friction: " + 
            ChatColor.WHITE + punchManager.getConfig().getDouble("knockback.simple.friction", 2.5));
        sender.sendMessage(ChatColor.AQUA + "Modo Boost: " + 
            (punchManager.getConfig().getBoolean("prison.boostMode", true) ? 
                ChatColor.GREEN + "ACTIVO" : ChatColor.RED + "INACTIVO"));
        
        sender.sendMessage(ChatColor.GRAY + "--- Mundos Habilitados ---");
        List<String> enabledWorlds = punchManager.getConfig().getStringList("worlds.enabled-worlds");
        sender.sendMessage(ChatColor.WHITE + String.join(", ", enabledWorlds));
    }
    
    private void handleTest(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo los jugadores pueden usar el comando test.");
            return;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /prisonpunch test <jugador>");
            return;
        }
        
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Jugador no encontrado: " + args[1]);
            return;
        }
        
        if (target.equals(player)) {
            sender.sendMessage(ChatColor.RED + "No puedes probarlo en ti mismo.");
            return;
        }
        
        // Aplicar empuje de flecha de prueba (simular flecha)
        // Para test, crear una flecha temporal
        org.bukkit.entity.Arrow testArrow = player.getWorld().spawnArrow(
            player.getLocation().add(0, 1.5, 0), 
            target.getLocation().subtract(player.getLocation()).toVector().normalize(), 
            1.0f, 0.0f);
        testArrow.setShooter(player);
        
        boolean success = punchManager.applyArrowPush(player, target, testArrow);
        
        // Remover la flecha de prueba
        testArrow.remove();
        
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Empuje de flecha de prueba aplicado a " + target.getName());
            target.sendMessage(ChatColor.YELLOW + "Has recibido un empuje de flecha de prueba de " + player.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "No se pudo aplicar el empuje de flecha de prueba.");
        }
    }
    
    private void handleConfig(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Configuraciones disponibles:");
            sender.sendMessage(ChatColor.AQUA + "- horizontal: Knockback horizontal base");
            sender.sendMessage(ChatColor.AQUA + "- vertical: Knockback vertical base");
            sender.sendMessage(ChatColor.AQUA + "- friction: Fricción del knockback");
            sender.sendMessage(ChatColor.AQUA + "- boostmode: Modo boost realista (true/false)");
            sender.sendMessage(ChatColor.GRAY + "Uso: /prisonpunch config <opción> [valor]");
            return;
        }
        
        String configOption = args[1].toLowerCase();
        
        if (args.length == 2) {
            // Mostrar valor actual
            showConfigValue(sender, configOption);
        } else {
            // Establecer nuevo valor
            String value = args[2];
            setConfigValue(sender, configOption, value);
        }
    }
    
    private void showConfigValue(CommandSender sender, String option) {
        String path = getConfigPath(option);
        if (path == null) {
            sender.sendMessage(ChatColor.RED + "Opción de configuración no válida: " + option);
            return;
        }
        
        Object value = punchManager.getConfig().get(path);
        sender.sendMessage(ChatColor.YELLOW + option + ": " + ChatColor.WHITE + value);
    }
    
    private void setConfigValue(CommandSender sender, String option, String value) {
        String path = getConfigPath(option);
        if (path == null) {
            sender.sendMessage(ChatColor.RED + "Opción de configuración no válida: " + option);
            return;
        }
        
        try {
            // Determinar el tipo de valor
            Object currentValue = punchManager.getConfig().get(path);
            Object newValue;
            
            if (currentValue instanceof Boolean) {
                newValue = Boolean.parseBoolean(value);
            } else if (currentValue instanceof Double) {
                newValue = Double.parseDouble(value);
            } else if (currentValue instanceof Integer) {
                newValue = Integer.parseInt(value);
            } else {
                newValue = value;
            }
            
            punchManager.getConfig().set(path, newValue);
            punchManager.saveConfig();
            punchManager.reloadConfig();
            
            sender.sendMessage(ChatColor.GREEN + "Configuración actualizada: " + 
                ChatColor.YELLOW + option + " = " + ChatColor.WHITE + newValue);
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Valor no válido para " + option + ": " + value);
        }
    }
    
    private String getConfigPath(String option) {
        switch (option.toLowerCase()) {
            case "horizontal":
                return "knockback.simple.horizontal";
            case "vertical":
                return "knockback.simple.vertical";
            case "friction":
                return "knockback.simple.friction";
            case "boostmode":
                return "prison.boostMode";
            case "slowdown":
                return "knockback.simple.slowdown";
            case "verticalLimit":
                return "knockback.simple.verticalLimit";
            default:
                return null;
        }
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Comandos de PrisonPunch ===");
        sender.sendMessage(ChatColor.YELLOW + "/prisonpunch reload" + ChatColor.GRAY + " - Recargar configuración");
        sender.sendMessage(ChatColor.YELLOW + "/prisonpunch enable" + ChatColor.GRAY + " - Habilitar sistema");
        sender.sendMessage(ChatColor.YELLOW + "/prisonpunch disable" + ChatColor.GRAY + " - Deshabilitar sistema");
        sender.sendMessage(ChatColor.YELLOW + "/prisonpunch status" + ChatColor.GRAY + " - Ver estado y configuración");
        sender.sendMessage(ChatColor.YELLOW + "/prisonpunch test <jugador>" + ChatColor.GRAY + " - Probar punch");
        sender.sendMessage(ChatColor.YELLOW + "/prisonpunch config [opción] [valor]" + ChatColor.GRAY + " - Configuración");
        sender.sendMessage(ChatColor.AQUA + "Sistema basado en configuración de AzuriteSpigot");
    }
    
    private void sendMessage(CommandSender sender, String path) {
        String message = punchManager.getConfig().getString(path, "");
        if (!message.isEmpty()) {
            String prefix = punchManager.getConfig().getString("general.prefix", "&8[&cPrisonPunch&8] &r");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("hysercore.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "enable", "disable", "status", "test", "config", "help"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("test")) {
                // Autocompletar nombres de jugadores
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            } else if (subCommand.equals("config")) {
                completions.addAll(Arrays.asList("horizontal", "vertical", "friction", "boostmode", "slowdown"));
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("config")) {
            // Autocompletar valores de configuración
            String option = args[1].toLowerCase();
            if (option.equals("boostmode")) {
                completions.addAll(Arrays.asList("true", "false"));
            }
        }
        
        return completions;
    }
}