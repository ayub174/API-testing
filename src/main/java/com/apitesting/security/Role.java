package com.apitesting.security;

import java.util.EnumSet;
import java.util.Set;

/**
 * Tre roller med var sin uppsättning standard-behörigheter:
 *  - ANVANDARE  = låntagare (sök + låna + egna lån)
 *  - HANDLAGGARE = bibliotekarie (katalog, lager, lånedisk)
 *  - ADMIN      = full kontroll (personal, roller, permanent borttagning)
 * En admin kan dessutom slå på/av enskilda behörigheter per konto (granulärt).
 */
public enum Role {

    ADMIN(EnumSet.allOf(Permission.class)),

    HANDLAGGARE(EnumSet.of(
            Permission.BOOK_READ,
            Permission.BOOK_CREATE,
            Permission.BOOK_UPDATE,
            Permission.USER_READ,
            Permission.USER_MANAGE,
            Permission.LOAN_BORROW,
            Permission.LOAN_RETURN,
            Permission.LOAN_VIEW_OWN,
            Permission.LOAN_VIEW_ALL,
            Permission.LOAN_MANAGE
    )),

    ANVANDARE(EnumSet.of(
            Permission.BOOK_READ,
            Permission.LOAN_BORROW,
            Permission.LOAN_RETURN,
            Permission.LOAN_VIEW_OWN
    ));

    private final Set<Permission> defaultPermissions;

    Role(Set<Permission> defaultPermissions) {
        this.defaultPermissions = defaultPermissions;
    }

    /** Returnerar en kopia så att kontots egna behörigheter kan muteras fritt. */
    public Set<Permission> defaultPermissions() {
        return EnumSet.copyOf(defaultPermissions);
    }
}
