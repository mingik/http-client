package com.mkm.http.client.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mintik on 6/3/16.
 */
public class MyModelTwo implements MyModel {
    private String name;
    private double age;
    private List<String> hobbies = new ArrayList<>();
    private int id;

    public MyModelTwo() {}

    public MyModelTwo(String name, int id, double age) {
        this.name = name;
        this.id = id;
        this.age = age;
        this.hobbies.add("basketball");
        this.hobbies.add("reading");
        this.hobbies.add("jogging");
    }

    @Override
    public String toString() {
        return "MyModelTwo{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", hobbies=" + hobbies +
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

    public double getAge() {
        return age;
    }

    public void setAge(double age) {
        this.age = age;
    }

    public List<String> getHobbies() {
        return hobbies;
    }

    public void setHobbies(List<String> hobbies) {
        this.hobbies = hobbies;
    }
}
