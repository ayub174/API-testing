package com.apitesting.controller;

import com.apitesting.model.Account;
import com.apitesting.model.AccountResponse;
import com.apitesting.security.Permission;
import com.apitesting.security.Role;
import com.apitesting.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Admin-vy för behörighetshantering. Kräver PERMISSION_MANAGE (centralt i
 * SecurityConfig; @PreAuthorize här som extra demonstration av metod-säkerhet).
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
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
