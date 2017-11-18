"""
Generates speech audio files for articles in the database.
"""

import logging

from . import database
from .bingspeech import BingSpeechApi
from .config import config
from .tokenizer import Tokenizer, tokens_to_sentences


def main():
    database.init()
    logging.basicConfig(level=logging.INFO)
    for article_id in database.get_articles(has_audioblob=False):
        article = database.get_article_by_id(article_id)
        tok = Tokenizer(config['linguisticsapi']['key'])
        sentences = tokens_to_sentences(tok.tokenize(article['summary'], 'en'))
        logging.info('SYNTHESIZING {} -- {}'.format(article_id, article['url']))
