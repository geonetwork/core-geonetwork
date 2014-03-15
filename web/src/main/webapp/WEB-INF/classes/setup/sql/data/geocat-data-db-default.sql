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

INSERT INTO Groups VALUES (42,'TestGroup',NULL,NULL,NULL);
INSERT INTO GroupsDes VALUES (42,'eng','Test Group');
INSERT INTO GroupsDes VALUES (42,'fre','Test Group');
INSERT INTO GroupsDes VALUES (42,'ger','Test Group');
INSERT INTO GroupsDes VALUES (42,'ita','Test Group');
INSERT INTO GroupsDes VALUES (42,'roh','Test Group');

INSERT INTO public.schematron (id, file, schemaname) VALUES (1, 'schematron-rules-inspire-strict.disabled.xsl', 'iso19139.che');
INSERT INTO public.schematron (id, file, schemaname) VALUES (2, 'schematron-rules-bgdi.required.xsl', 'iso19139.che');
INSERT INTO public.schematron (id, file, schemaname) VALUES (3, 'schematron-rules-geobasisdatensatz.required.xsl', 'iso19139.che');

INSERT INTO public.schematroncriteriagroup (name, schematronid, requirement) VALUES ('Lichtenstein', 1, 'REQUIRED');
INSERT INTO public.schematroncriteriagroup (name, schematronid, requirement) VALUES ('DefaultInspireStrict', 1, 'REPORT_ONLY');
INSERT INTO public.schematroncriteriagroup (name, schematronid, requirement) VALUES ('BGDI', 2, 'REQUIRED');
INSERT INTO public.schematroncriteriagroup (name, schematronid, requirement) VALUES ('Geobasisdatensatz', 3, 'REQUIRED');

-- INSERT INTO public.schematroncriteria (id, type, value, group_name, group_schematronid) VALUES (1, 'GROUP', '', 'Lichtenstein', 1);
INSERT INTO public.schematroncriteria (id, type, value, group_name, group_schematronid) VALUES (1, 'XPATH', '*//gmd:keyword/gco:CharacterString/text() = ''BGDI''__OR__*//gmd:keyword/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString/text() = ''BGDI''', 'BGDI', 2);
INSERT INTO public.schematroncriteria (id, type, value, group_name, group_schematronid) VALUES (2, 'GROUP', '42', 'BGDI', 2);
INSERT INTO public.schematroncriteria (id, type, value, group_name, group_schematronid) VALUES (3, 'XPATH', '*//gmd:keyword/gco:CharacterString/text() = ''Geobasisdatensatz''__OR__*//gmd:keyword/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString/text() = ''Geobasisdatensatz''', 'Geobasisdatensatz', 3);
INSERT INTO public.schematroncriteria (id, type, value, group_name, group_schematronid) VALUES (4, 'GROUP', '42', 'Geobasisdatensatz', 3);
INSERT INTO public.schematroncriteria (id, type, value, group_name, group_schematronid) VALUES (5, 'ALWAYS_ACCEPT', '', 'DefaultInspireStrict', 1);
