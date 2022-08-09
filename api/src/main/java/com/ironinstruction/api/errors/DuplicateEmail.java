package com.ironinstruction.api.errors;

public class DuplicateEmail extends Exception{
    private final String email;
    public DuplicateEmail(String email) {
        super();
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
