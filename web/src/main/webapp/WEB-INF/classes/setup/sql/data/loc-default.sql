INSERT INTO Languages VALUES  ('eng', 'English', 'eng', 'y', 'n');
INSERT INTO Languages VALUES  ('deu', 'German', 'deu', 'y', 'n');
INSERT INTO Languages VALUES  ('fra', 'French', 'fra', 'y', 'n');
INSERT INTO Languages VALUES  ('fre', 'French', 'fre', 'y', 'n');
INSERT INTO Languages VALUES  ('ger', 'German', 'ger', 'y', 'n');
INSERT INTO Languages VALUES  ('ita', 'Italian', 'ita', 'y', 'n');

DELETE FROM isolanguagesdes ;
DELETE FROM IsoLanguages;

--
-- Data for Name: isolanguages; Type: TABLE DATA; Schema: public; Owner: www-data
--

INSERT INTO isolanguages (id, code, shortcode) VALUES (123,	'eng',	'en');
INSERT INTO isolanguages (id, code, shortcode) VALUES (124,	'ita',	'it');
INSERT INTO isolanguages (id, code, shortcode) VALUES (501,	'fra',	'fr');
INSERT INTO isolanguages (id, code, shortcode) VALUES (137,	'fre',	'fr');
INSERT INTO isolanguages (id, code, shortcode) VALUES (150,	'ger',	'de');
INSERT INTO isolanguages (id, code, shortcode) VALUES (500,	'deu',	'de');
INSERT INTO isolanguages (id, code, shortcode) VALUES (358,	'roh',	'rm');

--
-- Data for Name: isolanguagesdes; Type: TABLE DATA; Schema: public; Owner: www-data
--

INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (501,   	'fra',	'Français');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (500,   	'fra',	'Allemand');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (500,   	'ita',	'Tedesco');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (500,   	'eng',	'German');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (500,   	'fre',	'Allemand');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (500,   	'deu',	'Deutsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (500,   	'ger',	'Deutsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (501,   	'fre',	'Français');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (501,   	'eng',	'French');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (501,   	'ita',	'Francese');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (501,   	'ger',	'Französisch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (501,   	'deu',	'Französisch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (123,   	'fre',	'Anglais');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (123,   	'fra',	'Anglais');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (123,   	'ger',	'Englisch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (123,   	'ita',	'Inglese');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (123,   	'eng',	'English');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (123,   	'deu',	'Englisch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (137,   	'fre',	'Français');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (137,   	'fra',	'Français');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (137,   	'eng',	'French');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (137,   	'ger',	'Französisch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (137,   	'ita',	'Francese');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (137,   	'deu',	'Französisch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (150,   	'fre',	'Allemand');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (150,   	'fra',	'Allemand');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (150,   	'eng',	'German');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (150,   	'ger',	'Deutsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (150,   	'deu',	'Deutsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (150,   	'ita',	'Tedesco');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (358,   	'eng',	'Rumantsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (358,   	'ita',	'Rumantsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (358,   	'ger',	'Rumantsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (358,   	'deu',	'Rumantsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (358,   	'fra',	'Rumantsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (358,   	'fre',	'Rumantsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (124,   	'fra',	'Italien');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (124,   	'fre',	'Italien');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (124,   	'ita',	'Italiano');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (124,   	'eng',	'Italien');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (124,	'deu',	'Italienisch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (124,	'ger',	'Italienisch');


--
-- PostgreSQL database dump complete
--

