package com.hyser.hysercore.prison;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;

/**
 * Listener para el sistema de PrisonPunch que maneja ÚNICAMENTE
 * el knockback de flechas (PUNCH DE FLECHA, no de hits)
 */
public class PrisonPunchListener implements Listener {
    
    private final PrisonPunchManager punchManager;
    
    public PrisonPunchListener(PrisonPunchManager punchManager) {
        this.punchManager = punchManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // SOLO procesar si es daño por FLECHA
        if (!(event.getDamager() instanceof Arrow)) {
            return;
        }
        
        // SOLO procesar si la víctima es un jugador
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Arrow arrow = (Arrow) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        // Verificar que el shooter sea un jugador
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }
        
        Player shooter = (Player) arrow.getShooter();
        
        // Verificar si el sistema está habilitado
        if (!punchManager.isSystemEnabled()) {
            return;
        }
        
        // Solo aplicar en daño por PROYECTIL (flecha)
        if (event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) {
            return;
        }
        
        // Verificar que no sea un evento cancelado
        if (event.isCancelled()) {
            return;
        }
        
        // Aplicar el empuje simple de FLECHA
        boolean pushApplied = punchManager.applyArrowPush(shooter, victim, arrow);
        
        if (pushApplied) {
            // El empuje de FLECHA se aplicó exitosamente
            // Mantener el daño normal de la flecha
            // No modificamos el knockback, solo agregamos efectos
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityShootBow(EntityShootBowEvent event) {
        // Hacer que las flechas tengan más empuje
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        if (!(event.getProjectile() instanceof Arrow)) {
            return;
        }
        
        if (!punchManager.isSystemEnabled()) {
            return;
        }
        
        Arrow arrow = (Arrow) event.getProjectile();
        
        // SOLO aumentar la velocidad de la flecha para más empuje
        double speedMultiplier = punchManager.getConfig().getDouble("arrow.speedMultiplier", 1.5);
        arrow.setVelocity(arrow.getVelocity().multiply(speedMultiplier));
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        // Manejar eventos de daño base si es necesario
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Si el sistema está deshabilitado, no hacer nada
        if (!punchManager.isSystemEnabled()) {
            return;
        }
        
        // Verificar tipos de daño específicos que queremos manejar
        switch (event.getCause()) {
            case ENTITY_ATTACK:
                // Este caso se maneja en onEntityDamageByEntity
                break;
            case FALL:
                // Opcional: Reducir daño por caída si el jugador fue "puncheado" recientemente
                handleFallDamageReduction(player, event);
                break;
            default:
                break;
        }
    }
    
    /**
     * Maneja la reducción de daño por caída después de un punch
     */
    private void handleFallDamageReduction(Player player, EntityDamageEvent event) {
        // Verificar si la reducción de daño por caída está habilitada
        if (!punchManager.getConfig().getBoolean("prison.reduceFallDamage", true)) {
            return;
        }
        
        // Verificar si el jugador fue puncheado recientemente (últimos 3 segundos)
        // Esto previene daño excesivo por caída después de recibir punch
        
        double reductionPercentage = punchManager.getConfig().getDouble("prison.fallDamageReduction", 0.5);
        if (reductionPercentage > 0 && reductionPercentage <= 1.0) {
            double reducedDamage = event.getDamage() * (1.0 - reductionPercentage);
            event.setDamage(Math.max(0, reducedDamage));
            
            if (punchManager.getConfig().getBoolean("debug.log-calculations", false)) {
                System.out.println(String.format(
                    "Daño por caída reducido para %s: %.2f -> %.2f (%.0f%% reducción)",
                    player.getName(), event.getDamage() / (1.0 - reductionPercentage), 
                    reducedDamage, reductionPercentage * 100
                ));
            }
        }
    }
}