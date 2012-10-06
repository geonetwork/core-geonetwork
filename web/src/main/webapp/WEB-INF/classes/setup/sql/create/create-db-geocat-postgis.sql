-- ======================================================================
-- === Extra SQL required for geocat service
-- ======================================================================

CREATE TABLE deletedobjects (
    id integer NOT NULL,
    description text NOT NULL,
    xml text NOT NULL,
    deletiondate text NOT NULL
);


ALTER TABLE public.deletedobjects OWNER TO "www-data";


CREATE TABLE Formats
  (
    id          int,
    name        varchar(200),
    version     varchar(200),
    validated   varchar(1),
    primary key(id)
  );

-- ======================================================================


ALTER TABLE Users
    ALTER username      TYPE text,
    ALTER surname       TYPE text,
    ALTER name          TYPE text,
    ALTER address       TYPE text,
    ALTER city          TYPE text,
    ALTER state         TYPE text,
    ALTER zip           TYPE text,
    ALTER country       TYPE text,
    ALTER email         TYPE text,
    ALTER organisation  TYPE text,
    ADD streetnumber         text,
    ADD streetname           text,
    ADD postbox              text,
    ADD positionname         text,
    ADD onlineresource       text,
    ADD onlinename           text,
    ADD onlinedescription    text,
    ADD hoursofservice       text,
    ADD contactinstructions  text,
    ADD publicaccess         character(1)           default 'y',
    ADD orgacronym           text,
    ADD directnumber         text,
    ADD mobile               text,
    ADD phone                text,
    ADD facsimile            text,
    ADD email1               text,
    ADD phone1               text,
    ADD facsimile1           text,
    ADD email2               text,
    ADD phone2               text,
    ADD facsimile2           text,
    ADD parentinfo           integer,
    ADD validated            character(1)           default 'n'
    ;
-- ======================================================================

CREATE TABLE hiddenmetadataelements (
    metadataid integer NOT NULL,
    xpathexpr character varying(255) NOT NULL,
    level character varying(8) NOT NULL
);


ALTER TABLE public.hiddenmetadataelements OWNER TO "www-data";

ALTER TABLE ONLY hiddenmetadataelements ADD CONSTRAINT hiddenmetadataelements_pkey PRIMARY KEY (metadataid, xpathexpr);


--
-- Name: countries; Type: TABLE; Schema: public; Owner: www-data; Tablespace: 
--

CREATE TABLE countries (
    gid integer NOT NULL,
    "LAND" character varying(254),
    "SEARCH" character varying(254),
    "BOUNDING" character varying(254),
    the_geom geometry,
    "ID" integer,
    "DESC" text,
    CONSTRAINT enforce_dims_the_geom CHECK ((ndims(the_geom) = 2)),
    CONSTRAINT enforce_geotype_the_geom CHECK (((geometrytype(the_geom) = 'MULTIPOLYGON'::text) OR (the_geom IS NULL))),
    CONSTRAINT enforce_srid_the_geom CHECK ((srid(the_geom) = 21781))
);


ALTER TABLE public.countries OWNER TO "www-data";

--
-- Name: countries_gid_seq; Type: SEQUENCE; Schema: public; Owner: www-data
--

CREATE SEQUENCE countries_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.countries_gid_seq OWNER TO "www-data";

--
-- Name: countries_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: www-data
--

ALTER SEQUENCE countries_gid_seq OWNED BY countries.gid;


--
-- Name: countries_gid_seq; Type: SEQUENCE SET; Schema: public; Owner: www-data
--

SELECT pg_catalog.setval('countries_gid_seq', 4, true);


--
-- Name: gid; Type: DEFAULT; Schema: public; Owner: www-data
--

ALTER TABLE ONLY countries ALTER COLUMN gid SET DEFAULT nextval('countries_gid_seq'::regclass);

--
-- Name: gemeindenBB; Type: TABLE; Schema: public; Owner: www-data; Tablespace: 
--

CREATE TABLE "gemeindenBB" (
    gid integer NOT NULL,
    "GEMNAME" character varying(254),
    "BEZIRKSNR" smallint,
    "KANTONSNR" smallint,
    "OBJECTVAL" numeric(40,0),
    "BOUNDING" character varying(254),
    "SEARCH" character varying(254),
    "GEMNAME_L" character varying(254),
    the_geom geometry,
    "DESC" text
);


