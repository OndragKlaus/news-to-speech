"""
Scraper for news articles.
"""

from concurrent.futures import ThreadPoolExecutor
from .provider import SueddeutscheZeitung

import argparse
import bs4
import datetime
import logging
import queue
import requests
import time

import database

parser = argparse.ArgumentParser()
parser.add_argument('--once', action='store_true')
parser.add_argument('--dry', action='store_true')

providers = [
    SueddeutscheZeitung()
]


def get_article(provider, info):
    logging.info('GET {}'.format(info.url))
    html = requests.get(info.url).text
    soup = bs4.BeautifulSoup(html, 'lxml')
    metadata = provider.get_article_metadata(info, html, soup)
    summary = provider.summarize_article(info, html, soup)

    provider_id = database.get_provider_id(provider.get_provider_id(), create=True)
    category_id = database.get_category_id(metadata.category or 'unknown', create=True)

    try:
        database.create_article(
            provider_id = provider_id,
            category_id = category_id,
            guid = info.id,
            url = info.url,
            #language = info.language,  # XXX
            author = ';'.join(metadata.authors or []),  # XXX
            title = metadata.title,
            body = '',
            summary = summary,
            last_modified = metadata.date_published,  # XXX rename to date_published
            last_summarize = datetime.datetime.now()  # XXX rename to date_summarized
        )
    except Exception as exc:
        logging.error(exc)


def main():
    args = parser.parse_args()
    logging.basicConfig(level=logging.INFO)
    database.init()

    while True:
        for provider in providers:
            for info in provider.get_recent_article_urls():
                if database.has_article_with_guid(info.id):
                    logging.info('SKIP {}'.format(info.url))
                    continue
                get_article(provider, info)
        if args.once:
            break
        time.sleep(5.0)
