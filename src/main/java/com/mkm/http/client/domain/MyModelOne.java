package com.mkm.http.client.domain;

/**
 * Created by mintik on 5/25/16.
 */
public class MyModelOne implements MyModel {
    private String name;
    private int id;

    public MyModelOne() {}

    public MyModelOne(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return "MyModelOne{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
