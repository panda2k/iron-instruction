package com.ironinstruction.api.requests;

public class RefreshTokenRequest {
    private String refreshToken;

    // empty constructor for jackson to cast json body to object
    public RefreshTokenRequest() { }

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String token) {
        this.refreshToken = token;
    }
}

