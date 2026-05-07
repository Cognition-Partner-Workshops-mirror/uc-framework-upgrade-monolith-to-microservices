package io.spring.article.application;

import static java.util.stream.Collectors.toList;

import io.spring.article.application.client.UserServiceClient;
import io.spring.article.application.data.ArticleData;
import io.spring.article.application.data.ArticleDataList;
import io.spring.article.application.data.ArticleFavoriteCount;
import io.spring.article.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.article.infrastructure.mybatis.readservice.ArticleReadService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Query service for articles. Uses UserServiceClient instead of direct
 * UserRelationshipQueryService to fetch follow relationships from user-service.
 */
@Service
@AllArgsConstructor
public class ArticleQueryService {
  private ArticleReadService articleReadService;
  private ArticleFavoritesReadService articleFavoritesReadService;
  private UserServiceClient userServiceClient;

  public Optional<ArticleData> findById(String id, String userId) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    } else {
      if (userId != null) {
        fillExtraInfo(id, userId, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public Optional<ArticleData> findBySlug(String slug, String userId) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    } else {
      if (userId != null) {
        fillExtraInfo(articleData.getId(), userId, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page, String userId) {
    List<String> articleIds = articleReadService.queryArticles(tag, author, favoritedBy, page);
    int articleCount = articleReadService.countArticle(tag, author, favoritedBy);
    if (articleIds.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, userId);
      return new ArticleDataList(articles, articleCount);
    }
  }

  // Cross-service call to user-service to get followed users for feed
  public ArticleDataList findUserFeed(String userId, Page page) {
    try {
      // Call user-service to get followed user IDs
      Set<String> followedUsers = userServiceClient.getFollowingUsers(userId, List.of());
      if (followedUsers.isEmpty()) {
        return new ArticleDataList(new ArrayList<>(), 0);
      }
      List<String> followedUserList = new ArrayList<>(followedUsers);
      List<ArticleData> articles =
          articleReadService.findArticlesOfAuthors(followedUserList, page);
      fillExtraInfo(articles, userId);
      int count = articleReadService.countFeedSize(followedUserList);
      return new ArticleDataList(articles, count);
    } catch (Exception e) {
      // Graceful degradation: return empty feed if user-service is unreachable
      return new ArticleDataList(new ArrayList<>(), 0);
    }
  }

  private void fillExtraInfo(List<ArticleData> articles, String userId) {
    setFavoriteCount(articles);
    if (userId != null) {
      setIsFavorite(articles, userId);
      setIsFollowingAuthor(articles, userId);
    }
  }

  // Cross-service call to user-service to check follow relationships
  private void setIsFollowingAuthor(List<ArticleData> articles, String userId) {
    try {
      List<String> authorIds =
          articles.stream()
              .map(a -> a.getProfileData().getId())
              .collect(toList());
      Set<String> followingAuthors = userServiceClient.getFollowingUsers(userId, authorIds);
      articles.forEach(
          articleData -> {
            if (followingAuthors.contains(articleData.getProfileData().getId())) {
              articleData.getProfileData().setFollowing(true);
            }
          });
    } catch (Exception e) {
      // Graceful degradation: leave following as false if user-service is unreachable
    }
  }

  private void setFavoriteCount(List<ArticleData> articles) {
    List<ArticleFavoriteCount> favoritesCounts =
        articleFavoritesReadService.articlesFavoriteCount(
            articles.stream().map(ArticleData::getId).collect(toList()));
    Map<String, Integer> countMap = new HashMap<>();
    favoritesCounts.forEach(item -> countMap.put(item.getId(), item.getCount()));
    articles.forEach(
        articleData -> articleData.setFavoritesCount(countMap.get(articleData.getId())));
  }

  private void setIsFavorite(List<ArticleData> articles, String userId) {
    Set<String> favoritedArticles =
        articleFavoritesReadService.userFavorites(
            articles.stream().map(ArticleData::getId).collect(toList()), userId);
    articles.forEach(
        articleData -> {
          if (favoritedArticles.contains(articleData.getId())) {
            articleData.setFavorited(true);
          }
        });
  }

  // Cross-service call to user-service to check follow relationship for single article
  private void fillExtraInfo(String id, String userId, ArticleData articleData) {
    articleData.setFavorited(articleFavoritesReadService.isUserFavorite(userId, id));
    articleData.setFavoritesCount(articleFavoritesReadService.articleFavoriteCount(id));
    try {
      articleData
          .getProfileData()
          .setFollowing(
              userServiceClient.isFollowing(userId, articleData.getProfileData().getId()));
    } catch (Exception e) {
      // Graceful degradation: leave following as false if user-service is unreachable
    }
  }
}
