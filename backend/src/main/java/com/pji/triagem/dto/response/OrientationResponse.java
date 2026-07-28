package com.pji.triagem.dto.response;

import com.pji.triagem.model.Classification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrientationResponse {

    private Long assessmentId;
    private Long childId;
    private String childName;
    private Classification finalClassification;
    private String title;
    private String description;

    @Builder.Default
    private List<OrientationItemResponse> mainActions = new ArrayList<>();

    @Builder.Default
    private List<OrientationItemResponse> warningSigns = new ArrayList<>();

    @Builder.Default
    private List<OrientationItemResponse> homeCare = new ArrayList<>();

    @Builder.Default
    private List<OrientationItemResponse> whenSeekHelp = new ArrayList<>();

    @Builder.Default
    private List<OrientationSymptomResponse> symptoms = new ArrayList<>();

    private String disclaimer;
}
