package com.ironinstruction.api.requests;

public class LoginRequest {
    private String email;
    private String password;

    // empty constructor for jackson to cast json body to object
    public LoginRequest() {}

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
