package com.apitesting.controller;

import com.apitesting.exception.UnauthorizedException;
import com.apitesting.model.User;
import com.apitesting.service.AuthService;
import com.apitesting.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    // GET /api/users - Skyddad endpoint som kräver Bearer token
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        validateBearerToken(authHeader);
        return ResponseEntity.ok(userService.findAll());
    }

    // GET /api/users/{id} - Skyddad endpoint
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        validateBearerToken(authHeader);
        return ResponseEntity.ok(userService.findById(id));
    }

    // POST /api/users - Registrera en ny användare (kräver ingen auth)
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        if (userService.emailExists(user.getEmail())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Conflict");
            error.put("message", "Email finns redan registrerad: " + user.getEmail());
            return ResponseEntity.status(409).body(error);
        }
        User created = userService.save(user);
        URI location = URI.create("/api/users/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    // PUT /api/users/{id} - Uppdatera användare (kräver Bearer token)
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        validateBearerToken(authHeader);
        return ResponseEntity.ok(userService.update(id, user));
    }

    // DELETE /api/users/{id} - Skyddad endpoint
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        validateBearerToken(authHeader);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void validateBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Bearer token saknas eller är felaktigt formaterad");
        }
        String token = authHeader.substring(7);
        if (!authService.isTokenValid(token)) {
            throw new UnauthorizedException("Ogiltig eller utgången token");
        }
    }
}
