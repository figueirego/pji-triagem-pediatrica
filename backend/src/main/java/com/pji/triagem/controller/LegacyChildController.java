package com.pji.triagem.controller;

import com.pji.triagem.base.dto.ResponseDTO;
import com.pji.triagem.base.service.ResponseService;
import com.pji.triagem.dto.request.CreateChildRequest;
import com.pji.triagem.dto.response.ChildHomeResponse;
import com.pji.triagem.dto.response.ChildResponse;
import com.pji.triagem.service.ChildService;
import com.pji.triagem.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/criancas")
@AllArgsConstructor
public class LegacyChildController {

    private final ResponseService responseService;
    private final ChildService childService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ResponseDTO<List<ChildHomeResponse>>> list() {
        return responseService.ok(childService.findChildrenForHomeByUserId(currentUserId()));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<ChildResponse>> create(@Valid @RequestBody CreateChildRequest request) {
        return responseService.created(childService.createChild(currentUserId(), request));
    }

    @GetMapping("/{childId}")
    public ResponseEntity<ResponseDTO<ChildResponse>> detail(@PathVariable Long childId) {
        return responseService.ok(childService.findByIdAndUserId(childId, currentUserId()));
    }

    @PutMapping("/{childId}")
    public ResponseEntity<ResponseDTO<ChildResponse>> update(
            @PathVariable Long childId,
            @Valid @RequestBody CreateChildRequest request
    ) {
        return responseService.ok(childService.updateChild(currentUserId(), childId, request));
    }

    @DeleteMapping("/{childId}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long childId) {
        childService.deleteChild(currentUserId(), childId);
        return responseService.ok("Criança removida com sucesso");
    }

    private Long currentUserId() {
        return currentUserService.getCurrentUser().getId();
    }
}
