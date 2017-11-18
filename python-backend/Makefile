
.PHONY: scraper
scraper:
	python -c "import nts_backend.scraper; nts_backend.scraper.main()"

.PHONY: synth
synth:
	python -c "import nts_backend.synth; nts_backend.synth.main()"

.PHONY: synth-export
synth-export:
	python -c "import nts_backend.synth; nts_backend.synth.main()" --export-to export

.PHONY: frontend
frontend:
	FLASK_APP=frontend/__init__.py flask run

.PHONY: psql
psql:
	python -c "import nts_backend.database; nts_backend.database.psql()"

.PHONY: create-tables
create-tables:
	python -c "import nts_backend.database; nts_backend.database.psql()" -f create_tables.sql
