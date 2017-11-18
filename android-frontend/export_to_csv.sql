select
  a.article_id, p.name as provider_name, c.name as category_name,
  a.url, a.author, a.title, a.summary,
  a.date_published, a.date_summarized
from article a
  inner join provider p using (provider_id)
  inner join category c using (category_id);
