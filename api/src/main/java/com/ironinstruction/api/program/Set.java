package com.ironinstruction.api.program;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class Set {
    @Id
    private String id;
    private int reps;
    private int completedReps;
    private float percentage;
    private PercentageOptions percentageReference;
    private float weight;
    private float rpe;
    private String videoRef;
    private boolean videoRequested;

    public Set() { 
        this.id = new ObjectId().toString();
    }

    public Set(int reps, float percentage, PercentageOptions percentageReference, boolean videoRequested) {
        this.id = new ObjectId().toString();
        this.reps = reps;
        this.percentage = percentage;
        this.percentageReference = percentageReference;
        this.videoRequested = videoRequested;
        this.completedReps = -1;
    }

    public Set(float rpe, int reps, float weight, boolean videoRequested) {
        this.id = new ObjectId().toString();
        this.rpe = rpe;
        this.reps = reps;
        this.weight = weight;
        this.videoRequested = videoRequested;
        this.completedReps = -1;
    }

    public String generateId() {
        this.id = new ObjectId().toString();
        return this.id;
    }

    public String getId() {
        return this.id;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getCompletedReps() {
        return completedReps;
    }

    public void setCompletedReps(int completedReps) {
        this.completedReps = completedReps;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public PercentageOptions getPercentageReference() {
        return percentageReference;
    }

    public void setPercentageReference(PercentageOptions percentageReference) {
        this.percentageReference = percentageReference;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getRpe() {
        return rpe;
    }

    public void setRpe(float rpe) {
        this.rpe = rpe;
    }

    public String getVideoRef() {
        return videoRef;
    }

    public void setVideoRef(String videoRef) {
        this.videoRef = videoRef;
    }

    public boolean isVideoRequested() {
        return videoRequested;
    }

    public void setVideoRequested(boolean videoRequested) {
        this.videoRequested = videoRequested;
    }
}
