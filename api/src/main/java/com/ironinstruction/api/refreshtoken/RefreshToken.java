package com.ironinstruction.api.refreshtoken;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "refreshTokens")
public class RefreshToken {
    @Id
    private final String token;

    public RefreshToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
