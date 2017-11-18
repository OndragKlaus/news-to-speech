"""
Generates speech audio files for articles in the database.
"""

import argparse
import functools
import io
import logging
import operator
import pydub, pydub.playback
import time

from . import database
from .bingspeech import Speaker, BingSpeechApi
from .config import config
from .tokenizer import Tokenizer, tokens_to_sentences

parser = argparse.ArgumentParser()
parser.add_argument('--once', action='store_true')
parser.add_argument('--interval', type=float, default=10.0)


def synth_article(service: BingSpeechApi, tok: Tokenizer, title: str, text: str) -> bytes:
    """
    Generates an MP3 audio file for the specified title and text.
    """

    speaker = next(x for x in Speaker.ALL if x.name == 'HeddaRUS')
    def audiofy(text):
        return pydub.AudioSegment.from_mp3(io.BytesIO(service.synth(text, speaker)))

    segments = []
    segments.append(audiofy('Artikel: ' + title))
    print('TITLE:', title)
    for sentence in tokens_to_sentences(tok.tokenize('Zusammenfassung: ' + text, 'en')):  # XXX en?
        print('  *', sentence)
        segments.append(audiofy(sentence))

    result = functools.reduce(operator.add, segments)
    pydub.playback.play(result)


def main():
    args = parser.parse_args()
    database.init()
    logging.basicConfig(level=logging.INFO)

    logging.info('Connecting to Bing Speech API ...')
    service = BingSpeechApi(config['bingspeechapi']['key'])
    service.issue_token()
    tok = Tokenizer(config['linguisticsapi']['key'])

    logging.info('Checking pending articles ...')
    while True:
        for article_id in database.get_articles(has_audioblob=False):
            article = database.get_article_by_id(article_id)
            logging.info('SYNTHESIZING {} -- {}'.format(article_id, article['url']))
            try:
                synth_article(service, tok, article['title'], article['summary'])
            except Exception as exc:
                logging.exception(exc)
        if args.once:
            break
        time.sleep(args.interval)
