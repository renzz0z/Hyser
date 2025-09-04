package com.hyser.hysercore.listeners;

import com.hyser.hysercore.managers.ChatGameManager;
import org.bukkit.event.Listener;

/**
 * Listener dedicado para ChatGames
 * En este caso, la funcionalidad de listener está incluida en ChatGameManager,
 * pero esta clase puede usarse para listeners adicionales en el futuro.
 */
public class ChatGameListener implements Listener {
    
    private ChatGameManager gameManager;
    
    public ChatGameListener(ChatGameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    // Aquí se pueden agregar listeners adicionales si es necesario
    // Por ejemplo: eventos de join/quit de jugadores, eventos de mundo, etc.
}