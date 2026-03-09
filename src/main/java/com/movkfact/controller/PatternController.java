package com.movkfact.controller;

import com.movkfact.entity.DetectionPattern;
import com.movkfact.service.DetectionPatternService;
import com.movkfact.service.detection.PatternCache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API REST de gestion des patterns de détection de types (S10.2).
 *
 * <p>Endpoints :</p>
 * <ul>
 *   <li>{@code GET    /api/settings/patterns}         — liste tous les patterns</li>
 *   <li>{@code GET    /api/settings/patterns/types}   — liste les ColumnType distincts</li>
 *   <li>{@code POST   /api/settings/patterns}         — crée un pattern</li>
 *   <li>{@code PUT    /api/settings/patterns/{id}}    — modifie un pattern</li>
 *   <li>{@code DELETE /api/settings/patterns/{id}}    — supprime un pattern</li>
 *   <li>{@code POST   /api/settings/patterns/reload}  — recharge le cache en mémoire</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/settings/patterns")
public class PatternController {

    private final DetectionPatternService service;
    private final PatternCache patternCache;

    public PatternController(DetectionPatternService service, PatternCache patternCache) {
        this.service = service;
        this.patternCache = patternCache;
    }

    @GetMapping
    public ResponseEntity<List<DetectionPattern>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getTypes() {
        List<String> types = java.util.Arrays.stream(com.movkfact.enums.ColumnType.values())
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(types);
    }

    @PostMapping
    public ResponseEntity<DetectionPattern> create(@RequestBody DetectionPattern dto) {
        DetectionPattern created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetectionPattern> update(@PathVariable Long id,
                                                   @RequestBody DetectionPattern dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reload")
    public ResponseEntity<Map<String, Object>> reload() {
        service.reload();
        return ResponseEntity.ok(Map.of(
                "reloaded", true,
                "types", patternCache.getActiveTypeCount()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRegex(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
