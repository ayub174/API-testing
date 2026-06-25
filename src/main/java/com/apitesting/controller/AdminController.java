package com.apitesting.controller;

import com.apitesting.model.Account;
import com.apitesting.model.AccountResponse;
import com.apitesting.model.CreateAccountRequest;
import com.apitesting.security.Permission;
import com.apitesting.security.Role;
import com.apitesting.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Admin-vy för konto- och behörighetshantering. Behörigheterna styrs per
 * endpoint i SecurityConfig:
 *  - kontohantering (skapa/ta bort konto, lista) → ACCOUNT_MANAGE
 *  - roll-/behörighetshantering (grant/revoke/role) → PERMISSION_MANAGE
 * (En ADMIN har båda; en HANDLAGGARE har ingen av dem.)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AccountService accountService;

    public AdminController(AccountService accountService) {
        this.accountService = accountService;
    }

    // GET /api/admin/accounts - lista alla konton med roll och behörigheter
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> listAccounts() {
        List<AccountResponse> accounts = accountService.findAll().stream()
                .map(AccountResponse::new)
                .toList();
        return ResponseEntity.ok(accounts);
    }

    // POST /api/admin/accounts - skapa ett konto (ACCOUNT_MANAGE).
    // Personal utan PERMISSION_MANAGE får bara skapa låntagare (ANVANDARE).
    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request,
                                                         Authentication auth) {
        Role role = parseRole(request.getRole());
        if (role != Role.ANVANDARE && !canManageStaff(auth)) {
            throw new AccessDeniedException("Endast admin kan skapa konton med rollen " + role);
        }
        Account account = accountService.createAccount(request.getUsername(), request.getPassword(), role);
        return ResponseEntity
                .created(URI.create("/api/admin/accounts/" + account.getUsername()))
                .body(new AccountResponse(account));
    }

    // DELETE /api/admin/accounts/{username} - ta bort ett konto (ACCOUNT_MANAGE).
    // Personal utan PERMISSION_MANAGE får bara ta bort låntagare (ANVANDARE).
    @DeleteMapping("/accounts/{username}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String username, Authentication auth) {
        Account target = accountService.findByUsername(username);
        if (target.getRole() != Role.ANVANDARE && !canManageStaff(auth)) {
            throw new AccessDeniedException("Endast admin kan ta bort konton med rollen " + target.getRole());
        }
        accountService.deleteAccount(username);
        return ResponseEntity.noContent().build();
    }

    /** Sant om den inloggade får hantera personalkonton/roller (admin). */
    private boolean canManageStaff(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(Permission.PERMISSION_MANAGE.name()));
    }

    // POST /api/admin/accounts/{username}/permissions/{permission} - ge behörighet
    @PostMapping("/accounts/{username}/permissions/{permission}")
    public ResponseEntity<AccountResponse> grant(@PathVariable String username,
                                                 @PathVariable String permission) {
        Account account = accountService.grantPermission(username, parsePermission(permission));
        return ResponseEntity.ok(new AccountResponse(account));
    }

    // DELETE /api/admin/accounts/{username}/permissions/{permission} - ta bort behörighet
    @DeleteMapping("/accounts/{username}/permissions/{permission}")
    public ResponseEntity<AccountResponse> revoke(@PathVariable String username,
                                                  @PathVariable String permission) {
        Account account = accountService.revokePermission(username, parsePermission(permission));
        return ResponseEntity.ok(new AccountResponse(account));
    }

    // PUT /api/admin/accounts/{username}/role - byt roll (nollställer behörigheter)
    @PutMapping("/accounts/{username}/role")
    public ResponseEntity<AccountResponse> changeRole(@PathVariable String username,
                                                      @RequestBody Map<String, String> body) {
        String roleValue = body.get("role");
        if (roleValue == null) {
            throw new IllegalArgumentException("Fältet 'role' krävs");
        }
        Account account = accountService.changeRole(username, parseRole(roleValue));
        return ResponseEntity.ok(new AccountResponse(account));
    }

    // GET /api/admin/permissions - alla möjliga behörigheter (för UI:t)
    @GetMapping("/permissions")
    public ResponseEntity<List<String>> permissions() {
        return ResponseEntity.ok(Arrays.stream(Permission.values()).map(Enum::name).toList());
    }

    // GET /api/admin/roles - alla möjliga roller (för UI:t)
    @GetMapping("/roles")
    public ResponseEntity<List<String>> roles() {
        return ResponseEntity.ok(Arrays.stream(Role.values()).map(Enum::name).toList());
    }

    private Permission parsePermission(String value) {
        try {
            return Permission.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Okänd behörighet: " + value);
        }
    }

    private Role parseRole(String value) {
        try {
            return Role.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Okänd roll: " + value);
        }
    }
}
