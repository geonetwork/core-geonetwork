
CREATE TABLE HarvestHistory
  (
    id             int not null,
    harvestDate    varchar2(30),
        harvesterUuid  varchar2(250),
        harvesterName  varchar2(128),
        harvesterType  varchar2(128),
    deleted        char(1) default 'n' not null,
    info           varchar2(2000),
    params         clob,
    primary key(id)
  );

CREATE INDEX HarvestHistoryNDX1 ON HarvestHistory(harvestDate);

CREATE TABLE StatusValues
  (
    id        int not null,
    name      varchar(32)   not null,
    reserved  char(1)       default 'n' not null,
    primary key(id)
  );


CREATE TABLE StatusValuesDes
  (
    idDes   int not null,
    langId  varchar2(5) not null,
    label   varchar2(96)   not null,
    primary key(idDes,langId)
  );


CREATE TABLE MetadataStatus
  (
    metadataId  int not null,
    statusId    int default 0 not null,
    userId      int not null,
    changeDate   varchar2(30)    not null,
    changeMessage   varchar2(2048) not null,
    primary key(metadataId,statusId,userId,changeDate),
    foreign key(metadataId) references Metadata(id),
    foreign key(statusId)   references StatusValues(id),
    foreign key(userId)     references Users(id)
  );

CREATE INDEX MetadataNDX3 ON Metadata(owner);

CREATE TABLE spatialIndex
  (
    fid int,
    id  varchar(250),
    the_geom SDO_GEOMETRY,
    primary key(fid)
    );

CREATE INDEX spatialIndexNDX1 ON spatialIndex(id);
DELETE FROM user_sdo_geom_metadata WHERE TABLE_NAME = 'SPATIALINDEX';
INSERT INTO user_sdo_geom_metadata (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID) VALUES ( 'SPATIALINDEX', 'the_geom', SDO_DIM_ARRAY( SDO_DIM_ELEMENT('Longitude', -180, 180, 10), SDO_DIM_ELEMENT('Latitude', -90, 90, 10)), 8307);
CREATE INDEX spatialIndexNDX2 on spatialIndex(the_geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

ALTER TABLE Settings MODIFY ( value CLOB );
alter table settings drop primary key cascade;
alter table settings add primary key (id);
ALTER TABLE Settings ADD FOREIGN KEY (parentId) REFERENCES Settings (id);

ALTER TABLE metadata MODIFY ( data CLOB );
ALTER TABLE MetadataNotifications MODIFY ( errormsg CLOB );
ALTER TABLE CswServerCapabilitiesInfo MODIFY ( label CLOB );
alter table CswServerCapabilitiesInfo drop primary key;
alter table CswServerCapabilitiesInfo add primary key (idField);

CREATE TABLE Validation
  (
    metadataId   int,
    valType      varchar2(40),
    status       int,
    tested       int,
    failed       int,
    valDate      varchar2(30),
    
    primary key(metadataId, valType),
    foreign key(metadataId) references Metadata(id)
);

CREATE TABLE Thesaurus (
    id   varchar(250) not null,
    activated    varchar(1),
    primary key(id)
  );

ALTER TABLE Users MODIFY username varchar2(256);

ALTER TABLE Metadata MODIFY createDate varchar2(30);
ALTER TABLE Metadata MODIFY changeDate varchar2(30);
ALTER TABLE Metadata ADD doctype varchar2(255);

DROP TABLE IndexLanguages;

ALTER TABLE Languages DROP COLUMN isocode;

ALTER TABLE IsoLanguages ADD shortcode varchar2(2);

ALTER TABLE Categories MODIFY (name varchar2(255));
ALTER TABLE CategoriesDes MODIFY (label varchar2(255));
ALTER TABLE Settings MODIFY (name varchar2(64));

