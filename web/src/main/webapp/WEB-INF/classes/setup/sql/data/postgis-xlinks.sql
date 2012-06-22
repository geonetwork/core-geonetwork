--
-- PostgreSQL database dump
--

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: xlinks; Type: TABLE; Schema: public; Owner: www-data; Tablespace: 
--

CREATE TABLE xlinks (
    gid integer NOT NULL,
    "ID" numeric(19,0),
    "DESC" text,
    "GEO_ID" text,
    "SEARCH" text,
    the_geom geometry,
    "SHOW_NATIVE" character(1),
    CONSTRAINT enforce_dims_the_geom CHECK ((ndims(the_geom) = 2)),
    CONSTRAINT enforce_geotype_the_geom CHECK (((geometrytype(the_geom) = 'MULTIPOLYGON'::text) OR (the_geom IS NULL))),
    CONSTRAINT enforce_srid_the_geom CHECK ((srid(the_geom) = 21781))
);


ALTER TABLE public.xlinks OWNER TO "www-data";

--
-- Name: xlinks_gid_seq; Type: SEQUENCE; Schema: public; Owner: www-data
--

CREATE SEQUENCE xlinks_gid_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.xlinks_gid_seq OWNER TO "www-data";

--
-- Name: xlinks_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: www-data
--

ALTER SEQUENCE xlinks_gid_seq OWNED BY xlinks.gid;


--
-- Name: xlinks_gid_seq; Type: SEQUENCE SET; Schema: public; Owner: www-data
--

SELECT pg_catalog.setval('xlinks_gid_seq', 296, true);


--
-- Name: gid; Type: DEFAULT; Schema: public; Owner: www-data
--

ALTER TABLE xlinks ALTER COLUMN gid SET DEFAULT nextval('xlinks_gid_seq'::regclass);


--
-- Data for Name: xlinks; Type: TABLE DATA; Schema: public; Owner: www-data
--

--
-- Name: xlinks_pkey; Type: CONSTRAINT; Schema: public; Owner: www-data; Tablespace: 
--

ALTER TABLE ONLY xlinks
    ADD CONSTRAINT xlinks_pkey PRIMARY KEY (gid);


--
-- Name: xlinks_the_geom_gist; Type: INDEX; Schema: public; Owner: www-data; Tablespace: 
--

CREATE INDEX xlinks_the_geom_gist ON xlinks USING gist (the_geom);


--
-- Name: xlinks; Type: ACL; Schema: public; Owner: www-data
--

REVOKE ALL ON TABLE xlinks FROM PUBLIC;
REVOKE ALL ON TABLE xlinks FROM "www-data";
GRANT ALL ON TABLE xlinks TO "www-data";


--
-- Name: xlinks_gid_seq; Type: ACL; Schema: public; Owner: www-data
--

REVOKE ALL ON SEQUENCE xlinks_gid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE xlinks_gid_seq FROM "www-data";
GRANT ALL ON SEQUENCE xlinks_gid_seq TO "www-data";


--
-- PostgreSQL database dump complete
--

