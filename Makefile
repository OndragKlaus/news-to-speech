
.PHONY: scraper
scraper:
	python -c "import scraper; scraper.main()"

.PHONY: psql
psql:
	python -c "import database; database.psql()"

.PHONY: create-tables
create-tables:
	python -c "import database; database.psql()" -f database/create_tables.sql
