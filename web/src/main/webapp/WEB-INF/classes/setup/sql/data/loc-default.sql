INSERT INTO Languages VALUES  ('eng', 'English', 'eng', 'y', 'n');
INSERT INTO Languages VALUES  ('fre', 'French', 'fre', 'y', 'n');
INSERT INTO Languages VALUES  ('ger', 'German', 'ger', 'y', 'n');
INSERT INTO Languages VALUES  ('ita', 'Italian', 'ita', 'y', 'n');
INSERT INTO Languages VALUES  ('roh', 'Rumantsch', 'roh', 'y', 'n');

DELETE FROM isolanguagesdes;
DELETE FROM IsoLanguages;

--
-- Data for Name: isolanguages; Type: TABLE DATA; Schema: public; Owner: www-data
--

INSERT INTO isolanguages (id, code, shortcode) VALUES (123,	'eng',	'en');
INSERT INTO isolanguages (id, code, shortcode) VALUES (124,	'ita',	'it');
INSERT INTO isolanguages (id, code, shortcode) VALUES (137,	'fre',	'fr');
INSERT INTO isolanguages (id, code, shortcode) VALUES (150,	'ger',	'de');
INSERT INTO isolanguages (id, code, shortcode) VALUES (358,	'roh',	'rm');

--
-- Data for Name: isolanguagesdes; Type: TABLE DATA; Schema: public; Owner: www-data
--

INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (123,   	'fre',	'Anglais');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (123,   	'ger',	'Englisch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (123,   	'ita',	'Inglese');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (123,   	'eng',	'English');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (137,   	'fre',	'Français');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (137,   	'eng',	'French');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (137,   	'ger',	'Französisch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (137,   	'ita',	'Francese');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (150,   	'fre',	'Allemand');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (150,   	'eng',	'German');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (150,   	'ger',	'Deutsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (150,   	'ita',	'Tedesco');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (358,   	'eng',	'Rumantsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (358,   	'ita',	'Rumantsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (358,   	'ger',	'Rumantsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (358,   	'fre',	'Rumantsch');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (124,   	'fre',	'Italien');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (124,   	'ita',	'Italiano');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (124,   	'eng',	'Italien');
INSERT INTO isolanguagesdes (iddes, langid, label) VALUES (124,	    'ger',	'Italienisch');


INSERT INTO GroupsDes VALUES (-1,'eng','Guest');
INSERT INTO GroupsDes VALUES (0,'eng','Intranet');
INSERT INTO GroupsDes VALUES (1,'eng','All');
INSERT INTO GroupsDes VALUES (2,'eng','Sample Group');

INSERT INTO GroupsDes VALUES (-1,'fre','Invité');
INSERT INTO GroupsDes VALUES (0,'fre','Intranet');
INSERT INTO GroupsDes VALUES (1,'fre','Tous');
INSERT INTO GroupsDes VALUES (2,'fre','Groupe exemple');

INSERT INTO GroupsDes VALUES (-1,'ger','Gast');
INSERT INTO GroupsDes VALUES (0,'ger','Intranet');
INSERT INTO GroupsDes VALUES (1,'ger','Alle');
INSERT INTO GroupsDes VALUES (2,'ger','Beispielgruppe');


INSERT INTO GroupsDes VALUES (-1,'ita','Visitatore');
INSERT INTO GroupsDes VALUES (0,'ita','Intranet');
INSERT INTO GroupsDes VALUES (1,'ita','Tutti');
INSERT INTO GroupsDes VALUES (2,'ita','Gruppo di esempio');

INSERT INTO GroupsDes VALUES (-1,'roh','Guest');
INSERT INTO GroupsDes VALUES (0,'roh','Intranet');
INSERT INTO GroupsDes VALUES (1,'roh','All');
INSERT INTO GroupsDes VALUES (2,'roh','Sample Group');

