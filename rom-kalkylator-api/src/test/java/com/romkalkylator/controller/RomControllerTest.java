package com.romkalkylator.controller;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private byte[] enkelExcel() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream ut = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Deltagare");
            sheet.createRow(0).createCell(0).setCellValue("Rubrik");
            Row rad = sheet.createRow(1);
            rad.createCell(3).setCellValue("Maria");
            rad.createCell(4).setCellValue("Asplund");
            rad.createCell(5).setCellValue("197403199321");
            rad.createCell(12).setCellValue("A");
            rad.createCell(14).setCellValue("2026-06-23");
            rad.createCell(15).setCellValue("2026-06-27");
            wb.write(ut);
            return ut.toByteArray();
        }
    }

    @Test
    void beraknarOchReturnerarResultat() throws Exception {
        MockMultipartFile fil = new MockMultipartFile(
                "file", "deltagare.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", enkelExcel());

        mockMvc.perform(multipart("/api/rom/berakna").file(fil).param("manad", "2026-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manad").value("2026-06"))
                .andExpect(jsonPath("$.antalDeltagare").value(1))
                .andExpect(jsonPath("$.totalBelopp").value(240))
                .andExpect(jsonPath("$.deltagare[0].ersattaDagar").value(4));
    }

    @Test
    void tomFilGer400() throws Exception {
        MockMultipartFile fil = new MockMultipartFile(
                "file", "tom.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        mockMvc.perform(multipart("/api/rom/berakna").file(fil))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ogiltigManadGer400() throws Exception {
        MockMultipartFile fil = new MockMultipartFile(
                "file", "deltagare.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", enkelExcel());

        mockMvc.perform(multipart("/api/rom/berakna").file(fil).param("manad", "juni"))
                .andExpect(status().isBadRequest());
    }
}
