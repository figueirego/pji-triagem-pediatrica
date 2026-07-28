package com.pji.triagem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentStatsResponse {

    private Long totalAssessments;
    private Long lowRiskCount;
    private Long moderateRiskCount;
    private Long highRiskCount;
    private Long total;
    private Long low;
    private Long mod;
    private Long high;

    public static AssessmentStatsResponse from(AssessmentHistoryResponse history) {
        return AssessmentStatsResponse.builder()
                .totalAssessments(history.getTotalAssessments())
                .lowRiskCount(history.getLowRiskCount())
                .moderateRiskCount(history.getModerateRiskCount())
                .highRiskCount(history.getHighRiskCount())
                .total(history.getTotalAssessments())
                .low(history.getLowRiskCount())
                .mod(history.getModerateRiskCount())
                .high(history.getHighRiskCount())
                .build();
    }
}
