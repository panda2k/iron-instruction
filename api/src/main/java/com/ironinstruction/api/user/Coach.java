package com.ironinstruction.api.user;

import com.ironinstruction.api.program.Program;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "users")
public class Coach extends User {
    private ArrayList<Program> programs;
    private ArrayList<String> athleteEmails;

    public Coach(String name, String email, String passwordHash, String passwordSalt, UserType userType) {
        super(name, email, passwordHash, passwordSalt, userType);
        this.programs = new ArrayList<Program>();
        this.athleteEmails = new ArrayList<String>();
    }

    public void addProgram(Program program) {
        this.programs.add(program);
    }

    public void addAthlete(String email) {
        this.athleteEmails.add(email);
    }
}