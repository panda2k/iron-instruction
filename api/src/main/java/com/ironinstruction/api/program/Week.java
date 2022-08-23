package com.ironinstruction.api.program;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;

public class Week {
    @Id
    private String id;
    private String coachNotes;
    private String athleteNotes;
    private ArrayList<Day> days;

    public Week(String coachNotes) {
        this.coachNotes = coachNotes;
        this.days = new ArrayList<Day>();
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
