from bs4 import BeautifulSoup
import newspaper
import io


class Article(newspaper.Article):
    def parse(self, source=''):
        super().parse()
        soup = BeautifulSoup(self.html, 'lxml')
        if source == 'sz':
            text_buf = io.StringIO()
            for paragraph in soup.find('section', id='article-body').find_all('p', recursive=False):
                text_buf.write(paragraph.text)
                text_buf.write('\n\n')
            self.text = text_buf.getvalue()
            text_buf.close()

            author_section = soup.find('section', 'authors')
            author_more_info = author_section.find('span', 'moreInfo')
            self.authors = []
            for author in author_more_info.find_all('span'):
                self.authors.append(author.text)
        else:
            pass
