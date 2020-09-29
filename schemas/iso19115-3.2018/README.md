# ISO 19115-3:2018 schema plugin

This is the ISO19115-3:2018 schema plugin included in core-geonetwork.

The main changes with the previous ISO19115-3 plugin are:

* Update to latest XSD (https://github.com/ISO-TC211/XML/wiki/Schema-Updates)
* Add support for 19115-2 Imagery standards
* directly included in core-geonetwork

## Reference documents:

* http://www.iso.org/iso/catalogue_detail.htm?csnumber=53798
* http://www.iso.org/iso/catalogue_detail.htm?csnumber=32579
* https://github.com/ISO-TC211/XML/
 
## Talks 

* [Using the latest ISO Standard for
Geographic Information (ISO19115-1)
for an INSPIRE
Discovery Service](http://cnig.gouv.fr/wp-content/uploads/2015/06/titellus_fx_prunayre_iso19115-3_inspire2015.pdf) (INSPIRE conference 2015)

## Catalogue using ISO19115-3

* SPW / Metawal http://metawal.wallonie.be/
* Ifremer / Sextant https://sextant.ifremer.fr/ (only some projects - EMODnet Checkpoint, CERSAT)

## Description:

This plugin is composed of:

* indexing
* editing
 * editor associated resources
 * directory support for contact, logo and format.
* viewing
* CSW
* from ISO19115-3 conversion
* from ISO19115-2 conversion
* from/to ISO19139 conversion
* multilingual metadata support
* validation (XSD and Schematron)

## Metadata rules:

### Metadata identifier

The metadata identifier is stored in the element mdb:MD_Metadata/mdb:metadataIdentifier.
Only the code is set by default but more complete description may be defined (see authority,
codeSpace, version, description).

```xml
<mdb:metadataIdentifier>
  <mcc:MD_Identifier>
    <mcc:code>
      <gco:CharacterString>{{MetadataUUID}}</gco:CharacterString>
    </mcc:code>
  </mcc:MD_Identifier>
</mdb:metadataIdentifier>
```

### Metadata linkage ("point of truth")

The metadata linkage is updated when saving the record. The link added points
to the catalog the metadata was created. If the metadata is harvested by another
catalog, then this link will provide a way to retrieve the original record in the
source catalog.

```xml
<mdb:metadataLinkage>
  <cit:CI_OnlineResource>
    <cit:linkage>
      <gco:CharacterString>http://localhost/geonetwork/srv/eng/home?uuid={{MetadataUUID}}</gco:CharacterString>
    </cit:linkage>
    <cit:function>
      <cit:CI_OnLineFunctionCode
         codeList="http://standards.iso.org/iso/19139/resources/codelist/gmxCodelists.xml#CI_OnLineFunctionCode"
         codeListValue="completeMetadata"/>
    </cit:function>
  </cit:CI_OnlineResource>
</mdb:metadataLinkage>
```


### Parent metadata

The parent metadata records is referenced using the following form from the editor:

```
<mdb:parentMetadata uuidref="{{ParentMetadataUUID}}}"/>
```

Nevertheless, the citation code is also indexed.

### Validation

Validation steps are first XSD validation made on the schema, then the schematron validation defined in folder  [iso19115-3.2018/schematron](https://github.com/geonetwork/core-geonetwork/tree/master/schemas/iso19115-3.2018/src/main/plugin/iso19115-3.2018/schematron). 2 famillies of rules are available:
* ISO rules (defined by TC211)
* INSPIRE rules


## CSW requests:

If requesting using output schema http://www.isotc211.org/2005/gmd an ISO19139 record is returned. 
To retrieve the record in ISO19115-3.2018, use http://standards.iso.org/iso/19115/-3/mdb/2.0 output schema.
```xml
<?xml version="1.0"?>
<csw:GetRecordById xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
  service="CSW"
  version="2.0.2"
  outputSchema="http://standards.iso.org/iso/19115/-3/mdb/2.0">
    <csw:Id>cecd1ebf-719e-4b1f-b6a7-86c17ed02c62</csw:Id>
    <csw:ElementSetName>brief</csw:ElementSetName>
</csw:GetRecordById>
```
Note: outputSchema = own will also return the record in ISO19115-3.


## More work required

### Formatter


### GML support

* Polygon or line editing and view.

### Imagery

* Better support of mac:childOperation 


## Community

Comments and questions to geonetwork-developers or geonetwork-users mailing lists.


## Contributors

* Simon Pigot (CSIRO)
* François Prunayre (titellus)
* Arnaud De Groof (Spacebel)
* Ted Habermann (hdfgroup)
