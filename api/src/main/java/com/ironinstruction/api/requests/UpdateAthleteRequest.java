package com.ironinstruction.api.requests;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UpdateAthleteRequest {
    private String weightClass;
    private float weight;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dob;
    private float squatMax;
    private float benchMax;
    private float deadliftMax;
    private float height;

    public UpdateAthleteRequest () { }

    public UpdateAthleteRequest (String weightClass, float weight, Date dob, float squatMax, float benchMax, float deadliftMax, float height) {
        this.weightClass = weightClass;
        this.weight = weight;
        this.dob = dob;
        this.squatMax = squatMax;
        this.benchMax = benchMax;
        this.deadliftMax = deadliftMax;
        this.height = height;
    } 

    public void setWeightClass(String weightClass) {
        this.weightClass = weightClass;
    }

    public String getWeightClass() {
        return this.weightClass;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getWeight() {
        return this.weight;
    }

    public Date getDob() {
        return this.dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public float getSquatMax() {
        return this.squatMax;
    }

    public void setSquatMax(float squatMax) {
        this.squatMax = squatMax; 
    }

    public float getBenchMax() {
        return this.benchMax;
    }

    public void setBenchMax (float benchMax) {
        this.benchMax = benchMax; 
    }
    public float getDeadliftMax () {
        return this.deadliftMax;
    }

    public void setDeadliftMax (float deadliftMax) {
        this.deadliftMax = deadliftMax; 
    }
    public float getHeight() {
        return this.height;
    }

    public void setHeight (float height) {
        this.height = height; 
    }
}

