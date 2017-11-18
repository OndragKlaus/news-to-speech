
.PHONY: scraper
scraper:
	python -c "import nts_backend.scraper; nts_backend.scraper.main()"

.PHONY: psql
psql:
	python -c "import nts_backend.database; nts_backend.database.psql()"

.PHONY: create-tables
create-tables:
	python -c "import nts_backend.database; nts_backend.database.psql()" -f create_tables.sql
