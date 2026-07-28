package com.pji.triagem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymptomResponse {

    private Long id;
    private String code;
    private String name;
    private String shortDescription;
    private String iconRef;
    private String colorHex;
    private Integer order;
}
