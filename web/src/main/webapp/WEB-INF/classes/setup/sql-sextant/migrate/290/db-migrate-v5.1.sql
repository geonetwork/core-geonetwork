--
-- DB migration script for Sextant from v5.0 to 5.1

--SET search_path = geonetwork;

CREATE TABLE CustomElementSet
  (
    xpath  varchar(1000) not null
  );

INSERT INTO Settings VALUES (960,1,'wiki',NULL);
INSERT INTO Settings VALUES (961,960,'markup','none');