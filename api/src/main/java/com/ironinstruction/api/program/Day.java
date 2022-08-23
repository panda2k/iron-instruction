package com.ironinstruction.api.program;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;

import com.ironinstruction.api.errors.ResourceNotFound;

public class Day {
    @Id
    private String id;
    private String coachNotes;
    private String athleteNotes;
    private ArrayList<Exercise> exercises;

    public Day(String coachNotes) {
        this.id = new ObjectId().toString();
        this.coachNotes = coachNotes;
        this.exercises = new ArrayList<Exercise>();
    }

    public Exercise findExerciseById(String exerciseId) throws ResourceNotFound {
        for (Exercise exercise : this.exercises) {
            if (exercise.getId().equals(exerciseId)) {
                return exercise;
            }
        }

        throw new ResourceNotFound("Exercise with id '" + exerciseId + "' not found");
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
