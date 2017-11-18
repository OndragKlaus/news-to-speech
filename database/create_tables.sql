DROP TABLE Provider;
DROP TABLE Article;


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
  author          TEXT,
  title           TEXT NOT NULL,
  body            TEXT NOT NULL,
  summary         TEXT,
  last_modified   TIMESTAMP,
  last_summarized TIMESTAMP
);