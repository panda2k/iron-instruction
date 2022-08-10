package com.ironinstruction.api.errors;

import org.springframework.security.core.AuthenticationException;

public class InvalidEmail extends AuthenticationException {
    public InvalidEmail(String msg) {
        super(msg);
    }
}
