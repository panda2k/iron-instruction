package com.ironinstruction.api.requests;

public class CreateExerciseRequest {
    private String name;
    private String videoRef;

    public CreateExerciseRequest() { }

    public CreateExerciseRequest(String name, String videoRef) {
        this.name = name;
        this.videoRef = videoRef;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getVideoRef() {
        return this.videoRef;
    }

    public void setVideoRef(String videoRef) {
        this.videoRef = videoRef;
    }
}
