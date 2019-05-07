DROP VIEW IF EXISTS public.ontology_term;
DROP VIEW IF EXISTS public.ontology_term_column_correspondance;
DROP TABLE IF EXISTS public.ontology_term_table;

-- TABLES

create table dataset_access_level_table (
	dataset_id integer NOT NULL REFERENCES beacon_dataset_table(id),
	parent_field text NOT NULL,
	field text NOT NULL,
	access_level text NOT NULL,
	CONSTRAINT dataset_access_level_table_pkey PRIMARY KEY (dataset_id, parent_field, field),
  CONSTRAINT dataset_access_level_table_access_level_check CHECK (access_level = ANY (ARRAY['NOT_SUPPORTED', 'PUBLIC', 'REGISTERED', 'CONTROLLED']))
);

CREATE TABLE ontology_term_table(
	id serial NOT NULL PRIMARY KEY,
	ontology TEXT NOT NULL,
	term TEXT NOT NULL,
	sample_table_column_name TEXT NOT NULL,
	sample_table_column_value TEXT,
	additional_comments TEXT
);

ALTER TABLE beacon_sample_table ADD COLUMN IF NOT EXISTS sex text DEFAULT 'UNKNOWN';
alter table beacon_sample_table add column IF NOT EXISTS age integer;
alter table beacon_sample_table add column IF NOT EXISTS age_of_onset integer;
alter table beacon_sample_table add column IF NOT EXISTS provenance text;

-- VIEWS

CREATE OR REPLACE VIEW ontology_term AS
SELECT id, ontology, term
FROM ontology_term_table;

CREATE OR REPLACE VIEW ontology_term_column_correspondance AS
SELECT id, ontology, term, sample_table_column_name, sample_table_column_value, additional_comments
FROM ontology_term_table;

CREATE OR REPLACE VIEW public.beacon_dataset_access_level AS
	SELECT dat.stable_id as dataset_stable_id,
		parent_field,
		field,
		access_level
	FROM dataset_access_level_table dal
	INNER JOIN beacon_dataset_table dat ON dat.id=dal.dataset_id;
