package com.ab5y.pmpquiz.models;

/**
 * Created by Abhay on 27/6/2016.
 */
public class Option {
    public int id;
    public String text;
    public boolean isCorrectAnswer;
    public int questionId;

    public Option(int id, String text) {
        this.id = id;
        this.text = text;
    }
    public Option(int id, String text, boolean isCorrectAnswer, int questionId){
        this.id = id;
        this.text = text;
        this.isCorrectAnswer = isCorrectAnswer;
        this.questionId = questionId;
    }
}