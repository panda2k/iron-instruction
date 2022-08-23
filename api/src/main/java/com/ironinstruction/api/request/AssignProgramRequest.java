package com.ironinstruction.api.request;

public class AssignProgramRequest {
    private String email;

    public AssignProgramRequest() { } 

    public AssignProgramRequest(String email) {
        this.email = email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
