package com.pji.triagem.controller;

import com.pji.triagem.base.dto.ResponseDTO;
import com.pji.triagem.base.service.ResponseService;
import com.pji.triagem.dto.response.SymptomResponse;
import com.pji.triagem.service.SymptomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/symptoms")
@AllArgsConstructor
@Tag(name = "Symptoms", description = "Endpoints do catálogo de sintomas")
public class SymptomController {

    private final ResponseService responseService;
    private final SymptomService symptomService;

    @GetMapping
    @Operation(
            summary = "Listar sintomas disponíveis",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Catálogo de sintomas",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SymptomResponse.class)))
                    )
            }
    )
    public ResponseEntity<ResponseDTO<List<SymptomResponse>>> findAll() {
        return responseService.ok(symptomService.findAllSymptoms());
    }
}
