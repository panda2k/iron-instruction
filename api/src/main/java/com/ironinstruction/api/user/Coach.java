package com.ironinstruction.api.user;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "users")
public class Coach extends User {
    private ArrayList<String> athleteEmails;

    protected Coach() {
        super();
    }

    public Coach(String name, String email, String passwordHash, String passwordSalt) {
        super(name, email, passwordHash, passwordSalt, UserType.COACH);
        this.athleteEmails = new ArrayList<String>();
    }

    public void addAthlete(String email) {
        this.athleteEmails.add(email);
    }
}
