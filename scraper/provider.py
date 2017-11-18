
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

    title: str
    authors: List[str] = None
    category: str = None
    keywords: List[str] = None
    date_published: datetime = None


class BaseProvider:

    def get_provider_id(self) -> str:
        raise NotImplementedError

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
        'de-AT': 'german',
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

    def get_provider_id(self) -> str:
        return 'de.sueddeutsche'

    def get_article_metadata(self, item: ArticleUrl, html: str,
                             soup: bs4.BeautifulSoup) -> ArticleMetadata:
        from .article import Article
        from urllib.parse import urlparse
        import time
        import traceback

        article = Article(item.url)
        article.download(input_html=html)
        try:
            article.parse(source='sz', soup=soup)
        except:
            traceback.print_exc()
            return ArticleMetadata()

        # Determine the category name from the URL.
        category = urlparse(item.url).path.lstrip('/').partition('/')[0]

        # Convert the time.struct_time to datetime.
        timestamp = time.mktime(item.data['published_parsed'])
        date_published = datetime.fromtimestamp(timestamp)

        return ArticleMetadata(
            title=item.data['title'],
            authors=article.authors,
            category=category,
            keywords=article.keywords,
            date_published=date_published)


class DerStandard(RssProvider):

    FEED_URL = 'http://derStandard.at/?page=rss&ressort=Seite1'

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
            import pdb; pdb.set_trace()
            article.parse(source='derStandard', soup=soup)
        except:
            traceback.print_exc()
            return ArticleMetadata()


        # Use page header to determine category.
        category = soup.find('div', id='pageTop') \
            .find('div', id='breadcrumb') \
            .find('span', 'item').a.text

        # Convert the time.struct_time to datetime.
        timestamp = time.mktime(item.data['published_parsed'])
        date_published = datetime.fromtimestamp(timestamp)

        return ArticleMetadata(
            title=item.data['title'],
            authors=article.authors,
            category=category,
            keywords=article.keywords,
            date_published=date_published)

#p = DerStandard()
#import requests
#html = requests.get('http://derstandard.at/2000068057992/Deutschland-Eltern-duerfen-ihre-Kinder-nicht-mehr-mittels-Smartwatch-abhoeren').text
#soup = bs4.BeautifulSoup(html, 'lxml')
#item = ArticleUrl('0', 'http://derstandard.at/2000068057992/Deutschland-Eltern-duerfen-ihre-Kinder-nicht-mehr-mittels-Smartwatch-abhoeren', 'de-AT', None)
#p.get_article_metadata(item, html, soup)
