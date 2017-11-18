
from .app import app
from pony.orm import db_session
from nts_backend.database import Article, Provider, init as init_database
from flask import request, render_template

init_database()


@app.route('/')
@db_session
def index():
  return render_template('index.html', providers=Provider.select())
