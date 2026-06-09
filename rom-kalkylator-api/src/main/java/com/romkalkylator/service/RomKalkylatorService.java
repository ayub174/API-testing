package com.romkalkylator.service;

import com.romkalkylator.exception.OgiltigFilException;
import com.romkalkylator.model.DeltagareErsattning;
import com.romkalkylator.model.Niva;
import com.romkalkylator.model.RomResultat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

/**
 * Läser in en deltagar-Excel och räknar ut dagsersättningen per deltagare för
 * en given månad. Endast helgfria vardagar (mån–fre exkl. svenska röda dagar)
 * inom deltagarens start–slutperiod ersätts.
 */
@Service
public class RomKalkylatorService {

    // Kolumnindex (0-baserade) enligt exportens layout
    private static final int KOL_FORNAMN = 3;       // D
    private static final int KOL_EFTERNAMN = 4;      // E
    private static final int KOL_PERSONNUMMER = 5;   // F
    private static final int KOL_NIVA = 12;          // M
    private static final int KOL_STARTDATUM = 14;    // O
    private static final int KOL_SLUTDATUM = 15;     // P

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private final SvenskaHelgdagarService helgdagarService;

    public RomKalkylatorService(SvenskaHelgdagarService helgdagarService) {
        this.helgdagarService = helgdagarService;
    }

    public RomResultat berakna(InputStream excel, YearMonth manad) {
        RomResultat resultat = new RomResultat();
        resultat.setManad(manad.toString());

        LocalDate manadensForsta = manad.atDay(1);
        LocalDate manadensSista = manad.atEndOfMonth();

        try (Workbook workbook = new XSSFWorkbook(excel)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rader = sheet.iterator();

            // Hoppa över rubrikraden
            if (rader.hasNext()) {
                rader.next();
            }

            while (rader.hasNext()) {
                Row rad = rader.next();
                if (arTomRad(rad)) {
                    continue;
                }
                behandlaRad(rad, manadensForsta, manadensSista, resultat);
            }
        } catch (OgiltigFilException e) {
            throw e;
        } catch (Exception e) {
            throw new OgiltigFilException("Kunde inte läsa Excel-filen: " + e.getMessage(), e);
        }

        return resultat;
    }

    private void behandlaRad(Row rad, LocalDate manadensForsta, LocalDate manadensSista,
                             RomResultat resultat) {
        int radnr = rad.getRowNum() + 1; // 1-baserat för läsbara meddelanden
        String fornamn = lasText(rad, KOL_FORNAMN);
        String efternamn = lasText(rad, KOL_EFTERNAMN);
        String personnummer = lasText(rad, KOL_PERSONNUMMER);

        LocalDate start;
        LocalDate slut;
        Niva niva;
        try {
            start = lasDatum(rad, KOL_STARTDATUM);
            slut = lasDatum(rad, KOL_SLUTDATUM);
            niva = Niva.fromString(lasText(rad, KOL_NIVA));
        } catch (RuntimeException e) {
            resultat.getVarningar().add("Rad " + radnr + " hoppades över: " + e.getMessage());
            return;
        }

        if (start == null || slut == null) {
            resultat.getVarningar().add("Rad " + radnr + " hoppades över: start- eller slutdatum saknas");
            return;
        }

        DeltagareErsattning deltagare = new DeltagareErsattning();
        deltagare.setFornamn(fornamn);
        deltagare.setEfternamn(efternamn);
        deltagare.setPersonnummer(personnummer);
        deltagare.setNiva(niva.name());
        deltagare.setDagsbelopp(niva.getDagsbelopp());

        int ersattaDagar = raknaErsattaDagar(start, slut, manadensForsta, manadensSista);
        int belopp = ersattaDagar * niva.getDagsbelopp();
        deltagare.setErsattaDagar(ersattaDagar);
        deltagare.setBelopp(belopp);
        if (ersattaDagar == 0) {
            deltagare.setKommentar("Ingen ersatt dag i vald månad");
        }

        resultat.getDeltagare().add(deltagare);
        resultat.setAntalDeltagare(resultat.getAntalDeltagare() + 1);
        resultat.setTotalErsattaDagar(resultat.getTotalErsattaDagar() + ersattaDagar);
        resultat.setTotalBelopp(resultat.getTotalBelopp() + belopp);
    }

    /**
     * Antal helgfria vardagar inom överlappet mellan deltagarens period
     * [start, slut] och målmånaden [manadensForsta, manadensSista]. Båda
     * gränserna är inklusive.
     */
    private int raknaErsattaDagar(LocalDate start, LocalDate slut,
                                  LocalDate manadensForsta, LocalDate manadensSista) {
        LocalDate effektivStart = start.isAfter(manadensForsta) ? start : manadensForsta;
        LocalDate effektivSlut = slut.isBefore(manadensSista) ? slut : manadensSista;
        if (effektivStart.isAfter(effektivSlut)) {
            return 0;
        }

        int antal = 0;
        for (LocalDate d = effektivStart; !d.isAfter(effektivSlut); d = d.plusDays(1)) {
            if (helgdagarService.arHelgfriVardag(d)) {
                antal++;
            }
        }
        return antal;
    }

    private boolean arTomRad(Row rad) {
        if (rad == null) {
            return true;
        }
        String niva = lasText(rad, KOL_NIVA);
        String start = lasText(rad, KOL_STARTDATUM);
        String namn = lasText(rad, KOL_FORNAMN);
        return niva.isEmpty() && start.isEmpty() && namn.isEmpty();
    }

    private String lasText(Row rad, int kolumn) {
        Cell cell = rad.getCell(kolumn);
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().format(ISO);
                }
                // Undvik decimaler för heltal (t.ex. personnummer som tal)
                double v = cell.getNumericCellValue();
                if (v == Math.floor(v)) {
                    return String.valueOf((long) v);
                }
                return String.valueOf(v);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private LocalDate lasDatum(Row rad, int kolumn) {
        Cell cell = rad.getCell(kolumn);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String text = lasText(rad, kolumn);
        if (text.isEmpty()) {
            return null;
        }
        try {
            // Stöd för ISO-datum, ev. med tidsdel
            if (text.length() > 10) {
                text = text.substring(0, 10);
            }
            return LocalDate.parse(text, ISO);
        } catch (Exception e) {
            throw new IllegalArgumentException("ogiltigt datum \"" + text + "\"");
        }
    }
}
