DROP TABLE files;

CREATE TABLE files
(
  id integer NOT NULL,
  content text NOT NULL,
  mimetype character varying(255) NOT NULL,
  CONSTRAINT files_pkey PRIMARY KEY (id)
);
