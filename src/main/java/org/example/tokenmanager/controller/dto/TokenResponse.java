package org.example.tokenmanager.controller.dto;

public class TokenResponse {
    private final String token;
    private final String userId;

    public TokenResponse(String token, String userId) {
        this.token = token;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }
}
