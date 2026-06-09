package com.romkalkylator.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SvenskaHelgdagarServiceTest {

    private final SvenskaHelgdagarService service = new SvenskaHelgdagarService();

    @Test
    void paskdagen2026ArFemteApril() {
        Set<LocalDate> helgdagar = service.helgdagarForAr(2026);
        assertTrue(helgdagar.contains(LocalDate.of(2026, 4, 5)), "Påskdagen 2026 = 5 april");
        assertTrue(helgdagar.contains(LocalDate.of(2026, 4, 3)), "Långfredagen 2026 = 3 april");
        assertTrue(helgdagar.contains(LocalDate.of(2026, 4, 6)), "Annandag påsk 2026 = 6 april");
        assertTrue(helgdagar.contains(LocalDate.of(2026, 5, 14)), "Kristi himmelsfärd 2026 = 14 maj");
    }

    @Test
    void fastaHelgdagarFinns() {
        Set<LocalDate> helgdagar = service.helgdagarForAr(2026);
        assertTrue(helgdagar.contains(LocalDate.of(2026, 1, 1)), "Nyårsdagen");
        assertTrue(helgdagar.contains(LocalDate.of(2026, 1, 6)), "Trettondedag jul");
        assertTrue(helgdagar.contains(LocalDate.of(2026, 5, 1)), "Första maj");
        assertTrue(helgdagar.contains(LocalDate.of(2026, 6, 6)), "Nationaldagen");
        assertTrue(helgdagar.contains(LocalDate.of(2026, 12, 25)), "Juldagen");
        assertTrue(helgdagar.contains(LocalDate.of(2026, 12, 26)), "Annandag jul");
    }

    @Test
    void midsommardagen2026ArTjugondeJuni() {
        Set<LocalDate> helgdagar = service.helgdagarForAr(2026);
        assertTrue(helgdagar.contains(LocalDate.of(2026, 6, 20)), "Midsommardagen 2026 = 20 juni (lördag)");
    }

    @Test
    void helgaftnarRaknasInteSomRodaDagar() {
        Set<LocalDate> helgdagar = service.helgdagarForAr(2026);
        assertFalse(helgdagar.contains(LocalDate.of(2026, 12, 24)), "Julafton ska inte vara röd dag");
        assertFalse(helgdagar.contains(LocalDate.of(2026, 6, 19)), "Midsommarafton ska inte vara röd dag");
        assertFalse(helgdagar.contains(LocalDate.of(2026, 12, 31)), "Nyårsafton ska inte vara röd dag");
    }

    @Test
    void arHelgfriVardagSkiljerPaVardagHelgOchRodDag() {
        // Tisdag 23 juni 2026 – helgfri vardag
        assertTrue(service.arHelgfriVardag(LocalDate.of(2026, 6, 23)));
        // Lördag 20 juni 2026 – helg
        assertFalse(service.arHelgfriVardag(LocalDate.of(2026, 6, 20)));
        // Nationaldagen (lördag dessutom) – röd dag
        assertFalse(service.arHelgfriVardag(LocalDate.of(2026, 6, 6)));
        // Första maj 2026 (fredag) – röd dag
        assertFalse(service.arHelgfriVardag(LocalDate.of(2026, 5, 1)));
    }

    @Test
    void antalHelgdagarPpAr() {
        // 13 definierade helgdagar (vissa kan sammanfalla men gör det inte 2026)
        assertEquals(13, service.helgdagarForAr(2026).size());
    }
}
