package com.ab5y.pmpquiz.models;

/**
 * Created by Abhay on 18/7/2016.
 */
public class Followee implements User {

    static int userTypeID = 1;
    int userID;
    String userName;
    String fullName;
    int numCategoriesAuthored;
    boolean following;

    public Followee(String userName, String fullName, int userID, int userTypeID) {
        this.userName = userName;
        this.fullName = fullName;
        this.userID = userID;
        this.userTypeID = userTypeID;
    }

    public Followee(String userName, int userID, int userTypeID, String fullName, int numCategoriesAuthored){
        this.userName = userName;
        this.userID = userID;
        this.userTypeID = userTypeID;
        this.fullName = fullName;
        this.numCategoriesAuthored = numCategoriesAuthored;
    }

    @Override
    public int userTypeID() {
        return userTypeID;
    }

    public String getUserName(){
        return userName;
    }

    public String getFullName() {
        return fullName;
    }

    public int getNumCategoriesAuthored() {
        return numCategoriesAuthored;
    }

    public int getUserID() {
        return userID;
    }

    public void setNumCategoriesAuthored(int numCategoriesAuthored) {
        this.numCategoriesAuthored = numCategoriesAuthored;
    }

    public boolean getFollowing() {
        return following;
    }

    public void setFollowing (boolean following) {
        this.following = following;
    }
}
