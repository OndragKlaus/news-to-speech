
from typing import *
from datetime import datetime

import bs4
import feedparser
import requests
import sumy.parsers.html
import sumy.nlp.tokenizers
import sumy.nlp.stemmers
import sumy.summarizers.lsa
import sumy.utils


class ArticleUrl(NamedTuple):

    id: str
    url: str
    language: str
    data: Any = None


class ArticleMetadata(NamedTuple):

    authors: List[str] = None
    category: str = None
    keywords: List[str] = None
    date_published: datetime = None


class BaseProvider:

    def get_recent_article_urls(self) -> Iterable[ArticleUrl]:
        """
        Returns a list of recent articles that need to be parsed. The method
        may return articles that have been returned before, but they must be
        identifiable by the globally unique #ArticleUrl.id.
        """

        raise NotImplementedError

    def get_article_metadata(self, item: ArticleUrl, html: str,
                             soup: bs4.BeautifulSoup) -> ArticleMetadata:
        """
        Extract additional metadata from the article at the URL *url*. You
        get access to the pages' HTML content and the parsed BeautifulSoup
        as well.
        """

        raise NotImplementedError

    def summarize_article(self, item: ArticleUrl, html: str,
                          soup: bs4.BeautifulSoup) -> str:
        """
        Summarize the article. The default implementation uses the #sumy
        package.
        """

        SENTENCES_COUNT = 10  # XXX
        tokenizer = sumy.nlp.tokenizers.Tokenizer(item.language)
        parser = sumy.parsers.html.HtmlParser.from_string(html, item.url, tokenizer)
        stemmer = sumy.nlp.stemmers.Stemmer(item.language)
        summarizer = sumy.summarizers.lsa.LsaSummarizer(stemmer)
        summarizer.stop_wors = sumy.utils.get_stop_words(item.language)
        return ' '.join(map(str, summarizer(parser.document, SENTENCES_COUNT)))


class RssProvider(BaseProvider):

    language_map = {
        'de': 'german',
        'en': 'english',
        'cz': 'czech',
        'fr': 'frensh'
        # XXX
    }

    def __init__(self, feed_url):
        self.feed_url = feed_url

    def get_recent_article_urls(self) -> Iterable[ArticleUrl]:
        feed = feedparser.parse(requests.get(self.feed_url).text)
        language = self.language_map[feed['feed']['language']]
        for entry in feed['entries']:
            yield ArticleUrl(entry['id'], entry['link'], language, data=entry)


class SueddeutscheZeitung(RssProvider):

    FEED_URL = 'http://www.sueddeutsche.de/news/rss?search=Suchbegriff+eingeben'\
               '&sort=date&all%5B%5D=dep&typ%5B%5D=article&sys%5B%5D=sz&sys%5B%'\
               '5D=dpa&catsz%5B%5D=alles&catdpa%5B%5D=alles&time=P1D'

    def __init__(self):
        super().__init__(self.FEED_URL)

    def get_article_metadata(self, item: ArticleUrl, html: str,
                             soup: bs4.BeautifulSoup) -> ArticleMetadata:
        from .article import Article
        from urllib.parse import urlparse
        import traceback

        article = Article(item.url)
        article.download(input_html=html)
        try:
            article.parse(source='sz', soup=soup)
        except:
            traceback.print_exc()
            return ArticleMetadata()

        import pdb; pdb.set_trace()

        # Determine the category name from the URL.
        category = urlparse(item.url).path.lstrip('/').partition('/')[0]

        return ArticleMetadata(authors=article.authors, category=category)