create table countries_search as select "ID","LAND",ST_Buffer(the_geom, 2) from countries;
INSERT INTO geometry_columns VALUES ('','public','countries_search', 'the_geom', 2,21781,'MULTIPOLYGON');
alter TABLE countries_search OWNER TO "www-data";

create table kantone_search as select "KANTONSNR","NAME",ST_Buffer(the_geom, 2) from "kantoneBB";
INSERT INTO geometry_columns VALUES ('','public','kantone_search', 'the_geom', 2,21781,'MULTIPOLYGON');
alter TABLE kantone_search OWNER TO "www-data";

create table gemeinden_search as select "OBJECTVAL","GEMNAME",ST_Buffer(the_geom, 2) from "gemeindenBB";
INSERT INTO geometry_columns VALUES ('','public','gemeinden_search', 'the_geom', 2,21781,'MULTIPOLYGON');
alter TABLE gemeinden_search OWNER TO "www-data";