package com.hyser.hysercore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.hyser.hysercore.managers.ChatGameManager;
import com.hyser.hysercore.commands.ChatGamesCommand;
import com.hyser.hysercore.utils.PlayerStats;
import com.hyser.hysercore.enchantments.managers.SwordEnchantmentManager;
import com.hyser.hysercore.enchantments.commands.SwordEnchantCommand;
import com.hyser.hysercore.enchantments.commands.EnchantBookCommand;
import com.hyser.hysercore.enchantments.listeners.SwordEnchantmentListener;
import com.hyser.hysercore.enchantments.listeners.AntiAutoArmorListener;
import com.hyser.hysercore.enchantments.listeners.EnchantmentBookListener;
import com.hyser.hysercore.waypoints.LunarWaypoints;
import com.hyser.hysercore.waypoints.WaypointsCommand;
import com.hyser.hysercore.teamviewer.LunarTeamViewer;
import com.hyser.hysercore.teamviewer.TeamViewerCommand;
import com.hyser.hysercore.prison.PrisonPunchManager;
import com.hyser.hysercore.prison.PrisonPunchListener;
import com.hyser.hysercore.prison.PrisonPunchCommand;

import java.io.File;
import java.io.IOException;

public class HyserCore extends JavaPlugin implements Listener {

    private ChatGameManager gameManager;
    private ChatGamesCommand chatGamesCommand;
    private FileConfiguration chatGamesConfig;
    private File chatGamesConfigFile;
    private SwordEnchantmentManager enchantmentManager;
    private LunarWaypoints waypointManager;
    private LunarTeamViewer teamViewerManager;
    private PrisonPunchManager punchManager;

