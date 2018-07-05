INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/url', '', 0, 7211, 'n');
UPDATE Settings SET internal='n' WHERE name='system/inspire/enable';

UPDATE Settings SET datatype = 0, value = 'off' WHERE name = 'system/localrating/enable' and value = 'n';
UPDATE Settings SET datatype = 0, value = 'basic' WHERE name = 'system/localrating/enable' and value = 'y';

INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (-1, 'Average', 'y');
INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (0, 'Completeness', 'n');
INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (1, 'Discoverability', 'n');
INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (2, 'Readability', 'n');
INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (3, 'DataQuality', 'n');
INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (4, 'ServiceQuality', 'n');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'eng', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'eng', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'eng', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'eng', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'eng', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'eng', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'ita', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'ita', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'ita', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'ita', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'ita', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'ita', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'nor', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'nor', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'nor', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'nor', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'nor', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'nor', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'cat', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'cat', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'cat', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'cat', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'cat', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'cat', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'por', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'por', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'por', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'por', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'por', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'por', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'ger', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'ger', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'ger', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'ger', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'ger', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'ger', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'spa', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'spa', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'spa', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'spa', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'spa', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'spa', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'fre', 'Moyenne');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'fre', 'Complétude#Est-ce que les informations sur cette page sont suffisamment précises pour savoir ce que vous pouvez attendre de cette ressource ?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'fre', 'Découvrabilité#Était-il facile de trouver cette page ?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'fre', 'Lisibilité#Était-il facile de comprendre le contenu de cette page ?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'fre', 'Qualité des données#Est-ce que cette ressource contient les informations attendues ? Les données sont-elles assez précises ? assez récentes ?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'fre', 'Cette données est elle accessible dans un format ou via un service simple à utiliser ?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'vie', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'vie', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'vie', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'vie', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'vie', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'vie', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'fin', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'fin', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'fin', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'fin', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'fin', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'fin', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'tur', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'tur', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'tur', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'tur', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'tur', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'tur', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'ara', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'ara', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'ara', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'ara', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'ara', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'ara', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'rus', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'rus', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'rus', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'rus', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'rus', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'rus', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'dut', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'dut', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'dut', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'dut', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'dut', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'dut', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'chi', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'chi', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'chi', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'chi', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'chi', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'chi', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'pol', 'Average');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'pol', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'pol', 'Discoverability#Was it easy to find this information page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'pol', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'pol', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'pol', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');


UPDATE Settings SET value='3.4.2' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvester/disabledHarvesterTypes', '', 0, 9011, 'n');
