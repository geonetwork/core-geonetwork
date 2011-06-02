-- 2.6.4 changes
CREATE TABLE CustomElementSet
  (
    xpath  varchar(1000) not null
  );

UPDATE Languages SET isocode = 'spa' where id ='es';

ALTER TABLE Languages ADD isInspire char(1);
ALTER TABLE Languages ADD isDefault char(1);

UPDATE Languages SET isInspire = 'y', isDefault = 'y' where id ='en';
UPDATE Languages SET isInspire = 'y', isDefault = 'n' where id ='fr';
UPDATE Languages SET isInspire = 'y', isDefault = 'n' where id ='es';
UPDATE Languages SET isInspire = 'n', isDefault = 'n' where id ='ru';
UPDATE Languages SET isInspire = 'n', isDefault = 'n' where id ='cn';
UPDATE Languages SET isInspire = 'y', isDefault = 'n' where id ='de';
UPDATE Languages SET isInspire = 'y', isDefault = 'n' where id ='nl';
UPDATE Languages SET isInspire = 'y', isDefault = 'n' where id ='pt';
UPDATE Languages SET isInspire = 'n', isDefault = 'n' where id ='ca';
UPDATE Languages SET isInspire = 'n', isDefault = 'n' where id ='tr';

UPDATE Settings SET value='2.6.5' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';
