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