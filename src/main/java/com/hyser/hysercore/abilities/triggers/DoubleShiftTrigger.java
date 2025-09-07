package com.hyser.hysercore.abilities.triggers;

import com.hyser.hysercore.abilities.AbilityTrigger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DoubleShiftTrigger extends AbilityTrigger {
    private static Map<UUID, Long> lastShiftTime = new HashMap<>();
    private long maxInterval;
    
    public DoubleShiftTrigger(String type, ConfigurationSection config) {
        super(type, config);
        this.maxInterval = config.getLong(\"within\", 500); // 500ms por defecto\n    }\n    \n    @Override\n    public boolean matches(Event event, Player player) {\n        if (!(event instanceof PlayerToggleSneakEvent)) {\n            return false;\n        }\n        \n        PlayerToggleSneakEvent sneakEvent = (PlayerToggleSneakEvent) event;\n        if (!sneakEvent.isSneaking()) {\n            return false; // Solo cuando empieza a hacer shift\n        }\n        \n        UUID playerId = player.getUniqueId();\n        long currentTime = System.currentTimeMillis();\n        \n        if (lastShiftTime.containsKey(playerId)) {\n            long timeDiff = currentTime - lastShiftTime.get(playerId);\n            if (timeDiff <= maxInterval) {\n                lastShiftTime.remove(playerId); // Resetear para evitar triple shift\n                return true;\n            }\n        }\n        \n        lastShiftTime.put(playerId, currentTime);\n        return false;\n    }\n}"