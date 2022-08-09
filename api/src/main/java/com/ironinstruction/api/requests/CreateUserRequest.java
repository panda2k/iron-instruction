package com.ironinstruction.api.requests;

import com.ironinstruction.api.user.UserType;

public class CreateUserRequest {
    private final String email;
    private final String password;
    private final String name;
    private final UserType userType;

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

    public CreateUserRequest(String email, String password, String name, UserType userType) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.userType = userType;
    }
}
