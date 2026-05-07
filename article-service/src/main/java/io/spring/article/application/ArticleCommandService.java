package io.spring.article.application;

import io.spring.article.api.NewArticleParam;
import io.spring.article.api.UpdateArticleParam;
import io.spring.article.core.article.Article;
import io.spring.article.core.article.ArticleRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

// Command service for creating and updating articles
@Service
@Validated
@AllArgsConstructor
public class ArticleCommandService {

  private ArticleRepository articleRepository;

  public Article createArticle(@Valid NewArticleParam newArticleParam, String userId) {
    Article article =
        new Article(
            newArticleParam.getTitle(),
            newArticleParam.getDescription(),
            newArticleParam.getBody(),
            newArticleParam.getTagList(),
            userId);
    articleRepository.save(article);
    return article;
  }

  public Article updateArticle(Article article, @Valid UpdateArticleParam updateArticleParam) {
    article.update(
        updateArticleParam.getTitle(),
        updateArticleParam.getDescription(),
        updateArticleParam.getBody());
    articleRepository.save(article);
    return article;
  }
}
