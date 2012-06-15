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
