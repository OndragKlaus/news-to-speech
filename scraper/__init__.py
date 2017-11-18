"""
Scraper for news articles.
"""

import argparse
import asyncio
import feedparser
import requests
import sys
import toml

from database import conn


async def get_rss_urls(rss_url):
    loop = asyncio.get_event_loop()
    response = await loop.run_in_executor(None, requests.get, rss_url)
    feed = await loop.run_in_executor(None, feedparser.parse, response.text)
    for item in feed['entries']:
        yield item['link']


async def fill_database():
    sz_url = 'http://www.sueddeutsche.de/news/rss?search=Suchbegriff+eingeben&sort=date&all%5B%5D=dep&typ%5B%5D=article&sys%5B%5D=sz&sys%5B%5D=dpa&catsz%5B%5D=alles&catdpa%5B%5D=alles&time=P1D'
    async for link in get_rss_urls(sz_url):
        print(link)


def main():
    loop = asyncio.get_event_loop()
    loop.run_until_complete(fill_database())
    loop.close()
