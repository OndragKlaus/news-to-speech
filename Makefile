
scraper:
	python -c "import scraper; scraper.main()"

psql:
	python -c "import database; database.psql()"

create-tables:
	python -c "import database; database.psql()" -f database/create_tables.sql
