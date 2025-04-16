package com.esprit.knowlity.Model;

public class Question {
    private int id;
    private int evaluationId;
    private int point;
    private int ordreQuestion;
    private String enonce;
    private String codeSnippet;
    private String programmingLanguage;
    private boolean hasMathFormula;
    private String mathFormula;
    private String title;

    public Question() {
        // Default constructor
    }

    // Constructor
    public Question(int id, int evaluationId, int point, int ordreQuestion, String enonce,
                    String codeSnippet, String programmingLanguage, boolean hasMathFormula,
                    String mathFormula, String title) {
        this.id = id;
        this.evaluationId = evaluationId;
        this.point = point;
        this.ordreQuestion = ordreQuestion;
        this.enonce = enonce;
        this.codeSnippet = codeSnippet;
        this.programmingLanguage = programmingLanguage;
        this.hasMathFormula = hasMathFormula;
        this.mathFormula = mathFormula;
        this.title = title;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEvaluationId() { return evaluationId; }
    public void setEvaluationId(int evaluationId) { this.evaluationId = evaluationId; }
    public int getPoint() { return point; }
    public void setPoint(int point) { this.point = point; }
    public int getOrdreQuestion() { return ordreQuestion; }
    public void setOrdreQuestion(int ordreQuestion) { this.ordreQuestion = ordreQuestion; }
    public String getEnonce() { return enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }
    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }
    public String getProgrammingLanguage() { return programmingLanguage; }
    public void setProgrammingLanguage(String programmingLanguage) { this.programmingLanguage = programmingLanguage; }
    public boolean isHasMathFormula() { return hasMathFormula; }
    public void setHasMathFormula(boolean hasMathFormula) { this.hasMathFormula = hasMathFormula; }
    public String getMathFormula() { return mathFormula; }
    public void setMathFormula(String mathFormula) { this.mathFormula = mathFormula; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}