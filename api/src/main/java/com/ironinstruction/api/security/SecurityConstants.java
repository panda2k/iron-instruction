package com.ironinstruction.api.security;

import com.ironinstruction.api.utils.PasswordManager;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class SecurityConstants {
    private static final PasswordManager passwordManager = new PasswordManager();
    public static final String ACCESS_SECRET; // create a random secret every time the server is booted
    public static final String REFRESH_SECRET;

    static {
        try {
            REFRESH_SECRET = passwordManager.hash(passwordManager.createSalt(), passwordManager.createSalt());
            ACCESS_SECRET = passwordManager.hash(passwordManager.createSalt(), passwordManager.createSalt());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static final int ACCESS_EXPIRATION_TIME_MINUTES = 15; // 15 minutes
    public static final String TOKEN_PREFIX = "Bearer";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/api/v1/users";
    public static final String LOGIN_URL = "/api/v1/login";
    public static final String REFRESH_URL = "/api/v1/refreshtoken";
    public static final int REFRESH_EXPIRATION_TIME_MINUTES = 60 * 24 * 30; // 30 days
    public static final int URL_EXPIRATION_TIME_MINUTES = 10;
    public static final String S3_BUCKET_NAME = "iron-instruction";
    public static final long S3_MAX_FILE_SIZE = 100000000;
}
