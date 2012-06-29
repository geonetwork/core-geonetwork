DELETE FROM categoriesdes;
DELETE FROM categories;
INSERT INTO categories VALUES (1, 'default');
INSERT INTO categoriesdes VALUES (1, 'eng', 'Default');
INSERT INTO categoriesdes VALUES (1, 'fra', 'Défaut');
INSERT INTO categoriesdes VALUES (1, 'deu', 'Default');
INSERT INTO categoriesdes VALUES (1, 'ger', 'Default');
INSERT INTO categoriesdes VALUES (1, 'fre', 'Défaut');

DELETE FROM StatusValuesDes;

INSERT INTO StatusValuesDes VALUES (0,'deu','Unknown');
INSERT INTO StatusValuesDes VALUES (1,'deu','Draft');
INSERT INTO StatusValuesDes VALUES (2,'deu','Approved');
INSERT INTO StatusValuesDes VALUES (3,'deu','Retired');
INSERT INTO StatusValuesDes VALUES (4,'deu','Submitted');
INSERT INTO StatusValuesDes VALUES (5,'deu','Rejected');

INSERT INTO StatusValuesDes VALUES (0,'ger','Unknown');
INSERT INTO StatusValuesDes VALUES (1,'ger','Draft');
INSERT INTO StatusValuesDes VALUES (2,'ger','Approved');
INSERT INTO StatusValuesDes VALUES (3,'ger','Retired');
INSERT INTO StatusValuesDes VALUES (4,'ger','Submitted');
INSERT INTO StatusValuesDes VALUES (5,'ger','Rejected');

INSERT INTO StatusValuesDes VALUES (0,'eng','Unknown');
INSERT INTO StatusValuesDes VALUES (1,'eng','Draft');
INSERT INTO StatusValuesDes VALUES (2,'eng','Approved');
INSERT INTO StatusValuesDes VALUES (3,'eng','Retired');
INSERT INTO StatusValuesDes VALUES (4,'eng','Submitted');
INSERT INTO StatusValuesDes VALUES (5,'eng','Rejected');

INSERT INTO StatusValuesDes VALUES (0,'fra','Unknown');
INSERT INTO StatusValuesDes VALUES (1,'fra','Draft');
INSERT INTO StatusValuesDes VALUES (2,'fra','Approved');
INSERT INTO StatusValuesDes VALUES (3,'fra','Retired');
INSERT INTO StatusValuesDes VALUES (4,'fra','Submitted');
INSERT INTO StatusValuesDes VALUES (5,'fra','Rejected');

INSERT INTO StatusValuesDes VALUES (0,'fre','Unknown');
INSERT INTO StatusValuesDes VALUES (1,'fre','Draft');
INSERT INTO StatusValuesDes VALUES (2,'fre','Approved');
INSERT INTO StatusValuesDes VALUES (3,'fre','Retired');
INSERT INTO StatusValuesDes VALUES (4,'fre','Submitted');
INSERT INTO StatusValuesDes VALUES (5,'fre','Rejected');


INSERT INTO CswServerCapabilitiesInfo VALUES (1, 'eng', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (2, 'eng', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (3, 'eng', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (4, 'eng', 'accessConstraints', '');

INSERT INTO CswServerCapabilitiesInfo VALUES (5, 'deu', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (6, 'deu', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (7, 'deu', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (8, 'deu', 'accessConstraints', '');

INSERT INTO CswServerCapabilitiesInfo VALUES (9, 'ger', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (10, 'ger', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (11, 'ger', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (12, 'ger', 'accessConstraints', '');

INSERT INTO CswServerCapabilitiesInfo VALUES (13, 'fra', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (14, 'fra', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (15, 'fra', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (16, 'fra', 'accessConstraints', '');

INSERT INTO CswServerCapabilitiesInfo VALUES (17, 'fre', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (18, 'fre', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (19, 'fre', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (20, 'fre', 'accessConstraints', '');
