package com.apitesting.security;

/**
 * Granulära behörigheter. Enum-namnet används verbatim som Spring-authority,
 * t.ex. hasAuthority("BOOK_CREATE").
 */
public enum Permission {
    BOOK_READ,
    BOOK_CREATE,
    BOOK_UPDATE,
    BOOK_DELETE,
    USER_READ,
    USER_MANAGE,
    PERMISSION_MANAGE
}
