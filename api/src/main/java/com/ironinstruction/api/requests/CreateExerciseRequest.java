package com.ironinstruction.api.requests;

public class CreateExerciseRequest {
    private String name;
    private String coachNotes;
    private String videoRef;

    public CreateExerciseRequest() { }

    public CreateExerciseRequest(String name, String coachNotes, String videoRef) {
        this.name = name;
        this.coachNotes = coachNotes;
        this.videoRef = videoRef;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getCoachNotes() {
        return this.coachNotes;
    }

    public void setCoachNotes(String coachNotes) {
        this.coachNotes = coachNotes;
    }

    public String getVideoRef() {
        return this.videoRef;
    }

    public void setVideoRef(String videoRef) {
        this.videoRef = videoRef;
    }
}
