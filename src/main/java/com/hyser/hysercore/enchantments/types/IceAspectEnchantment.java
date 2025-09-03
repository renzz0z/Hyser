package com.hyser.hysercore.enchantments.types;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class IceAspectEnchantment extends SwordEnchantment {
    
    private Plugin plugin;
    private FileConfiguration config;
    private List<Location> iceBlocks;
    
    public IceAspectEnchantment(FileConfiguration config, Plugin plugin) {
        super("ice_aspect", "Ice Aspect", "Efectos de hielo");
        this.config = config;
        this.plugin = plugin;
        this.iceBlocks = new ArrayList<>();
    }
    
    @Override
    public void onAttack(Player attacker, Player target) {
        double chance = config.getDouble("enchantments.ice-aspect.activation-chance", 0.15);
        
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }
        
        // Cooldown check
        if (isOnCooldown(attacker)) {
            return;
        }
        
        setCooldown(attacker, config.getInt("enchantments.ice-aspect.cooldown-seconds", 3));
        
        String mode = config.getString("enchantments.ice-aspect.mode", "both");
        
        // Aplicar efectos de estado
        if (mode.equals("effects") || mode.equals("both")) {
            applyStatusEffects(target);
        }
        
        // Crear jaula de hielo MEJORADA
        if (mode.equals("cage") || mode.equals("both")) {
            createImprovedIceCage(target);
        }
        
        // Sonido
        if (config.getBoolean("enchantments.ice-aspect.play-sound", true)) {
            String soundName = config.getString("enchantments.ice-aspect.sound", "GLASS");
            try {
                Sound sound = Sound.valueOf(soundName);
                target.getWorld().playSound(target.getLocation(), sound, 1.0f, 1.0f);
            } catch (Exception e) {
                // Sonido de respaldo para 1.8
                target.getWorld().playSound(target.getLocation(), Sound.GLASS, 1.0f, 1.0f);
            }
        }
        
        // Mensaje al atacante
        String message = config.getString("messages.ice-aspect-cage", "&b❄ &f{target} &aha sido encerrado en una celda de hielo!")
            .replace("{target}", target.getName());
        attacker.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
        
        // Mensaje al objetivo
        String targetMessage = config.getString("messages.ice-aspect-target", "&c❄ ¡Has sido afectado por Ice Aspect!");
        target.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', targetMessage));
    }
    
    private void applyStatusEffects(Player target) {
        List<String> effects = config.getStringList("enchantments.ice-aspect.status-effects");
        int duration = config.getInt("enchantments.ice-aspect.effect-duration-seconds", 5) * 20; // Convertir a ticks
        int amplifier = config.getInt("enchantments.ice-aspect.effect-amplifier", 1);
        
        for (String effectName : effects) {
            try {
                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                if (effectType != null) {
                    // Manejar efectos especiales para 1.8.8
                    if (effectName.equals("JUMP")) {
                        // En 1.8, JUMP reduce el salto con amplificador negativo
                        effectType = PotionEffectType.JUMP;
                        amplifier = -amplifier; // Amplificador negativo para reducir salto
                    }
                    
                    target.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error al aplicar efecto " + effectName + ": " + e.getMessage());
            }
        }
    }
    
    private void createImprovedIceCage(Player target) {
        Location center = target.getLocation().getBlock().getLocation();
        int radius = config.getInt("enchantments.ice-aspect.igloo-radius", 3);
        int height = config.getInt("enchantments.ice-aspect.igloo-height", 3);
        int duration = config.getInt("enchantments.ice-aspect.cage-duration-seconds", 6);
        
        // MEJORADO: Evitar colocar bloques en el piso
        boolean avoidFloor = config.getBoolean("enchantments.ice-aspect.avoid-floor-blocks", true);
        int minHeight = config.getInt("enchantments.ice-aspect.min-cage-height", 2);
        boolean createGaps = config.getBoolean("enchantments.ice-aspect.create-air-gaps", true);
        
        Material tempMaterial;
        try {
            tempMaterial = Material.valueOf(config.getString("enchantments.ice-aspect.ice-material", "PACKED_ICE"));
        } catch (Exception e) {
            tempMaterial = Material.ICE; // Respaldo para 1.8
        }
        final Material iceMaterial = tempMaterial;
        
        List<Location> blocksToPlace = new ArrayList<>();
        final List<Location> originalBlocks = new ArrayList<>();
        
        // Crear igloo tipo domo MEJORADO
        for (int x = -radius; x <= radius; x++) {
            for (int y = (avoidFloor ? 1 : 0); y <= height; y++) { // NUEVO: No colocar en Y=0 si está habilitado
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    
                    // Solo crear la cáscara del igloo, no rellenarlo
                    if (distance <= radius && distance >= radius - 1) {
                        Location blockLoc = center.clone().add(x, y, z);
                        Block block = blockLoc.getBlock();
                        
                        // MEJORADO: No reemplazar bloques sólidos importantes
                        if (block.getType() == Material.AIR || 
                            block.getType() == Material.WATER || 
                            block.getType() == Material.LAVA ||
                            block.getType().name().contains("LEAVES") ||
                            block.getType().name().contains("GRASS")) {
                            
                            // NUEVO: Crear huecos de aire para escapar
                            if (createGaps && y == minHeight && (x == 0 || z == 0) && ThreadLocalRandom.current().nextBoolean()) {
                                continue; // Crear hueco de escape
                            }
                            
                            originalBlocks.add(blockLoc.clone());
                            blocksToPlace.add(blockLoc.clone());
                        }
                    }
                }
            }
        }
        
        // Colocar bloques de hielo
        for (Location loc : blocksToPlace) {
            loc.getBlock().setType(iceMaterial);
            iceBlocks.add(loc);
        }
        
        // Programar destrucción automática
        new BukkitRunnable() {
            @Override
            public void run() {
                destroyIceCage(originalBlocks, iceMaterial);
            }
        }.runTaskLater(plugin, duration * 20L);
    }
    
    private void destroyIceCage(List<Location> blocks, final Material iceMaterial) {
        for (Location loc : blocks) {
            if (loc.getBlock().getType() == iceMaterial) {
                loc.getBlock().setType(Material.AIR);
                iceBlocks.remove(loc);
                
                // Sonido de destrucción
                if (config.getBoolean("enchantments.ice-aspect.play-destruction-sound", true)) {
                    try {
                        loc.getWorld().playSound(loc, Sound.GLASS, 0.5f, 2.0f);
                    } catch (Exception e) {
                        // Sonido de respaldo
                        loc.getWorld().playSound(loc, Sound.FIZZ, 0.5f, 1.0f);
                    }
                }
            }
        }
    }
    
    // Método para limpiar bloques de hielo al descargar el plugin
    public void cleanupIceBlocks() {
        for (Location loc : iceBlocks) {
            try {
                if (loc.getBlock().getType().name().contains("ICE")) {
                    loc.getBlock().setType(Material.AIR);
                }
            } catch (Exception e) {
                // Ignorar errores de bloques no cargados
            }
        }
        iceBlocks.clear();
    }
}