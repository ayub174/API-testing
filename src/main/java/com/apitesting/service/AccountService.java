package com.apitesting.service;

import com.apitesting.exception.ResourceNotFoundException;
import com.apitesting.model.Account;
import com.apitesting.security.Permission;
import com.apitesting.security.Role;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory-konton för inloggning. Data återställs vid omstart.
 * Seedar ett ADMIN-konto och ett HANDLAGGARE-konto.
 */
@Service
public class AccountService {

    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final PasswordEncoder passwordEncoder;

    public AccountService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initData() {
        create("admin", "hemligt123", Role.ADMIN);
        create("handlaggare", "handlaggare123", Role.HANDLAGGARE);
        create("anvandare", "anvandare123", Role.ANVANDARE);
    }

    private Account create(String username, String rawPassword, Role role) {
        Account account = new Account(
                idCounter.getAndIncrement(),
                username,
                passwordEncoder.encode(rawPassword),
                role
        );
        accounts.put(username, account);
        return account;
    }

    public boolean usernameExists(String username) {
        return accounts.containsKey(username);
    }

    /** Självregistrering – skapar alltid ett ANVANDARE-konto (låntagare). */
    public Account register(String username, String rawPassword) {
        return createAccount(username, rawPassword, Role.ANVANDARE);
    }

    /** Admin skapar ett konto med valfri roll. */
    public Account createAccount(String username, String rawPassword, Role role) {
        if (usernameExists(username)) {
            throw new IllegalStateException("Användarnamnet '" + username + "' är upptaget");
        }
        return create(username, rawPassword, role);
    }

    public void deleteAccount(String username) {
        if (!accounts.containsKey(username)) {
            throw new ResourceNotFoundException("Kontot '" + username + "' hittades inte");
        }
        accounts.remove(username);
    }

    public Account findByUsername(String username) {
        Account account = accounts.get(username);
        if (account == null) {
            throw new ResourceNotFoundException("Kontot '" + username + "' hittades inte");
        }
        return account;
    }

    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }

    public boolean hasPermission(String username, Permission permission) {
        Account account = accounts.get(username);
        return account != null && account.getPermissions().contains(permission);
    }

    public Account grantPermission(String username, Permission permission) {
        Account account = findByUsername(username);
        account.getPermissions().add(permission);
        return account;
    }

    public Account revokePermission(String username, Permission permission) {
        Account account = findByUsername(username);
        account.getPermissions().remove(permission);
        return account;
    }

    /**
     * Byter roll och nollställer behörigheterna till den nya rollens standard.
     * TODO (övning): vill vi behålla custom-grants vid rollbyte i stället?
     */
    public Account changeRole(String username, Role role) {
        Account account = findByUsername(username);
        account.setRole(role);
        account.setPermissions(role.defaultPermissions());
        return account;
    }
}
