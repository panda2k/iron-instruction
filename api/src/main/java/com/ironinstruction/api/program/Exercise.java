package com.ironinstruction.api.program;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;

import com.ironinstruction.api.errors.ResourceNotFound;

public class Exercise {
    @Id
    private String id;
    private String name;
    private String coachNotes;
    private String athleteNotes;
    private String videoRef;
    private ArrayList<Set> sets;

    public Exercise(String name, String coachNotes, String videoRef) {
        this.id = new ObjectId().toString();
        this.name = name;
        this.coachNotes = coachNotes;
        this.videoRef = videoRef;
        this.sets = new ArrayList<Set>();
    }

    public Set findSetById(String setId) throws ResourceNotFound {
        for (Set set : this.sets) {
            if (set.getId().equals(setId)) {
                return set;
            }
        }

         throw new ResourceNotFound(setId);
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
