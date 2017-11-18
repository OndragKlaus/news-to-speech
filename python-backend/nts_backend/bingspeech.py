
from typing import *
from enum import Enum
import cgi
import requests


class Speaker(NamedTuple):
    language: str
    gender: str
    name: str

    ALL = []

    @classmethod
    def first(cls, language, gender='Female'):
        for speaker in cls.ALL:
            if language in speaker.language and speaker.gender == gender:
                return speaker
        return None


Speaker.ALL = [Speaker(*a) for a in [
    ("ar-EG",   "Female", "Hoda"),
    ("ar-SA", "Male", "Naayf"),
    ("ca-ES", "Female", "HerenaRUS"),
    ("cs-CZ", "Male", "Vit"),
    ("da-DK", "Female", "HelleRUS"),
    ("de-AT", "Male", "Michael"),
    ("de-CH", "Male", "Karsten"),
    ("de-DE", "Female", "Hedda"),
    ("de-DE", "Female", "HeddaRUS"),
    ("de-DE", "Male", "Stefan Apollo) "),
    ("el-GR", "Male", "Stefanos"),
    ("en-AU", "Female", "Catherine"),
    ("en-AU", "Female", "HayleyRUS"),
    ("en-CA", "Female", "Linda"),
    ("en-CA", "Female", "HeatherRUS"),
    ("en-GB", "Female", "Susan Apollo)"),
    ("en-GB", "Female", "HazelRUS"),
    ("en-GB", "Male", "George Apollo)"),
    ("en-IE", "Male", "Shaun"),
    ("en-IN", "Female", "Heera Apollo)"),
    ("en-IN", "Female", "PriyaRUS"),
    ("en-IN", "Male", "Ravi Apollo)"),
    ("en-US", "Female", "ZiraRUS"),
    ("en-US", "Female", "JessaRUS"),
    ("en-US", "Male", "BenjaminRUS"),
    ("es-ES", "Female", "Laura Apollo)"),
    ("es-ES", "Female", "HelenaRUS"),
    ("es-ES", "Male", "Pablo Apollo)"),
    ("es-MX", "Female", "HildaRUS"),
    ("es-MX", "Male", "Raul Apollo)"),
    ("fi-FI", "Female", "HeidiRUS"),
    ("fr-CA", "Female", "Caroline"),
    ("fr-CA", "Female", "HarmonieRUS"),
    ("fr-CH", "Male", "Guillaume"),
    ("fr-FR", "Female", "Julie Apollo)"),
    ("fr-FR", "Female", "HortenseRUS"),
    ("fr-FR", "Male", "Paul Apollo)"),
    ("he-IL", "Male", "Asaf"),
    ("hi-IN", "Female", "Kalpana Apollo)"),
    ("hi-IN", "Female", "Kalpana"),
    ("hi-IN", "Male", "Hemant"),
    ("hu-HU", "Male", "Szabolcs"),
    ("id-ID", "Male", "Andika"),
    ("it-IT", "Male", "Cosimo Apollo)"),
    ("ja-JP", "Female", "Ayumi Apollo)"),
    ("ja-JP", "Male", "Ichiro Apollo)"),
    ("ja-JP", "Female", "HarukaRUS"),
    ("ja-JP", "Female", "LuciaRUS"),
    ("ja-JP", "Male", "EkaterinaRUS"),
    ("ko-KR", "Female", "HeamiRUS"),
    ("nb-NO", "Female", "HuldaRUS"),
    ("nl-NL", "Female", "HannaRUS"),
    ("pl-PL", "Female", "PaulinaRUS"),
    ("pt-BR", "Female", "HeloisaRUS"),
    ("pt-BR", "Male", "Daniel Apollo)"),
    ("pt-PT", "Female", "HeliaRUS"),
    ("ro-RO", "Male", "Andrei"),
    ("ru-RU", "Female", "Irina Apollo)"),
    ("ru-RU", "Male", "Pavel Apollo)"),
    ("sk-SK", "Male", "Filip"),
    ("sv-SE", "Female", "HedvigRUS"),
    ("th-TH", "Male", "Pattara"),
    ("tr-TR", "Female", "SedaRUS"),
    ("zh-CN", "Female", "HuihuiRUS"),
    ("zh-CN", "Female", "Yaoyao Apollo)"),
    ("zh-CN", "Male", "Kangkang Apollo)"),
    ("zh-HK", "Female", "Tracy Apollo)"),
    ("zh-HK", "Female", "TracyRUS"),
    ("zh-HK", "Male", "Danny Apollo)"),
    ("zh-TW", "Female", "Yating Apollo)"),
    ("zh-TW", "Female", "HanHanRUS"),
    ("zh-TW", "Male", "Zhiwei Apollo)"),
]]


class OutputFormat(Enum):
    PCM_16K = 'riff-16khz-16bit-mono-pcm'
    MP3_128 = 'audio-16khz-128kbitrate-mono-mp3'
    MP3_64 = 'audio-16khz-64kbitrate-mono-mp3'
    MP3_32 = 'audio-16khz-32kbitrate-mono-mp3'


class BingSpeechApi:

    def __init__(self, api_key, session=None):
        self.api_key = api_key
        self.token = None

    def issue_token(self):
        url = 'https://api.cognitive.microsoft.com/sts/v1.0/issueToken'
        headers = {'Ocp-Apim-Subscription-Key': self.api_key}
        response = requests.post(url, headers=headers)
        response.raise_for_status()
        self.token = response.text

    def synth(self, text: str, speaker: Speaker, format: OutputFormat = OutputFormat.MP3_32) -> bytes:
        # Headers for the XML Post request.
        headers = {
            'Authorization': 'Bearer ' + self.token,
            'Content-type': 'application/ssml+xml',
            'X-Microsoft-OutputFormat': format.value
        }

        # Generate the XML request data.
        template = "<speak version='1.0' xml:lang='{language}'>"\
            "<voice xml:lang='{language}' xml:gender='{gender}' "\
                    "name='Microsoft Server Speech Text to Speech Voice ({language}, {speaker})'>"\
                "{text}"\
            "</voice>"\
        "</speak>"

        xml = template.format(
            language=cgi.escape(speaker.language),
            gender=cgi.escape(speaker.gender),
            speaker=cgi.escape(speaker.name),
            text=cgi.escape(text)
        )

        # Issue the request.
        url = 'https://speech.platform.bing.com/synthesize'
        response = requests.post(url, data=xml, headers=headers, stream=True)
        response.raise_for_status()
        return response.content
