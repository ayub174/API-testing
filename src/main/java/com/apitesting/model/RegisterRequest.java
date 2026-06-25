package com.apitesting.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Självregistrering av en låntagare (skapar ett ANVANDARE-konto). */
public class RegisterRequest {

    @NotBlank(message = "Användarnamn får inte vara tomt")
    @Size(min = 3, max = 30, message = "Användarnamn måste vara mellan 3 och 30 tecken")
    private String username;

    @NotBlank(message = "Lösenord får inte vara tomt")
    @Size(min = 6, message = "Lösenord måste vara minst 6 tecken")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
