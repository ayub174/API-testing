package com.apitesting.security;

import java.util.EnumSet;
import java.util.Set;

/**
 * Två roller med var sin uppsättning standard-behörigheter.
 * ADMIN har allt; HANDLAGGARE har en delmängd. En admin kan sedan
 * lägga till/ta bort enskilda behörigheter per konto (granulärt).
 */
public enum Role {

    ADMIN(EnumSet.allOf(Permission.class)),

    HANDLAGGARE(EnumSet.of(
            Permission.BOOK_READ,
            Permission.BOOK_CREATE,
            Permission.BOOK_UPDATE,
            Permission.USER_READ
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
