package com.hyser.hysercore.games;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.concurrent.ThreadLocalRandom;

public class NumberGame extends ChatGame {
    
    private FileConfiguration config;
    private int targetNumber;
    
    public NumberGame(FileConfiguration config) {
        super("Adivinar Número");
        this.config = config;
        generateQuestion();
    }
    
    @Override
    public void generateQuestion() {
        int[] range = config.getIntegerList("game-types.number.range").stream().mapToInt(i -> i).toArray();
        if (range.length < 2) {
            range = new int[]{1, 1000};
        }
        
        targetNumber = ThreadLocalRandom.current().nextInt(range[0], range[1] + 1);
        answer = String.valueOf(targetNumber);
        
        question = "Adivina el número entre " + range[0] + " y " + range[1];
        
        // Generar pistas matemáticas
        String parity = targetNumber % 2 == 0 ? "par" : "impar";
        String range1 = "Está entre " + Math.max(range[0], targetNumber - 50) + " y " + Math.min(range[1], targetNumber + 50);
        String range2 = "Está entre " + Math.max(range[0], targetNumber - 20) + " y " + Math.min(range[1], targetNumber + 20);
        
        // Pistas adicionales basadas en propiedades del número
        String divisibility = "";
        if (targetNumber % 10 == 0) {
            divisibility = "Es múltiplo de 10";
        } else if (targetNumber % 5 == 0) {
            divisibility = "Es múltiplo de 5";
        } else if (targetNumber % 3 == 0) {
            divisibility = "Es múltiplo de 3";
        } else {
            divisibility = "No es múltiplo de 5";
        }
        
        hints = new String[]{
            "Es un número " + parity,
            divisibility,
            range1,
            range2,
            "El número es " + targetNumber
        };
    }
    
    @Override
    public boolean checkAnswer(String playerAnswer) {
        try {
            int playerNumber = Integer.parseInt(playerAnswer.trim());
            return playerNumber == targetNumber;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}