"""
Scraper for news articles.
"""

from concurrent.futures import ThreadPoolExecutor
from . import database
from .provider import SueddeutscheZeitung, DerStandard

import argparse
import bs4
import datetime
import logging
import queue
import requests
import time

parser = argparse.ArgumentParser()
parser.add_argument('--once', action='store_true')

providers = [
    SueddeutscheZeitung(),
    DerStandard()
]


@database.orm.db_session
def get_article(provider, info):
    logging.info('GET {}'.format(info.url))
    html = requests.get(info.url).text
    soup = bs4.BeautifulSoup(html, 'lxml')
    metadata = provider.get_article_metadata(info, html, soup)
    summary = provider.summarize_article(info, html, soup)

    if not metadata:
        logging.warn('NO METADATA {}'.format(info.url))
        return

    try:
        database.Article(
            provider_id = database.Provider.get_or_create(
                name = provider.get_provider_id(),
                pretty_name = provider.get_provider_pretty_name()
            ),
            category_id = database.Category.get_or_create(name=metadata.category or 'unknown'),
            guid = info.id,
            url = info.url,
            author = ';'.join(metadata.authors or []),  # XXX
            title = metadata.title,
            summary = summary,
            is_top_article = metadata.is_top_article,
            date_published = metadata.date_published,
            date_summarized = datetime.datetime.now()
        )
    except Exception as exc:
        logging.exception(exc)


def main():
    args = parser.parse_args()
    logging.basicConfig(level=logging.INFO)
    database.init()

    logging.info('Entering provision loop...')
    while True:
        for provider in providers:
            logging.info('Testing {}'.format(provider.get_provider_id()))
            for info in provider.get_recent_article_urls():
                if database.Article.exists(guid=info.id):
                    logging.info('SKIP {}'.format(info.url))
                    continue
                get_article(provider, info)
        if args.once:
            break
        time.sleep(30.0)
