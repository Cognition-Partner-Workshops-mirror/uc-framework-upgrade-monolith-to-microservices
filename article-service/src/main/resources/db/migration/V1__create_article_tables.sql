CREATE TABLE articles (
  id VARCHAR(255) PRIMARY KEY,
  user_id VARCHAR(255) NOT NULL,
  slug VARCHAR(255) UNIQUE NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  body TEXT,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE tags (
  id VARCHAR(255) PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE article_tags (
  article_id VARCHAR(255) NOT NULL,
  tag_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (article_id, tag_id)
);

CREATE TABLE comments (
  id VARCHAR(255) PRIMARY KEY,
  body TEXT NOT NULL,
  article_id VARCHAR(255) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE article_favorites (
  article_id VARCHAR(255) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (article_id, user_id)
);
