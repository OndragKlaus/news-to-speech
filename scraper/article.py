from bs4 import BeautifulSoup
import newspaper
import io

import pdb


class Article(newspaper.Article):
    def parse(self, source=''):
        super().parse()
        soup = BeautifulSoup(self.html, 'lxml')
        if source == 'sz':
            text_buf = io.StringIO()
            pdb.set_trace()
            for paragraph in soup.find('section', id='article-body').find_all('p', recursive=True):
                text_buf.write(paragraph.text)
                text_buf.write('\n')
            self.text = text_buf.getvalue()
            pdb.set_trace()
            text_buf.close()
        else:
            pass


a = Article(url='http://www.sueddeutsche.de/panorama/russland-glamour-zu-vermieten-1.3752152')
a.download()
a.parse(source='sz')
