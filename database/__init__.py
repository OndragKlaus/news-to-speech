
import os
import pathlib
import psycopg2
import subprocess
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
    subprocess.call(cmd, env=env)


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
