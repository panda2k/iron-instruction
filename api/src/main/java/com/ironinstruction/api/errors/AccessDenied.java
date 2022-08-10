package com.ironinstruction.api.errors;

import org.springframework.security.core.AuthenticationException;

public class AccessDenied extends AuthenticationException {
    public AccessDenied(String msg) {
        super(msg);
    }
}
