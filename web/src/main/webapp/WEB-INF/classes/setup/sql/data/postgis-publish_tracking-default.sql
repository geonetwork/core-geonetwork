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
-- Name: publish_tracking; Type: TABLE; Schema: public; Owner: www-data; Tablespace: 
--

CREATE TABLE publish_tracking (
    uuid character varying(60),
    entity character varying(60) NOT NULL,
    validated character(1) NOT NULL,
    published character(1) NOT NULL,
    failurerule character varying(200),
    failurereasons text,
    changetime timestamp without time zone DEFAULT now() NOT NULL,
    changedate date DEFAULT ('now'::text)::date NOT NULL
);


ALTER TABLE public.publish_tracking OWNER TO "www-data";

--
-- Data for Name: publish_tracking; Type: TABLE DATA; Schema: public; Owner: www-data
--



--
-- PostgreSQL database dump complete
--