    @Override
    public void onEnable() {
        // Registrar eventos
        getServer().getPluginManager().registerEvents(this, this);
        
        // Crear directorio de configuración si no existe
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        // Cargar configuraciones
        saveDefaultConfig();
        loadChatGamesConfig();
        
        // Inicializar sistema de ChatGames
        gameManager = new ChatGameManager(this);
        getServer().getPluginManager().registerEvents(gameManager, this);
        
        // Inicializar sistema de Sword Enchantments MEJORADO
        enchantmentManager = new SwordEnchantmentManager(this);
        getServer().getPluginManager().registerEvents(new SwordEnchantmentListener(enchantmentManager), this);
        getServer().getPluginManager().registerEvents(new AntiAutoArmorListener(enchantmentManager.getConfig()), this);
        getServer().getPluginManager().registerEvents(new EnchantmentBookListener(enchantmentManager), this);
        
        // Inicializar sistema de LunarWaypoints MEJORADO
        waypointManager = new LunarWaypoints(this);
        
        // Inicializar sistema de LunarTeamViewer NUEVO
        teamViewerManager = new LunarTeamViewer(this);
        
        // Inicializar sistema de PrisonPunch NUEVO
        punchManager = new PrisonPunchManager(this);
        getServer().getPluginManager().registerEvents(new PrisonPunchListener(punchManager), this);
        
        // Registrar comandos
        chatGamesCommand = new ChatGamesCommand(this, gameManager);
        getCommand("chatgames").setExecutor(chatGamesCommand);
        getCommand("chatgames").setTabCompleter(chatGamesCommand);
        
        SwordEnchantCommand enchantCommand = new SwordEnchantCommand(this, enchantmentManager);
        getCommand("swordenchant").setExecutor(enchantCommand);
        getCommand("swordenchant").setTabCompleter(enchantCommand);
        
        EnchantBookCommand bookCommand = new EnchantBookCommand(enchantmentManager);
        getCommand("enchantbook").setExecutor(bookCommand);
        getCommand("enchantbook").setTabCompleter(bookCommand);
        
        WaypointsCommand waypointsCommand = new WaypointsCommand(this, waypointManager);
        getCommand("waypoints").setExecutor(waypointsCommand);
        getCommand("waypoints").setTabCompleter(waypointsCommand);
        
        TeamViewerCommand teamViewerCommand = new TeamViewerCommand(this, teamViewerManager);
        getCommand("teamviewer").setExecutor(teamViewerCommand);
        getCommand("teamviewer").setTabCompleter(teamViewerCommand);
        
        PrisonPunchCommand punchCommand = new PrisonPunchCommand(this, punchManager);
        getCommand("prisonpunch").setExecutor(punchCommand);
        getCommand("prisonpunch").setTabCompleter(punchCommand);
        
        // Mensaje de inicio
        getLogger().info("=================================");
        getLogger().info("     HyserCore v1.0.0 MEJORADO");
        getLogger().info("=================================");
        getLogger().info("Plugin habilitado correctamente!");
        getLogger().info("Sistema de ChatGames cargado.");
        getLogger().info("Sistema de Sword Enchantments cargado.");
        getLogger().info("Sistema de LunarWaypoints cargado.");
        getLogger().info("Sistema de LunarTeamViewer cargado.");
        getLogger().info("Sistema de PrisonPunch cargado (config AzuriteSpigot).");
        
        boolean enabled = chatGamesConfig.getBoolean("general.enabled", true);
        getLogger().info("ChatGames: " + (enabled ? "HABILITADO" : "DESHABILITADO"));
        
        boolean punchEnabled = punchManager.isSystemEnabled();
        getLogger().info("PrisonPunch: " + (punchEnabled ? "HABILITADO" : "DESHABILITADO"));
        if (punchEnabled) {
            getLogger().info("Knockback config: Horizontal=" + 
                punchManager.getConfig().getDouble("knockback.simple.horizontal", 0.425) +
                ", Vertical=" + punchManager.getConfig().getDouble("knockback.simple.vertical", 0.5) +
                ", Boost Mode=" + punchManager.getConfig().getBoolean("prison.boostMode", true));
        }
        
        if (enabled) {
            int minPlayers = chatGamesConfig.getInt("general.min-players", 3);
            int minInterval = chatGamesConfig.getInt("general.interval.min", 300);
            int maxInterval = chatGamesConfig.getInt("general.interval.max", 600);
            getLogger().info("Intervalo de juegos: " + minInterval + "-" + maxInterval + " segundos");
            getLogger().info("Jugadores mínimos: " + minPlayers);
        }
        
        getLogger().info("Tipos de juegos disponibles:");
        if (chatGamesConfig.getBoolean("game-types.math.enabled", true)) {
            getLogger().info("- Matemáticas");
        }
        if (chatGamesConfig.getBoolean("game-types.word.enabled", true)) {
            getLogger().info("- Palabras");
        }
        if (chatGamesConfig.getBoolean("game-types.trivia.enabled", true)) {
            getLogger().info("- Trivia");
        }
        if (chatGamesConfig.getBoolean("game-types.complete.enabled", true)) {
            getLogger().info("- Completar frases");
        }
        if (chatGamesConfig.getBoolean("game-types.number.enabled", true)) {
            getLogger().info("- Adivinar números");
        }
        
        getLogger().info("=================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("Deshabilitando HyserCore...");
        
        if (gameManager != null) {
            gameManager.shutdown();
        }
        
        if (punchManager != null) {
            punchManager.shutdown();
        }
        
        // Guardar configuraciones
        saveChatGamesConfig();
        
        getLogger().info("HyserCore deshabilitado correctamente!");
    }
    
    private void loadChatGamesConfig() {
        chatGamesConfigFile = new File(getDataFolder(), "chatgames.yml");
        if (!chatGamesConfigFile.exists()) {
            saveResource("chatgames.yml", false);
        }
        chatGamesConfig = YamlConfiguration.loadConfiguration(chatGamesConfigFile);
        
        // Verificar integridad de la configuración
        validateConfig();
    }
    
    private void validateConfig() {
        boolean needsSave = false;
        
        // Verificar configuración general
        if (!chatGamesConfig.contains("general.enabled")) {
            chatGamesConfig.set("general.enabled", true);
            needsSave = true;
        }
        
        if (!chatGamesConfig.contains("general.prefix")) {
            chatGamesConfig.set("general.prefix", "&7[&6ChatGames&7] &r");
            needsSave = true;
        }
        
        if (!chatGamesConfig.contains("general.interval.min")) {
            chatGamesConfig.set("general.interval.min", 300);
            needsSave = true;
        }
        
        if (!chatGamesConfig.contains("general.interval.max")) {
            chatGamesConfig.set("general.interval.max", 600);
            needsSave = true;
        }
        
        if (!chatGamesConfig.contains("general.duration")) {
            chatGamesConfig.set("general.duration", 60);
            needsSave = true;
        }
        
        if (!chatGamesConfig.contains("general.min-players")) {
            chatGamesConfig.set("general.min-players", 3);
            needsSave = true;
        }
        
        // Verificar tipos de juegos
        if (!chatGamesConfig.contains("game-types.math.enabled")) {
            chatGamesConfig.set("game-types.math.enabled", true);
            needsSave = true;
        }
        
        if (!chatGamesConfig.contains("game-types.word.enabled")) {
            chatGamesConfig.set("game-types.word.enabled", true);
            needsSave = true;
        }
        
        if (!chatGamesConfig.contains("game-types.trivia.enabled")) {
            chatGamesConfig.set("game-types.trivia.enabled", true);
            needsSave = true;
        }
        
        if (!chatGamesConfig.contains("game-types.complete.enabled")) {
            chatGamesConfig.set("game-types.complete.enabled", true);
            needsSave = true;
        }
        
        if (!chatGamesConfig.contains("game-types.number.enabled")) {
            chatGamesConfig.set("game-types.number.enabled", true);
            needsSave = true;
        }
        
        if (needsSave) {
            saveChatGamesConfig();
            getLogger().info("Configuración de ChatGames actualizada con valores por defecto.");
        }
    }
    
    public void saveChatGamesConfig() {
        try {
            chatGamesConfig.save(chatGamesConfigFile);
        } catch (IOException e) {
            getLogger().severe("No se pudo guardar chatgames.yml: " + e.getMessage());
        }
    }
    
    public FileConfiguration getChatGamesConfig() {
        return chatGamesConfig;
    }
    
    public void reloadChatGamesConfig() {
        chatGamesConfig = YamlConfiguration.loadConfiguration(chatGamesConfigFile);
        validateConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Verificar si los mensajes de bienvenida están habilitados
        if (!getConfig().getBoolean("settings.show-welcome-message", true)) {
            return;
        }
        
        String prefix = ChatColor.translateAlternateColorCodes('&', 
            chatGamesConfig.getString("general.prefix", "&7[&6ChatGames&7] &r"));
        
        // Enviar mensaje de bienvenida con información sobre ChatGames
        player.sendMessage(prefix + ChatColor.GOLD + "¡Bienvenido al servidor!");
        
        if (chatGamesConfig.getBoolean("general.enabled", true)) {
            player.sendMessage(prefix + ChatColor.AQUA + "¡Los ChatGames están activos! Participa y gana recompensas.");
            
            // Mostrar estadísticas del jugador si existen
            if (gameManager != null) {
                PlayerStats stats = gameManager.getPlayerStats();
                int gamesPlayed = stats.getGamesPlayed(player.getUniqueId());
                if (gamesPlayed > 0) {
                    int gamesWon = stats.getGamesWon(player.getUniqueId());
                    double winRate = stats.getWinRate(player.getUniqueId());
                    player.sendMessage(prefix + ChatColor.YELLOW + "Tus estadísticas: " + 
                        ChatColor.WHITE + gamesPlayed + " jugados, " + 
                        ChatColor.GREEN + gamesWon + " ganados " +
                        ChatColor.GRAY + "(" + String.format("%.1f", winRate) + "%)");
                }
            }
        }
        
        // Log para el servidor
        getLogger().info("Jugador " + player.getName() + " se ha conectado al servidor");
    }
    
    // Comando de información adicional (opcional)
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Este método puede manejar comandos adicionales si es necesario
        // Por ahora, todos los comandos principales son manejados por ChatGamesCommand
        return false;
    }
    
