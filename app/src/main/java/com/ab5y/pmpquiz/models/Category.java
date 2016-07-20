package com.ab5y.pmpquiz.models;

/**
 * Created by Abhay on 7/7/2016.
 */
public class Category {
    public int id;
    public String name;
    public String author;
    public int numQuestions;
    public int lastAttemptScore;

    public Category(int id, String name){
        this.id = id;
        this.name = name;
    }

    public Category(int id, String name, String author, int numQuestions, int lastAttemptScore) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.numQuestions = numQuestions;
        this.lastAttemptScore = lastAttemptScore;
    }

    public void setLastAttemptScore(int lastAttemptScore) {
        this.lastAttemptScore = lastAttemptScore;
    }

    public int getLastAttemptScore() {
        return this.lastAttemptScore;
    }
}
