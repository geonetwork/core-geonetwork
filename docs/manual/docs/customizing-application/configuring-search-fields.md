# Configuring search fields

In some cases it's relevant to modify or extend the search fields of the metadata index. For example to add a field (which is then searchable or can be used in a default view) or change the content of the field created from the metadata (indexation).

## Index field types

In the index, JSON document representing the metadata is stored.

Simple fields are stored like a map of key = value. eg.

``` js
"_source": {
  "docType": "metadata",
  "documentStandard": "iso19115-3.2018",
  "document": "",
  "metadataIdentifier": "7451e2bd-22e8-4a74-a999-01c58b630369",
  "standardName": "ISO 19115",
  "indexingDate": "2020-11-19T06:30:47.009Z",
  "dateStamp": "2020-11-17T13:56:43",
  "mainLanguage": "fre"
```

### Multilingual text field

Multilingual fields are stored using an object with the following properties:

-   default the record default language value (this property is updated on the client side based on the UI language)
-   lang{langCode} one or more properties containing all record languages values

``` js
"_source": {
  "resourceTitleObject": {
    "default": "Aménagements routiers et autoroutiers - Série",
    "langfre": "Aménagements routiers et autoroutiers - Série",
    "langger": "Straßen- und Autobahnverbesserungen - Standard"
},
```

### Codelist

Codelists are stored as an object like titles, abstract, \...

If the record is multilingual, codelist translations are stored in the index for the record languages:

``` js
"cl_spatialRepresentationType" : {
    "key": "grid",
    "default": "Grid",
    "langeng": "Grid",
    "langfre": "Grid",
    "text": "{{> inner text of the codelist element. Used in some profiles eg. ISO HNAP}}",
    "link": "./resources/codeList.xml#MD_SpatialRepresentationTypeCode",
  }
```

When creating a facets on a codelist, 2 options:

-   if the catalog content is in one language (and there is no need to translate codelist in other language), use the default property eg. cl_spatialRepresentationType.default
-   if you have a catalog containing a mix of languages without having all records translated in all languages, use the key eg. cl_spatialRepresentationType.key and do translation on the client side. See GnSearchModule.js to load extra codelist translations on the Angular app.

The second one is also required if you want the codelist to be translated in the user interface language (whatever the record language). The codelist translations are loaded by the application depending on the UI language.

Use the default property in record view. Depending on the UI language, the default property contains the translation in the UI language or fallback to the record default.

### Thesaurus

Each thesaurus are described by the following fields:

-   ``th_{thesaurusId}Number`` with the count of non empty keywords
-   ``th_{thesaurusId}``, an array of multilingual keyword which may contains a link (when using Anchor)
-   (optional) ``th_{thesaurusId}_tree`` containing hierarchy when broader terms are found. default property contains the record default language hierarchy, key property contains the hierarchy of broader terms keys. This can be used to build tree depending on UI language (thesaurus translations has to be loaded by the client app).

``` js
{
  "th_httpinspireeceuropaeumetadatacodelistPriorityDatasetPriorityDatasetNumber": "3",
  "th_httpinspireeceuropaeumetadatacodelistPriorityDatasetPriorityDataset": [{
      "default": "Agglomerations - industrial noise exposure delineation (Noise Directive)",
      "langfre": "Agglomerations - industrial noise exposure delineation (Noise Directive)",
      "link": "http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/Agglomerations-IndustrialNoiseExposureDelineation-dir-2002-49"
    },
    {
      "default": "Agglomerations - noise exposure delineation day-evening-night (Noise Directive)",
      "langfre": "Agglomerations - noise exposure delineation day-evening-night (Noise Directive)",
      "link": "http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/Agglomerations-NoiseExposureDelineationDEN-dir-2002-49"
    },
    {
      "default": "Designated waters (Water Framework Directive)",
      "langfre": "Designated waters (Water Framework Directive)",
      "link": "http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/DesignatedWaters-dir-2000-60"
    }
  ],
  "th_httpinspireeceuropaeumetadatacodelistPriorityDatasetPriorityDataset_tree": {
    "default": [
      "Directive 2000/60/EC",
      "Directive 2000/60/EC^Protected areas (Water Framework Directive)",
      "Directive 2000/60/EC^Protected areas (Water Framework Directive)^Designated waters (Water Framework Directive)",
      "Directive 2002/49/EC",
      "Directive 2002/49/EC^Environmental noise exposure (Noise Directive)",
      "Directive 2002/49/EC^Environmental noise exposure (Noise Directive)^Agglomerations - industrial noise exposure delineation (Noise Directive)",
      "Directive 2002/49/EC^Environmental noise exposure (Noise Directive)^Agglomerations - noise exposure delineation (Noise Directive)",
      "Directive 2002/49/EC^Environmental noise exposure (Noise Directive)^Agglomerations - noise exposure delineation (Noise Directive)^Agglomerations - noise exposure delineation day-evening-night (Noise Directive)"
    ],
    "key": [
      "http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/dir-2000-60",
      "http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/dir-2000-60^http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/ProtectedAreas-dir-2000-60",
      "http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/dir-2000-60^http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/ProtectedAreas-dir-2000-60^http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/DesignatedWaters-dir-2000-60",
      "http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/dir-2002-49",
      "http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/dir-2002-49^http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/EnvironmentalNoiseExposure-dir-2002-49",
      "http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/dir-2002-49^http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/EnvironmentalNoiseExposure-dir-2002-49^http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/Agglomerations-IndustrialNoiseExposureDelineation-dir-2002-49",
      "http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/dir-2002-49^http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/EnvironmentalNoiseExposure-dir-2002-49^http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/Agglomerations-NoiseExposureDelineation-dir-2002-49",
      "http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/dir-2002-49^http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/EnvironmentalNoiseExposure-dir-2002-49^http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/Agglomerations-NoiseExposureDelineation-dir-2002-49^http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset/Agglomerations-NoiseExposureDelineationDEN-dir-2002-49"
    ]
  }
}
```

