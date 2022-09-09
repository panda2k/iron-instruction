package com.ironinstruction.api.requests;

import com.ironinstruction.api.program.PercentageOptions;

public class CreateSetRequest {
    private int reps;
    private float rpe;
    private float percentage;
    private float weight;
    private PercentageOptions percentageReference;
    private boolean videoRequested;

    public CreateSetRequest() { }

    public CreateSetRequest(int reps, float rpe, float percentage, float weight, PercentageOptions percentageReference, boolean videoRequested) {
        this.reps = reps;
        this.rpe = rpe;
        this.percentage = percentage;
        this.weight = weight;
        this.percentageReference = percentageReference;
        this.videoRequested = videoRequested;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getReps() {
        return this.reps;
    }

    public void setRpe(float rpe) {
        this.rpe = rpe;
    }

    public float getRpe() {
        return this.rpe;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public float getPercentage() {
        return this.percentage;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getWeight() {
        return this.weight;
    }

    public void setPercentageReference(PercentageOptions percentageReference) {
        this.percentageReference = percentageReference;
    }

    public PercentageOptions getPercentageReference() {
        return this.percentageReference;
    }

    public void setVideoRequested(boolean videoRequested) {
        this.videoRequested = videoRequested;
    }

    public boolean getVideoRequested() {
        return this.videoRequested;
    }
}
