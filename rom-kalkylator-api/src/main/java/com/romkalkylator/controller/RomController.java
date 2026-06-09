package com.romkalkylator.controller;

import com.romkalkylator.exception.OgiltigFilException;
import com.romkalkylator.model.RomResultat;
import com.romkalkylator.service.RomKalkylatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/rom")
public class RomController {

    private final RomKalkylatorService kalkylatorService;

    public RomController(RomKalkylatorService kalkylatorService) {
        this.kalkylatorService = kalkylatorService;
    }

    /**
     * Läser in en deltagar-Excel och räknar ut dagsersättningen för en månad.
     *
     * @param file  uppladdad .xlsx-fil
     * @param manad målmånad i formatet YYYY-MM (valfri; standard = innevarande månad)
     */
    @PostMapping("/berakna")
    public ResponseEntity<RomResultat> berakna(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "manad", required = false) String manad) {

        if (file == null || file.isEmpty()) {
            throw new OgiltigFilException("Ingen fil bifogades.");
        }
        String filnamn = file.getOriginalFilename();
        if (filnamn != null && !filnamn.toLowerCase().endsWith(".xlsx")) {
            throw new OgiltigFilException("Endast .xlsx-filer stöds.");
        }

        YearMonth malmanad = tolkaManad(manad);

        try {
            RomResultat resultat = kalkylatorService.berakna(file.getInputStream(), malmanad);
            return ResponseEntity.ok(resultat);
        } catch (IOException e) {
            throw new OgiltigFilException("Kunde inte läsa den uppladdade filen.", e);
        }
    }

    private YearMonth tolkaManad(String manad) {
        if (manad == null || manad.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(manad.trim());
        } catch (DateTimeParseException e) {
            throw new OgiltigFilException("Ogiltig månad: \"" + manad + "\". Använd formatet YYYY-MM.");
        }
    }
}
