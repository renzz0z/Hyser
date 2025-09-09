package com.hyser.hysercore.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LangManager {
    private static LangManager instance;
    private final JavaPlugin plugin;
    private YamlConfiguration langConfig;
    private final Map<String, String> messageCache = new HashMap<>();
    
    private LangManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    public static LangManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new LangManager(plugin);
        }
        return instance;
    }
    
    public static LangManager getInstance() {
        return instance;
    }
    
    public void loadMessages() {
        File langFile = new File(plugin.getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        messageCache.clear();
        
        // Pre-cargar mensajes más comunes en caché
        cacheMessage("general.prefix");
        cacheMessage("energy.charge.start");
        cacheMessage("energy.release.explosion");
        cacheMessage("sequential.ability-ready");
        cacheMessage("sequential.already-charging");
    }
    
    private void cacheMessage(String key) {
        String message = langConfig.getString(key, "§7Mensaje no encontrado: " + key);
        messageCache.put(key, ChatColor.translateAlternateColorCodes('&', message));
    }
    
    public String getMessage(String key) {
        // Intentar obtener del caché primero
        String cached = messageCache.get(key);
        if (cached != null) {
            return cached;
        }
        
        // Si no está en caché, obtener de la configuración
        String message = langConfig.getString(key, "§7Mensaje no encontrado: " + key);
        String translated = ChatColor.translateAlternateColorCodes('&', message);
        
        // Agregar al caché para uso futuro
        messageCache.put(key, translated);
        
        return translated;
    }
    
    public String getMessage(String key, String... replacements) {
        String message = getMessage(key);
        
        // Aplicar reemplazos
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        return message;
    }
    
    public String getPrefix() {
        return getMessage("general.prefix");
    }
    
    public String getMessageWithPrefix(String key) {
        return getPrefix() + getMessage(key);
    }
    
    public String getMessageWithPrefix(String key, String... replacements) {
        return getPrefix() + getMessage(key, replacements);
    }
}