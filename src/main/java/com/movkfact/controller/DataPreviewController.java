package com.movkfact.controller;

import com.movkfact.dto.PreviewRequestDTO;
import com.movkfact.dto.PreviewResponseDTO;
import com.movkfact.response.ApiResponse;
import com.movkfact.service.DataPreviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller pour la prévisualisation de génération de données (S7.1).
 * POST /api/datasets/preview — génère 5 lignes sans persistance.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Data Preview", description = "Preview data generation without persistence")
public class DataPreviewController {

    @Autowired
    private DataPreviewService dataPreviewService;

    @PostMapping("/datasets/preview")
    @Operation(
        summary = "Prévisualiser la génération de données",
        description = "Génère jusqu'à 5 lignes à partir d'une configuration de colonnes, sans persister en base."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Prévisualisation générée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Type inconnu ou contraintes invalides")
    })
    public ResponseEntity<ApiResponse<PreviewResponseDTO>> previewDataset(
            @Valid @RequestBody PreviewRequestDTO request) {
        PreviewResponseDTO response = dataPreviewService.generatePreview(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Preview generated"));
    }
}
