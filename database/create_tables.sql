CREATE TABLE IF NOT EXISTS Provider (
  provider_id BIGSERIAL PRIMARY KEY,
  name        TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Article (
  article_id      BIGSERIAL PRIMARY KEY,
  provider_id     BIGSERIAL REFERENCES Provider (provider_id),
  guid            TEXT NOT NULL UNIQUE,
  title           TEXT NOT NULL,
  body            TEXT NOT NULL,
  summary         TEXT,
  last_modified   TIMESTAMP,
  last_summarized TIMESTAMP
);
