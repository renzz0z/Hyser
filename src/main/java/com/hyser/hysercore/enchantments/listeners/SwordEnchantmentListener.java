package com.hyser.hysercore.enchantments.listeners;

import com.hyser.hysercore.enchantments.managers.SwordEnchantmentManager;
import com.hyser.hysercore.enchantments.types.SwordEnchantment;
import com.hyser.hysercore.enchantments.types.DashEnchantment;
import com.hyser.hysercore.enchantments.types.SharpnessEnchantment;
import com.hyser.hysercore.enchantments.types.ExecutionerEnchantment;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SwordEnchantmentListener implements Listener {
    
    private SwordEnchantmentManager enchantmentManager;
    
    public SwordEnchantmentListener(SwordEnchantmentManager enchantmentManager) {
        this.enchantmentManager = enchantmentManager;
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        Player target = (Player) event.getEntity();
        ItemStack weapon = attacker.getItemInHand();
        
        List<SwordEnchantment> enchantments = enchantmentManager.getEnchantsOnSword(weapon);
        if (enchantments.isEmpty()) {
            return;
        }
        
        // Aplicar efectos de encantamientos
        for (SwordEnchantment enchant : enchantments) {
            enchant.onAttack(attacker, target);
            
            // Manejar encantamientos especiales que modifican daño
            if (enchant instanceof SharpnessEnchantment) {
                SharpnessEnchantment sharpness = (SharpnessEnchantment) enchant;
                double bonus = sharpness.getBonusDamage();
                event.setDamage(event.getDamage() + bonus);
            } else if (enchant instanceof ExecutionerEnchantment) {
                ExecutionerEnchantment executioner = (ExecutionerEnchantment) enchant;
                if (executioner.shouldActivate(target)) {
                    double multiplier = executioner.getBonusMultiplier();
                    event.setDamage(event.getDamage() * multiplier);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        
        List<SwordEnchantment> enchantments = enchantmentManager.getEnchantsOnSword(item);
        
        for (SwordEnchantment enchant : enchantments) {
            if (enchant instanceof DashEnchantment && 
                (event.getAction().name().contains("RIGHT_CLICK"))) {
                DashEnchantment dash = (DashEnchantment) enchant;
                dash.activateDash(player);
                event.setCancelled(true);
                break;
            }
        }
    }
}