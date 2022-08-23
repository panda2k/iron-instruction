package com.ironinstruction.api.program;

import com.ironinstruction.api.errors.ResourceNotFound;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "programs")
public class Program {
    @Id
    private String id;
    private String name;
    private String description;
    private ArrayList<Week> weeks;
    private boolean template; // if template program, can't be directly assigned to athlete
    private String athleteEmail; // who the program is assigned to
    private final String coachEmail;

    public Program(String coachEmail, String name, String description, boolean template) {
        this.coachEmail = coachEmail;
        this.name = name;
        this.description = description;
        this.template = template;
        this.weeks = new ArrayList<Week>();
    }

    public Week findWeekById(String weekId) throws ResourceNotFound {
        for (Week week : this.weeks) {
            if (week.getId().equals(weekId)) {
                return week;
            }
        }

        throw new ResourceNotFound("Can't find week with id '" + weekId + "'");
    }

    public String getCoachEmail() {
        return this.coachEmail;
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
