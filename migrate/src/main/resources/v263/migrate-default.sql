
-- FIXME : IndexLanguages created here but not in trunk
CREATE TABLE IndexLanguages
  (
    id            int,
    languageName  varchar(32)   not null,
    selected      char(1)       default 'n' not null,

    primary key(id, languageName)

  );

UPDATE Settings SET value='2.6.3' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';