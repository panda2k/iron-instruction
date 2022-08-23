package com.ironinstruction.api.program;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;

public class Day {
    @Id
    private String id;
    private String coachNotes;
    private String athleteNotes;
    private ArrayList<Exercise> exercises;

    public Day(String coachNotes) {
        this.coachNotes = coachNotes;
        this.exercises = new ArrayList<Exercise>();
    }

    public String getId() {
        return id;
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

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public void addExercise(Exercise exercise) {
        this.exercises.add(exercise);
    }
}
