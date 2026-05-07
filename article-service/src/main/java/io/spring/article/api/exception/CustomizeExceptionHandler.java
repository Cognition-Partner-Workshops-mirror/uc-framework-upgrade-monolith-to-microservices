package io.spring.article.api.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// Centralized exception handler for validation errors in the article-service
@RestControllerAdvice
public class CustomizeExceptionHandler extends ResponseEntityExceptionHandler {

  // Spring 6 changed the parameter type from HttpStatus to HttpStatusCode
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException e,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<Map<String, Object>> errorList = new ArrayList<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(
            fieldError -> {
              Map<String, Object> error = new HashMap<>();
              error.put("field", fieldError.getField());
              error.put("message", fieldError.getDefaultMessage());
              errorList.add(error);
            });
    Map<String, Object> body = new HashMap<>();
    body.put("errors", errorList);
    return ResponseEntity.status(status).body(body);
  }
}
