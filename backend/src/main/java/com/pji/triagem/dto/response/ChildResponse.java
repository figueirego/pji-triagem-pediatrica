package com.pji.triagem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildResponse {

    private Long id;
    private String name;
    private String cpf;
    private LocalDate birthDate;
    private String age;
    private Integer ageInMonths;
    private BigDecimal weightKg;
    private String avatarEmoji;
    private LocalDateTime createdAt;
}
