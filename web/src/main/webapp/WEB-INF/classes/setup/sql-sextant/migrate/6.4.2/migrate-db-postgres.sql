
UPDATE Settings SET value = 'true' WHERE name = 'system/inspire/enable';
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/allowPublishInvalidMd', 'true', 2, 100003, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/automaticUnpublishInvalidMd', 'false', 2, 100004, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/forceValidationOnMdSave', 'false', 2, 100005, 'n');

INSERT INTO Settings (name, value, datatype, position, internal)
  VALUES ('map/bingKey', 'AplOhn33DW5iCpDv0bY-CzSriFoi6GE2r5cY94SqSi47koQ1s4XlylK8DUB7NZFZ', 0, 9595, 'n');

INSERT INTO Settings (name, value, datatype, position, internal)
  VALUES ('system/cors/allowedHosts', 'sextant-test.ifremer.fr,isi.ifremer.fr,w3.ifremer.fr,www.ifremer.fr,wwz.ifremer.fr,ifremer.komix.fr,atlasbenthal.ifremer.fr,projets-test.ifremer.fr,projets.ifremer.fr,www.coriolis.eu.org,oiseaux-manche.org,ez5-sextant-val.ifremer.fr', 0, 561, 'y');

UPDATE Settings SET value='3.2.1' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';
