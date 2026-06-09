package com.romkalkylator.model;

/**
 * Ersättningsnivå för en deltagare. Varje nivå motsvarar ett fast dagsbelopp
 * som betalas ut per helgfri vardag deltagaren är inskriven i tjänsten.
 */
public enum Niva {
    A(60),
    B(80),
    C(90);

    private final int dagsbelopp;

    Niva(int dagsbelopp) {
        this.dagsbelopp = dagsbelopp;
    }

    /** Belopp i kronor som betalas ut per helgfri vardag. */
    public int getDagsbelopp() {
        return dagsbelopp;
    }

    /**
     * Tolkar en nivåtext från Excel-filen. Tål omgivande blanksteg och
     * gemener/versaler (t.ex. " a " -> A).
     *
     * @throws IllegalArgumentException om värdet saknas eller är okänt
     */
    public static Niva fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Nivå saknas");
        }
        String normaliserad = value.trim().toUpperCase();
        switch (normaliserad) {
            case "A":
                return A;
            case "B":
                return B;
            case "C":
                return C;
            default:
                throw new IllegalArgumentException("Okänd nivå: " + value);
        }
    }
}
