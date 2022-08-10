package com.ironinstruction.api.security;

import com.ironinstruction.api.utils.PasswordManager;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class SecurityConstants {
    private static final PasswordManager passwordManager = new PasswordManager();
    public static final String SECRET; // create a random secret every time the server is booted

    static {
        try {
            SECRET = passwordManager.hash(passwordManager.createSalt(), passwordManager.createSalt());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static final int EXPIRATION_TIME = 900_000; // 15 minutes
    public static final String TOKEN_PREFIX = "Bearer";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/api/v1/users";
    public static final String LOGIN_URL = "/api/v1/login";
}
