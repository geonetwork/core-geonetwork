INSERT INTO geometry_columns VALUES ('', 'public', 'countriesBB', 'the_geom', 2, 21781, 'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('', 'public', 'countries', 'the_geom', 2, 21781, 'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('', 'public', 'non_validated', 'the_geom', 2, 21781, 'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('', 'public', 'xlinks', 'the_geom', 2, 21781, 'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('', 'public', 'spatialIndex', 'the_geom', 2, 4326, 'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('', 'public', 'gemeindenBB', 'the_geom', 2, 21781, 'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('', 'public', 'kantoneBB', 'the_geom', 2, 21781, 'MULTIPOLYGON');

create table countries_search as select "ID","LAND",ST_Buffer(ST_Transform(the_geom, 4326), .001) from countries;
ALTER TABLE countries_search RENAME st_buffer TO the_geom;
alter TABLE countries_search OWNER TO "www-data";

create table kantone_search as select "KANTONSNR","NAME",ST_Buffer(ST_Transform(the_geom, 4326), .001) from "kantoneBB";
ALTER TABLE kantone_search RENAME st_buffer TO the_geom;
alter TABLE kantone_search OWNER TO "www-data";

create table gemeinden_search as select "OBJECTVAL","GEMNAME",ST_Buffer(ST_Transform(the_geom, 4326), .001) from "gemeindenBB";
ALTER TABLE gemeinden_search RENAME st_buffer TO the_geom;
alter TABLE gemeinden_search OWNER TO "www-data";

INSERT INTO geometry_columns VALUES ('','public','countries_search', 'the_geom', 2,21781,'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('','public','kantone_search', 'the_geom', 2,21781,'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('','public','gemeinden_search', 'the_geom', 2,21781,'MULTIPOLYGON');
