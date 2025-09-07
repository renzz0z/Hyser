package com.hyser.hysercore.games;

public abstract class ChatGame {
    
    protected String question;
    protected String answer;
    protected String typeName;
    protected String[] hints;
    
    public ChatGame(String typeName) {
        this.typeName = typeName;
        this.hints = new String[0];
    }
    
    public abstract void generateQuestion();
    
    public boolean checkAnswer(String playerAnswer) {
        if (answer == null || playerAnswer == null) {
            return false;
        }
        
        String cleanAnswer = answer.toLowerCase().trim().replaceAll("[^a-zA-Z0-9áéíóúñü ]", "");
        String cleanPlayerAnswer = playerAnswer.toLowerCase().trim().replaceAll("[^a-zA-Z0-9áéíóúñü ]", "");
        
        return cleanAnswer.equals(cleanPlayerAnswer);
    }
    
    public String getQuestion() {
        return question;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    public String[] getHints() {
        return hints;
    }
    
    public String getHint(int index) {
        if (hints == null || index < 0 || index >= hints.length) {
            return null;
        }
        return hints[index];
    }
}