package com.ironinstruction.api.requests;

public class UpdateUserRequest {
    private String email;
    private String name;

    public UpdateUserRequest(String email, String name) {
        this.email = email;
        this.name = name;   
    }

    public UpdateUserRequest() { }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;  
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
