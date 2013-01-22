--
-- DB migration script for Sextant from v5.0 to 5.1

--SET search_path = geonetwork;

CREATE TABLE CustomElementSet
  (
    xpath  varchar(1000) not null
  );
  
  
ALTER TABLE Metadata ALTER COLUMN harvestUri TYPE varchar(455);

ALTER TABLE HarvestHistory ADD elapsedTime int;

INSERT INTO Settings VALUES (960,1,'wiki',NULL);
INSERT INTO Settings VALUES (961,960,'markup','none');
INSERT INTO Settings VALUES (962,960,'output','strip');
INSERT INTO Settings VALUES (963,960,'mefoutput','strip');

INSERT INTO Settings VALUES (964,1,'wysiwyg',NULL);
INSERT INTO Settings VALUES (965,964,'enable','false');



INSERT INTO StatusValues VALUES  (0,'unknown','y');
INSERT INTO StatusValues VALUES  (1,'draft','y');
INSERT INTO StatusValues VALUES  (2,'approved','y');
INSERT INTO StatusValues VALUES  (3,'retired','y');
INSERT INTO StatusValues VALUES  (4,'submitted','y');
INSERT INTO StatusValues VALUES  (5,'rejected','y');

INSERT INTO StatusValuesDes VALUES (0,'eng','Unknown');
INSERT INTO StatusValuesDes VALUES (1,'eng','Draft');
INSERT INTO StatusValuesDes VALUES (2,'eng','Approved');
INSERT INTO StatusValuesDes VALUES (3,'eng','Retired');
INSERT INTO StatusValuesDes VALUES (4,'eng','Submitted');
INSERT INTO StatusValuesDes VALUES (5,'eng','Rejected');

INSERT INTO StatusValuesDes VALUES (0,'fre','Inconnu');
INSERT INTO StatusValuesDes VALUES (1,'fre','Brouillon');
INSERT INTO StatusValuesDes VALUES (2,'fre','Validé');
INSERT INTO StatusValuesDes VALUES (3,'fre','Retiré');
INSERT INTO StatusValuesDes VALUES (4,'fre','A valider');
INSERT INTO StatusValuesDes VALUES (5,'fre','Rejeté');


