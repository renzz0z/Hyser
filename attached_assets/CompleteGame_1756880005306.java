package com.hyser.hysercore.games;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CompleteGame extends ChatGame {
    
    private FileConfiguration config;
    
    public CompleteGame(FileConfiguration config) {
        super("Completar Frase");
        this.config = config;
        generateQuestion();
    }
    
    @Override
    public void generateQuestion() {
        List<Map<?, ?>> completeQuestions = (List<Map<?, ?>>) config.getList("questions.complete");
        
        if (completeQuestions == null || completeQuestions.isEmpty()) {
            generateDefaultQuestion();
            return;
        }
        
        Map<?, ?> selectedQuestion = completeQuestions.get(ThreadLocalRandom.current().nextInt(completeQuestions.size()));
        
        question = (String) selectedQuestion.get("question");
        answer = (String) selectedQuestion.get("answer");
        
        List<String> hintsList = (List<String>) selectedQuestion.get("hints");
        if (hintsList != null) {
            hints = hintsList.toArray(new String[0]);
        } else {
            hints = new String[]{"Es un refrán", "Piensa en el significado", "La respuesta es: " + answer};
        }
    }
    
    private void generateDefaultQuestion() {
        String[][] questions = {
            {"Más vale pájaro en mano que...", "ciento volando", "Es un refrán", "Habla de números", "...ciento volando"},
            {"En casa de herrero, cuchillo de...", "palo", "Es un refrán", "Material opuesto", "...de palo"},
            {"A caballo regalado no se le mira el...", "diente", "Es un refrán", "Parte de la boca", "...el diente"},
            {"Ojos que no ven, corazón que no...", "siente", "Es un refrán", "Emoción", "...que no siente"},
            {"Agua que no has de beber...", "dejala correr", "Es un refrán", "No interferir", "...déjala correr"},
            {"Camarón que se duerme...", "se lo lleva la corriente", "Es un refrán", "Sobre el río", "...se lo lleva la corriente"},
            {"El que madruga...", "dios le ayuda", "Es un refrán", "Sobre levantarse temprano", "...Dios le ayuda"},
            {"No por mucho madrugar...", "amanece mas temprano", "Es un refrán", "Sobre el tiempo", "...amanece más temprano"},
            {"A quien madruga...", "dios le ayuda", "Es un refrán", "Sobre levantarse temprano", "...Dios le ayuda"},
            {"Dime con quién andas y te diré...", "quien eres", "Es un refrán", "Sobre las amistades", "...quién eres"},
            {"En boca cerrada...", "no entran moscas", "Es un refrán", "Sobre el silencio", "...no entran moscas"},
            {"Perro que ladra...", "no muerde", "Es un refrán", "Sobre amenazas", "...no muerde"},
            {"A mal tiempo...", "buena cara", "Es un refrán", "Sobre el optimismo", "...buena cara"},
            {"Más vale tarde...", "que nunca", "Es un refrán", "Sobre el tiempo", "...que nunca"},
            {"No hay mal que...", "por bien no venga", "Es un refrán", "Sobre las adversidades", "...por bien no venga"}
        };
        
        String[] selected = questions[ThreadLocalRandom.current().nextInt(questions.length)];
        question = selected[0];
        answer = selected[1];
        hints = new String[]{selected[2], selected[3], selected[4]};
    }
}