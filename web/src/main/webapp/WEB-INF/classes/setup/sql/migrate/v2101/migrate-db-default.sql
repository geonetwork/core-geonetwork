UPDATE Settings SET value='2.10.1' where id=15;
update formats set validated='y' where validated is null;

CREATE TABLE public.schematron
(
  id integer NOT NULL,
  file character varying(255) NOT NULL,
  schemaname character varying(255) NOT NULL,
  CONSTRAINT schematron_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.schematron
  OWNER TO "www-data";


CREATE TABLE public.schematroncriteria
(
  id integer NOT NULL,
  type integer NOT NULL,
  value character varying(255) NOT NULL,
  group_name character varying(255) NOT NULL,
  CONSTRAINT schematroncriteria_pkey PRIMARY KEY (id),
  CONSTRAINT fk_6cnta0l4uqjcaypy97f5nnmkm FOREIGN KEY (group_name)
      REFERENCES public.schematroncriteriagroup (name) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.schematroncriteria
  OWNER TO "www-data";


CREATE TABLE public.schematroncriteriagroup
(
  name character varying(255) NOT NULL,
  requirement integer NOT NULL,
  schematron integer NOT NULL,
  CONSTRAINT schematroncriteriagroup_pkey PRIMARY KEY (name),
  CONSTRAINT fk_on8ywt18r3quvll6lb0oprga7 FOREIGN KEY (schematron)
      REFERENCES public.schematron (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.schematroncriteriagroup
  OWNER TO "www-data";



CREATE TABLE public.schematrondes
(
  iddes integer NOT NULL,
  label character varying(96) NOT NULL,
  langid character varying(5) NOT NULL,
  CONSTRAINT schematrondes_pkey PRIMARY KEY (iddes, langid),
  CONSTRAINT fk_sh1xwulyb1jeoc6puqpiuc5d2 FOREIGN KEY (iddes)
      REFERENCES public.schematron (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.schematrondes
  OWNER TO "www-data";
