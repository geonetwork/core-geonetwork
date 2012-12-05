-- TODO if needed to migrate 2.4.2 and previous version

UPDATE Settings SET value='1.1.0' WHERE name='version';
UPDATE Settings SET value='geocat' WHERE name='subVersion';
UPDATE Settings SET value='prefer_locale' where name='only';

INSERT INTO settings VALUES ( 3, 1, 'wmtTimestamp', '20120809');

UPDATE settings SET value='0 0 1 * * ?' where name = 'every';
INSERT INTO Settings VALUES (24,20,'securePort','443');

DROP VIEW sharedusers;
ALTER TABLE users ALTER "password" TYPE character varying(120);
ALTER TABLE users ADD security varchar(128);
ALTER TABLE users ADD authtype varchar(32);
update users SET security = 'update_hash_required';

DELETE FROM StatusValuesDes where langid='fra' or langid='deu';
DELETE FROM categoriesdes where langid='fra' or langid='deu';
DELETE FROM CswServerCapabilitiesInfo where langid='fra' or langid='deu';

DELETE FROM isolanguagesdes where langid='fra' or langid='deu' or iddes='500' or iddes='501';
DELETE FROM isolanguages where code='fra' or code='deu';
DELETE FROM groupsdes where langid='fra' or langid='deu';

UPDATE operationsdes SET langid='ger' where langid='deu';
UPDATE operationsdes SET langid='fre' where langid='fra';

UPDATE statusvaluesdes SET langid='ger' where langid='deu';
UPDATE statusvaluesdes SET langid='fre' where langid='fra';

UPDATE regionsdes SET langid='ger' where langid='deu';
UPDATE regionsdes SET langid='fre' where langid='fra';

UPDATE categoriesdes SET langid='ger' where langid='deu';
UPDATE categoriesdes SET langid='fre' where langid='fra';


DELETE FROM Languages where id='fra' or id='deu';

