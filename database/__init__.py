
import os
import pathlib
import psycopg2
import subprocess
import sys
import toml

conn = None

with open(pathlib.Path(__file__).parent.joinpath('config.toml')) as fp:
    config = toml.load(fp)['database']
    del fp


def init():
    global conn
    if conn: return
    conn = psycopg2.connect(**config)


def create_tables():
    init()
    with open(pathlib.Path(__file__).parent.joinpath('create_tables.sql')) as fp:
        stmts = fp.read().split(';')
    cursor = conn.cursor()
    for stmt in stmts:
        if not stmt: continue
        cursor.execute(stmt)


def psql():
    cmd = ['psql', '-U', config['user'], '-d', config['dbname'], '-h', config['host']]
    env = os.environ.copy()
    env['PGPASSWORD'] = config['password']
    subprocess.call(cmd + sys.argv[1:], env=env)


def get_provider_id(name, create=False):
    cur = conn.cursor()
    if create:
        cur.execute("""
            INSERT INTO Provider (name) SELECT %s
            WHERE NOT EXISTS (SELECT 1 FROM Provider WHERE name = %s LIMIT 1)
        """, (name, name))
        conn.commit()
    cur.execute("SELECT provider_id FROM Provider WHERE name = %s LIMIT 1", (name,))
    row = cur.fetchone()
    if row is None:
        raise ValueError('provider {!r} does not exist'.format(name))
    return row[0]


def get_category_id(name, create=False):
    cur = conn.cursor()
    if create:
        cur.execute("""
            INSERT INTO Category (name) SELECT %s
            WHERE NOT EXISTS (SELECT 1 FROM Category WHERE name ILIKE %s)
        """, (name, name))
        conn.commit()
    cur.execute("SELECT category_id FROM Category WHERE name ILIKE %s", (name,))
    row = cur.fetchone()
    if row is None:
        raise ValueError('category {!r} does not exist'.format(name))
    return row[0]


def has_article_with_guid(guid):
    cur = conn.cursor()
    cur.execute("SELECT 1 FROM Article WHERE guid = %s", (guid,))
    return bool(cur.fetchone())


def delete_article_by_guid(guid, notexist_ok=False):
    cur = conn.cursor()
    # XXX get the number of rows that were deleted? Instead of this:
    if not notexist_ok and not has_article_with_guid(guid):
        raise ValueError('article {!r} does not exist.'.format(guid))
    cur.execute("DELETE FROM Article WHERE guid = %s", (guid,))
    conn.commit()


def create_article(provider, category, guid, url, author, title,
                   summary, is_top_article, date_published, date_summarized):
    cur = conn.cursor()
    cur.execute("""
        INSERT INTO Article (
            provider_id, category_id, guid, url, author,
            title, summary, is_top_article, date_published, date_summarized)
        SELECT %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
    """, (
        get_provider_id(provider, create=False),
        get_category_id(category, create=True),
        guid, url, author, title, summary,
        is_top_article, date_published, date_summarized)
    )
    conn.commit()

def get_articles(provider_id=None, category_id=None):
    conds = []
    vars = []
    if provider_id:
        conds.append('provider_id = %s')
        vars.append(provider_id)
    if category_id:
        conds.append('category_id = %s')
        vars.append(category_id)

    sql = 'SELECT article_id FROM Article'
    if conds:
        sql += ' WHERE ' + ' AND '.join(conds)

    cur = conn.cursor()
    cur.execute(sql, vars)
    return [article_id[0] for article_id in cur.fetchall()]

def get_article_by_id(article_id):
    cur = conn.cursor()
    cur.execute('SELECT * FROM Article WHERE article_id = %s', article_id)
    colnames = [desc[0] for desc in cur.description]
    return dict(zip(colnames, cur.fetchone()))
