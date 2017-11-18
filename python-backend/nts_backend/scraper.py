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


def get_article(provider, info):
    logging.info('GET {}'.format(info.url))
    html = requests.get(info.url).text
    soup = bs4.BeautifulSoup(html, 'lxml')
    metadata = provider.get_article_metadata(info, html, soup)
    summary = provider.summarize_article(info, html, soup)

    if not metadata:
        logger.warn('NO METADATA {}'.format(info.url))
        return

    # Ensure that the provider exists.
    database.get_provider_id(provider.get_provider_id(), create=True)

    try:
        database.create_article(
            provider = provider.get_provider_id(),
            category = metadata.category or 'unknown',
            guid = info.id,
            url = info.url,
            #language = info.language,  # XXX
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

    while True:
        for provider in providers:
            for info in provider.get_recent_article_urls():
                if database.has_article_with_guid(info.id):
                    logging.info('SKIP {}'.format(info.url))
                    continue
                get_article(provider, info)
        if args.once:
            break
        time.sleep(30.0)
