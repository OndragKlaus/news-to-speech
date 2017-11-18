from .config import config

import requests
import json


class Tokenizer:
    API_URL = 'https://westus.api.cognitive.microsoft.com/linguistics/v1.0/analyze'

    def __init__(self, api_key):
        self.api_key = api_key

    def tokenize(self, text, language):
        payload = {
            'language': language,
            'analyzerIds': ['08ea174b-bfdb-4e64-987e-602f85da7f72'],
            'text': text
        }
        headers = {
            'Content-Type': 'application/json',
            'Ocp-Apim-Subscription-Key': self.api_key
        }

        req = requests.post(
                Tokenizer.API_URL, data=json.dumps(payload), headers=headers)
        return json.loads(req.content)[0]['result']


def tokens_to_sentences(tokenization):
    sentences = []
    for sentence_token in tokenization:
        word_list = [token['NormalizedToken'] for token in sentence_token['Tokens']]
        sentence = ' '.join(word_list)
        sentences.append(sentence)
    return sentences
