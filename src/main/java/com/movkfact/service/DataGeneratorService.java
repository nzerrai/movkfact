package com.movkfact.service;

import com.movkfact.dto.GenerationRequestDTO;
import com.movkfact.dto.GenerationResponseDTO;

/**
 * Interface du service de génération de données.
 * Responsable de générer des données selon les configurations fournies.
 */
public interface DataGeneratorService {
    /**
     * Génère un ensemble de données selon les paramètres de requête.
     *
     * @param request Requête de génération contenant les colonnes à générer et le nombre de lignes
     * @return Réponse contenant les données générées et les métadonnées
     * @throws IllegalArgumentException si la requête est invalide
     */
    GenerationResponseDTO generate(GenerationRequestDTO request);
}
