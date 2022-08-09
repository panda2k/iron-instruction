package com.ironinstruction.api.program;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;

public class Exercise {
    @Id
    private String id;
    private String name;
    private String coachNotes;
    private String athleteNotes;
    private String videoRef;
    private ArrayList<Set> sets;

    public Exercise(String name, String coachNotes, String athleteNotes, String videoRef) {
        this.name = name;
        this.coachNotes = coachNotes;
        this.athleteNotes = athleteNotes;
        this.videoRef = videoRef;
        this.sets = new ArrayList<Set>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public ArrayList<Set> getSets() {
        return sets;
    }

    public void addSet(Set set) {
        this.sets.add(set);
    }
}
