package com.parxsys.excelconverter;

public class User {

    private String id;
    private String fanme;
    private String lanme;
    private String gender;
    private String country;
    private String age;
    private String date;

    public User(String id, String fanme, String lanme, String gender, String country, String age, String date) {
        this.id = id;
        this.fanme = fanme;
        this.lanme = lanme;
        this.gender = gender;
        this.country = country;
        this.age = age;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFanme() {
        return fanme;
    }

    public void setFanme(String fanme) {
        this.fanme = fanme;
    }

    public String getLanme() {
        return lanme;
    }

    public void setLanme(String lanme) {
        this.lanme = lanme;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
