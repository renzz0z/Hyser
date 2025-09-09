package com.hyser.hysercore.abilities.actions;

import com.hyser.hysercore.abilities.AbilityAction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import org.bukkit.Sound;
import org.bukkit.Effect;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public class EnergyReleaseAction extends AbilityAction {
    private double radius;
    private double knockbackForce;
    private double damageAmount;
    private int requiredEnergy;
    private boolean affectPlayers;
    private boolean affectMobs;
    private boolean createEffects;
    private String releaseMessage;
    
    public EnergyReleaseAction(String type, ConfigurationSection config) {
        super(type, config);
        this.radius = config.getDouble("radius", 5.0);
        this.knockbackForce = config.getDouble("knockback_force", 2.5);
        this.damageAmount = config.getDouble("damage", 0.0);
        this.requiredEnergy = config.getInt("required_energy", 100);
        this.affectPlayers = config.getBoolean("affect_players", true);
        this.affectMobs = config.getBoolean("affect_mobs", true);
        this.createEffects = config.getBoolean("create_effects", true);
        this.releaseMessage = config.getString("release_message", "&c⚡ ¡EXPLOSIÓN DE ENERGÍA! ⚡");
    }
    
    @Override
    public void execute(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Verificar que el jugador tiene suficiente energía
        int currentEnergy = EnergyChargeAction.getEnergyLevel(playerId);
        if (currentEnergy < requiredEnergy) {
            player.sendMessage(ChatColor.RED + "⚡ Energía insuficiente: " + currentEnergy + "/" + requiredEnergy);
            return;
        }
        
        Location playerLoc = player.getLocation();
        
        // Mensaje de liberación
        String message = ChatColor.translateAlternateColorCodes('&', releaseMessage);
        player.sendMessage(message);
        
        // Efectos visuales y sonoros en el jugador
        if (createEffects) {
            player.playSound(playerLoc, Sound.EXPLODE, 2.0f, 0.8f);
            player.playSound(playerLoc, Sound.ENDERDRAGON_GROWL, 1.5f, 1.2f);
            
            // Efectos de partículas en el área
            for (int i = 0; i < 360; i += 30) {
                double x = Math.cos(Math.toRadians(i)) * radius;
                double z = Math.sin(Math.toRadians(i)) * radius;
                Location effectLoc = playerLoc.clone().add(x, 0.5, z);
                playerLoc.getWorld().playEffect(effectLoc, Effect.EXPLOSION_LARGE, 0);
            }
            
            // Efecto central
            playerLoc.getWorld().playEffect(playerLoc.add(0, 1, 0), Effect.EXPLOSION_HUGE, 0);
        }
        
        // Obtener entidades cercanas
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        int affectedCount = 0;
        
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }
            
            LivingEntity target = (LivingEntity) entity;
            
            // Filtrar por tipo
            if (target instanceof Player && !affectPlayers) {
                continue;
            }
            if (!(target instanceof Player) && !affectMobs) {
                continue;
            }
            
            // No afectar al usuario
            if (target.equals(player)) {
                continue;
            }
            
            // Calcular dirección de knockback (desde el jugador hacia la entidad)
            Vector direction = target.getLocation().toVector().subtract(playerLoc.toVector());
            direction.setY(0); // Ignorar diferencia de altura inicialmente
            direction = direction.normalize();
            
            // Aplicar knockback
            Vector knockback = direction.multiply(knockbackForce);
            knockback.setY(0.5); // Elevación ligera
            target.setVelocity(knockback);
            
            // Aplicar daño si está configurado
            if (damageAmount > 0) {
                target.damage(damageAmount, player);
            }
            
            affectedCount++;
            
            // Efectos individuales en cada objetivo
            if (createEffects && target instanceof Player) {
                Player targetPlayer = (Player) target;
                targetPlayer.sendMessage(ChatColor.RED + "⚡ ¡Fuiste empujado por una explosión de energía!");
                targetPlayer.playSound(targetPlayer.getLocation(), Sound.HURT_FLESH, 1.0f, 0.8f);
            }
        }
        
        // Mensaje de resultado
        if (affectedCount > 0) {
            player.sendMessage(ChatColor.YELLOW + "⚡ Empujaste " + affectedCount + " enemigos");
        } else {
            player.sendMessage(ChatColor.GRAY + "⚡ No hay enemigos en el área");
        }
        
        // Consumir energía
        EnergyChargeAction.consumeEnergy(playerId);
        player.sendMessage(ChatColor.GRAY + "⚡ Energía consumida completamente");
    }
}