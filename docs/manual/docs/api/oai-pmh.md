# Open Archive Initiative (OAI) {#oai-pmh}

The Open Archives Initiative Protocol for Metadata Harvesting (OAI-PMH) standard exposes the metadata records in your catalog in an XML format defined by version 2.0 of the OAI-PMH protocol.

## Configuration

The following URL is the standard end point for the catalog (substitute your GeoNetwork URL): <http://localhost:8080/geonetwork/srv/api/oaipmh>?

## Requests

Standard OAI-PMH requests can be done using the url above and the 6 verbs provided by the standard:

-   Identify
-   ListMetadataFormats
-   ListSets
-   ListRecords
-   ListIdentifiers
-   GetRecord


### Identify operation

This verb is used to retrieve information about a repository.

http://localhost:8080/geonetwork/srv/api/oaipmh?verb=Identify

```xml
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
  <responseDate>2025-06-03T13:03:08.054227ZZ</responseDate>
  <request verb="Identify">http://localhost:8080/geonetwork/srv/api/oaipmh</request>
  <Identify>
    <repositoryName>My GeoNetwork catalogue</repositoryName>
    <baseURL>http://localhost:8080/geonetwork/srv/api/oaipmh</baseURL>
    <protocolVersion>2.0</protocolVersion>
    <adminEmail>root@localhost</adminEmail>
    <earliestDatestamp>2005-03-31T17:13:30ZZ</earliestDatestamp>
    <deletedRecord>no</deletedRecord>
    <granularity>YYYY-MM-DDThh:mm:ssZ</granularity>
  </Identify>
</OAI-PMH>
```


[More information](https://www.openarchives.org/OAI/openarchivesprotocol.html#Identify).

### ListMetadataFormats operation

This verb is used to retrieve the metadata formats available from a repository.

In the catalogue, this corresponds to the schema plugins that are installed.

http://localhost:8080/geonetwork/srv/api/oaipmh?verb=ListMetadataFormats

```xml

<?xml version="1.0" encoding="UTF-8"?>
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
  <responseDate>2025-06-04T06:12:54.543994ZZ</responseDate>
  <request verb="ListMetadataFormats">http://localhost:8080/geonetwork/srv/api/oaipmh</request>
  <ListMetadataFormats>
    <metadataFormat>
      <metadataPrefix>iso19115-3.2018</metadataPrefix>
      <schema>https://schemas.isotc211.org/19115/-3/mdb/2.0/mdb.xsd</schema>
      <metadataNamespace>http://standards.iso.org/iso/19115/-3/mdb/2.0</metadataNamespace>
    </metadataFormat>
    <metadataFormat>
      <metadataPrefix>iso19139</metadataPrefix>
      <schema>http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</schema>
      <metadataNamespace>http://www.isotc211.org/2005/gmd</metadataNamespace>
    </metadataFormat>
    <metadataFormat>
      <metadataPrefix>dublin-core</metadataPrefix>
      <schema>http://localhost:8080/geonetwork/xml/schemas/dublin-core/schema.xsd</schema>
      <metadataNamespace />
    </metadataFormat>
    <metadataFormat>
      <metadataPrefix>iso19110</metadataPrefix>
      <schema>http://www.isotc211.org/2005/gfc/gfc.xsd</schema>
      <metadataNamespace>http://www.isotc211.org/2005/gfc</metadataNamespace>
    </metadataFormat>
    <metadataFormat>
      <metadataPrefix>oai_dc</metadataPrefix>
      <schema>http://www.openarchives.org/OAI/2.0/oai_dc.xsd</schema>
      <metadataNamespace>http://www.openarchives.org/OAI/2.0/</metadataNamespace>
    </metadataFormat>
  </ListMetadataFormats>
</OAI-PMH>
```


[More information](https://www.openarchives.org/OAI/openarchivesprotocol.html#ListMetadataFormats).

### ListSets operation

This verb is used to retrieve the set structure of a repository, useful for selective harvesting.

In the catalogue, this corresponds to the categories.

http://localhost:8080/geonetwork/srv/api/oaipmh?verb=ListSets

```xml

<?xml version="1.0" encoding="UTF-8"?>
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
  <responseDate>2025-06-04T06:13:43.916077ZZ</responseDate>
  <request verb="ListSets">http://localhost:8080/geonetwork/srv/api/oaipmh</request>
  <ListSets>
    <set>
      <setSpec>maps</setSpec>
      <setName>Cartes &amp; graphiques</setName>
    </set>
    <set>
      <setSpec>datasets</setSpec>
      <setName>Jeux de données</setName>
    </set>
...
```

[More information](https://www.openarchives.org/OAI/openarchivesprotocol.html#ListSets).


### ListRecords operation

This verb is used to harvest records from a repository.

http://localhost:8080/geonetwork/srv/api/oaipmh?verb=ListRecords&metadataPrefix=oai_dc
http://localhost:8080/geonetwork/srv/api/oaipmh?verb=ListRecords&metadataPrefix=iso19139
http://localhost:8080/geonetwork/srv/api/oaipmh?verb=ListRecords&metadataPrefix=datacite

`metadataPrefix` is the metadata format to be returned, it can be a schema plugin identifier such as `iso19139`, `iso19115-3.2018`, ...
or a conversion defined in each schema plugin `formatter` folder (eg. `oai_dc`).

If the conversion is not available, the response will skip the record and continue with the next one.


```xml
<?xml version="1.0" encoding="UTF-8"?>
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
  <responseDate>2025-06-04T07:03:18.211967ZZ</responseDate>
  <request identifier="da165110-88fd-11da-a88f-000d939bc5d8" metadataPrefix="oai_dc" verb="GetRecord">http://localhost:8080/geonetwork/srv/api/oaipmh</request>
  <GetRecord>
    <record>
      <header>
        <identifier>da165110-88fd-11da-a88f-000d939bc5d8</identifier>
        <datestamp>2007-11-06T11:13:00ZZ</datestamp>
        <setSpec>maps</setSpec>
        <setSpec>datasets</setSpec>
        <setSpec>interactiveResources</setSpec>
      </header>
      <metadata>
        <oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:dc="http://purl.org/dc/elements/1.1/" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
          <dc:identifier>da165110-88fd-11da-a88f-000d939bc5d8</dc:identifier>
          <dc:date>2007-11-06T11:13:00Z</dc:date>
          <dc:title>Hydrological Basins in Africa (Sample record, please remove!)</dc:title>
          <dc:subject>watersheds</dc:subject>
          <dc:subject>river basins</dc:subject>
...
```


The maximum number of records in the response is limited by the setting `system/oai/maxrecords`.

[More information](https://www.openarchives.org/OAI/openarchivesprotocol.html#ListRecords).


### ListIdentifiers operation

This verb is an abbreviated form of ListRecords, retrieving only headers rather than records.

http://localhost:8080/geonetwork/srv/api/oaipmh?verb=ListIdentifiers&metadataPrefix=iso19139

[More information](https://www.openarchives.org/OAI/openarchivesprotocol.html#ListIdentifiers).


### GetRecord operation

This verb is used to retrieve an individual metadata record from a repository.

http://localhost:8080/geonetwork/srv/api/oaipmh?verb=GetRecord&identifier=da165110-88fd-11da-a88f-000d939bc5d8&metadataPrefix=iso19139

[More information](https://www.openarchives.org/OAI/openarchivesprotocol.html#GetRecord).

## Reference

Please see <https://www.openarchives.org/OAI/openarchivesprotocol.html> for further details.
