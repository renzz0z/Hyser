package com.hyser.hysercore.abilities.triggers;

import com.hyser.hysercore.abilities.AbilityTrigger;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatTrigger extends AbilityTrigger {
    private static Map<UUID, Map<UUID, Integer>> hitCounter = new HashMap<>();
    private static Map<UUID, Map<UUID, Integer>> damageGiven = new HashMap<>();
    private static Map<UUID, Map<UUID, Integer>> damageReceived = new HashMap<>();
    
    private String triggerMode;
    private int requiredHits;
    private double requiredDamage;
    private String targetType; // "any", "player", "mob"
    
    public CombatTrigger(String type, ConfigurationSection config) {
        super(type, config);
        this.triggerMode = type.toLowerCase();
        this.requiredHits = config.getInt("required_hits", 1);
        this.requiredDamage = config.getDouble("required_damage", 0.0);
        this.targetType = config.getString("target_type", "any");
    }
    
    @Override
    public boolean matches(Event event, Player player) {
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return false;
        }
        
        EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
        Entity damager = damageEvent.getDamager();
        Entity victim = damageEvent.getEntity();
        double damage = damageEvent.getDamage();
        
        UUID playerId = player.getUniqueId();
        
        switch (triggerMode) {
            case "on_hit": // Cuando el jugador pega a otro
                if (damager instanceof Player && ((Player) damager).getUniqueId().equals(playerId)) {
                    return checkHitTarget(playerId, victim, damage);
                }
                break;
                
            case "on_receive_hit": // Cuando le pegan al jugador
                if (victim instanceof Player && ((Player) victim).getUniqueId().equals(playerId)) {
                    return checkReceiveHit(playerId, damager, damage);
                }
                break;
                
            case "hit_count": // Cuando alcanza X golpes dados
                if (damager instanceof Player && ((Player) damager).getUniqueId().equals(playerId)) {
                    return checkHitCount(playerId, victim, damage);
                }
                break;
                
            case "receive_count": // Cuando recibe X golpes
                if (victim instanceof Player && ((Player) victim).getUniqueId().equals(playerId)) {
                    return checkReceiveCount(playerId, damager, damage);
                }
                break;
        }
        
        return false;
    }
    
    private boolean checkHitTarget(UUID playerId, Entity target, double damage) {
        if (!isValidTarget(target)) return false;
        
        // Actualizar estadísticas
        updateHitStats(playerId, target, damage);
        
        return true;
    }
    
    private boolean checkReceiveHit(UUID playerId, Entity attacker, double damage) {
        if (!isValidTarget(attacker)) return false;
        
        // Actualizar estadísticas de daño recibido
        updateReceiveStats(playerId, attacker, damage);
        
        return true;
    }
    
    private boolean checkHitCount(UUID playerId, Entity target, double damage) {
        if (!isValidTarget(target)) return false;
        
        updateHitStats(playerId, target, damage);
        
        UUID targetId = target.getUniqueId();
        int hits = hitCounter.computeIfAbsent(playerId, k -> new HashMap<>())
                            .getOrDefault(targetId, 0);
        
        return hits >= requiredHits;
    }
    
    private boolean checkReceiveCount(UUID playerId, Entity attacker, double damage) {
        if (!isValidTarget(attacker)) return false;
        
        updateReceiveStats(playerId, attacker, damage);
        
        UUID attackerId = attacker.getUniqueId();
        int hits = hitCounter.computeIfAbsent(attackerId, k -> new HashMap<>())
                            .getOrDefault(playerId, 0);
        
        return hits >= requiredHits;
    }
    
    private void updateHitStats(UUID playerId, Entity target, double damage) {
        UUID targetId = target.getUniqueId();
        
        // Actualizar contador de golpes
        hitCounter.computeIfAbsent(playerId, k -> new HashMap<>())
                  .put(targetId, hitCounter.get(playerId).getOrDefault(targetId, 0) + 1);
        
        // Actualizar daño dado
        damageGiven.computeIfAbsent(playerId, k -> new HashMap<>())
                   .put(targetId, (int) (damageGiven.get(playerId).getOrDefault(targetId, 0) + damage));
    }
    
    private void updateReceiveStats(UUID playerId, Entity attacker, double damage) {
        UUID attackerId = attacker.getUniqueId();
        
        // Actualizar daño recibido
        damageReceived.computeIfAbsent(playerId, k -> new HashMap<>())
                      .put(attackerId, (int) (damageReceived.get(playerId).getOrDefault(attackerId, 0) + damage));
    }
    
    private boolean isValidTarget(Entity entity) {
        switch (targetType.toLowerCase()) {
            case "player":
                return entity instanceof Player;
            case "mob":
                return !(entity instanceof Player);
            case "any":
            default:
                return true;
        }
    }
    
    // Métodos estáticos para obtener estadísticas
    public static int getHitsGiven(UUID playerId, UUID targetId) {
        return hitCounter.getOrDefault(playerId, new HashMap<>()).getOrDefault(targetId, 0);
    }
    
    public static int getDamageGiven(UUID playerId, UUID targetId) {
        return damageGiven.getOrDefault(playerId, new HashMap<>()).getOrDefault(targetId, 0);
    }
    
    public static int getDamageReceived(UUID playerId, UUID attackerId) {
        return damageReceived.getOrDefault(playerId, new HashMap<>()).getOrDefault(attackerId, 0);
    }
    
    public static void resetStats(UUID playerId) {
        hitCounter.remove(playerId);
        damageGiven.remove(playerId);
        damageReceived.remove(playerId);
    }
}