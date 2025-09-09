package com.hyser.hysercore.abilities.triggers;

import com.hyser.hysercore.abilities.AbilityTrigger;
import com.hyser.hysercore.utils.LangManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SequentialTrigger extends AbilityTrigger {
    private static Map<UUID, Boolean> abilityUsed = new HashMap<>();
    private static Map<UUID, Long> lastAbilityTime = new HashMap<>();
    
    private long sequenceTimeout; // Tiempo máximo para completar la secuencia
    private String sequenceType;
    
    public SequentialTrigger(String type, ConfigurationSection config) {
        super(type, config);
        this.sequenceTimeout = config.getLong("sequence_timeout", 5000); // 5 segundos por defecto
        this.sequenceType = type.toLowerCase();
    }
    
    @Override
    public boolean matches(Event event, Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // MODIFICADO: Ya no requiere objeto en mano para activar secuencia
        
        switch (sequenceType) {
            case "ability_then_double_shift":
                return handleAbilityThenDoubleShift(event, player, playerId, currentTime);
            default:
                return false;
        }
    }
    
    private boolean handleAbilityThenDoubleShift(Event event, Player player, UUID playerId, long currentTime) {
        // Verificar si el evento es un doble shift
        if (!(event instanceof PlayerToggleSneakEvent)) {
            return false;
        }
        
        PlayerToggleSneakEvent sneakEvent = (PlayerToggleSneakEvent) event;
        if (!sneakEvent.isSneaking()) {
            return false;
        }
        
        // Verificar si el jugador usó una ability recientemente
        if (!abilityUsed.containsKey(playerId)) {
            return false;
        }
        
        Long lastUseTime = lastAbilityTime.get(playerId);
        if (lastUseTime == null || (currentTime - lastUseTime) > sequenceTimeout) {
            // Tiempo expirado, limpiar datos y notificar
            abilityUsed.remove(playerId);
            lastAbilityTime.remove(playerId);
            player.sendMessage(getConfigMessage("sequential.sequence-timeout"));
            return false;
        }
        
        // Secuencia completada, limpiar datos
        abilityUsed.remove(playerId);
        lastAbilityTime.remove(playerId);
        
        return true;
    }
    
    private String getConfigMessage(String key) {
        LangManager langManager = LangManager.getInstance();
        if (langManager != null) {
            return langManager.getMessage(key);
        }
        // Fallback messages
        switch (key) {
            case "sequential.sequence-timeout": return org.bukkit.ChatColor.RED + "⚡ Secuencia expirada. Usa la habilidad de nuevo";
            default: return org.bukkit.ChatColor.GRAY + "Mensaje no encontrado";
        }
    }
    
    // Métodos estáticos para registrar uso de ability
    public static void registerAbilityUse(UUID playerId) {
        abilityUsed.put(playerId, true);
        lastAbilityTime.put(playerId, System.currentTimeMillis());
    }
    
    public static boolean isWaitingForSequence(UUID playerId) {
        return abilityUsed.containsKey(playerId);
    }
    
    public static void clearPlayerData(UUID playerId) {
        abilityUsed.remove(playerId);
        lastAbilityTime.remove(playerId);
    }
    
    public static long getRemainingTime(UUID playerId, long timeout) {
        Long lastUse = lastAbilityTime.get(playerId);
        if (lastUse == null) {
            return 0;
        }
        
        long elapsed = System.currentTimeMillis() - lastUse;
        return Math.max(0, timeout - elapsed);
    }
}