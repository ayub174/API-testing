package com.apitesting.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Hjälper de fristående övnings-endpointsen för Basic Auth och API-nyckel.
 * Den riktiga inloggningen sköts numera av Spring Security + JWT
 * (se SecurityConfig, JwtService och AuthController).
 */
@Service
public class AuthService {

    @Value("${app.auth.username}")
    private String validUsername;

    @Value("${app.auth.password}")
    private String validPassword;

    @Value("${app.auth.api-key}")
    private String validApiKey;

    public boolean validateCredentials(String username, String password) {
        return validUsername.equals(username) && validPassword.equals(password);
    }

    public boolean isApiKeyValid(String apiKey) {
        return validApiKey.equals(apiKey);
    }
}
