package com.pji.triagem.controller;

import com.pji.triagem.base.dto.ResponseDTO;
import com.pji.triagem.base.service.ResponseService;
import com.pji.triagem.dto.request.CreateAssessmentRequest;
import com.pji.triagem.dto.request.QuestionnaireRequest;
import com.pji.triagem.dto.response.AssessmentResultResponse;
import com.pji.triagem.dto.response.OrientationResponse;
import com.pji.triagem.dto.response.QuestionnaireResponse;
import com.pji.triagem.service.AssessmentService;
import com.pji.triagem.service.OrientationService;
import com.pji.triagem.service.SymptomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assessments")
@AllArgsConstructor
@Tag(name = "Assessments", description = "Endpoints do fluxo de triagem com múltiplos sintomas")
public class AssessmentController {

    private final ResponseService responseService;
    private final AssessmentService assessmentService;
    private final OrientationService orientationService;
    private final SymptomService symptomService;

    @PostMapping("/questionnaire")
    @Operation(
            summary = "Montar questionário agrupado por sintomas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Questionário agrupado por sintomas",
                            content = @Content(schema = @Schema(implementation = QuestionnaireResponse.class))
                    )
            }
    )
    public ResponseEntity<ResponseDTO<QuestionnaireResponse>> buildQuestionnaire(
            @Valid @RequestBody QuestionnaireRequest request
    ) {
        return responseService.ok(symptomService.buildQuestionnaire(request));
    }

    @PostMapping
    @Operation(
            summary = "Criar avaliação/triagem com múltiplos sintomas",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Triagem criada com sucesso",
                            content = @Content(schema = @Schema(implementation = AssessmentResultResponse.class))
                    )
            }
    )
    public ResponseEntity<ResponseDTO<AssessmentResultResponse>> create(
            @Valid @RequestBody CreateAssessmentRequest request
    ) {
        return responseService.created(assessmentService.createAssessment(request));
    }

    @GetMapping("/{assessmentId}")
    @Operation(
            summary = "Buscar detalhe de uma avaliação",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Avaliação retornada com sucesso",
                            content = @Content(schema = @Schema(implementation = AssessmentResultResponse.class))
                    )
            }
    )
    public ResponseEntity<ResponseDTO<AssessmentResultResponse>> getAssessment(@PathVariable Long assessmentId) {
        return responseService.ok(assessmentService.getAssessment(assessmentId));
    }

    @GetMapping("/{assessmentId}/orientation")
    @Operation(
            summary = "Buscar orientações de uma avaliação",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Orientações retornadas com sucesso",
                            content = @Content(schema = @Schema(implementation = OrientationResponse.class))
                    )
            }
    )
    public ResponseEntity<ResponseDTO<OrientationResponse>> getOrientation(@PathVariable Long assessmentId) {
        return responseService.ok(orientationService.getOrientationByAssessmentId(assessmentId));
    }
}
