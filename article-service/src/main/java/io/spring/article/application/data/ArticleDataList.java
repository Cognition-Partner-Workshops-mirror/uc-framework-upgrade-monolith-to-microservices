package io.spring.article.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

// Wrapper DTO for paginated article lists with total count
@Getter
public class ArticleDataList {
  @JsonProperty("articles")
  private final List<ArticleData> articleDatas;

  @JsonProperty("articlesCount")
  private final int count;

  public ArticleDataList(List<ArticleData> articleDatas, int count) {
    this.articleDatas = articleDatas;
    this.count = count;
  }
}
