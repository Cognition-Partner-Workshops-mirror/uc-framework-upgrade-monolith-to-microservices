-- Article-service tables only (split from monolith V1__create_tables.sql)
-- Users and follows tables remain in user-service

-- Local users cache table for profile data joins in article/comment queries.
-- Populated/synced from user-service via REST calls or event-driven updates.
create table users (
  id varchar(255) primary key,
  username varchar(255),
  bio text,
  image varchar(511)
);

create table articles (
  id varchar(255) primary key,
  user_id varchar(255),
  slug varchar(255) UNIQUE,
  title varchar(255),
  description text,
  body text,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create table article_favorites (
  article_id varchar(255) not null,
  user_id varchar(255) not null,
  primary key(article_id, user_id)
);

create table tags (
  id varchar(255) primary key,
  name varchar(255) not null
);

create table article_tags (
  article_id varchar(255) not null,
  tag_id varchar(255) not null
);

create table comments (
  id varchar(255) primary key,
  body text,
  article_id varchar(255),
  user_id varchar(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
