DROP TABLE IF EXISTS ArticleToKeyword;
DROP TABLE IF EXISTS Article;
DROP TABLE IF EXISTS Keyword;
DROP TABLE IF EXISTS Category;
DROP TABLE IF EXISTS Provider;

CREATE TABLE IF NOT EXISTS Provider (
  provider_id BIGSERIAL PRIMARY KEY,
  name        TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Category (
  category_id BIGSERIAL PRIMARY KEY,
  name        TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Keyword (
  keyword_id BIGSERIAL PRIMARY KEY,
  name       TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Article (
  article_id      BIGSERIAL PRIMARY KEY,
  provider_id     BIGSERIAL REFERENCES Provider (provider_id),
  category_id     BIGSERIAL REFERENCES Category (category_id),
  guid            TEXT    NOT NULL UNIQUE,
  url             TEXT    NOT NULL,
  author          TEXT,
  title           TEXT,
  summary         TEXT,
  is_top_article  BOOLEAN NOT NULL DEFAULT FALSE,
  date_published  TIMESTAMP,
  date_summarized TIMESTAMP,
  audioblob       BYTEA
);

CREATE TABLE IF NOT EXISTS ArticleToKeyword (
  article_id BIGSERIAL REFERENCES Article (article_id),
  keyword_id BIGSERIAL REFERENCES Keyword (keyword_id),
  PRIMARY KEY (article_id, keyword_id)
);