    // Métodos de utilidad para otros plugins o expansiones futuras
    public ChatGameManager getGameManager() {
        return gameManager;
    }
    
    public SwordEnchantmentManager getEnchantmentManager() {
        return enchantmentManager;
    }
    
    public LunarWaypoints getWaypointManager() {
        return waypointManager;
    }
    
    public LunarTeamViewer getTeamViewerManager() {
        return teamViewerManager;
    }
    
    public PrisonPunchManager getPunchManager() {
        return punchManager;
    }
    
    public boolean isGameActive() {
        return gameManager != null && gameManager.isGameActive();
    }
    
    public void forceStartGame(String gameType) {
        if (gameManager != null) {
            gameManager.forceStartGame(gameType);
        }
    }
    
    public void stopCurrentGame() {
        if (gameManager != null) {
            gameManager.stopCurrentGame();
        }
    }
    
    // Método para obtener estadísticas de un jugador (API para otros plugins)
    public PlayerStats getPlayerStats() {
        return gameManager != null ? gameManager.getPlayerStats() : null;
    }
    
    // Método para comprobar si los ChatGames están habilitados
    public boolean isChatGamesEnabled() {
        return chatGamesConfig.getBoolean("general.enabled", true);
    }
    
    // Método para habilitar/deshabilitar ChatGames programáticamente
    public void setChatGamesEnabled(boolean enabled) {
        chatGamesConfig.set("general.enabled", enabled);
        saveChatGamesConfig();
        if (gameManager != null) {
            gameManager.reloadConfig();
        }
    }
}