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
import xml.dom.minidom


class ArticleUrl(NamedTuple):
    id: str
    url: str
    language: str
    data: Any = None


class ArticleMetadata(NamedTuple):
    title: str = None
    authors: List[str] = None
    category: str = None
    keywords: List[str] = None
    date_published: datetime = None
    is_top_article: bool = False


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
                             soup: bs4.BeautifulSoup) -> Optional[ArticleMetadata]:
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
        self.last_modified = None

    def get_recent_article_urls(self) -> Iterable[ArticleUrl]:
        response = requests.get(self.feed_url)
        last_modified = None

        # Check for the last modification date.
        dom = xml.dom.minidom.parseString(response.text)
        nodes = dom.getElementsByTagName('lastBuildDate')
        if nodes:
            last_modified = nodes[0].firstChild.nodeValue
        if not last_modified:
            nodes = dom.getElementsByTagName('pubDate')
        if not last_modified and nodes:
            last_modified = nodes[0].firstChild.nodeValue

        # Otherwise, try to read it from the header.
        if not last_modified:
            last_modified = response.header.get('Last-Modified')

        # Skip if it hasn't changed.
        if last_modified and last_modified == self.last_modified:
            return []
        self.last_modified = last_modified

        # Parse the feed.
        feed = feedparser.parse(response.text)
        language = self.language_map[feed['feed']['language']]
        for entry in feed['entries']:
            yield ArticleUrl(entry['id'], entry['link'], language, data=entry)


class SueddeutscheZeitung(RssProvider):
    FEED_URL = 'http://rss.sueddeutsche.de/rss/TopThemen'

    def __init__(self):
        super().__init__(self.FEED_URL)

    def get_provider_id(self) -> str:
        return 'de.sueddeutsche'

    def get_article_metadata(self, item: ArticleUrl, html: str,
                             soup: bs4.BeautifulSoup) -> Optional[ArticleMetadata]:
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
            return None

        # When there's almost no content, it may be an SZ Plus article.
        if len(article.text) < 400:
            return None

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
                             soup: bs4.BeautifulSoup) -> Optional[ArticleMetadata]:
        from .article import Article
        from urllib.parse import urlparse
        import traceback

        article = Article(item.url)
        article.download(input_html=html)
        try:
            import pdb;
            pdb.set_trace()
            article.parse(source='derStandard', soup=soup)
        except:
            traceback.print_exc()
            return None

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

# p = DerStandard()
# import requests
# html = requests.get('http://derstandard.at/2000068057992/Deutschland-Eltern-duerfen-ihre-Kinder-nicht-mehr-mittels-Smartwatch-abhoeren').text
# soup = bs4.BeautifulSoup(html, 'lxml')
# item = ArticleUrl('0', 'http://derstandard.at/2000068057992/Deutschland-Eltern-duerfen-ihre-Kinder-nicht-mehr-mittels-Smartwatch-abhoeren', 'de-AT', None)
# p.get_article_metadata(item, html, soup)
