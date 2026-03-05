package com.movkfact.service;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.dto.PreviewRequestDTO;
import com.movkfact.dto.PreviewResponseDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import com.movkfact.service.generator.GeneratorFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de prévisualisation de génération de données (S7.1).
 * Génère jusqu'à 5 lignes sans persistance en base.
 */
@Service
public class DataPreviewService {

    /**
     * Génère un aperçu de données à partir d'une configuration de colonnes.
     * N'interagit pas avec la base de données.
     *
     * @param request la configuration des colonnes et le nombre de lignes (max 5)
     * @return les lignes générées et le nombre de colonnes
     */
    public PreviewResponseDTO generatePreview(PreviewRequestDTO request) {
        validateConstraints(request.getColumns());

        int count = Math.min(request.getCount() > 0 ? request.getCount() : 5, 5);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (ColumnConfigDTO col : request.getColumns()) {
                DataTypeGenerator gen = GeneratorFactory.createGenerator(col);
                row.put(col.getName(), gen.generate());
            }
            rows.add(row);
        }

        return new PreviewResponseDTO(rows, request.getColumns().size());
    }

    private void validateConstraints(List<ColumnConfigDTO> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Au moins une colonne est requise");
        }
        for (ColumnConfigDTO col : columns) {
            Map<String, Object> c = col.getConstraints();
            if (c == null) continue;

            if (c.get("min") != null && c.get("max") != null) {
                double min = ((Number) c.get("min")).doubleValue();
                double max = ((Number) c.get("max")).doubleValue();
                if (min > max) {
                    throw new IllegalArgumentException(
                        "Contrainte invalide pour '" + col.getName()
                        + "' : min (" + min + ") doit être <= max (" + max + ")"
                    );
                }
            }

            if (c.get("dateFrom") != null && c.get("dateTo") != null) {
                LocalDate from;
                LocalDate to;
                try {
                    from = LocalDate.parse((String) c.get("dateFrom"));
                    to   = LocalDate.parse((String) c.get("dateTo"));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException(
                        "Contrainte invalide pour '" + col.getName()
                        + "' : dateFrom/dateTo doivent être au format YYYY-MM-DD"
                    );
                }
                if (from.isAfter(to)) {
                    throw new IllegalArgumentException(
                        "Contrainte invalide pour '" + col.getName()
                        + "' : dateFrom doit être <= dateTo"
                    );
                }
            }
        }
    }
}
