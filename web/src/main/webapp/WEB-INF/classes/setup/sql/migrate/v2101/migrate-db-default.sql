UPDATE Settings SET value='2.10.1' where id=15;
update formats set validated='y' where validated is null;


CREATE TABLE public.schematron
(
  id integer NOT NULL,
  file character varying(255) NOT NULL,
  isoschema character varying(255) NOT NULL,
  required boolean NOT NULL,
  CONSTRAINT schematron_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);

CREATE TABLE public.schematroncriteria
(
  id integer NOT NULL,
  type integer NOT NULL,
  value character varying(255) NOT NULL,
  schematron integer NOT NULL,
  CONSTRAINT schematroncriteria_pkey PRIMARY KEY (id),
  CONSTRAINT fk_hlw63y63ilnkr5e3ds2rxc33j FOREIGN KEY (schematron)
      REFERENCES public.schematron (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

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