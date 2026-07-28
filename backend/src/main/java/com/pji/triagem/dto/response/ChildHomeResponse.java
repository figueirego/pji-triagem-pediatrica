package com.pji.triagem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildHomeResponse {

    private Long id;
    private String name;
    private String age;
    private Integer ageInMonths;
    private String avatarEmoji;
    private Long totalAssessments;
    private LocalDateTime lastAssessmentAt;
}
