package com.pji.triagem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateChildRequest {

    @NotBlank(message = "Nome da criança é obrigatório")
    private String name;

    @Pattern(regexp = "^$|^[0-9]{11}$", message = "CPF da criança deve conter 11 dígitos")
    private String cpf;

    @NotNull(message = "Data de nascimento da criança é obrigatória")
    @PastOrPresent(message = "Data de nascimento não pode estar no futuro")
    private LocalDate birthDate;

    @Positive(message = "Peso da criança deve ser positivo")
    private BigDecimal weightKg;

    private String avatarEmoji;
}
