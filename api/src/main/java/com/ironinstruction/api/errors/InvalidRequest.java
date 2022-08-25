package com.ironinstruction.api.errors;

public class InvalidRequest extends Exception {
    public InvalidRequest(String message) {
        super(message);
    }
}
