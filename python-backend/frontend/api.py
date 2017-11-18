import flask

from .app import app
from flask import Flask
from flask_restful import Resource, Api, reqparse
from nts_backend import database
from pony import orm


class Providers(Resource):
    @orm.db_session
    def get(self):
        res = orm.select(p for p in database.Provider)
        return [p.to_dict() for p in res]


class Categories(Resource):
    @orm.db_session
    def get(self):
        res = orm.select(c for c in database.Category)
        return [c.to_dict() for c in res]


class Articles(Resource):
    parser = reqparse.RequestParser()
    parser.add_argument('article_id', type=int, help='Id of the article')
    parser.add_argument('provider_id', type=int, help='Id of the article provider')
    parser.add_argument('category_id', type=int, help='Id of the article category')

    @orm.db_session
    def get(self):
        args = self.parser.parse_args()
        query = database.Article.select()
        if args['article_id']:
            query = query.filter(lambda a: a.article_id == args['article_id'])
        if args['provider_id']:
            query = query.filter(lambda a: a.provider_id == database.Provider[args['provider_id']])
        if args['category_id']:
            query = query.filter(lambda a: a.category_id == database.Category[args['category_id']])
        exclude = ['guid', 'date_published',
            'date_summarized', 'is_top_article']
        return [x.to_dict(exclude=exclude) for x in query]


api = Api(app)
api.add_resource(Providers, '/providers')
api.add_resource(Categories, '/categories')
api.add_resource(Articles, '/articles')


@app.route('/audiofile/<article_id>')
@orm.db_session
def serve_audio_file(article_id):
    article = database.Article.get(article_id=article_id)
    if article and article.audioblob:
        headers = {
                'Content-Type': 'audio/mp3',
                'Content-Disposition': 'attachment; filename=article.mp3'
        }
        response = flask.Response(response=article.audioblob, status=200, headers=headers)
        return response
    else:
        flask.abort(404)
