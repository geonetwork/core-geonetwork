-- TODO if needed to migrate 2.4.2 and previous version

UPDATE Settings SET value='1.1.0' WHERE name='version';
UPDATE Settings SET value='geocat' WHERE name='subVersion';
UPDATE Settings SET value='prefer_locale' where name='only';

INSERT INTO settings VALUES ( 3, 1, 'wmtTimestamp', '20120809');

INSERT INTO Settings VALUES (89,80,'bind',NULL);
INSERT INTO Settings VALUES (102,86,'subtree','false');
INSERT INTO Settings VALUES (140,89,'bindDn','cn=fake.name,ou=people,dc=fao,dc=org');
INSERT INTO Settings VALUES (141,89,'bindPw','fake_password');
INSERT INTO Settings VALUES (150,80,'anonBind','true');

UPDATE settings SET value='0 0 1 * * ?' where name = 'every';

ALTER TABLE HarvestHistory ADD elapsedTime int;
