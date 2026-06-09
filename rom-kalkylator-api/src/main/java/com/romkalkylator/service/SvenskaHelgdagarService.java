package com.romkalkylator.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Beräknar svenska officiella helgdagar (röda dagar) för ett givet år och
 * avgör om ett datum är en helgfri vardag (mån–fre som inte är röd dag).
 *
 * Helgaftnar (julafton, midsommarafton, nyårsafton) räknas INTE som röda dagar
 * enligt det beslutade kravet – endast officiella helgdagar.
 */
@Service
public class SvenskaHelgdagarService {

    private final Map<Integer, Set<LocalDate>> cachePerAr = new ConcurrentHashMap<>();

    /** Returnerar samtliga röda dagar för ett år (cachas per år). */
    public Set<LocalDate> helgdagarForAr(int ar) {
        return cachePerAr.computeIfAbsent(ar, this::beraknaHelgdagar);
    }

    /** True om datumet är en helgfri vardag: måndag–fredag och inte en röd dag. */
    public boolean arHelgfriVardag(LocalDate datum) {
        DayOfWeek veckodag = datum.getDayOfWeek();
        if (veckodag == DayOfWeek.SATURDAY || veckodag == DayOfWeek.SUNDAY) {
            return false;
        }
        return !helgdagarForAr(datum.getYear()).contains(datum);
    }

    private Set<LocalDate> beraknaHelgdagar(int ar) {
        Set<LocalDate> dagar = new HashSet<>();

        // Fasta helgdagar
        dagar.add(LocalDate.of(ar, 1, 1));    // Nyårsdagen
        dagar.add(LocalDate.of(ar, 1, 6));    // Trettondedag jul
        dagar.add(LocalDate.of(ar, 5, 1));    // Första maj
        dagar.add(LocalDate.of(ar, 6, 6));    // Sveriges nationaldag
        dagar.add(LocalDate.of(ar, 12, 25));  // Juldagen
        dagar.add(LocalDate.of(ar, 12, 26));  // Annandag jul

        // Rörliga helgdagar utifrån påskdagen
        LocalDate paskdagen = paskdagen(ar);
        dagar.add(paskdagen.minusDays(2));    // Långfredagen
        dagar.add(paskdagen);                 // Påskdagen
        dagar.add(paskdagen.plusDays(1));     // Annandag påsk
        dagar.add(paskdagen.plusDays(39));    // Kristi himmelsfärds dag
        dagar.add(paskdagen.plusDays(49));    // Pingstdagen

        // Midsommardagen: lördagen mellan 20 och 26 juni
        dagar.add(lordagIIntervall(LocalDate.of(ar, 6, 20), LocalDate.of(ar, 6, 26)));

        // Alla helgons dag: lördagen mellan 31 oktober och 6 november
        dagar.add(lordagIIntervall(LocalDate.of(ar, 10, 31), LocalDate.of(ar, 11, 6)));

        return dagar;
    }

    /**
     * Beräknar påskdagen (söndagen) för ett år med den anonyma gregorianska
     * algoritmen (Gauss/Meeus/Butcher).
     */
    private LocalDate paskdagen(int ar) {
        int a = ar % 19;
        int b = ar / 100;
        int c = ar % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int manad = (h + l - 7 * m + 114) / 31;
        int dag = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(ar, manad, dag);
    }

    /** Hittar lördagen inom ett (kort) datumintervall. */
    private LocalDate lordagIIntervall(LocalDate fran, LocalDate till) {
        LocalDate d = fran;
        while (!d.isAfter(till)) {
            if (d.getDayOfWeek() == DayOfWeek.SATURDAY) {
                return d;
            }
            d = d.plusDays(1);
        }
        throw new IllegalStateException("Ingen lördag i intervallet " + fran + " – " + till);
    }
}
