package com.apitesting.controller;

import com.apitesting.exception.UnauthorizedException;
import com.apitesting.model.Account;
import com.apitesting.model.LoginRequest;
import com.apitesting.model.LoginResponse;
import com.apitesting.model.RegisterRequest;
import com.apitesting.security.JwtService;
import com.apitesting.service.AccountService;
import com.apitesting.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AccountService accountService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthService authService,
                          AccountService accountService,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.accountService = accountService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // POST /api/auth/login - Verifiera lösenord och få en JWT
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unauthorized");
            error.put("message", "Felaktigt användarnamn eller lösenord");
            return ResponseEntity.status(401).body(error);
        }

        Account account = accountService.findByUsername(request.getUsername());
        String token = jwtService.generateToken(account);
        List<String> permissions = account.getPermissions().stream().map(Enum::name).toList();
        LoginResponse response = new LoginResponse(
                token,
                account.getUsername(),
                account.getRole().name(),
                permissions,
                jwtService.getExpirationMs() / 1000);
        return ResponseEntity.ok(response);
    }

    // POST /api/auth/register - Självregistrering som låntagare (ANVANDARE)
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (accountService.usernameExists(request.getUsername())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Conflict");
            error.put("message", "Användarnamnet är upptaget: " + request.getUsername());
            return ResponseEntity.status(409).body(error);
        }
        Account account = accountService.register(request.getUsername(), request.getPassword());
        Map<String, Object> response = new HashMap<>();
        response.put("username", account.getUsername());
        response.put("role", account.getRole().name());
        response.put("message", "Kontot skapades – du kan nu logga in");
        return ResponseEntity.status(201).body(response);
    }

    // POST /api/auth/logout - Stateless: klienten släpper sin token.
    // TODO (övning): en riktig logout kräver en denylist över invaliderade tokens.
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Utloggad");
        return ResponseEntity.ok(response);
    }

    // GET /api/auth/me - Hämta inloggad användare från säkerhetskontexten
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        Account account = accountService.findByUsername(authentication.getName());
        Map<String, Object> response = new HashMap<>();
        response.put("username", account.getUsername());
        response.put("role", account.getRole().name());
        response.put("permissions", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList());
        response.put("authenticated", true);
        return ResponseEntity.ok(response);
    }

    // GET /api/auth/basic - Fristående övning för Basic Auth
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

    // GET /api/auth/api-key - Fristående övning för API-nyckel i header
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
