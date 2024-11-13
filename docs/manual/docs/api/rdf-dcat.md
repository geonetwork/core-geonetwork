# DCAT {#rdf-dcat}

The catalogue has the capability to convert ISO to DCAT format in various API endpoint. [The first implementation of DCAT output was done in 2012](https://trac.osgeo.org/geonetwork/wiki/proposals/DCATandRDFServices) and was targeting interaction with semantic service and semantic sitemap support. DCAT output was available using a service named `rdf.search`. This service was deprecated in version 4.0.0 in favor of producing DCAT output in the [Catalog Service for the Web (CSW)](csw.md) or using the formatters API. 


## Supported DCAT profiles

A base conversion is provided with complementary extensions for various profiles of DCAT developed in Europe:

* a default DCAT export following W3C standard https://www.w3.org/TR/vocab-dcat-3/
* an extension for the European DCAT-AP https://semiceu.github.io/DCAT-AP/releases/3.0.0/
  * an extension for the European GeoDCAT-AP https://semiceu.github.io/GeoDCAT-AP/releases/
  * an extension for the European DCAT-AP-Mobility https://mobilitydcat-ap.github.io/mobilityDCAT-AP/releases/1.0.0/index.html
  * an extension for the European DCAT-AP-HVD https://semiceu.github.io/DCAT-AP/releases/2.2.0-hvd/

The mapping is done from ISO19115-3 to DCAT*. An ISO19139 to ISO19115-3 conversion can be applied before if needed.

[The SEMICeu XSLT conversion](https://github.com/SEMICeu/iso-19139-to-dcat-ap/blob/master/iso-19139-to-dcat-ap.xsl)) is also included with minor improvements. This conversion is from ISO19139 to RDF and if needed a conversion from ISO19115-3 is applied.

## Usage in the formatters API

Each DCAT formats are available using a formatter eg. http://localhost:8080/geonetwork/srv/api/records/be44fe5a-65ca-4b70-9d29-ac5bf1f0ebc5/formatters/eu-dcat-ap?output=xml

To add the formatter in the record view download list, the user interface configuration can be updated:

![image](img/dcat-in-download-menu.png)

```json
{
  "mods": {
    "search": {
      "downloadFormatter": [
        {
          "label": "exportMEF",
          "url": "/formatters/zip?withRelated=false",
          "class": "fa-file-zip-o"
        },
        {
          "label": "exportPDF",
          "url": "/formatters/xsl-view?output=pdf&language=${lang}",
          "class": "fa-file-pdf-o"
        },
        {
          "label": "exportXML",
          "url": "/formatters/xml",
          "class": "fa-file-code-o"
        },
        {
          "label": "DCAT",
          "url": "/formatters/dcat?output=xml"
        },
        {
          "label": "EU-DCAT-AP",
          "url": "/formatters/eu-dcat-ap?output=xml"
        },
        {
          "label": "EU-GEO-DCAT-AP",
          "url": "/formatters/eu-geodcat-ap?output=xml"
        },
        {
          "label": "EU-DCAT-AP-MOBILITY",
          "url": "/formatters/eu-dcat-ap-mobility?output=xml"
        },
        {
          "label": "EU-DCAT-AP-HVD",
          "url": "/formatters/eu-dcat-ap-hvd?output=xml"
        }
      ]
```


## Usage in the CSW service

All DCAT profiles are also accessible using CSW protocol.

A `GetRecordById` operation can be used: http://localhost:8080/geonetwork/srv/eng/csw?SERVICE=CSW&VERSION=2.0.2&REQUEST=GetRecordById&ID=da165110-88fd-11da-a88f-000d939bc5d8&outputSchema=https://semiceu.github.io/DCAT-AP/releases/2.2.0-hvd/ and is equivalent to the API http://localhost:8080/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/formatters/eu-dcat-ap-hvd?output=xml.

A `GetRecords` operation can be used to retrieve a set of records: http://localhost:8080/geonetwork/srv/fre/csw?SERVICE=CSW&VERSION=2.0.2&REQUEST=GetRecords&outputSchema=http://data.europa.eu/930/&elementSetName=full&resultType=results&maxRecords=300

Use the `outputSchema` parameter to select the DCAT profile to use. The following values are supported:

```xml
<ows:Parameter name="outputSchema">
   <ows:Value>http://data.europa.eu/930/</ows:Value>
   <ows:Value>http://data.europa.eu/930/#semiceu</ows:Value>
   <ows:Value>http://data.europa.eu/r5r/</ows:Value>
   <ows:Value>http://standards.iso.org/iso/19115/-3/mdb/2.0</ows:Value>
   <ows:Value>http://www.isotc211.org/2005/gfc</ows:Value>
   <ows:Value>http://www.isotc211.org/2005/gmd</ows:Value>
   <ows:Value>http://www.opengis.net/cat/csw/2.0.2</ows:Value>
   <ows:Value>http://www.w3.org/ns/dcat#</ows:Value>
   <ows:Value>http://www.w3.org/ns/dcat#core</ows:Value>
   <ows:Value>https://semiceu.github.io/DCAT-AP/releases/2.2.0-hvd/</ows:Value>
   <ows:Value>https://w3id.org/mobilitydcat-ap</ows:Value>
```

Those values are listed in the `GetCapabilities` operation http://localhost:8080/geonetwork/srv/eng/csw?SERVICE=CSW&VERSION=2.0.2&REQUEST=GetCapabilities.

## Usage in OGC API Records

For the time being, OGC API Records provides a simplified DCAT output (based on the index document). 

## DCAT validation

The DCAT validation can be done using online validation tool:
* https://www.itb.ec.europa.eu/shacl/dcat-ap/upload

Depending on the target DCAT profile to use, it may be required to build proper ISO template and metadata record containing all required fields. Usually profiles are adding constraints for usage of specific vocabularies and fields (eg. [for High Value datasets, specific vocabularies are defined for categories, license, applicable legislations, ...](https://semiceu.github.io/DCAT-AP/releases/2.2.0-hvd/#controlled-vocabularies-to-be-used)).


## Mapping considerations

The mapping is done from ISO19115-3 to DCAT. The mapping may not cover all usages and may be adapted. This can be done in the `iso19115-3.2018` schema plugin in the `formatter/dcat*` XSLT files.

Some points under discussion are:
* Object vs Reference: 
  * Should we use object or reference for some fields (eg. contact, organisation, ...)?
  * What should be the reference URI?
  * Where is defined the reference URI in ISO?

eg. for an organisation, the URI will be the first value in the following sequence:
```xml
(cit:partyIdentifier/*/mcc:code/*/text(),
cit:contactInfo/*/cit:onlineResource/*/cit:linkage/gco:CharacterString/text(),
cit:name/gcx:Anchor/@xlink:href,
@uuid)[1]
```

* No equivalent field in ISO (eg. Where to store `spdx:checksum` in ISO?)

* Associated resources: Links between are not always bidirectional so using the associated API would allow to populate more relations. This is also mitigated with the complete RDF graph of the catalogue is retrieved providing relations from all records.
