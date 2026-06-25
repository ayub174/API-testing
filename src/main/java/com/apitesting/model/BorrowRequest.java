package com.apitesting.model;

import jakarta.validation.constraints.NotNull;

/**
 * Begäran om att låna en bok. {@code username} används bara av personal när de
 * lånar åt en låntagare (endpointen /api/loans/borrow-for); vid eget lån
 * ignoreras det och den inloggade användaren används.
 */
public class BorrowRequest {

    @NotNull(message = "bookId krävs")
    private Long bookId;

    private String username;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
