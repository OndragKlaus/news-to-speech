
import pathlib
import psycopg2
import toml

with open(pathlib.Path(__file__).parent.joinpath('config.toml')) as fp:
    conn = psycopg2.connect(**toml.load(fp)['database'])
    del fp
