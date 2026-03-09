package com.movkfact.service;

import com.movkfact.entity.DetectionPattern;
import com.movkfact.repository.DetectionPatternRepository;
import com.movkfact.service.detection.PatternCache;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * Service CRUD pour les patterns de détection de types (S10.2).
 *
 * <p>Chaque modification (create/update/delete) déclenche un {@link PatternCache#reload()}
 * pour que les changements soient immédiatement actifs sans redémarrage.</p>
 */
@Service
public class DetectionPatternService {

    private final DetectionPatternRepository repository;
    private final PatternCache patternCache;

    public DetectionPatternService(DetectionPatternRepository repository,
                                   PatternCache patternCache) {
        this.repository = repository;
        this.patternCache = patternCache;
    }

    public List<DetectionPattern> findAll() {
        return repository.findAll();
    }

    /**
     * Crée un nouveau pattern après validation de la regex.
     *
     * @throws IllegalArgumentException si le pattern n'est pas une regex valide
     */
    public DetectionPattern create(DetectionPattern dto) {
        validateRegex(dto.getPattern());
        DetectionPattern saved = repository.save(dto);
        patternCache.reload();
        return saved;
    }

    /**
     * Met à jour un pattern existant après validation de la regex.
     *
     * @throws ResponseStatusException 404 si l'id n'existe pas
     * @throws IllegalArgumentException si le pattern n'est pas une regex valide
     */
    public DetectionPattern update(Long id, DetectionPattern dto) {
        DetectionPattern existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pattern introuvable : id=" + id));
        validateRegex(dto.getPattern());
        existing.setColumnType(dto.getColumnType());
        existing.setPattern(dto.getPattern());
        existing.setDescription(dto.getDescription());
        DetectionPattern saved = repository.save(existing);
        patternCache.reload();
        return saved;
    }

    /**
     * Supprime un pattern existant.
     *
     * @throws ResponseStatusException 404 si l'id n'existe pas
     */
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Pattern introuvable : id=" + id);
        }
        repository.deleteById(id);
        patternCache.reload();
    }

    /**
     * Recharge le cache sans modifier la BDD.
     */
    public void reload() {
        patternCache.reload();
    }

    private void validateRegex(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            throw new IllegalArgumentException("Le pattern ne peut pas être vide");
        }
        try {
            java.util.regex.Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Regex invalide : " + e.getMessage());
        }
    }
}
