from pony import orm
from datetime import datetime

import os
import psycopg2
import subprocess
import sys
import toml

from .config import config


db = orm.Database()


def init():
    global db
    db.bind(provider='postgres', **config['database'])
    db.generate_mapping()
    import pdb; pdb.set_trace()


class Provider(db.Entity):
    provider_id = orm.PrimaryKey(int, auto=True)
    name = orm.Required(str)
    pretty_name = orm.Required(str)
    articles = orm.Set('Article')


class Category(db.Entity):
    category_id = orm.PrimaryKey(int, auto=True)
    name = orm.Required(str)
    articles = orm.Set('Article')


class Article(db.Entity):
    article_id = orm.PrimaryKey(int, auto=True)
    provider_id = orm.Required(Provider)
    category_id = orm.Required(Category)
    guid = orm.Required(str)
    url = orm.Required(str)
    author = orm.Optional(str)
    title = orm.Optional(str)
    summary = orm.Optional(str)
    is_top_article = orm.Optional(bool)
    date_published = orm.Optional(datetime)
    date_summarized = orm.Optional(datetime)
    audioblob = orm.Optional(bytes)

def psql():
    data = config['database']
    cmd = ['psql', '-U', data['user'], '-d', data['database'], '-h', data['host']]
    env = os.environ.copy()
    env['PGPASSWORD'] = data['password']
    subprocess.call(cmd + sys.argv[1:], env=env)

#
#
#def get_provider_id(name, pretty_name, create=False):
#    cur = conn.cursor()
#    if create:
#        cur.execute("""
#            INSERT INTO Provider (name, pretty_name) SELECT %s, %s
#            WHERE NOT EXISTS (SELECT 1 FROM Provider WHERE name = %s LIMIT 1)
#        """, (name, pretty_name, name))
#        conn.commit()
#    cur.execute("SELECT provider_id FROM Provider WHERE name = %s LIMIT 1", (name,))
#    row = cur.fetchone()
#    if row is None:
#        raise ValueError('provider {!r} does not exist'.format(name))
#    return row[0]
#
#def get_providers():
#    pass
#
#def get_category_id(name, create=False):
#    cur = conn.cursor()
#    if create:
#        cur.execute("""
#            INSERT INTO Category (name) SELECT %s
#            WHERE NOT EXISTS (SELECT 1 FROM Category WHERE name ILIKE %s)
#        """, (name, name))
#        conn.commit()
#    cur.execute("SELECT category_id FROM Category WHERE name ILIKE %s", (name,))
#    row = cur.fetchone()
#    if row is None:
#        raise ValueError('category {!r} does not exist'.format(name))
#    return row[0]
#
#
#def has_article_with_guid(guid):
#    cur = conn.cursor()
#    cur.execute("SELECT 1 FROM Article WHERE guid = %s", (guid,))
#    return bool(cur.fetchone())
#
#
#def delete_article_by_guid(guid, notexist_ok=False):
#    cur = conn.cursor()
#    # XXX get the number of rows that were deleted? Instead of this:
#    if not notexist_ok and not has_article_with_guid(guid):
#        raise ValueError('article {!r} does not exist.'.format(guid))
#    cur.execute("DELETE FROM Article WHERE guid = %s", (guid,))
#    conn.commit()
#
#
#def create_article(provider, category, guid, url, author, title,
#                   summary, is_top_article, date_published, date_summarized):
#    cur = conn.cursor()
#    cur.execute("""
#        INSERT INTO Article (
#            provider_id, category_id, guid, url, author,
#            title, summary, is_top_article, date_published, date_summarized)
#        SELECT %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
#    """, (
#        get_provider_id(provider, create=False),
#        get_category_id(category, create=True),
#        guid, url, author, title, summary,
#        is_top_article, date_published, date_summarized)
#    )
#    conn.commit()
#
#
#def get_articles(provider_id=None, category_id=None, has_audioblob=None):
#    conds = []
#    vars = []
#    if provider_id:
#        conds.append('provider_id = %s')
#        vars.append(provider_id)
#    if category_id:
#        conds.append('category_id = %s')
#        vars.append(category_id)
#    if has_audioblob is not None:
#        if has_audioblob:
#            conds.append('audioblob IS NOT NULL')
#        else:
#            conds.append('audioblob IS NULL')
#
#    sql = 'SELECT article_id FROM Article'
#    if conds:
#        sql += ' WHERE ' + ' AND '.join(conds)
#
#    cur = conn.cursor()
#    cur.execute(sql, vars)
#    return [article_id[0] for article_id in cur.fetchall()]
#
#
#def get_article_by_id(article_id):
#    cur = conn.cursor()
#    cur.execute('SELECT * FROM Article WHERE article_id = %s', (article_id,))
#    colnames = [desc[0] for desc in cur.description]
#    return dict(zip(colnames, cur.fetchone()))
