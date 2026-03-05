package com.movkfact.controller;

import com.movkfact.repository.BankingLexiconRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lexicon")
public class LexiconController {

    private final BankingLexiconRepository repository;

    public LexiconController(BankingLexiconRepository repository) {
        this.repository = repository;
    }

    /**
     * GET /api/lexicon/banking
     * Retourne les entrées du lexique bancaire triées par groupe puis par libellé.
     * Utilisé par le wizard de création manuelle pour l'autocomplete des noms de colonnes.
     */
    @GetMapping("/banking")
    public ResponseEntity<List<Map<String, String>>> getBankingLexicon() {
        List<Map<String, String>> entries = repository.findAllByOrderByLexiconGroupAscLabelAsc()
                .stream()
                .map(e -> Map.of(
                        "label",         e.getLabel(),
                        "type",          e.getSuggestedType(),
                        "group",         e.getLexiconGroup()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(entries);
    }
}
