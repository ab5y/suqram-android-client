package com.ab5y.pmpquiz.models;

import com.ab5y.pmpquiz.custom.ui.Listable;

/**
 * Created by Abhay on 14/7/2016.
 */
public class UserType implements Listable {
    public String name;
    public int id;

    public UserType(String name, int id){
        this.name = name;
        this.id = id;
    }

    @Override
    public String getLabel() {
        return name;
    }
}
