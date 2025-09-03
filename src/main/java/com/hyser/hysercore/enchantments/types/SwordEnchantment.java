package com.hyser.hysercore.enchantments.types;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class SwordEnchantment {
    
    protected String name;
    protected String displayName;
    protected String loreText;
    private Map<UUID, Long> cooldowns;
    
    public SwordEnchantment(String name, String displayName, String loreText) {
        this.name = name;
        this.displayName = displayName;
        this.loreText = loreText;
        this.cooldowns = new HashMap<>();
    }
    
    public abstract void onAttack(Player attacker, Player target);
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getLoreText() {
        return loreText;
    }
    
    protected boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        
        long cooldownEnd = cooldowns.get(playerId);
        return System.currentTimeMillis() < cooldownEnd;
    }
    
    protected void setCooldown(Player player, int seconds) {
        UUID playerId = player.getUniqueId();
        long cooldownEnd = System.currentTimeMillis() + (seconds * 1000L);
        cooldowns.put(playerId, cooldownEnd);
    }
    
    protected int getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long cooldownEnd = cooldowns.get(playerId);
        long remaining = cooldownEnd - System.currentTimeMillis();
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }
}