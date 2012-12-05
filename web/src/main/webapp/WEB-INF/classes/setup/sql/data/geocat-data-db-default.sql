-- DELETE FROM categoriesdes;
DELETE FROM categories;
INSERT INTO categories VALUES (1, 'default');

DELETE FROM isolanguagesdes;
DELETE FROM IsoLanguages;

-- DELETE FROM StatusValuesDes;

INSERT INTO Languages VALUES  ('ita', 'Italian', 'ita', 'y', 'n');
INSERT INTO isolanguages (id, code, shortcode) VALUES (124,	'ita',	'it');

INSERT INTO Languages VALUES  ('eng', 'English', 'eng', 'y', 'n');
INSERT INTO isolanguages (id, code, shortcode) VALUES (123,	'eng',	'en');

INSERT INTO Languages VALUES  ('fre', 'French', 'fre', 'y', 'n');
INSERT INTO isolanguages (id, code, shortcode) VALUES (137,	'fre',	'fr');

INSERT INTO Languages VALUES  ('ger', 'German', 'ger', 'y', 'n');
INSERT INTO isolanguages (id, code, shortcode) VALUES (150,	'ger',	'de');

INSERT INTO Languages VALUES  ('roh', 'Rumantsch', 'roh', 'y', 'n');
INSERT INTO isolanguages (id, code, shortcode) VALUES (358,	'roh',	'rm');
