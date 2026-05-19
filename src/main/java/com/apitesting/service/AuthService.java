package com.apitesting.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    @Value("${app.auth.username}")
    private String validUsername;

    @Value("${app.auth.password}")
    private String validPassword;

    @Value("${app.auth.api-key}")
    private String validApiKey;

    private final Set<String> activeTokens = ConcurrentHashMap.newKeySet();

    public boolean validateCredentials(String username, String password) {
        return validUsername.equals(username) && validPassword.equals(password);
    }

    public String generateToken(String username) {
        String token = Base64.getEncoder().encodeToString(
                (username + ":" + UUID.randomUUID()).getBytes()
        );
        activeTokens.add(token);
        return token;
    }

    public boolean isTokenValid(String token) {
        return token != null && activeTokens.contains(token);
    }

    public void invalidateToken(String token) {
        activeTokens.remove(token);
    }

    public boolean isApiKeyValid(String apiKey) {
        return validApiKey.equals(apiKey);
    }
}
