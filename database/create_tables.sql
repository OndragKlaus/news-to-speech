DROP TABLE Article;
DROP TABLE Provider;
DROP TABLE Category;


CREATE TABLE IF NOT EXISTS Provider (
  provider_id BIGSERIAL PRIMARY KEY,
  name        TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Category (
  category_id BIGSERIAL PRIMARY KEY,
  name        TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Article (
  article_id      BIGSERIAL PRIMARY KEY,
  provider_id     BIGSERIAL REFERENCES Provider (provider_id),
  category_id     BIGSERIAL REFERENCES Category (category_id),
  guid            TEXT NOT NULL UNIQUE,
  url             TEXT NOT NULL,
  author          TEXT,
  title           TEXT,
  body            TEXT,
  summary         TEXT,
  last_modified   TIMESTAMP,
  last_summarized TIMESTAMP
);