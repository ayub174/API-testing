package com.apitesting.model;

import com.apitesting.security.Permission;

import java.util.List;

/**
 * Säkert svarsobjekt för admin-vyn: innehåller aldrig lösenordshashen.
 */
public class AccountResponse {

    private Long id;
    private String username;
    private String role;
    private List<String> permissions;

    public AccountResponse(Account account) {
        this.id = account.getId();
        this.username = account.getUsername();
        this.role = account.getRole().name();
        this.permissions = account.getPermissions().stream()
                .sorted()
                .map(Permission::name)
                .toList();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public List<String> getPermissions() {
        return permissions;
    }
}
