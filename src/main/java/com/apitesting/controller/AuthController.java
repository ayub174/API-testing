package com.apitesting.controller;

import com.apitesting.exception.UnauthorizedException;
import com.apitesting.model.LoginRequest;
import com.apitesting.model.LoginResponse;
import com.apitesting.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/login - Logga in och få en token
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        if (!authService.validateCredentials(request.getUsername(), request.getPassword())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unauthorized");
            error.put("message", "Felaktigt användarnamn eller lösenord");
            return ResponseEntity.status(401).body(error);
        }
        String token = authService.generateToken(request.getUsername());
        LoginResponse response = new LoginResponse(token, request.getUsername(), "ADMIN", 3600);
        return ResponseEntity.ok(response);
    }

    // POST /api/auth/logout - Logga ut (invaliderar token)
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Bearer token saknas");
        }
        String token = authHeader.substring(7);
        authService.invalidateToken(token);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Utloggad");
        return ResponseEntity.ok(response);
    }

    // GET /api/auth/me - Hämta inloggad användare (kräver Bearer token)
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Bearer token saknas");
        }
        String token = authHeader.substring(7);
        if (!authService.isTokenValid(token)) {
            throw new UnauthorizedException("Ogiltig eller utgången token");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("username", "admin");
        response.put("role", "ADMIN");
        response.put("authenticated", true);
        return ResponseEntity.ok(response);
    }

    // GET /api/auth/basic - Endpoint skyddad med Basic Auth
    @GetMapping("/basic")
    public ResponseEntity<?> basicAuth(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            throw new UnauthorizedException("Basic Auth-credentials saknas");
        }
        String credentials = new String(
                Base64.getDecoder().decode(authHeader.substring(6)),
                StandardCharsets.UTF_8
        );
        String[] parts = credentials.split(":", 2);
        if (parts.length != 2 || !authService.validateCredentials(parts[0], parts[1])) {
            throw new UnauthorizedException("Felaktiga inloggningsuppgifter");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Du är inloggad med Basic Auth");
        response.put("username", parts[0]);
        return ResponseEntity.ok(response);
    }

    // GET /api/auth/api-key - Endpoint skyddad med API-nyckel i header
    @GetMapping("/api-key")
    public ResponseEntity<?> apiKey(@RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        if (apiKey == null || !authService.isApiKeyValid(apiKey)) {
            throw new UnauthorizedException("Ogiltig eller saknad API-nyckel");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API-nyckeln är giltig");
        response.put("access", "granted");
        return ResponseEntity.ok(response);
    }
}
