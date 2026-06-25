package com.apitesting.controller;

import com.apitesting.model.User;
import com.apitesting.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CRUD för "användare" (en demoresurs, t.ex. kunder). Behörigheten styrs
 * centralt i SecurityConfig: läsning kräver USER_READ, ändring USER_MANAGE.
 * Registrering (POST) är öppen.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users - kräver USER_READ
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    // GET /api/users/{id} - kräver USER_READ
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // POST /api/users - öppen självregistrering
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

    // PUT /api/users/{id} - kräver USER_MANAGE
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    // DELETE /api/users/{id} - kräver USER_MANAGE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
