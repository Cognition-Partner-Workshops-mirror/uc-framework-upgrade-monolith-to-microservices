package io.spring.application.user;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
// Migrated: javax.validation -> jakarta.validation (Jakarta EE 10 namespace)
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = DuplicatedUsernameValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@interface DuplicatedUsernameConstraint {
  String message() default "duplicated username";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
