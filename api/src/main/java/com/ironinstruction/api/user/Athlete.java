package com.ironinstruction.api.user;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "users")
public class Athlete extends User {
    private String weightClass;
    private float weight;
    private Date dob;
    private float squatMax;
    private float benchMax;
    private float deadliftMax;
    private float height;

    protected Athlete() {
        super();
    }
    public Athlete(String name, String email, String passwordHash, String passwordSalt) {
        super(name, email, passwordHash, passwordSalt, UserType.ATHLETE);
    }

    public Athlete(String name, String email, String passwordHash, String passwordSalt, String weightClass, float weight, Date dob, float squatMax, float benchMax, float deadliftMax, float height) {
        super(name, email, passwordHash, passwordSalt, UserType.ATHLETE);
        this.weightClass = weightClass;
        this.weight = weight;
        this.dob = dob;
        this.squatMax = squatMax;
        this.benchMax = benchMax;
        this.deadliftMax = deadliftMax;
        this.height = height;
    }

    public String getWeightClass() {
        return weightClass;
    }

    public void setWeightClass(String weightClass) {
        this.weightClass = weightClass;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public Date getDob() {
        return dob;
    }

    public float getSquatMax() {
        return squatMax;
    }

    public void setSquatMax(float squatMax) {
        this.squatMax = squatMax;
    }

    public float getBenchMax() {
        return benchMax;
    }

    public void setBenchMax(float benchMax) {
        this.benchMax = benchMax;
    }

    public float getDeadliftMax() {
        return deadliftMax;
    }

    public void setDeadliftMax(float deadliftMax) {
        this.deadliftMax = deadliftMax;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
