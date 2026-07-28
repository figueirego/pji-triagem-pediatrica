package com.pji.triagem.dto.response;

import com.pji.triagem.model.OrientationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrientationItemResponse {

    private Long id;
    private String title;
    private String description;
    private OrientationType type;
    private Long symptomId;
    private String symptomName;
}
