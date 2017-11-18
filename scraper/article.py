from bs4 import BeautifulSoup
import newspaper
import io


class Article(newspaper.Article):
    def _find_sz_article_body(soup):
        article_body = soup.find('section', id='article-body')

        # dpa article
        if article_body is None:
            site_content = soup.find('div', id='sitecontent')
            article_body = site_content.find('section', 'body')

        return article_body

    def _find_sz_author_spans(soup):
        author_section = soup.find('section', 'authors')

        if author_section is not None:
            author_more_info = author_section.find('span', 'moreInfo')
            return author_more_info.find_all('span')
        else:
            return soup.find_all('span', 'authors')

    def parse(self, source=''):
        super().parse()
        soup = BeautifulSoup(self.html, 'lxml')
        if source == 'sz':
            text_buf = io.StringIO()

            article_body = Article._find_sz_article_body(soup)

            if article_body is not None:
                article_body = Article._find_sz_article_body(soup)
                for paragraph in article_body.find_all('p', recursive=False):
                    text_buf.write(paragraph.text)
                    text_buf.write('\n\n')
                self.text = text_buf.getvalue()
                text_buf.close()

                author_spans = Article._find_sz_author_spans(soup)
                self.authors = []
                for author in author_spans:
                    self.authors.append(author.text.strip())

            else:
                self.text = ''
        else:
            pass
