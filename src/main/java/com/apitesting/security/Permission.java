package com.apitesting.security;

/**
 * Granulära behörigheter. Enum-namnet används verbatim som Spring-authority,
 * t.ex. hasAuthority("BOOK_CREATE").
 */
public enum Permission {
    // Böcker (katalog)
    BOOK_READ,
    BOOK_CREATE,
    BOOK_UPDATE,
    BOOK_DELETE,

    // Lån
    LOAN_BORROW,      // låna en bok åt sig själv
    LOAN_RETURN,      // återlämna sitt eget lån
    LOAN_VIEW_OWN,    // se sina egna lån
    LOAN_VIEW_ALL,    // se alla lån (personal)
    LOAN_MANAGE,      // registrera lån/återlämning åt en låntagare (personal)

    // Användarresurs (demo-CRUD)
    USER_READ,
    USER_MANAGE,

    // Konto- och behörighetsadministration
    ACCOUNT_MANAGE,   // skapa/ta bort inloggningskonton (personal/låntagare)
    PERMISSION_MANAGE // hantera roller och behörigheter
}
