package com.ironinstruction.api.errors;

import org.springframework.security.core.AuthenticationException;

public class InvalidToken extends AuthenticationException {
    public InvalidToken(String msg) {
        super(msg);
    }
}
