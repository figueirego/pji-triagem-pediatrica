package com.pji.triagem.controller;

import com.pji.triagem.base.dto.ResponseDTO;
import com.pji.triagem.base.service.ResponseService;
import com.pji.triagem.dto.request.CreateAssessmentRequest;
import com.pji.triagem.dto.response.AssessmentHistoryResponse;
import com.pji.triagem.dto.response.AssessmentResultResponse;
import com.pji.triagem.dto.response.AssessmentStatsResponse;
import com.pji.triagem.service.AssessmentService;
import com.pji.triagem.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/avaliacoes")
@AllArgsConstructor
public class LegacyAssessmentController {

    private final ResponseService responseService;
    private final AssessmentService assessmentService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ResponseDTO<AssessmentHistoryResponse>> list(
            @RequestParam(required = false) Long childId,
            @RequestParam(name = "criancaId", required = false) Long criancaId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return responseService.ok(assessmentService.getHistory(
                currentUserService.getCurrentUser().getId(),
                childId != null ? childId : criancaId,
                page,
                size
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<ResponseDTO<AssessmentStatsResponse>> stats(
            @RequestParam(required = false) Long childId
    ) {
        return responseService.ok(assessmentService.getStats(currentUserService.getCurrentUser().getId(), childId));
    }

    @GetMapping("/{assessmentId}")
    public ResponseEntity<ResponseDTO<AssessmentResultResponse>> detail(@PathVariable Long assessmentId) {
        return responseService.ok(assessmentService.getAssessment(assessmentId));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<AssessmentResultResponse>> create(
            @Valid @RequestBody CreateAssessmentRequest request
    ) {
        return responseService.created(assessmentService.createAssessment(request));
    }
}
