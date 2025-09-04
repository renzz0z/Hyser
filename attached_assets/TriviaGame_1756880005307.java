package com.hyser.hysercore.games;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class TriviaGame extends ChatGame {
    
    private FileConfiguration config;
    
    public TriviaGame(FileConfiguration config) {
        super("Trivia");
        this.config = config;
        generateQuestion();
    }
    
    @Override
    public void generateQuestion() {
        List<String> categories = config.getStringList("game-types.trivia.categories");
        if (categories.isEmpty()) {
            categories = List.of("general", "minecraft", "historia", "ciencia");
        }
        
        String category = categories.get(ThreadLocalRandom.current().nextInt(categories.size()));
        ConfigurationSection questionsSection = config.getConfigurationSection("questions.trivia." + category);
        
        if (questionsSection == null) {
            generateDefaultQuestion(category);
            return;
        }
        
        List<Map<?, ?>> questionsList = (List<Map<?, ?>>) config.getList("questions.trivia." + category);
        if (questionsList == null || questionsList.isEmpty()) {
            generateDefaultQuestion(category);
            return;
        }
        
        Map<?, ?> selectedQuestion = questionsList.get(ThreadLocalRandom.current().nextInt(questionsList.size()));
        
        question = (String) selectedQuestion.get("question");
        answer = (String) selectedQuestion.get("answer");
        
        List<String> hintsList = (List<String>) selectedQuestion.get("hints");
        if (hintsList != null) {
            hints = hintsList.toArray(new String[0]);
        } else {
            hints = new String[]{"Piensa bien", "Es de la categoría " + category, "La respuesta es: " + answer};
        }
    }
    
    private void generateDefaultQuestion(String category) {
        switch (category.toLowerCase()) {
            case "minecraft":
                generateMinecraftQuestion();
                break;
            case "general":
                generateGeneralQuestion();
                break;
            case "historia":
                generateHistoryQuestion();
                break;
            case "ciencia":
                generateScienceQuestion();
                break;
            default:
                generateGeneralQuestion();
                break;
        }
    }
    
    private void generateMinecraftQuestion() {
        String[][] questions = {
            {"¿Quién creó Minecraft?", "notch", "Su nombre real es Markus", "Fundó Mojang", "La respuesta es Notch"},
            {"¿Cuántos ojos de ender necesitas para el portal?", "12", "Es un número par", "Más de 10", "Son doce"},
            {"¿Qué mob explota?", "creeper", "Es verde", "Hace sss", "Es un Creeper"},
            {"¿Cuál es el bloque más resistente?", "bedrock", "No se puede romper", "Está en el fondo", "Es Bedrock"},
            {"¿En qué dimensión está el Wither?", "nether", "Es roja y caliente", "Hay lava", "Es el Nether"}
        };
        
        String[] selected = questions[ThreadLocalRandom.current().nextInt(questions.length)];
        question = selected[0];
        answer = selected[1];
        hints = new String[]{selected[2], selected[3], selected[4]};
    }
    
    private void generateGeneralQuestion() {
        String[][] questions = {
            {"¿Cuál es la capital de España?", "madrid", "Es europea", "Empieza por M", "Es Madrid"},
            {"¿Cuántos continentes hay?", "7", "Es impar", "Menos de 10", "Son siete"},
            {"¿Cuál es el planeta más grande?", "jupiter", "Es gaseoso", "Empieza por J", "Es Júpiter"},
            {"¿Cuál es el océano más grande?", "pacifico", "Entre Asia y América", "Significa pacífico", "Es el Pacífico"},
            {"¿Cuántos días tiene un año?", "365", "Más de 300", "Menos de 400", "Son 365"}
        };
        
        String[] selected = questions[ThreadLocalRandom.current().nextInt(questions.length)];
        question = selected[0];
        answer = selected[1];
        hints = new String[]{selected[2], selected[3], selected[4]};
    }
    
    private void generateHistoryQuestion() {
        String[][] questions = {
            {"¿En qué año comenzó la Segunda Guerra Mundial?", "1939", "Siglo XX", "Década del 30", "Fue en 1939"},
            {"¿Quién fue el primer emperador romano?", "augusto", "Era Octavio", "Sobrino de César", "Fue Augusto"},
            {"¿En qué año descubrió América Colón?", "1492", "Siglo XV", "Década del 90", "Fue en 1492"},
            {"¿Quién pintó la Mona Lisa?", "leonardo", "Fue italiano", "Del Renacimiento", "Leonardo da Vinci"},
            {"¿En qué año cayó el Muro de Berlín?", "1989", "Siglo XX", "Década del 80", "Fue en 1989"}
        };
        
        String[] selected = questions[ThreadLocalRandom.current().nextInt(questions.length)];
        question = selected[0];
        answer = selected[1];
        hints = new String[]{selected[2], selected[3], selected[4]};
    }
    
    private void generateScienceQuestion() {
        String[][] questions = {
            {"¿Cuál es el elemento más abundante en el universo?", "hidrogeno", "Es el más ligero", "Su símbolo es H", "Es hidrógeno"},
            {"¿Cuántos huesos tiene el cuerpo humano?", "206", "Más de 200", "Menos de 210", "Son 206"},
            {"¿Cuál es la velocidad de la luz?", "300000000", "300 millones", "Metros por segundo", "300.000.000 m/s"},
            {"¿Cuál es el gas más abundante en la atmósfera?", "nitrogeno", "No es oxígeno", "Empieza por N", "Es nitrógeno"},
            {"¿Cuántos cromosomas tiene el ser humano?", "46", "Es par", "Más de 40", "Son 46"}
        };
        
        String[] selected = questions[ThreadLocalRandom.current().nextInt(questions.length)];
        question = selected[0];
        answer = selected[1];
        hints = new String[]{selected[2], selected[3], selected[4]};
    }
}