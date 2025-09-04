package com.hyser.hysercore.commands;

import com.hyser.hysercore.HyserCore;
import com.hyser.hysercore.managers.ChatGameManager;
import com.hyser.hysercore.utils.PlayerStats;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatGamesCommand implements CommandExecutor, TabCompleter {
    
    private HyserCore plugin;
    private ChatGameManager gameManager;
    
    public ChatGamesCommand(HyserCore plugin, ChatGameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hysercore.admin")) {
            String noPermission = ChatColor.translateAlternateColorCodes('&', 
                plugin.getChatGamesConfig().getString("messages.no-permission", "&cNo tienes permisos para usar este comando."));
            sender.sendMessage(noPermission);
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
            case "start":
                handleStart(sender, args);
                break;
            case "stop":
                handleStop(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            case "stats":
                handleStats(sender, args);
                break;
            case "enable":
                handleEnable(sender);
                break;
            case "disable":
                handleDisable(sender);
                break;
            case "help":
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void handleReload(CommandSender sender) {
        gameManager.reloadConfig();
        String reloadMessage = ChatColor.translateAlternateColorCodes('&', 
            plugin.getChatGamesConfig().getString("messages.plugin-reloaded", "&aConfiguración de ChatGames recargada correctamente."));
        sender.sendMessage(reloadMessage);
    }
    
    private void handleStart(CommandSender sender, String[] args) {
        if (gameManager.isGameActive()) {
            sender.sendMessage(ChatColor.RED + "Ya hay un ChatGame activo.");
            return;
        }
        
        String gameType = "random";
        if (args.length > 1) {
            gameType = args[1].toLowerCase();
        }
        
        if (gameType.equals("random")) {
            gameManager.forceStartGame(null);
        } else {
            List<String> validTypes = Arrays.asList("math", "word", "trivia", "complete", "number");
            if (!validTypes.contains(gameType)) {
                sender.sendMessage(ChatColor.RED + "Tipo de juego inválido. Tipos válidos: " + String.join(", ", validTypes));
                return;
            }
            gameManager.forceStartGame(gameType);
        }
        
        sender.sendMessage(ChatColor.GREEN + "ChatGame iniciado manualmente.");
    }
    
    private void handleStop(CommandSender sender) {
        if (!gameManager.isGameActive()) {
            sender.sendMessage(ChatColor.RED + "No hay ningún ChatGame activo.");
            return;
        }
        
        gameManager.stopCurrentGame();
        sender.sendMessage(ChatColor.GREEN + "ChatGame detenido.");
    }
    
    private void handleStatus(CommandSender sender) {
        boolean enabled = plugin.getChatGamesConfig().getBoolean("general.enabled", true);
        boolean gameActive = gameManager.isGameActive();
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int minPlayers = plugin.getChatGamesConfig().getInt("general.min-players", 3);
        
        sender.sendMessage(ChatColor.GOLD + "=== Estado de ChatGames ===");
        sender.sendMessage(ChatColor.YELLOW + "Sistema: " + (enabled ? ChatColor.GREEN + "Habilitado" : ChatColor.RED + "Deshabilitado"));
        sender.sendMessage(ChatColor.YELLOW + "Juego activo: " + (gameActive ? ChatColor.GREEN + "Sí" : ChatColor.RED + "No"));
        sender.sendMessage(ChatColor.YELLOW + "Jugadores online: " + ChatColor.WHITE + onlinePlayers + "/" + minPlayers + " (mínimo)");
        
        if (enabled && !gameActive) {
            int minInterval = plugin.getChatGamesConfig().getInt("general.interval.min", 300);
            int maxInterval = plugin.getChatGamesConfig().getInt("general.interval.max", 600);
            sender.sendMessage(ChatColor.YELLOW + "Próximo juego: " + ChatColor.WHITE + "Entre " + minInterval + " y " + maxInterval + " segundos");
        }
    }
    
    private void handleStats(CommandSender sender, String[] args) {
        if (args.length > 1) {
            // Estadísticas de un jugador específico
            String playerName = args[1];
            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Jugador no encontrado o no está online.");
                return;
            }
            
            showPlayerStats(sender, target);
        } else {
            // Ranking general
            showTopPlayers(sender);
        }
    }
    
    private void showPlayerStats(CommandSender sender, Player target) {
        PlayerStats stats = gameManager.getPlayerStats();
        UUID playerId = target.getUniqueId();
        
        int gamesPlayed = stats.getGamesPlayed(playerId);
        int gamesWon = stats.getGamesWon(playerId);
        double winRate = stats.getWinRate(playerId);
        int totalRewards = stats.getTotalRewards(playerId);
        
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getChatGamesConfig().getString("general.prefix", ""));
        
        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', 
            plugin.getChatGamesConfig().getString("messages.stats-header", "&8&m----------&r &6Estadísticas &8&m----------")));
        sender.sendMessage(ChatColor.YELLOW + "Jugador: " + ChatColor.WHITE + target.getName());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            plugin.getChatGamesConfig().getString("messages.stats-games-played", "&7Juegos jugados: &e{games}").replace("{games}", String.valueOf(gamesPlayed))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            plugin.getChatGamesConfig().getString("messages.stats-games-won", "&7Juegos ganados: &a{wins}").replace("{wins}", String.valueOf(gamesWon))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            plugin.getChatGamesConfig().getString("messages.stats-win-rate", "&7Tasa de victoria: &b{rate}%").replace("{rate}", String.format("%.1f", winRate))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            plugin.getChatGamesConfig().getString("messages.stats-total-rewards", "&7Recompensas totales: &6${rewards}").replace("{rewards}", String.valueOf(totalRewards))));
    }
    
    private void showTopPlayers(CommandSender sender) {
        PlayerStats stats = gameManager.getPlayerStats();
        Map<UUID, Integer> topPlayers = stats.getTopPlayers(10);
        
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getChatGamesConfig().getString("general.prefix", ""));
        
        sender.sendMessage(prefix + ChatColor.GOLD + "=== Top 10 Jugadores ===");
        
        if (topPlayers.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No hay estadísticas disponibles.");
            return;
        }
        
        int position = 1;
        for (Map.Entry<UUID, Integer> entry : topPlayers.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            String playerName = player != null ? player.getName() : "Jugador desconocido";
            int wins = entry.getValue();
            int totalGames = stats.getGamesPlayed(entry.getKey());
            double winRate = stats.getWinRate(entry.getKey());
            
            sender.sendMessage(ChatColor.YELLOW + "#" + position + " " + ChatColor.WHITE + playerName + 
                ChatColor.GRAY + " - " + ChatColor.GREEN + wins + " victorias " + 
                ChatColor.GRAY + "(" + String.format("%.1f", winRate) + "% de " + totalGames + " juegos)");
            position++;
        }
    }
    
    private void handleEnable(CommandSender sender) {
        plugin.getChatGamesConfig().set("general.enabled", true);
        plugin.saveChatGamesConfig();
        gameManager.reloadConfig();
        
        String enabledMessage = ChatColor.translateAlternateColorCodes('&', 
            plugin.getChatGamesConfig().getString("messages.games-enabled", "&aChatGames han sido habilitados."));
        sender.sendMessage(enabledMessage);
    }
    
    private void handleDisable(CommandSender sender) {
        plugin.getChatGamesConfig().set("general.enabled", false);
        plugin.saveChatGamesConfig();
        
        if (gameManager.isGameActive()) {
            gameManager.stopCurrentGame();
        }
        
        gameManager.reloadConfig();
        
        String disabledMessage = ChatColor.translateAlternateColorCodes('&', 
            plugin.getChatGamesConfig().getString("messages.games-disabled", "&cChatGames han sido deshabilitados."));
        sender.sendMessage(disabledMessage);
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Comandos de ChatGames ===");
        sender.sendMessage(ChatColor.YELLOW + "/chatgames reload" + ChatColor.GRAY + " - Recargar configuración");
        sender.sendMessage(ChatColor.YELLOW + "/chatgames start [tipo]" + ChatColor.GRAY + " - Iniciar juego manualmente");
        sender.sendMessage(ChatColor.YELLOW + "/chatgames stop" + ChatColor.GRAY + " - Detener juego actual");
        sender.sendMessage(ChatColor.YELLOW + "/chatgames status" + ChatColor.GRAY + " - Ver estado del sistema");
        sender.sendMessage(ChatColor.YELLOW + "/chatgames stats [jugador]" + ChatColor.GRAY + " - Ver estadísticas");
        sender.sendMessage(ChatColor.YELLOW + "/chatgames enable" + ChatColor.GRAY + " - Habilitar sistema");
        sender.sendMessage(ChatColor.YELLOW + "/chatgames disable" + ChatColor.GRAY + " - Deshabilitar sistema");
        sender.sendMessage(ChatColor.GRAY + "Tipos de juego: math, word, trivia, complete, number");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("hysercore.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            String[] subCommands = {"reload", "start", "stop", "status", "stats", "enable", "disable", "help"};
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("start")) {
                String[] gameTypes = {"math", "word", "trivia", "complete", "number", "random"};
                for (String gameType : gameTypes) {
                    if (gameType.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(gameType);
                    }
                }
            } else if (args[0].equalsIgnoreCase("stats")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}