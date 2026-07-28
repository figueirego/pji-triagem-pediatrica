package com.pji.triagem.controller;

import com.pji.triagem.base.dto.ResponseDTO;
import com.pji.triagem.base.service.ResponseService;
import com.pji.triagem.dto.request.CreateChildRequest;
import com.pji.triagem.dto.response.ChildHomeResponse;
import com.pji.triagem.dto.response.ChildResponse;
import com.pji.triagem.service.ChildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/children")
@AllArgsConstructor
@Tag(name = "Children", description = "Endpoints provisórios para home e cadastro da criança")
public class ChildController {

    private final ResponseService responseService;
    private final ChildService childService;

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Listar crianças do usuário para a home",
            description = "Por enquanto o userId é recebido pela URL. Futuramente o endpoint poderá usar /me com o usuário do JWT.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de crianças para a tela inicial",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChildHomeResponse.class)))
                    )
            }
    )
    public ResponseEntity<ResponseDTO<List<ChildHomeResponse>>> findChildrenForHome(@PathVariable Long userId) {
        return responseService.ok(childService.findChildrenForHomeByUserId(userId));
    }

    @PostMapping("/{userId}")
    @Operation(
            summary = "Criar criança vinculada a um usuário",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Criança criada com sucesso",
                            content = @Content(schema = @Schema(implementation = ChildResponse.class))
                    )
            }
    )
    public ResponseEntity<ResponseDTO<ChildResponse>> createChild(
            @PathVariable Long userId,
            @Valid @RequestBody CreateChildRequest request
    ) {
        return responseService.created(childService.createChild(userId, request));
    }

    @GetMapping("/{userId}/children/{childId}")
    @Operation(summary = "Buscar detalhe de uma criança do usuário")
    public ResponseEntity<ResponseDTO<ChildResponse>> findByIdAndUserId(
            @PathVariable Long userId,
            @PathVariable Long childId
    ) {
        return responseService.ok(childService.findByIdAndUserId(childId, userId));
    }

    @PutMapping("/{userId}/children/{childId}")
    @Operation(summary = "Atualizar dados de uma criança do usuário")
    public ResponseEntity<ResponseDTO<ChildResponse>> update(
            @PathVariable Long userId,
            @PathVariable Long childId,
            @Valid @RequestBody CreateChildRequest request
    ) {
        return responseService.ok(childService.updateChild(userId, childId, request));
    }

    @DeleteMapping("/{userId}/children/{childId}")
    @Operation(summary = "Remover uma criança do usuário")
    public ResponseEntity<ResponseDTO<String>> delete(
            @PathVariable Long userId,
            @PathVariable Long childId
    ) {
        childService.deleteChild(userId, childId);
        return responseService.ok("Criança removida com sucesso");
    }
}
