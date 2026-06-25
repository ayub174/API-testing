package com.apitesting.model;

import java.util.List;

public class LoginResponse {

    private String token;
    private String username;
    private String role;
    private List<String> permissions;
    private long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(String token, String username, String role, List<String> permissions, long expiresIn) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.permissions = permissions;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
