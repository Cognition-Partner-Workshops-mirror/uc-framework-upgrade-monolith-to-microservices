package io.spring.articleservice.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Centralized exception handler for the article-service. Provides consistent error responses across
 * all endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  /** Handle bean validation errors and return structured error response. */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + " " + e.getDefaultMessage())
            .collect(Collectors.toList());
    Map<String, Object> body = new HashMap<>();
    body.put("errors", Map.of("body", errors));
    return ResponseEntity.unprocessableEntity().body(body);
  }

  /** Handle resource not found exceptions. */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Void> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  /** Handle authorization exceptions. */
  @ExceptionHandler(NoAuthorizationException.class)
  public ResponseEntity<Map<String, String>> handleForbidden(NoAuthorizationException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
  }
}
