package io.spring.article.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Thrown when a user attempts an operation they are not authorized to perform
@ResponseStatus(HttpStatus.FORBIDDEN)
public class NoAuthorizationException extends RuntimeException {}
