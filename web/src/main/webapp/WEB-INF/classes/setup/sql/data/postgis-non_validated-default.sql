--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: non_validated; Type: TABLE; Schema: public; Owner: www-data; Tablespace: 
--

CREATE TABLE non_validated (
    gid integer NOT NULL,
    "ID" numeric,
    "GEO_ID" text,
    "DESC" text,
    "SEARCH" text,
    the_geom geometry,
    "SHOW_NATIVE" character(1),
    CONSTRAINT enforce_dims_the_geom CHECK ((ndims(the_geom) = 2)),
    CONSTRAINT enforce_geotype_the_geom CHECK (((geometrytype(the_geom) = 'MULTIPOLYGON'::text) OR (the_geom IS NULL))),
    CONSTRAINT enforce_srid_the_geom CHECK ((srid(the_geom) = 21781))
);


ALTER TABLE public.non_validated OWNER TO "www-data";

--
-- Name: non_validated_gid_seq; Type: SEQUENCE; Schema: public; Owner: www-data
--

CREATE SEQUENCE non_validated_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.non_validated_gid_seq OWNER TO "www-data";

--
-- Name: non_validated_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: www-data
--

ALTER SEQUENCE non_validated_gid_seq OWNED BY non_validated.gid;


--
-- Name: non_validated_gid_seq; Type: SEQUENCE SET; Schema: public; Owner: www-data
--

SELECT pg_catalog.setval('non_validated_gid_seq', 1552, true);


--
-- Name: gid; Type: DEFAULT; Schema: public; Owner: www-data
--

ALTER TABLE ONLY non_validated ALTER COLUMN gid SET DEFAULT nextval('non_validated_gid_seq'::regclass);


--
-- Data for Name: non_validated; Type: TABLE DATA; Schema: public; Owner: www-data
--



--
-- Name: non_validated_pkey; Type: CONSTRAINT; Schema: public; Owner: www-data; Tablespace: 
--

ALTER TABLE ONLY non_validated
    ADD CONSTRAINT non_validated_pkey PRIMARY KEY (gid);


--
-- Name: non_validated_the_geom_gist; Type: INDEX; Schema: public; Owner: www-data; Tablespace: 
--

CREATE INDEX non_validated_the_geom_gist ON non_validated USING gist (the_geom);


--
-- Name: non_validated; Type: ACL; Schema: public; Owner: www-data
--

REVOKE ALL ON TABLE non_validated FROM PUBLIC;
REVOKE ALL ON TABLE non_validated FROM "www-data";
GRANT ALL ON TABLE non_validated TO "www-data";


--
-- Name: non_validated_gid_seq; Type: ACL; Schema: public; Owner: www-data
--

REVOKE ALL ON SEQUENCE non_validated_gid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE non_validated_gid_seq FROM "www-data";
GRANT ALL ON SEQUENCE non_validated_gid_seq TO "www-data";


--
-- PostgreSQL database dump complete
--

