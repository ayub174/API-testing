package com.apitesting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoanIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private int stockOf(long bookId, String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/books/" + bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("stock").asInt();
    }

    @Test
    void anvandareCanBorrowAndStockDecrements() throws Exception {
        String token = login("anvandare", "anvandare123");
        int before = stockOf(3, token);

        MvcResult result = mockMvc.perform(post("/api/loans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(3))
                .andExpect(jsonPath("$.username").value("anvandare"))
                .andExpect(jsonPath("$.dueDate").isNotEmpty())
                .andExpect(jsonPath("$.returned").value(false))
                .andReturn();

        assertEquals(before - 1, stockOf(3, token));

        // Återlämna och se att saldot återställs.
        long loanId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(post("/api/loans/" + loanId + "/return")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returned").value(true));
        assertEquals(before, stockOf(3, token));
    }

    @Test
    void borrowingUnavailableBookReturns409() throws Exception {
        String token = login("anvandare", "anvandare123");
        // Bok 4 ("1984") seedas med lagersaldo 0.
        mockMvc.perform(post("/api/loans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":4}"))
                .andExpect(status().isConflict());
    }

    @Test
    void anvandareCannotViewAllLoans() throws Exception {
        String token = login("anvandare", "anvandare123");
        mockMvc.perform(get("/api/loans").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void handlaggareCanViewAllAndBorrowForUser() throws Exception {
        String token = login("handlaggare", "handlaggare123");
        mockMvc.perform(get("/api/loans").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/loans/borrow-for")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":3,\"username\":\"anvandare\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("anvandare"));
    }

    @Test
    void anvandareCannotBorrowForOthers() throws Exception {
        String token = login("anvandare", "anvandare123");
        mockMvc.perform(post("/api/loans/borrow-for")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":3,\"username\":\"anvandare\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void selfRegistrationCreatesAnvandareAndRejectsDuplicate() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testlantagare\",\"password\":\"hemligt\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ANVANDARE"));

        // Det nya kontot kan logga in.
        String token = login("testlantagare", "hemligt");
        mockMvc.perform(get("/api/loans/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Samma användarnamn igen → 409.
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testlantagare\",\"password\":\"hemligt\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void onlyAdminCanCreateAccounts() throws Exception {
        String handlaggareToken = login("handlaggare", "handlaggare123");
        mockMvc.perform(post("/api/admin/accounts")
                        .header("Authorization", "Bearer " + handlaggareToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"x\",\"password\":\"hemligt\",\"role\":\"ANVANDARE\"}"))
                .andExpect(status().isForbidden());

        String adminToken = login("admin", "hemligt123");
        mockMvc.perform(post("/api/admin/accounts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nybiblio\",\"password\":\"hemligt\",\"role\":\"HANDLAGGARE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("HANDLAGGARE"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/admin/accounts/nybiblio")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
