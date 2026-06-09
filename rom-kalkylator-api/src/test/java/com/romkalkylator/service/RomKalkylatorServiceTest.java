package com.romkalkylator.service;

import com.romkalkylator.model.DeltagareErsattning;
import com.romkalkylator.model.RomResultat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RomKalkylatorServiceTest {

    private final RomKalkylatorService service =
            new RomKalkylatorService(new SvenskaHelgdagarService());

    /** Bygger en xlsx i minnet med rubrikrad och givna datarader. */
    private byte[] byggExcel(String[][] rader) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream ut = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Deltagare");
            // Rubrikrad (innehållet spelar ingen roll, bara att den hoppas över)
            Row rubrik = sheet.createRow(0);
            rubrik.createCell(0).setCellValue("Rubrik");
            int radnr = 1;
            for (String[] r : rader) {
                Row rad = sheet.createRow(radnr++);
                rad.createCell(3).setCellValue(r[0]);   // D Förnamn
                rad.createCell(4).setCellValue(r[1]);   // E Efternamn
                rad.createCell(5).setCellValue(r[2]);   // F Personnummer
                rad.createCell(12).setCellValue(r[3]);  // M Nivå
                rad.createCell(14).setCellValue(r[4]);  // O Startdatum
                rad.createCell(15).setCellValue(r[5]);  // P Slutdatum
            }
            wb.write(ut);
            return ut.toByteArray();
        }
    }

    @Test
    void nivaAGerRattAntalDagarOchBelopp() throws Exception {
        // 23–27 juni 2026: 23 ti, 24 on, 25 to, 26 fr (4 helgfria vardagar), 27 lö
        byte[] excel = byggExcel(new String[][]{
                {"Maria", "Asplund", "197403199321", "A", "2026-06-23", "2026-06-27"}
        });

        RomResultat resultat = service.berakna(new ByteArrayInputStream(excel), YearMonth.of(2026, 6));

        assertEquals(1, resultat.getAntalDeltagare());
        DeltagareErsattning d = resultat.getDeltagare().get(0);
        assertEquals(4, d.getErsattaDagar());
        assertEquals(240, d.getBelopp());
        assertEquals(240, resultat.getTotalBelopp());
        assertEquals(4, resultat.getTotalErsattaDagar());
    }

    @Test
    void nivaerSummerasOchTotalBlirRatt() throws Exception {
        byte[] excel = byggExcel(new String[][]{
                {"Maria", "Asplund", "1", "A", "2026-06-23", "2026-06-27"},   // 4 dagar * 60 = 240
                {"Khalid", "Ais", "2", "B", "2026-06-23", "2026-06-27"},      // 4 dagar * 80 = 320
                {"Karl", "Berglund", "3", "C", "2026-06-23", "2026-06-27"}    // 4 dagar * 90 = 360
        });

        RomResultat resultat = service.berakna(new ByteArrayInputStream(excel), YearMonth.of(2026, 6));

        assertEquals(3, resultat.getAntalDeltagare());
        assertEquals(12, resultat.getTotalErsattaDagar());
        assertEquals(920, resultat.getTotalBelopp());
    }

    @Test
    void heladJuni2026HarTjugotvaHelgfriaVardagar() throws Exception {
        // Hela juni 2026. Nationaldagen 6/6 (lördag) påverkar inte vardagsräkningen.
        byte[] excel = byggExcel(new String[][]{
                {"Hel", "Manad", "1", "A", "2026-06-01", "2026-06-30"}
        });

        RomResultat resultat = service.berakna(new ByteArrayInputStream(excel), YearMonth.of(2026, 6));

        assertEquals(22, resultat.getDeltagare().get(0).getErsattaDagar());
    }

    @Test
    void periodUtanforManadGerNollDagar() throws Exception {
        byte[] excel = byggExcel(new String[][]{
                {"Framtid", "Person", "1", "A", "2026-08-01", "2026-08-31"}
        });

        RomResultat resultat = service.berakna(new ByteArrayInputStream(excel), YearMonth.of(2026, 6));

        assertEquals(1, resultat.getAntalDeltagare());
        assertEquals(0, resultat.getDeltagare().get(0).getErsattaDagar());
        assertEquals(0, resultat.getTotalBelopp());
    }

    @Test
    void ogiltigNivaGerVarningOchHoppasOver() throws Exception {
        byte[] excel = byggExcel(new String[][]{
                {"Fel", "Niva", "1", "X", "2026-06-23", "2026-06-27"}
        });

        RomResultat resultat = service.berakna(new ByteArrayInputStream(excel), YearMonth.of(2026, 6));

        assertEquals(0, resultat.getAntalDeltagare());
        assertEquals(1, resultat.getVarningar().size());
    }

    @Test
    void deltagareSomBorjarMittIManadenRaknasFranStartdatum() throws Exception {
        // Start 2026-06-24 (onsdag), slut långt fram. Juni: 24,25,26 (29,30) helgfria.
        byte[] excel = byggExcel(new String[][]{
                {"Sen", "Start", "1", "C", "2026-06-24", "2026-12-20"}
        });

        RomResultat resultat = service.berakna(new ByteArrayInputStream(excel), YearMonth.of(2026, 6));

        // 24 on, 25 to, 26 fr, 29 må, 30 ti = 5 helgfria vardagar
        assertEquals(5, resultat.getDeltagare().get(0).getErsattaDagar());
        assertEquals(450, resultat.getDeltagare().get(0).getBelopp());
    }
}
