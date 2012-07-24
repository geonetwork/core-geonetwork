INSERT INTO Settings VALUES (89,80,'bind',NULL);
INSERT INTO Settings VALUES (102,86,'subtree','false');
INSERT INTO Settings VALUES (140,89,'bindDn','cn=fake.name,ou=people,dc=fao,dc=org');
INSERT INTO Settings VALUES (141,89,'bindPw','fake_password');
INSERT INTO Settings VALUES (150,80,'anonBind','true');

UPDATE Settings SET value='2.9.0' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';