package com.hyser.hysercore.games;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WordGame extends ChatGame {
    
    private FileConfiguration config;
    
    public WordGame(FileConfiguration config) {
        super("Palabras");
        this.config = config;
        generateQuestion();
    }
    
    @Override
    public void generateQuestion() {
        List<String> words = config.getStringList("questions.words");
        if (words.isEmpty()) {
            words = getDefaultWords();
        }
        
        String originalWord = words.get(ThreadLocalRandom.current().nextInt(words.size()));
        answer = originalWord.toLowerCase();
        
        String[] gameTypes = {"scramble", "reverse", "riddle"};
        String gameType = gameTypes[ThreadLocalRandom.current().nextInt(gameTypes.length)];
        
        switch (gameType) {
            case "scramble":
                generateScrambleQuestion(originalWord);
                break;
            case "reverse":
                generateReverseQuestion(originalWord);
                break;
            case "riddle":
                generateRiddleQuestion(originalWord);
                break;
            default:
                generateScrambleQuestion(originalWord);
                break;
        }
    }
    
    private void generateScrambleQuestion(String word) {
        List<Character> letters = new ArrayList<>();
        for (char c : word.toCharArray()) {
            letters.add(c);
        }
        Collections.shuffle(letters);
        
        StringBuilder scrambled = new StringBuilder();
        for (char c : letters) {
            scrambled.append(c);
        }
        
        question = "Ordena las letras: " + scrambled.toString().toUpperCase();
        hints = new String[]{
            "Es una palabra de " + word.length() + " letras",
            "Empieza por " + Character.toString(word.charAt(0)).toUpperCase(),
            "La palabra es: " + word
        };
    }
    
    private void generateReverseQuestion(String word) {
        StringBuilder reversed = new StringBuilder(word);
        reversed.reverse();
        
        question = "¿Qué palabra es al revés? " + reversed.toString().toUpperCase();
        hints = new String[]{
            "Es una palabra de " + word.length() + " letras",
            "Termina en " + Character.toString(word.charAt(word.length() - 1)).toUpperCase(),
            "La palabra es: " + word
        };
    }
    
    private void generateRiddleQuestion(String word) {
        String riddle = generateRiddle(word.toLowerCase());
        question = riddle;
        hints = new String[]{
            "Es una palabra de " + word.length() + " letras",
            "Empieza por " + Character.toString(word.charAt(0)).toUpperCase(),
            "La palabra es: " + word
        };
    }
    
    private String generateRiddle(String word) {
        switch (word.toLowerCase()) {
            case "creeper":
                return "Soy verde y exploto, ¿quién soy?";
            case "enderdragon":
                return "Soy el jefe final del End, ¿quién soy?";
            case "diamante":
                return "Soy azul y muy valioso en las minas, ¿qué soy?";
            case "redstone":
                return "Soy rojo y conduzco electricidad en Minecraft, ¿qué soy?";
            case "nether":
                return "Soy una dimensión roja y caliente, ¿qué soy?";
            case "zombie":
                return "Camino lento y digo 'grrr', ¿quién soy?";
            case "skeleton":
                return "Disparo flechas y soy de huesos, ¿quién soy?";
            case "spider":
                return "Tengo ocho patas y trepo paredes, ¿quién soy?";
            case "agua":
                return "Soy líquida y azul, ¿qué soy?";
            case "fuego":
                return "Soy caliente y quemo, ¿qué soy?";
            case "montaña":
                return "Soy alta y rocosa, ¿qué soy?";
            case "océano":
                return "Soy grande y lleno de agua, ¿qué soy?";
            case "sol":
                return "Ilumino el día, ¿qué soy?";
            case "luna":
                return "Ilumino la noche, ¿qué soy?";
            case "estrella":
                return "Brillo en el cielo nocturno, ¿qué soy?";
            default:
                return "Adivina la palabra relacionada con: " + word.substring(0, 1).toUpperCase() + "_".repeat(word.length() - 1);
        }
    }
    
    private List<String> getDefaultWords() {
        List<String> defaultWords = new ArrayList<>();
        defaultWords.add("minecraft");
        defaultWords.add("creeper");
        defaultWords.add("diamante");
        defaultWords.add("redstone");
        defaultWords.add("nether");
        defaultWords.add("portal");
        defaultWords.add("zombie");
        defaultWords.add("skeleton");
        defaultWords.add("spider");
        defaultWords.add("agua");
        defaultWords.add("fuego");
        defaultWords.add("tierra");
        defaultWords.add("aire");
        return defaultWords;
    }
}