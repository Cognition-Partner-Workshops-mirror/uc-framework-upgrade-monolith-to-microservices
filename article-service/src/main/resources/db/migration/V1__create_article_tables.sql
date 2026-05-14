-- Article-service database schema — extracted from the monolith
-- Contains only article-bounded-context tables: articles, tags, favorites, comments

CREATE TABLE IF NOT EXISTS articles (
  id varchar(255) PRIMARY KEY,
  user_id varchar(255),
  slug varchar(255) UNIQUE,
  title varchar(255),
  description text,
  body text,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tags (
  id varchar(255) PRIMARY KEY,
  name varchar(255) UNIQUE
);

CREATE TABLE IF NOT EXISTS article_tags (
  article_id varchar(255) NOT NULL,
  tag_id varchar(255) NOT NULL,
  PRIMARY KEY (article_id, tag_id)
);

CREATE TABLE IF NOT EXISTS article_favorites (
  article_id varchar(255) NOT NULL,
  user_id varchar(255) NOT NULL,
  PRIMARY KEY (article_id, user_id)
);

CREATE TABLE IF NOT EXISTS comments (
  id varchar(255) PRIMARY KEY,
  body text,
  user_id varchar(255),
  article_id varchar(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