ALTER TABLE public."gemeindenBB" OWNER TO "www-data";

--
-- Name: gemeinden_gid_seq; Type: SEQUENCE; Schema: public; Owner: www-data
--

CREATE SEQUENCE gemeinden_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.gemeinden_gid_seq OWNER TO "www-data";

--
-- Name: gemeinden_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: www-data
--

ALTER SEQUENCE gemeinden_gid_seq OWNED BY "gemeindenBB".gid;


--
-- Name: gemeinden_gid_seq; Type: SEQUENCE SET; Schema: public; Owner: www-data
--

SELECT pg_catalog.setval('gemeinden_gid_seq', 5272, true);


--
-- Name: gid; Type: DEFAULT; Schema: public; Owner: www-data
--

ALTER TABLE ONLY "gemeindenBB" ALTER COLUMN gid SET DEFAULT nextval('gemeinden_gid_seq'::regclass);


--
-- Name: kantoneBB; Type: TABLE; Schema: public; Owner: www-data; Tablespace: 
--

CREATE TABLE "kantoneBB" (
    gid integer NOT NULL,
    "KANTONSNR" smallint,
    "NAME" character varying(254),
    "KUERZEL" character varying(4),
    "BOUNDING" character varying(254),
    "SEARCH" character varying(254),
    the_geom geometry
);


ALTER TABLE public."kantoneBB" OWNER TO "www-data";

--
-- Name: kantone_gid_seq; Type: SEQUENCE; Schema: public; Owner: www-data
--

CREATE SEQUENCE kantone_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.kantone_gid_seq OWNER TO "www-data";

--
-- Name: kantone_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: www-data
--

ALTER SEQUENCE kantone_gid_seq OWNED BY "kantoneBB".gid;


--
-- Name: kantone_gid_seq; Type: SEQUENCE SET; Schema: public; Owner: www-data
--

SELECT pg_catalog.setval('kantone_gid_seq', 5272, true);


--
-- Name: gid; Type: DEFAULT; Schema: public; Owner: www-data
--

ALTER TABLE ONLY "kantoneBB" ALTER COLUMN gid SET DEFAULT nextval('kantone_gid_seq'::regclass);


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
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
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

ALTER TABLE ONLY xlinks ALTER COLUMN gid SET DEFAULT nextval('xlinks_gid_seq'::regclass);


--
-- Data for Name: xlinks; Type: TABLE DATA; Schema: public; Owner: www-data
--



--
-- Name: xlinks_pkey; Type: CONSTRAINT; Schema: public; Owner: www-data; Tablespace: 
--

ALTER TABLE ONLY xlinks
    ADD CONSTRAINT xlinks_pkey PRIMARY KEY (gid);


--
-- Name: countries_search; Type: TABLE; Schema: public; Owner: www-data; Tablespace: 
--

CREATE TABLE countries_search (
    "ID" integer,
    "LAND" character varying(254),
    the_geom geometry
);


ALTER TABLE public.countries_search OWNER TO "www-data";

--
-- Name: gemeinden_search; Type: TABLE; Schema: public; Owner: www-data; Tablespace: 
--

CREATE TABLE gemeinden_search (
    "OBJECTVAL" numeric(40,0),
    "GEMNAME" character varying(254),
    the_geom geometry
);


ALTER TABLE public.gemeinden_search OWNER TO "www-data";

--
-- Name: kantone_search; Type: TABLE; Schema: public; Owner: www-data; Tablespace: 
--

CREATE TABLE kantone_search (
    "KANTONSNR" smallint,
    "NAME" character varying(254),
    the_geom geometry
);


ALTER TABLE public.kantone_search OWNER TO "www-data";

alter table kantone_search add PRIMARY KEY ("KANTONSNR");
alter table countries_search add PRIMARY KEY ("ID");
alter table gemeinden_search add PRIMARY KEY ("OBJECTVAL");

DELETE FROM geometry_columns where f_table_name='countriesBB' OR f_table_name='countries' OR f_table_name='non_validated' OR 
    f_table_name='xlinks' OR f_table_name='gemeindenBB' OR f_table_name='kantoneBB' OR f_table_name='spatialIndex' OR 
    f_table_name='countries_search' OR f_table_name='kantone_search' OR f_table_name='gemeinden_search';
