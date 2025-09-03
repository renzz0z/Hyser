package com.hyser.hysercore.managers;

import com.hyser.hysercore.HyserCore;
import com.hyser.hysercore.games.ChatGame;
import com.hyser.hysercore.games.MathGame;
import com.hyser.hysercore.games.WordGame;
import com.hyser.hysercore.games.TriviaGame;
import com.hyser.hysercore.games.CompleteGame;
import com.hyser.hysercore.games.NumberGame;
import com.hyser.hysercore.utils.PlayerStats;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ChatGameManager implements Listener {
    
    private HyserCore plugin;
    private FileConfiguration config;
    private ChatGame currentGame;
    private BukkitTask gameTask;
    private BukkitTask intervalTask;
    private PlayerStats playerStats;
    
    public ChatGameManager(HyserCore plugin) {
        this.plugin = plugin;
        this.playerStats = new PlayerStats();
        loadConfig();
        startGameInterval();
    }
    
    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "chatgames.yml");
        if (!configFile.exists()) {
            plugin.saveResource("chatgames.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    public void reloadConfig() {
        loadConfig();
        restartGameInterval();
        plugin.getLogger().info("Configuración de ChatGames recargada");
    }
    
    private void startGameInterval() {
        if (!config.getBoolean("general.enabled", true)) {
            return;
        }
        
        int minInterval = config.getInt("general.interval.min", 300);
        int maxInterval = config.getInt("general.interval.max", 600);
        int interval = ThreadLocalRandom.current().nextInt(minInterval, maxInterval + 1);
        
        intervalTask = new BukkitRunnable() {
            @Override
            public void run() {
                startRandomGame();
                startGameInterval(); // Programar el siguiente juego
            }
        }.runTaskLater(plugin, interval * 20L);
    }
    
    private void restartGameInterval() {
        if (intervalTask != null) {
            intervalTask.cancel();
        }
        startGameInterval();
    }
    
    private void startRandomGame() {
        if (currentGame != null) {
            return; // Ya hay un juego en progreso
        }
        
        int minPlayers = config.getInt("general.min-players", 3);
        if (Bukkit.getOnlinePlayers().size() < minPlayers) {
            if (config.getBoolean("advanced.cancel-if-not-enough-players", true)) {
                return;
            }
        }
        
        String gameType = selectRandomGameType();
        if (gameType == null) {
            return;
        }
        
        currentGame = createGame(gameType);
        if (currentGame != null) {
            startGame();
        }
    }
    
    private String selectRandomGameType() {
        List<String> availableTypes = new ArrayList<>();
        Map<String, Integer> weights = new HashMap<>();
        
        if (config.getBoolean("game-types.math.enabled", true)) {
            int weight = config.getInt("game-types.math.weight", 30);
            weights.put("math", weight);
            for (int i = 0; i < weight; i++) {
                availableTypes.add("math");
            }
        }
        
        if (config.getBoolean("game-types.word.enabled", true)) {
            int weight = config.getInt("game-types.word.weight", 25);
            weights.put("word", weight);
            for (int i = 0; i < weight; i++) {
                availableTypes.add("word");
            }
        }
        
        if (config.getBoolean("game-types.trivia.enabled", true)) {
            int weight = config.getInt("game-types.trivia.weight", 20);
            weights.put("trivia", weight);
            for (int i = 0; i < weight; i++) {
                availableTypes.add("trivia");
            }
        }
        
        if (config.getBoolean("game-types.complete.enabled", true)) {
            int weight = config.getInt("game-types.complete.weight", 15);
            weights.put("complete", weight);
            for (int i = 0; i < weight; i++) {
                availableTypes.add("complete");
            }
        }
        
        if (config.getBoolean("game-types.number.enabled", true)) {
            int weight = config.getInt("game-types.number.weight", 10);
            weights.put("number", weight);
            for (int i = 0; i < weight; i++) {
                availableTypes.add("number");
            }
        }
        
        if (availableTypes.isEmpty()) {
            return null;
        }
        
        return availableTypes.get(ThreadLocalRandom.current().nextInt(availableTypes.size()));
    }
    
    private ChatGame createGame(String type) {
        switch (type.toLowerCase()) {
            case "math":
                return new MathGame(config);
            case "word":
                return new WordGame(config);
            case "trivia":
                return new TriviaGame(config);
            case "complete":
                return new CompleteGame(config);
            case "number":
                return new NumberGame(config);
            default:
                return null;
        }
    }
    
    private void startGame() {
        if (currentGame == null) {
            return;
        }
        
        // Anunciar inicio del juego
        String prefix = ChatColor.translateAlternateColorCodes('&', config.getString("general.prefix", ""));
        String gameStarting = ChatColor.translateAlternateColorCodes('&', config.getString("messages.game-starting", ""));
        String gameType = ChatColor.translateAlternateColorCodes('&', config.getString("messages.game-type", "").replace("{type}", currentGame.getTypeName()));
        int duration = config.getInt("general.duration", 60);
        String gameDuration = ChatColor.translateAlternateColorCodes('&', config.getString("messages.game-duration", "").replace("{duration}", String.valueOf(duration)));
        int reward = config.getInt("rewards.winner.money", 1000);
        String gameReward = ChatColor.translateAlternateColorCodes('&', config.getString("messages.game-reward", "").replace("{reward}", String.valueOf(reward)));
        
        Bukkit.broadcastMessage(prefix + gameStarting);
        Bukkit.broadcastMessage(prefix + gameType);
        Bukkit.broadcastMessage(prefix + gameDuration);
        Bukkit.broadcastMessage(prefix + gameReward);
        
        // Mostrar pregunta
        String question = ChatColor.translateAlternateColorCodes('&', config.getString("messages.question", "").replace("{question}", currentGame.getQuestion()));
        Bukkit.broadcastMessage(prefix + question);
        
        // Programar avisos de tiempo
        scheduleTimeWarnings(duration);
        
        // Programar fin del juego
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                endGame(null);
            }
        }.runTaskLater(plugin, duration * 20L);
    }
    
    private void scheduleTimeWarnings(int duration) {
        // Aviso a los 30 segundos si la duración es mayor a 30
        if (duration > 30) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (currentGame != null) {
                        String prefix = ChatColor.translateAlternateColorCodes('&', config.getString("general.prefix", ""));
                        String timeLeft = ChatColor.translateAlternateColorCodes('&', config.getString("messages.time-left", "").replace("{time}", "30"));
                        Bukkit.broadcastMessage(prefix + timeLeft);
                    }
                }
            }.runTaskLater(plugin, (duration - 30) * 20L);
        }
        
        // Aviso a los 10 segundos
        if (duration > 10) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (currentGame != null) {
                        String prefix = ChatColor.translateAlternateColorCodes('&', config.getString("general.prefix", ""));
                        String timeLeft = ChatColor.translateAlternateColorCodes('&', config.getString("messages.time-left", "").replace("{time}", "10"));
                        Bukkit.broadcastMessage(prefix + timeLeft);
                    }
                }
            }.runTaskLater(plugin, (duration - 10) * 20L);
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (currentGame == null) {
            return;
        }
        
        Player player = event.getPlayer();
        String message = event.getMessage().trim();
        
        if (!player.hasPermission("hysercore.participate")) {
            return;
        }
        
        if (currentGame.checkAnswer(message)) {
            // Cancelar el evento para evitar que se muestre en el chat
            event.setCancelled(true);
            
            // Ejecutar en el hilo principal
            new BukkitRunnable() {
                @Override
                public void run() {
                    endGame(player);
                }
            }.runTask(plugin);
        }
    }
    
    private void endGame(Player winner) {
        if (currentGame == null) {
            return;
        }
        
        // Cancelar task del juego
        if (gameTask != null) {
            gameTask.cancel();
        }
        
        String prefix = ChatColor.translateAlternateColorCodes('&', config.getString("general.prefix", ""));
        
        if (winner != null) {
            // Hay ganador
            String winnerMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("messages.winner", "")
                    .replace("{player}", winner.getName())
                    .replace("{answer}", currentGame.getAnswer()));
            Bukkit.broadcastMessage(prefix + winnerMessage);
            
            // Dar recompensas
            giveRewards(winner, true);
            
            // Actualizar estadísticas
            playerStats.addWin(winner.getUniqueId());
        } else {
            // No hay ganador
            String noWinnerMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("messages.no-winner", "").replace("{answer}", currentGame.getAnswer()));
            Bukkit.broadcastMessage(prefix + noWinnerMessage);
        }
        
        String gameEnded = ChatColor.translateAlternateColorCodes('&', config.getString("messages.game-ended", ""));
        Bukkit.broadcastMessage(prefix + gameEnded);
        
        // Actualizar estadísticas para todos los jugadores que participaron
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("hysercore.participate")) {
                playerStats.addGame(player.getUniqueId());
                if (winner == null || !player.equals(winner)) {
                    giveRewards(player, false);
                }
            }
        }
        
        currentGame = null;
    }
    
    private void giveRewards(Player player, boolean isWinner) {
        String rewardType = isWinner ? "winner" : "participation";
        
        // Recompensa de dinero (requiere Vault)
        int money = config.getInt("rewards." + rewardType + ".money", 0);
        if (money > 0) {
            // Aquí se integraría con Vault para dar dinero
            String moneyReward = ChatColor.translateAlternateColorCodes('&', 
                config.getString("messages.money-reward", "").replace("{amount}", String.valueOf(money)));
            player.sendMessage(moneyReward);
        }
        
        // Recompensa de experiencia
        int experience = config.getInt("rewards." + rewardType + ".experience", 0);
        if (experience > 0) {
            player.giveExp(experience);
            String expReward = ChatColor.translateAlternateColorCodes('&', 
                config.getString("messages.exp-reward", "").replace("{amount}", String.valueOf(experience)));
            player.sendMessage(expReward);
        }
        
        // Comandos de recompensa
        List<String> commands = config.getStringList("rewards." + rewardType + ".commands");
        for (String command : commands) {
            command = command.replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
        
        String rewardGiven = ChatColor.translateAlternateColorCodes('&', config.getString("messages.reward-given", ""));
        player.sendMessage(rewardGiven);
    }
    
    public void forceStartGame(String type) {
        if (currentGame != null) {
            endGame(null);
        }
        
        currentGame = createGame(type);
        if (currentGame != null) {
            startGame();
        }
    }
    
    public void stopCurrentGame() {
        if (currentGame != null) {
            endGame(null);
        }
    }
    
    public boolean isGameActive() {
        return currentGame != null;
    }
    
    public PlayerStats getPlayerStats() {
        return playerStats;
    }
    
    public void shutdown() {
        if (gameTask != null) {
            gameTask.cancel();
        }
        if (intervalTask != null) {
            intervalTask.cancel();
        }
        if (currentGame != null) {
            endGame(null);
        }
    }
}