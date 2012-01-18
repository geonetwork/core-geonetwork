
CREATE TABLE HarvestHistory
  (
    id             int not null,
    harvestDate    varchar(30),
        harvesterUuid  varchar(250),
        harvesterName  varchar(128),
        harvesterType  varchar(128),
    deleted        char(1) default 'n' not null,
    info           varchar(2000),
    params         long,

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
    langId  varchar(5) not null,
    label   varchar(96)   not null,
    primary key(idDes,langId)
  );


CREATE TABLE MetadataStatus
  (
    metadataId  int not null,
    statusId    int default 0 not null,
    userId      int not null,
    changeDate   varchar(30)    not null,
    changeMessage   varchar(2048) not null,
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
