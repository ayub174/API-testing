package com.apitesting.model;

import com.apitesting.security.Permission;
import com.apitesting.security.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ett inloggningskonto (skiljt från {@link User} som är en CRUD-demoresurs).
 * Lösenordet lagras som BCrypt-hash och exponeras aldrig i JSON.
 */
public class Account {

    private Long id;
    private String username;

    @JsonIgnore
    private String passwordHash;

    private Role role;
    private Set<Permission> permissions;

    public Account() {
    }

    public Account(Long id, String username, String passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.permissions = role.defaultPermissions();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions == null ? EnumSet.noneOf(Permission.class) : EnumSet.copyOf(permissions);
    }
}
