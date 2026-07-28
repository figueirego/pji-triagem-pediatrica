package com.pji.triagem.dto.response;

import com.pji.triagem.model.Classification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentHistoryItemResponse {

    private Long id;
    private Long childId;
    private String childName;

    @Builder.Default
    private List<String> symptoms = new ArrayList<>();

    private LocalDateTime createdAt;
    private Classification classification;
}
