package com.silverio.tasks.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
    var body = new ApiError(
        Instant.now(),
        HttpStatus.NOT_FOUND.value(),
        "Recurso não encontrado",
        ex.getMessage(),
        req.getRequestURI(),
        null);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
    var body = new ApiError(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        "Requisição inválida",
        ex.getMessage(),
        req.getRequestURI(),
        null);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    var campos = new HashMap<String, String>();
    for (var error : ex.getBindingResult().getAllErrors()) {
      if (error instanceof FieldError fe) {
        campos.put(fe.getField(), fe.getDefaultMessage());
      }
    }
    var body = new ApiError(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        "Validação falhou",
        "Existem campos inválidos na requisição",
        req.getRequestURI(),
        campos);
    return ResponseEntity.badRequest().body(body);
  }

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
    log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);

    var body = new ApiError(
        Instant.now(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Erro interno",
        "Ocorreu um erro inesperado. Tente novamente.",
        req.getRequestURI(),
        null);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiError> handleMethodNotSupported(
      org.springframework.web.HttpRequestMethodNotSupportedException ex,
      HttpServletRequest req) {

    var body = new ApiError(
        Instant.now(),
        HttpStatus.METHOD_NOT_ALLOWED.value(),
        "Método não permitido",
        "Este endpoint não aceita o método HTTP utilizado.",
        req.getRequestURI(),
        null);

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {

    // Tenta detectar caso clássico: LocalDateTime com formato inválido
    Throwable cause = ex.getCause();
    while (cause != null) {
      if (cause instanceof InvalidFormatException ife) {
        // Se tentou converter para LocalDateTime, já damos uma mensagem específica
        if (ife.getTargetType() != null && ife.getTargetType().equals(LocalDateTime.class)) {
          var body = new ApiError(
              Instant.now(),
              HttpStatus.BAD_REQUEST.value(),
              "Requisição inválida",
              "Formato de data inválido. Use o padrão dd/MM/yyyy HH:mm (ex.: 05/02/2026 00:00).",
              req.getRequestURI(),
              null);
          return ResponseEntity.badRequest().body(body);
        }
      }
      cause = cause.getCause();
    }

    // Fallback genérico (JSON quebrado, tipos errados etc.)
    var body = new ApiError(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        "Requisição inválida",
        "Não foi possível ler o corpo da requisição. Verifique o JSON enviado.",
        req.getRequestURI(),
        null);
    return ResponseEntity.badRequest().body(body);
  }

}
