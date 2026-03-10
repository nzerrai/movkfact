package com.movkfact.service.detection;

import com.movkfact.dto.ColumnFeedbackRequest;
import com.movkfact.dto.InferenceResult;
import com.movkfact.entity.ColumnTypeFeedback;
import com.movkfact.enums.ColumnType;
import com.movkfact.enums.InferenceLevel;
import com.movkfact.repository.ColumnTypeFeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service d'apprentissage adaptatif (S10.1) — Niveau 0 du pipeline de détection.
 *
 * <p><b>Responsabilités :</b></p>
 * <ul>
 *   <li>{@link #normalize(String)} : normalise un nom de colonne en slug (ex : "Salaire Espéré" → "salaire-espere")</li>
 *   <li>{@link #recordFeedback(List, Long)} : enregistre les mappings validés par l'utilisateur</li>
 *   <li>{@link #lookup(String, Long)} : retrouve le meilleur type appris — domain-first puis fallback global (S10.2)</li>
 * </ul>
 *
 * <p><b>Confiance calculée :</b> {@code min(95, 80 + 5 * count)}, plafonnée à 95
 * pour permettre aux corrections humaines futures de prendre la main.</p>
 *
 * <p><b>Seuil de fiabilité :</b> configurable via {@code detection.learning.min-count} (défaut 3).
 * En dessous du seuil, le mapping n'est pas retourné au Niveau 0.</p>
 */
@Service
public class ColumnLearningService {

    private static final Logger logger = LoggerFactory.getLogger(ColumnLearningService.class);

    @Value("${detection.learning.min-count:3}")
    private int minCount;

    @Autowired
    private ColumnTypeFeedbackRepository feedbackRepository;

    /**
     * Normalise un nom de colonne en slug ASCII.
     * <ul>
     *   <li>Lowercase</li>
     *   <li>Suppression des accents (NFD + strip diacritiques)</li>
     *   <li>Espaces / underscores / tirets → tiret simple</li>
     *   <li>Caractères non alphanumériques restants supprimés</li>
     * </ul>
     *
     * @param columnName Nom brut de la colonne (peut être null)
     * @return Slug normalisé, chaîne vide si l'entrée est null/vide
     */
    public String normalize(String columnName) {
        if (columnName == null || columnName.isBlank()) return "";
        String lower = columnName.trim().toLowerCase();
        String withoutAccents = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return withoutAccents
                .replaceAll("[\\s_\\-]+", "-")
                .replaceAll("[^a-z0-9\\-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Recherche le meilleur mapping appris pour un nom normalisé.
     *
     * <p>Stratégie V2 — domain-first, fallback global (S10.2) :</p>
     * <ol>
     *   <li>Si {@code domainId} non null : cherche d'abord dans le corpus du domaine</li>
     *   <li>Si aucun résultat domaine : fallback sur le corpus global (domain_id IS NULL)</li>
     * </ol>
     * <p>Passer {@code domainId = null} reproduit exactement le comportement V1 (global uniquement).</p>
     *
     * @param normalizedName Nom de colonne déjà normalisé via {@link #normalize(String)}
     * @param domainId       Identifiant du domaine courant, ou {@code null} pour le corpus global seul
     * @return {@link InferenceResult} avec niveau {@code LEARNED}, ou {@link Optional#empty()} si inconnu / sous le seuil
     */
    public Optional<InferenceResult> lookup(String normalizedName, Long domainId) {
        if (normalizedName == null || normalizedName.isBlank()) return Optional.empty();

        // 1. Lookup domaine-spécifique si domainId fourni
        if (domainId != null) {
            List<ColumnTypeFeedback> domainCandidates = feedbackRepository.findByNameAndDomain(normalizedName, domainId);
            Optional<InferenceResult> domainResult = resolveResult(normalizedName, domainCandidates);
            if (domainResult.isPresent()) {
                logger.debug("ColumnLearningService: '{}' → {} (LEARNED domain={}, count={})",
                        normalizedName, domainResult.get().getType(), domainId, domainResult.get().getLearnedCount());
                return domainResult;
            }
        }

        // 2. Fallback global (domain_id IS NULL)
        List<ColumnTypeFeedback> globalCandidates = feedbackRepository.findGlobal(normalizedName);
        Optional<InferenceResult> globalResult = resolveResult(normalizedName, globalCandidates);
        globalResult.ifPresent(r -> logger.debug("ColumnLearningService: '{}' → {} (LEARNED global, count={})",
                normalizedName, r.getType(), r.getLearnedCount()));
        return globalResult;
    }

    /**
     * Résout le meilleur candidat depuis une liste de feedback.
     * Trie par count DESC de façon autonome — ne dépend pas de l'ordre du repository.
     */
    private Optional<InferenceResult> resolveResult(String normalizedName, List<ColumnTypeFeedback> candidates) {
        if (candidates.isEmpty()) return Optional.empty();

        List<ColumnTypeFeedback> sorted = candidates.stream()
                .sorted(Comparator.comparingInt(ColumnTypeFeedback::getCount).reversed())
                .collect(Collectors.toList());

        ColumnTypeFeedback best = sorted.get(0);

        if (best.getCount() < minCount) {
            logger.debug("ColumnLearningService: '{}' count={} < min-count={}, skipping",
                    normalizedName, best.getCount(), minCount);
            return Optional.empty();
        }

        // Tie check : deuxième candidat aussi fort → ambiguïté → pas de promotion
        if (sorted.size() > 1 && sorted.get(1).getCount() >= best.getCount()) {
            logger.debug("ColumnLearningService: '{}' ambiguous (tie), skipping", normalizedName);
            return Optional.empty();
        }

        ColumnType type;
        try {
            type = ColumnType.valueOf(best.getValidatedType());
        } catch (IllegalArgumentException e) {
            logger.warn("ColumnLearningService: unknown type '{}' in corpus, ignoring", best.getValidatedType());
            return Optional.empty();
        }

        double confidence = Math.min(95.0, 80.0 + 5.0 * best.getCount());
        return Optional.of(new InferenceResult(type, confidence, InferenceLevel.LEARNED, best.getCount()));
    }

    /**
     * Enregistre le feedback utilisateur pour une liste de colonnes.
     *
     * <p>Pour chaque entrée : upsert dans {@code column_type_feedback}
     * (insert ou incrément du count si le mapping existe déjà).
     * Seules les entrées avec un {@code validatedType} non null sont traitées.</p>
     *
     * @param feedbacks Liste des feedbacks de colonnes
     * @param domainId  Identifiant du domaine (peut être null pour un mapping global)
     */
    @Transactional
    public void recordFeedback(List<ColumnFeedbackRequest> feedbacks, Long domainId) {
        if (feedbacks == null || feedbacks.isEmpty()) return;

        for (ColumnFeedbackRequest feedback : feedbacks) {
            if (feedback.getValidatedType() == null || feedback.getValidatedType().isBlank()) continue;
            if (feedback.getColName() == null || feedback.getColName().isBlank()) continue;

            String normalized = normalize(feedback.getColName());
            if (normalized.isBlank()) continue;

            // Stocker globalement (domain_id = null) pour que le lookup V1 fonctionne
            upsert(normalized, feedback.getValidatedType(), null);

            // Stocker aussi par domaine si fourni (pour V2 scope-domaine)
            if (domainId != null) {
                upsert(normalized, feedback.getValidatedType(), domainId);
            }

            logger.info("FEEDBACK_RECORDED | column={} (normalized: {}) | detected={} | validated={} | domain={}",
                    feedback.getColName(), normalized,
                    feedback.getDetectedType(), feedback.getValidatedType(), domainId);
        }
    }

    private void upsert(String normalized, String validatedType, Long domainId) {
        Optional<ColumnTypeFeedback> existing =
                feedbackRepository.findByColumnNameNormalizedAndValidatedTypeAndDomainId(
                        normalized, validatedType, domainId);

        if (existing.isPresent()) {
            existing.get().incrementCount();
            feedbackRepository.save(existing.get());
        } else {
            feedbackRepository.save(new ColumnTypeFeedback(normalized, validatedType, domainId));
        }
    }
}
