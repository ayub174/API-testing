package com.apitesting.controller;

import com.apitesting.model.BorrowRequest;
import com.apitesting.model.Loan;
import com.apitesting.security.Permission;
import com.apitesting.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Lånehantering. Behörigheter styrs centralt i SecurityConfig:
 *  - POST /api/loans              LOAN_BORROW   (låna åt sig själv)
 *  - POST /api/loans/borrow-for   LOAN_MANAGE   (personal lånar åt en låntagare)
 *  - GET  /api/loans/me           LOAN_VIEW_OWN
 *  - GET  /api/loans              LOAN_VIEW_ALL (personal)
 *  - POST /api/loans/{id}/return  LOAN_RETURN eller LOAN_MANAGE
 */
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    // Låna en bok åt sig själv
    @PostMapping
    public ResponseEntity<Loan> borrow(@Valid @RequestBody BorrowRequest request, Authentication auth) {
        Loan loan = loanService.borrow(request.getBookId(), auth.getName());
        return ResponseEntity.created(URI.create("/api/loans/" + loan.getId())).body(loan);
    }

    // Personal lånar åt en angiven låntagare
    @PostMapping("/borrow-for")
    public ResponseEntity<Loan> borrowFor(@Valid @RequestBody BorrowRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Fältet 'username' krävs när personal lånar åt en låntagare");
        }
        Loan loan = loanService.borrow(request.getBookId(), request.getUsername());
        return ResponseEntity.created(URI.create("/api/loans/" + loan.getId())).body(loan);
    }

    // Mina egna lån
    @GetMapping("/me")
    public ResponseEntity<List<Loan>> myLoans(Authentication auth) {
        return ResponseEntity.ok(loanService.findByUsername(auth.getName()));
    }

    // Alla lån (personal). ?active=true visar bara aktiva, ?overdue=true bara försenade.
    @GetMapping
    public ResponseEntity<List<Loan>> allLoans(
            @RequestParam(required = false, defaultValue = "false") boolean active,
            @RequestParam(required = false, defaultValue = "false") boolean overdue) {
        return ResponseEntity.ok(loanService.findAll(active, overdue));
    }

    // Återlämna ett lån. Egen återlämning kräver LOAN_RETURN; personal med
    // LOAN_MANAGE får återlämna åt vem som helst.
    @PostMapping("/{id}/return")
    public ResponseEntity<Loan> returnLoan(@PathVariable Long id, Authentication auth) {
        boolean canManage = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(Permission.LOAN_MANAGE.name()));
        return ResponseEntity.ok(loanService.returnLoan(id, auth.getName(), canManage));
    }
}
