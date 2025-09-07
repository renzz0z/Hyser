package com.hyser.hysercore.abilities.actions;

import com.hyser.hysercore.abilities.AbilityAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public class TeleportAction extends AbilityAction {
    private String target;
    private double maxDistance;
    private boolean safe;
    
    public TeleportAction(String type, ConfigurationSection config) {
        super(type, config);
        this.target = config.getString("target", "LOOKING_AT");
        this.maxDistance = config.getDouble("max_distance", 10.0);
        this.safe = config.getBoolean("safe", true);
    }
    
    @Override
    public void execute(Player player) {
        if ("LOOKING_AT".equals(target)) {
            Location targetLoc = getTargetLocation(player);
            if (targetLoc != null && isValidTeleportLocation(targetLoc)) {
                player.teleport(targetLoc);
            }
        }
    }
    
    private Location getTargetLocation(Player player) {
        Block targetBlock = player.getTargetBlock((java.util.Set<Material>) null, (int) maxDistance);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            // Si no hay bloque, usar raycast
            Vector direction = player.getLocation().getDirection();
            Location start = player.getEyeLocation();
            Location target = start.clone().add(direction.multiply(maxDistance));
            
            // Buscar superficie sólida hacia abajo
            for (int y = target.getBlockY(); y > 0; y--) {
                target.setY(y);
                if (target.getBlock().getType() != Material.AIR) {
                    target.setY(y + 1); // Posicionar encima del bloque
                    return target;
                }
            }
            return null;
        }
        
        Location teleportLoc = targetBlock.getLocation().add(0, 1, 0);
        return teleportLoc;
    }
    
    private boolean isValidTeleportLocation(Location loc) {
        if (!safe) return true;
        
        // Verificar que haya espacio para el jugador (2 bloques de altura)
        Block block1 = loc.getBlock();
        Block block2 = loc.clone().add(0, 1, 0).getBlock();
        
        return block1.getType() == Material.AIR && block2.getType() == Material.AIR;
    }
}