DROP TABLE files;

CREATE TABLE files
(
  id integer NOT NULL,
  content text NOT NULL,
  mimetype character varying(255) NOT NULL,
  CONSTRAINT files_pkey PRIMARY KEY (id)
);

UPDATE metadata
  SET data = REPLACE(
      data,
      'theme.inspire-theme', 'theme.httpinspireeceuropaeutheme-theme')
  WHERE data LIKE '%theme.inspire-theme%';
