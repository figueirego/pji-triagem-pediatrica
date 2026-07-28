package com.pji.triagem.controller;

import com.pji.triagem.base.dto.ResponseDTO;
import com.pji.triagem.base.service.ResponseService;
import com.pji.triagem.dto.response.AssessmentHistoryResponse;
import com.pji.triagem.dto.response.AssessmentStatsResponse;
import com.pji.triagem.service.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assessments")
@AllArgsConstructor
@Tag(name = "Assessment History", description = "Endpoints do histórico de avaliações por usuário")
public class UserAssessmentController {

    private final ResponseService responseService;
    private final AssessmentService assessmentService;

    @GetMapping("/users/{userId}/history")
    @Operation(
            summary = "Buscar histórico de avaliações do usuário",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Histórico retornado com sucesso",
                            content = @Content(schema = @Schema(implementation = AssessmentHistoryResponse.class))
                    )
            }
    )
    public ResponseEntity<ResponseDTO<AssessmentHistoryResponse>> getHistory(
            @Parameter(description = "Id do usuário responsável")
            @PathVariable Long userId,
            @Parameter(description = "Filtro opcional por criança")
            @RequestParam(required = false) Long childId,
            @RequestParam(name = "criancaId", required = false) Long criancaId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return responseService.ok(assessmentService.getHistory(userId, childId != null ? childId : criancaId, page, size));
    }

    @GetMapping("/users/{userId}/stats")
    @Operation(summary = "Buscar estatísticas do histórico de avaliações do usuário")
    public ResponseEntity<ResponseDTO<AssessmentStatsResponse>> getStats(
            @Parameter(description = "Id do usuário responsável")
            @PathVariable Long userId,
            @Parameter(description = "Filtro opcional por criança")
            @RequestParam(required = false) Long childId
    ) {
        return responseService.ok(assessmentService.getStats(userId, childId));
    }
}
