package io.spring.application.article;

import io.spring.application.ArticleQueryService;
import io.spring.core.article.Article;
// Migrated: javax.validation -> jakarta.validation (Jakarta EE 10 namespace)
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

class DuplicatedArticleValidator
    implements ConstraintValidator<DuplicatedArticleConstraint, String> {

  @Autowired private ArticleQueryService articleQueryService;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return !articleQueryService.findBySlug(Article.toSlug(value), null).isPresent();
  }
}
