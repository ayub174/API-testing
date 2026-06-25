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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

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
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    @Test
    void loginAsAdminReturnsTokenAndRole() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"hemligt123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.permissions").isArray());
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"fel\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void booksRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void handlaggareCannotDeleteBook() throws Exception {
        String token = login("handlaggare", "handlaggare123");
        mockMvc.perform(delete("/api/books/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadAndManage() throws Exception {
        String token = login("admin", "hemligt123");
        mockMvc.perform(get("/api/books").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/accounts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void handlaggareCannotAccessAdmin() throws Exception {
        String token = login("handlaggare", "handlaggare123");
        mockMvc.perform(get("/api/admin/accounts").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void grantingPermissionTakesEffectImmediately() throws Exception {
        String adminToken = login("admin", "hemligt123");
        String hlToken = login("handlaggare", "handlaggare123");

        // Innan: handläggare kan inte ta bort.
        mockMvc.perform(delete("/api/books/5").header("Authorization", "Bearer " + hlToken))
                .andExpect(status().isForbidden());

        // Admin ger BOOK_DELETE.
        mockMvc.perform(post("/api/admin/accounts/handlaggare/permissions/BOOK_DELETE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Efter: samma gamla token fungerar nu (live-uppslag).
        MvcResult result = mockMvc.perform(delete("/api/books/5")
                        .header("Authorization", "Bearer " + hlToken))
                .andReturn();
        assertEquals(204, result.getResponse().getStatus());

        // Städa upp så testordning inte påverkar andra test.
        mockMvc.perform(delete("/api/admin/accounts/handlaggare/permissions/BOOK_DELETE")
                .header("Authorization", "Bearer " + adminToken));
    }
}
