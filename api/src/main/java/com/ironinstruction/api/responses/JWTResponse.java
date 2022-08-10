package com.ironinstruction.api.responses;

public class JWTResponse {
    private final String token;
    private final long expirationTimestamp;
    private final String refreshToken;

    public JWTResponse(String token, long expirationTimestamp, String refreshToken) {
        this.token = token;
        this.expirationTimestamp = expirationTimestamp;
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }

    public long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
