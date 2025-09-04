package com.hyser.hysercore.games;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.concurrent.ThreadLocalRandom;

public class MathGame extends ChatGame {
    
    private FileConfiguration config;
    
    public MathGame(FileConfiguration config) {
        super("Matemáticas");
        this.config = config;
        generateQuestion();
    }
    
    @Override
    public void generateQuestion() {
        String[] difficulties = {"easy", "medium", "hard"};
        String difficulty = difficulties[ThreadLocalRandom.current().nextInt(difficulties.length)];
        
        String[] operations = config.getStringList("game-types.math.difficulty." + difficulty + ".operations").toArray(new String[0]);
        int[] range = config.getIntegerList("game-types.math.difficulty." + difficulty + ".range").stream().mapToInt(i -> i).toArray();
        
        if (operations.length == 0) {
            operations = new String[]{"suma", "resta", "multiplicacion"};
        }
        if (range.length < 2) {
            range = new int[]{1, 50};
        }
        
        String operation = operations[ThreadLocalRandom.current().nextInt(operations.length)];
        
        int num1 = ThreadLocalRandom.current().nextInt(range[0], range[1] + 1);
        int num2 = ThreadLocalRandom.current().nextInt(range[0], range[1] + 1);
        int result;
        
        switch (operation.toLowerCase()) {
            case "suma":
                result = num1 + num2;
                question = "¿Cuánto es " + num1 + " + " + num2 + "?";
                break;
            case "resta":
                if (num1 < num2) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                }
                result = num1 - num2;
                question = "¿Cuánto es " + num1 + " - " + num2 + "?";
                break;
            case "multiplicacion":
                // Para multiplicación, usar números más pequeños
                num1 = ThreadLocalRandom.current().nextInt(2, Math.min(range[1], 15));
                num2 = ThreadLocalRandom.current().nextInt(2, Math.min(range[1], 15));
                result = num1 * num2;
                question = "¿Cuánto es " + num1 + " × " + num2 + "?";
                break;
            case "division":
                // Para división, asegurar que sea exacta
                num2 = ThreadLocalRandom.current().nextInt(2, Math.min(range[1], 10));
                result = ThreadLocalRandom.current().nextInt(2, Math.min(range[1], 20));
                num1 = num2 * result;
                question = "¿Cuánto es " + num1 + " ÷ " + num2 + "?";
                break;
            default:
                result = num1 + num2;
                question = "¿Cuánto es " + num1 + " + " + num2 + "?";
                break;
        }
        
        answer = String.valueOf(result);
        hints = new String[]{
            "Es un número " + (result % 2 == 0 ? "par" : "impar"),
            "Está entre " + Math.max(0, result - 10) + " y " + (result + 10),
            "La respuesta es " + result
        };
    }
}