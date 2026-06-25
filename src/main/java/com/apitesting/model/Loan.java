package com.apitesting.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ett lån av en bok. Kopplas till låntagarens inloggningskonto (username).
 * Ett aktivt lån har returnedAt == null. dueDate sätts vid utlåning.
 */
public class Loan {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String username;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime borrowedAt;

    private LocalDate dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnedAt;

    public Loan() {
    }

    public Loan(Long id, Long bookId, String bookTitle, String username,
                LocalDateTime borrowedAt, LocalDate dueDate) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.username = username;
        this.borrowedAt = borrowedAt;
        this.dueDate = dueDate;
    }

    /** Härledd: lånet är återlämnat. */
    public boolean isReturned() {
        return returnedAt != null;
    }

    /** Härledd: aktivt lån vars förfallodatum har passerat. */
    public boolean isOverdue() {
        return returnedAt == null && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(LocalDateTime borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }
}
