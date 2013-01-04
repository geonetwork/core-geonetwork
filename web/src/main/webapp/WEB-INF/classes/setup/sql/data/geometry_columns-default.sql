INSERT INTO geometry_columns VALUES ('', 'public', 'countriesBB', 'the_geom', 2, 21781, 'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('', 'public', 'countries', 'the_geom', 2, 21781, 'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('', 'public', 'non_validated', 'the_geom', 2, 21781, 'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('', 'public', 'xlinks', 'the_geom', 2, 21781, 'MULTIPOLYGON');
-- INSERT INTO geometry_columns VALUES ('', 'public', 'spatialIndex', 'the_geom', 2, 4326, 'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('', 'public', 'gemeindenBB', 'the_geom', 2, 21781, 'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('', 'public', 'kantoneBB', 'the_geom', 2, 21781, 'MULTIPOLYGON');

INSERT INTO geometry_columns VALUES ('','public','countries_search', 'the_geom', 2,21781,'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('','public','kantone_search', 'the_geom', 2,21781,'MULTIPOLYGON');
INSERT INTO geometry_columns VALUES ('','public','gemeinden_search', 'the_geom', 2,21781,'MULTIPOLYGON');


alter table kantone_search add column "SEARCH" varchar(254);
UPDATE kantone_search SET "SEARCH"="kantoneBB"."SEARCH" from "kantoneBB" WHERE kantone_search."KANTONSNR"="kantoneBB"."KANTONSNR";

alter table countries_search add column "SEARCH" varchar(254);
UPDATE countries_search SET "SEARCH"="countries"."SEARCH" from "countries" WHERE countries_search."ID"="countries"."ID";

alter table gemeinden_search add column "SEARCH" varchar(254);
UPDATE gemeinden_search SET "SEARCH"="gemeindenBB"."SEARCH" from "gemeindenBB" WHERE gemeinden_search."OBJECTVAL"="gemeindenBB"."OBJECTVAL";
