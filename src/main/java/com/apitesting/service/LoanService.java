package com.apitesting.service;

import com.apitesting.exception.ResourceNotFoundException;
import com.apitesting.model.Book;
import com.apitesting.model.Loan;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory-lånehantering. Ett lån minskar bokens lagersaldo, en återlämning
 * ökar det igen. Varje lån får ett förfallodatum (utlåningsdag + lånetid).
 */
@Service
public class LoanService {

    /** Lånetid i dagar. TODO (övning): gör konfigurerbar via application.properties. */
    static final int LOAN_PERIOD_DAYS = 14;

    /** Max antal samtidigt aktiva (ej återlämnade) lån per användare. */
    static final int MAX_ACTIVE_LOANS = 3;

    private final ConcurrentHashMap<Long, Loan> loans = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final BookService bookService;

    public LoanService(BookService bookService) {
        this.bookService = bookService;
    }

    /** Lånar ut en bok till en användare om den finns i lager och lånegränsen inte är nådd. */
    public synchronized Loan borrow(Long bookId, String username) {
        Book book = bookService.findById(bookId); // kastar 404 om boken saknas
        if (book.getStock() == null || book.getStock() <= 0) {
            throw new IllegalStateException("Boken '" + book.getTitle() + "' är inte tillgänglig för utlåning");
        }
        if (countActiveLoans(username) >= MAX_ACTIVE_LOANS) {
            throw new IllegalStateException(
                    "Lånegränsen är nådd (max " + MAX_ACTIVE_LOANS + " aktiva lån per användare)");
        }
        bookService.adjustStock(bookId, -1);
        Loan loan = new Loan(
                idCounter.getAndIncrement(),
                bookId,
                book.getTitle(),
                username,
                LocalDateTime.now(),
                LocalDate.now().plusDays(LOAN_PERIOD_DAYS)
        );
        loans.put(loan.getId(), loan);
        return loan;
    }

    /**
     * Återlämnar ett lån. En användare får bara återlämna sina egna lån;
     * personal med LOAN_MANAGE (canManage=true) får återlämna åt vem som helst.
     */
    public synchronized Loan returnLoan(Long loanId, String requester, boolean canManage) {
        Loan loan = findById(loanId);
        if (loan.isReturned()) {
            throw new IllegalStateException("Lånet är redan återlämnat");
        }
        if (!canManage && !loan.getUsername().equals(requester)) {
            throw new AccessDeniedException("Du kan bara återlämna dina egna lån");
        }
        loan.setReturnedAt(LocalDateTime.now());
        bookService.adjustStock(loan.getBookId(), 1);
        return loan;
    }

    public Loan findById(Long id) {
        Loan loan = loans.get(id);
        if (loan == null) {
            throw new ResourceNotFoundException("Lånet med id " + id + " hittades inte");
        }
        return loan;
    }

    public long countActiveLoans(String username) {
        return loans.values().stream()
                .filter(l -> l.getUsername().equals(username) && !l.isReturned())
                .count();
    }

    public List<Loan> findByUsername(String username) {
        return loans.values().stream()
                .filter(l -> l.getUsername().equals(username))
                .sorted(Comparator.comparing(Loan::getBorrowedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Loan> findAll(boolean activeOnly, boolean overdueOnly) {
        return loans.values().stream()
                .filter(l -> !overdueOnly || l.isOverdue())
                .filter(l -> !activeOnly || !l.isReturned())
                .sorted(Comparator.comparing(Loan::getBorrowedAt).reversed())
                .collect(Collectors.toList());
    }
}
