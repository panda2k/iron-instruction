package com.ironinstruction.api.program;

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
    private String coachNotes;
    private String athleteNotes;
    private String videoRef;
    private boolean videoRequested;

    public Set(int reps, float percentage, PercentageOptions percentageReference, String coachNotes, boolean videoRequested) {
        this.reps = reps;
        this.percentage = percentage;
        this.percentageReference = percentageReference;
        this.coachNotes = coachNotes;
        this.videoRequested = videoRequested;
    }

    public Set(int reps, float rpe, String coachNotes, boolean videoRequested) {
        this.reps = reps;
        this.rpe = rpe;
        this.coachNotes = coachNotes;
        this.videoRequested = videoRequested;
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

    public String getCoachNotes() {
        return coachNotes;
    }

    public void setCoachNotes(String coachNotes) {
        this.coachNotes = coachNotes;
    }

    public String getAthleteNotes() {
        return athleteNotes;
    }

    public void setAthleteNotes(String athleteNotes) {
        this.athleteNotes = athleteNotes;
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
