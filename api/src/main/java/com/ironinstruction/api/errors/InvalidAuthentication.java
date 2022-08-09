package com.ironinstruction.api.errors;

public class InvalidAuthentication extends Exception {
    public InvalidAuthentication(String message) {
        super(message);
    }
}
