"""
Generates speech audio files for articles in the database.
"""

import argparse
import functools
import io
import os
import logging
import operator
import pydub, pydub.playback
import tempfile
import time

from . import database
from .bingspeech import Speaker, BingSpeechApi
from .config import config
from .tokenizer import Tokenizer, tokens_to_sentences

parser = argparse.ArgumentParser()
parser.add_argument('--once', action='store_true')
parser.add_argument('--interval', type=float, default=10.0)
parser.add_argument('--export-to')


def audio_memory_export(segment, *args, **kwargs):
    with tempfile.NamedTemporaryFile(delete=False) as fp:
        try:
            segment.export(fp, *args, **kwargs)
            fp.close()
            with open(fp.name, 'rb') as src:
                return src.read()
        finally:
            try:
                os.remove(fp.name)
            except FileNotFoundError:
                pass


def synth_article(service: BingSpeechApi, tok: Tokenizer, article: dict) -> bytes:
    """
    Generates an MP3 audio file for the specified title and text.
    """

    logging.info('SYNTHESIZING {} -- {}'.format(article['article_id'], article['url']))

    speaker = next(x for x in Speaker.ALL if x.name == 'HeddaRUS')
    def audiofy(text):
        return pydub.AudioSegment.from_mp3(io.BytesIO(service.synth(text, speaker)))

    segments = []
    segments.append(audiofy('Artikel: ' + article['title']))
    print('TITLE:', article['title'])
    for sentence in tokens_to_sentences(tok.tokenize('Zusammenfassung: ' + article['summary'], 'en')):  # XXX en?
        print('  *', sentence)
        segments.append(audiofy(sentence))

    result = functools.reduce(operator.add, segments)
    audioblob = audio_memory_export(result, format='mp3', bitrate='64')
    database.save_article_audioblob(article['article_id'], audioblob)


def main():
    args = parser.parse_args()
    database.init()
    logging.basicConfig(level=logging.INFO)

    if args.export_to:
        ids = database.get_articles(has_audioblob=True)
        logging.info('Exporting {} synthesized audio files to {}'.format(len(ids), args.export_to))
        os.makedirs(args.export_to, exist_ok=True)
        for article_id in ids:
            audiodata = database.get_article_audioblob(article_id)
            filename = os.path.join(args.export_to, '{}.mp3'.format(article_id))
            logging.info('  - {}'.format(filename))
            with open(filename, 'wb') as fp:
                fp.write(audiodata)
        return

    logging.info('Connecting to Bing Speech API ...')
    service = BingSpeechApi(config['bingspeechapi']['key'])
    service.issue_token()
    tok = Tokenizer(config['linguisticsapi']['key'])

    logging.info('Checking pending articles ...')
    while True:
        for article_id in database.get_articles(has_audioblob=False):
            article = database.get_article_by_id(article_id)
            try:
                synth_article(service, tok, article)
            except Exception as exc:
                logging.exception(exc)
        if args.once:
            break
        time.sleep(args.interval)