### Other types

Index document also contains other types of object for field like:

-   geom representing the bounding boxes of the record stored as GeoJSON
-   contact stored as simple fields and as object:

``` 
{
  "Org": "Direction Asset Management (SPW - Mobilité et Infrastructure)",
  "pointOfContactOrg": "Direction Asset Management (SPW - Mobilité et Infrastructure)",
  "contact: [
    {
      "organisation": "Direction Asset Management (SPW - Mobilité et Infrastructure)",
      "role": "pointOfContact",
      "email": "frederic.plumier@spw.wallonie.be",
      "website": "",
      "logo": "",
      "individual": "",
      "position": "",
      "phone": "",
      "address": "Boulevard du Nord, 8, NAMUR, 5000, Belgique"
    }
  ]
}
```

-   link

``` js
"link": [
    {
      "protocol": "WWW:LINK-1.0-http--link",
      "url": "http://geoapps.spw.wallonie.be/portailRoutes",
      "name": "Portail cartographique des routes - Application sécurisée",
      "description": "Application de consultation des routes et autoroutes de Wallonie. Cette application est sécurisée et n'est accessible que pour les agents de la DGO1 du SPW.",
      "applicationProfile": "",
      "group": 0
    },
```

-   recordLink

``` js
"recordLink": [
    {
      "type": "siblings",
      "associationType": "isComposedOf",
      "initiativeType": "collection",
      "to": "f010eda4-e791-44b1-8b2a-309f352f7d8f",
      "url": "",
      "title": "",
      "origin": "catalog"
    },
```

## Add a search field

Indexed fields are defined on a per schema basis on the schema folder (see `schemas/iso19139/src/main/plugin/iso19139/index-fields/index.xsl`).

This file define for each search criteria the corresponding element in a metadata record. For example, indexing the resource identifier of an ISO19139 record:

``` xml
<xsl:for-each select="gmd:identifier/*/gmd:code/(gco:CharacterString|gmx:Anchor)">
   <resourceIdentifier>
     <xsl:value-of select="."/>
   </resourceIdentifier>
 </xsl:for-each>
```

Once the field added to the index, user could query using it as a search criteria in the different kind of search services. For example using:

``` shell
curl -X POST "localhost:8080/geonetwork/srv/api/search/records/_search" \
    -H 'Accept: application/json' \
    -H 'Content-Type: application/json;charset=utf-8' \
    -d '{"from":0,"size":0,"query":{"query_string":{"query":"+resourceIdentifier:1234"}}}'
```

To customize how the field is indexed see `web/src/main/webResources/WEB-INF/data/config/index/records.json`.

To return it in the search response, use the `ce` parameter of the query. See <https://www.elastic.co/guide/en/elasticsearch/reference/current/search-fields.html>.

## Boosting at search time

See <https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#_boosting>.

By default, the search is defined as (see `web-ui/src/main/resources/catalog/js/CatController.js`):

``` js
'queryBase': 'any:(${any}) resourceTitleObject.default:(${any})^2',
```

## Scoring

By default the search engine compute score according to search criteria and the corresponding result set and the index content.

By default, the search score is defined as (see `web-ui/src/main/resources/catalog/js/CatController.js`):

``` js
'scoreConfig': {
   "boost": "5",
   "functions": [
     // Boost down member of a series
     {
       "filter": { "exists": { "field": "parentUuid" } },
       "weight": 0.3
     },
     // Boost down obsolete records
     {
       "filter": { "match": { "codelist_status": "obsolete" } },
       "weight": 0.3
     },
     // {
     //   "filter": { "match": { "codelist_resourceScope": "service" } },
     //   "weight": 0.8
     // },
     // Start boosting down records more than 3 months old
     {
       "gauss": {
         "dateStamp": {
           "scale":  "365d",
           "offset": "90d",
           "decay": 0.5
         }
       }
     }
   ],
   "score_mode": "multiply"
 },
```

## Language analyzer

By default a `rd` analyzer is used. If the catalog content is english, it may make sense to change the analyzer to `sh`. To customize the analyzer see `web/src/main/webResources/WEB-INF/data/config/index/records.json`
