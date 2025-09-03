package com.hyser.hysercore.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStats {
    
    private Map<UUID, Integer> gamesPlayed;
    private Map<UUID, Integer> gamesWon;
    private Map<UUID, Integer> totalRewards;
    
    public PlayerStats() {
        this.gamesPlayed = new HashMap<>();
        this.gamesWon = new HashMap<>();
        this.totalRewards = new HashMap<>();
    }
    
    public void addGame(UUID playerId) {
        gamesPlayed.put(playerId, gamesPlayed.getOrDefault(playerId, 0) + 1);
    }
    
    public void addWin(UUID playerId) {
        gamesWon.put(playerId, gamesWon.getOrDefault(playerId, 0) + 1);
    }
    
    public void addReward(UUID playerId, int amount) {
        totalRewards.put(playerId, totalRewards.getOrDefault(playerId, 0) + amount);
    }
    
    public int getGamesPlayed(UUID playerId) {
        return gamesPlayed.getOrDefault(playerId, 0);
    }
    
    public int getGamesWon(UUID playerId) {
        return gamesWon.getOrDefault(playerId, 0);
    }
    
    public int getTotalRewards(UUID playerId) {
        return totalRewards.getOrDefault(playerId, 0);
    }
    
    public double getWinRate(UUID playerId) {
        int played = getGamesPlayed(playerId);
        if (played == 0) {
            return 0.0;
        }
        return (double) getGamesWon(playerId) / played * 100.0;
    }
    
    public Map<UUID, Integer> getTopPlayers(int limit) {
        return gamesWon.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .limit(limit)
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                java.util.LinkedHashMap::new
            ));
    }
}