package com.pji.triagem.handler;

import com.pji.triagem.base.dto.ResponseDTO;
import com.pji.triagem.dto.response.ApiError;
import com.pji.triagem.dto.response.ApiFieldError;
import com.pji.triagem.exception.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.List;
import java.util.Objects;

@ControllerAdvice
@RequiredArgsConstructor
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler({ AccessDeniedException.class })
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return handleException(ex, HttpStatus.FORBIDDEN, request, ex.getMessage());
    }

    @ExceptionHandler({ InformationFoundExeption.class })
    public ResponseEntity<Object> handleInformationFoundException(InformationFoundExeption ex, WebRequest request) {
        return handleException(ex, HttpStatus.CONFLICT, request, ex.getMessage());
    }

    @ExceptionHandler({ RuntimeException.class })
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.warn(ex.getMessage());
        return handleException(ex, HttpStatus.BAD_REQUEST, request, ex.getMessage());
    }

    @ExceptionHandler({ ResourceNotFoundException.class })
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                  WebRequest request) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = ex.getResource() + " não encontrado";
        } else if (ex.getResource() != null && !ex.getResource().isBlank()) {
            message = message + " - " + ex.getResource();
        }

        return handleException(ex, HttpStatus.NOT_FOUND, request, message);
    }

    @ExceptionHandler({ ServicesException.class })
    public ResponseEntity<Object> handleServicesException(ServicesException ex,
                                                          WebRequest request) {
        return handleException(ex, HttpStatus.BAD_REQUEST, request, ex.getMessage());
    }
    @ExceptionHandler({ ValidationException.class })
    public ResponseEntity<Object> handleValidationException(ValidationException ex,
                                                            WebRequest request) {
        return handleException(ex, HttpStatus.BAD_REQUEST, request, ex.getMessage());
    }

    @ExceptionHandler({ InvalidLoginException.class })
    public ResponseEntity<Object> handleInvalidLoginException(InvalidLoginException ex,
                                                              WebRequest request) {
        return handleException(ex, HttpStatus.UNAUTHORIZED, request, ex.getMessage());
    }

    @ExceptionHandler({ BadCredentialsException.class })
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex,
                                                                WebRequest request) {
        return handleException(ex, HttpStatus.UNAUTHORIZED, request, "Usuário ou senha inválidos");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ApiError response = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .mensagem("Erro de validação")
                .campos(ex.getBindingResult().getFieldErrors().stream()
                        .map(this::toApiFieldError)
                        .toList())
                .build();

        return handleExceptionInternal(ex, response, headers, HttpStatus.BAD_REQUEST, request);
    }

    protected ResponseEntity<Object> handleException(Exception ex, HttpStatus status, WebRequest req, String message) {
        ApiError response = ApiError.builder()
                .status(status.value())
                .mensagem(message)
                .build();
        return handleExceptionInternal(ex, response, new HttpHeaders(), status, req);
    }

    private ApiFieldError toApiFieldError(FieldError fieldError) {
        return ApiFieldError.builder()
                .campo(fieldError.getField())
                .erro(Objects.requireNonNullElse(fieldError.getDefaultMessage(), "valor inválido"))
                .build();
    }

}
