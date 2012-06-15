CREATE TABLE publish_tracking (
    uuid varchar(60),
    entity varchar(60) not null,
    validated character(1) not null,
    published character(1) not null,
    failurerule varchar(200),
    failurereasons text,
    changetime timestamp not null default current_timestamp,
    changedate date not null default current_date
);


ALTER TABLE public.publish_tracking OWNER TO "www-data";