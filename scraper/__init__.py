"""
Scraper for news articles.
"""


from .provider import SueddeutscheZeitung

import bs4
import requests

import database; database.init()

providers = [
    SueddeutscheZeitung()
]


def main():
    database.init()
    for provider in providers:
        for info in provider.get_recent_article_urls():
            print(info)
            html = requests.get(info.url).text
            soup = bs4.BeautifulSoup(html, 'lxml')
            metadata = provider.get_article_metadata(info, html, soup)
            summary = provider.summarize_article(info, html, soup)
            print(' ', metadata)
            print(' ', summary)
            print('-' * 50)
            # XXX Insert into database
