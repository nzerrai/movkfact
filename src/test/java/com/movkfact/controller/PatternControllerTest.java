package com.movkfact.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movkfact.entity.DetectionPattern;
import com.movkfact.repository.DetectionPatternRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests REST pour PatternController (S10.2).
 *
 * Couvre : GET all, GET types, POST (happy path + regex invalide),
 * PUT, DELETE (204 + 404), POST /reload.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PatternControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DetectionPatternRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private DetectionPattern saved;

    @BeforeEach
    void setUp() {
        saved = repository.save(new DetectionPattern("TEXT", "(?i)^ctrl_test$", "test controller"));
    }

    // ── GET /api/settings/patterns ────────────────────────────────────────────

    @Test
    void get_all_returns_200_and_list() throws Exception {
        mockMvc.perform(get("/api/settings/patterns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", greaterThan(0)));
    }

    // ── GET /api/settings/patterns/types ─────────────────────────────────────

    @Test
    void get_types_returns_all_column_type_names() throws Exception {
        mockMvc.perform(get("/api/settings/patterns/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem("FIRST_NAME")))
                .andExpect(jsonPath("$", hasItem("URL")))
                .andExpect(jsonPath("$", hasItem("COMPANY")));
    }

    // ── POST /api/settings/patterns — happy path ──────────────────────────────

    @Test
    void post_valid_pattern_returns_201() throws Exception {
        DetectionPattern dto = new DetectionPattern("EMAIL", "(?i)^courriel$", "FR courriel");
        mockMvc.perform(post("/api/settings/patterns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.columnType", is("EMAIL")))
                .andExpect(jsonPath("$.pattern", is("(?i)^courriel$")));
    }

    // AC4 — regex invalide → 400
    @Test
    void post_invalid_regex_returns_400() throws Exception {
        DetectionPattern dto = new DetectionPattern("EMAIL", "[invalid", "mauvaise regex");
        mockMvc.perform(post("/api/settings/patterns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Regex invalide")));
    }

    @Test
    void post_empty_pattern_returns_400() throws Exception {
        DetectionPattern dto = new DetectionPattern("EMAIL", "", "vide");
        mockMvc.perform(post("/api/settings/patterns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    // ── PUT /api/settings/patterns/{id} ──────────────────────────────────────

    @Test
    void put_existing_pattern_returns_200() throws Exception {
        DetectionPattern update = new DetectionPattern("TEXT", "(?i)^ctrl_updated$", "modifié");
        mockMvc.perform(put("/api/settings/patterns/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pattern", is("(?i)^ctrl_updated$")));
    }

    @Test
    void put_unknown_id_returns_404() throws Exception {
        DetectionPattern update = new DetectionPattern("TEXT", "(?i)^test$", "test");
        mockMvc.perform(put("/api/settings/patterns/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    // AC5 — DELETE → 204
    @Test
    void delete_existing_pattern_returns_204() throws Exception {
        mockMvc.perform(delete("/api/settings/patterns/" + saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_unknown_id_returns_404() throws Exception {
        mockMvc.perform(delete("/api/settings/patterns/999999"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/settings/patterns/reload ───────────────────────────────────

    @Test
    void reload_returns_200_with_type_count() throws Exception {
        mockMvc.perform(post("/api/settings/patterns/reload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reloaded", is(true)))
                .andExpect(jsonPath("$.types", greaterThan(0)));
    }
}
