package com.ironinstruction.api.responses;

public class JWTResponse {
    private final String accessToken;
    private final String refreshToken;

    public JWTResponse(String accessToken, String refreshToken)  {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
