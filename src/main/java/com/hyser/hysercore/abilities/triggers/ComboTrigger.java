package com.hyser.hysercore.abilities.triggers;

import com.hyser.hysercore.abilities.AbilityTrigger;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.configuration.ConfigurationSection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class ComboTrigger extends AbilityTrigger {
    private static final Map<UUID, Integer> hitCountGiven = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> hitCountReceived = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastHitTime = new ConcurrentHashMap<>();
    
    private String comboType; // "give" o "receive"
    private int requiredHits;
    private long resetTime; // Tiempo en ms para resetear combo si no hay hits
    
    public ComboTrigger(String type, ConfigurationSection config) {
        super(type, config);
        this.comboType = type.toLowerCase().replace("combo_", "");
        this.requiredHits = config.getInt("required_hits", 3);
        this.resetTime = config.getLong("reset_time", 5000); // 5 segundos por defecto
    }
    
    @Override
    public boolean matches(Event event, Player player) {
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return false;
        }
        
        EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
        Entity damager = damageEvent.getDamager();
        Entity victim = damageEvent.getEntity();
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Verificar si el combo se resetea por tiempo
        if (shouldResetCombo(playerId, currentTime)) {
            resetCombo(playerId);
        }
        
        boolean triggerActivated = false;
        
        switch (comboType) {
            case "give":
                // Cuando el jugador pega a otro
                if (damager instanceof Player && ((Player) damager).getUniqueId().equals(playerId)) {
                    int currentHits = hitCountGiven.getOrDefault(playerId, 0) + 1;
                    hitCountGiven.put(playerId, currentHits);
                    lastHitTime.put(playerId, currentTime);
                    
                    if (currentHits >= requiredHits) {
                        triggerActivated = true;
                        resetCombo(playerId); // Resetear después de activar
                    }
                }
                break;
                
            case "receive":
                // Cuando le pegan al jugador
                if (victim instanceof Player && ((Player) victim).getUniqueId().equals(playerId)) {
                    int currentHits = hitCountReceived.getOrDefault(playerId, 0) + 1;
                    hitCountReceived.put(playerId, currentHits);
                    lastHitTime.put(playerId, currentTime);
                    
                    if (currentHits >= requiredHits) {
                        triggerActivated = true;
                        resetCombo(playerId); // Resetear después de activar
                    }
                }
                break;
        }
        
        return triggerActivated;
    }
    
    private boolean shouldResetCombo(UUID playerId, long currentTime) {
        Long lastHit = lastHitTime.get(playerId);
        return lastHit != null && (currentTime - lastHit) > resetTime;
    }
    
    private void resetCombo(UUID playerId) {
        hitCountGiven.remove(playerId);
        hitCountReceived.remove(playerId);
        lastHitTime.remove(playerId);
    }
    
    // Métodos públicos para obtener estado de combo (para mostrar en lore)
    public static int getHitsGiven(UUID playerId) {
        return hitCountGiven.getOrDefault(playerId, 0);
    }
    
    public static int getHitsReceived(UUID playerId) {
        return hitCountReceived.getOrDefault(playerId, 0);
    }
    
    public static void clearPlayerData(UUID playerId) {
        hitCountGiven.remove(playerId);
        hitCountReceived.remove(playerId);
        lastHitTime.remove(playerId);
    }
    
    // Getters para configuración
    public String getComboType() {
        return comboType;
    }
    
    public int getRequiredHits() {
        return requiredHits;
    }
}