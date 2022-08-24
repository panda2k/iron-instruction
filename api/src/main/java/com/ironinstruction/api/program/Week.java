package com.ironinstruction.api.program;

import com.ironinstruction.api.errors.ResourceNotFound;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;

public class Week {
    @Id
    private String id;
    private String coachNotes;
    private String athleteNotes;
    private ArrayList<Day> days;

    public Week() { }

    public Week(String coachNotes) {
        this.id = new ObjectId().toString();
        this.coachNotes = coachNotes;
        this.days = new ArrayList<Day>();
    }

    public Day findDayById(String dayId) throws ResourceNotFound {
        for (Day day : this.days) {
            if (day.getId().equals(dayId)) {
                return day;
            }
        }

        throw new ResourceNotFound(dayId);
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

    public ArrayList<Day> getDays() {
        return days;
    }

    public void addDay(Day day) {
        this.days.add(day);
    }
}
