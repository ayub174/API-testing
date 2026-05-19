package com.apitesting.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class User {

    private Long id;

    @NotBlank(message = "Namn får inte vara tomt")
    @Size(min = 2, max = 50, message = "Namn måste vara mellan 2 och 50 tecken")
    private String name;

    @NotBlank(message = "Email får inte vara tom")
    @Email(message = "Ogiltig email-adress")
    private String email;

    @Size(min = 6, message = "Lösenord måste vara minst 6 tecken")
    private String password;

    private String role;

    public User() {
    }

    public User(Long id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
