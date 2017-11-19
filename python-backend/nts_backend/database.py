from pony import orm
from datetime import datetime

import os
import psycopg2
import subprocess
import sys
import toml

from .config import config

import pony
pony.options.CUT_TRACEBACK = False


db = orm.Database()


def init():
    global db
    db.bind(provider='postgres', **config['database'])
    db.generate_mapping()


def psql():
    data = config['database']
    cmd = ['psql', '-U', data['user'], '-d', data['database'], '-h', data['host']]
    env = os.environ.copy()
    env['PGPASSWORD'] = data['password']
    subprocess.call(cmd + sys.argv[1:], env=env)


class Provider(db.Entity):
    provider_id = orm.PrimaryKey(int, auto=True)
    name = orm.Required(str)
    pretty_name = orm.Required(str)
    articles = orm.Set('Article')

    @classmethod
    def get_or_create(cls, name, pretty_name):
        obj = cls.get(name=name)
        if not obj:
            obj = cls(name=name, pretty_name=pretty_name)
        return obj

    def query_categories(self):
        # XXX can we do this over self.articles, too?
        return orm.select(a.category_id for a in Article if a.provider_id == self)


class Category(db.Entity):
    category_id = orm.PrimaryKey(int, auto=True)
    name = orm.Required(str)
    articles = orm.Set('Article')

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.name = self.name.strip().lower()

    @classmethod
    def get_or_create(cls, name):
        name = name.strip().lower()
        obj = cls.get(name=name)
        if not obj:
            obj = cls(name=name)
        return obj

    def articles_for_provider(self, provider):
        return orm.select(a for a in Article if a in self.articles and a in provider.articles)


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
    audio = orm.Optional('ArticleAudio')


class ArticleAudio(db.Entity):
    article_id = orm.PrimaryKey(Article)
    mp3data = orm.Required(bytes, lazy=True)
