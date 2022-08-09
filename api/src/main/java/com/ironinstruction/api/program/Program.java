package com.ironinstruction.api.program;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;

public class Program {
    @Id
    private String id;
    private String name;
    private String description;
    private ArrayList<Week> weeks;
    private boolean template; // if template program, can't be directly assigned to athlete
    private String athleteEmail; // who the program is assigned to

    public Program(String name, String description, boolean template) {
        this.name = name;
        this.description = description;
        this.template = template;
        this.weeks = new ArrayList<Week>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Week> getWeeks() {
        return weeks;
    }

    public void addWeek(Week week) {
        this.weeks.add(week);
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public String getAthleteEmail() {
        return athleteEmail;
    }

    public void setAthleteEmail(String athleteEmail) {
        this.athleteEmail = athleteEmail;
    }
}
