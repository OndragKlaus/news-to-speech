## news-to-speech

### Development Environment

#### 1) Virtual Environment

Use pipenv to install the virtual environment:

    $ pipenv install

If you can't use `pipenv shell` (eg. on Git for Windows where starting `bash`
overwrites `PS1`), try:

    $ . $(pipenv --venv)/Scripts/activate

#### 2) PostgresSQL configuration

Copy `database/config.toml.example` to `database/config.toml` and update the
parameters to connect to your Postgres instance. Then run the following
command to initialize the database tables:

    $ make create-tables

#### 3) Run the article scraper

To collect articles into the database, run the scraper with make:

    $ make scraper
