package com.ironinstruction.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ironinstruction.api.user.UserType;

public class CreateUserRequest {
    private String email;
    private String password;
    private String name;
    private UserType userType;

    public String getEmail() {
        return email;
    }

    public UserType getUserType() {
        return userType;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public CreateUserRequest(
        @JsonProperty(value = "email", required = true) String email, 
        @JsonProperty(value = "password", required = true) String password,
        @JsonProperty(value = "name", required = true) String name,
        @JsonProperty(value="userType", required = true) UserType userType
    ) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.userType = userType;
    }
}
