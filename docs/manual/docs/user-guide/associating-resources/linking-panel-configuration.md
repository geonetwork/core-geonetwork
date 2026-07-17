# Associated resources panel configuration

The schema's associated resource panel configuration file is located at `schemas/iso19115-3.2018/src/main/plugin/iso19139/config/associated-panel/default.json`.

The configuration file defines the types of resources that can be associated with a record, and the fields that are required for each type of resource.

* `types` defines configuration for distributions (eg. overview, service URL, website)
* `associatedResourcesTypes` defines configuration for associated resources (eg. parent, source, revision)

## Distribution configuration

For distribution, configuration has the following properties:

* `group` is used to group different types of resources in the UI
* `label` is the label key of the resource type (defined in JSON loc file or in database translations)
* `sources` defines the sources that can be used to add a resource of this type. The source can be:
  * `filestore`: file uploaded to the metadata file store
  * `thumbnailMaker`: file created when the metadata register a WMS service and that can be used to create a thumbnail for the record
  * `metadataStore`: metadata registered in the catalogue (eg. WMS service metadata record can be used to populate a WMS service URL)
* `icon` is the icon class.
* `fileStoreFilter` is a regular expression to filter files available in the metadata store
* `process` is the XSL process to be applied when the resource is added
* `fields` defines the fields to populate for the resource and their properties (eg. visible or not, multilingual or not, default value)

Example:

```json
    "types": [
      {
        "group": "overview",
        "label": "onlineDiscoverThumbnail",
        "sources": {
          "filestore": true,
          "thumbnailMaker": true
        },
        "icon": "fa gn-icon-thumbnail",
        "fileStoreFilter": "*.{jpg,JPG,jpeg,JPEG,png,PNG,gif,GIF}",
        "process": "thumbnail-add",
        "fields": {
          "url": {
            "isMultilingual": false,
            "param": "thumbnail_url",
            "label": "overviewUrl"
          },
          "name": {
            "label": "overviewDescription",
            "param": "thumbnail_desc"
          }
        }
      },
```



## Associated resources

For associated resources, configuration has the following properties:

* `type` is the type of association (eg. parent, child, sibling)
* `label` is the label key of the resource type (defined in JSON loc file or in database translations)
* `config` defines the configuration for the association type, which has the following properties:
  * `fields` allows to defined values for `associationType` and `initiativeType`
  * `sources` defines the sources that can be used to add a resource of this type 

### Sources

`sources` defines the sources that can be used to add a resource of this type.

#### Metadata store

When `metadataStore` is defined as a source, the user can search for metadata records in the catalogue to associate with the current record. The search form can be configured using the `params` property. For example, you can filter the search results to only show records that are not templates by setting `isTemplate` to `n`.

```json
        "sources": {
          "metadataStore": {
            "label": "searchAservice",
            "params": {
              "resourceType": ["service"],
              "isTemplate": "n"
            }
          },
```

`searchParamsPerType` allows to define specific search parameter for associated resources depending on their association and initiative types to target specific records:

* `crossReference-study` association type will only search for records with `dublin-core` schema
* `crossReference-*` association type will only search for records that are not harvested

```json
"config": {
  "sources": {
    "metadataStore": {
      "searchParamsPerType": {
        "crossReference-study": {
          "documentStandard": "dublin-core"
        },
        "crossReference-*": {
          "isHarvested": "false"
        }
```
In another plugin:

* `catalog-*` association type will only search for records with a `catalog` resource type
* `nextResource-*` association type will only search for `dataset` or `service`

```json
"config": {
  "sources": {
    "metadataStore": {
      "searchParamsPerType": {
        "catalog-*": {
          "resourceType": ["catalog"]
        },
        "nextResource-*": {
          "resourceType": ["dataset", "service"]
        }
      }
```



#### DOI 

Allows to select metadata from a DOI endpoint:

* DataCite
* Crossref

Can be configured to search both types of providers or only one of them. When both types of providers are configured, results from both are combined in the results list.

For each type of source:

* `url`: API end-point.
* `params`: search parameters. Currently supported query value, with the query to use. It allows the following placeholders:
  * `{query}`: replaced with the text entered in the search field.
  * `{prefix}`: replaced with the DOI prefix configured.

```json
"config": {
  "sources": {
    "doiapi": {
      "prefix": "10.1111",
      "datacite": {
        "url": "https://api.datacite.org/dois",
        "params": {"query": "titles.title:{query}* OR doi:{query} OR id:{query}"}
      },
      "crossref": {
        "url": "https://api.crossref.org/works?select=DOI%2Ctitle%2Ctype%2Cprefix%2Cabstract%2CURL",
        "params": {"query": "rows=10&amp;query={prefix}%2F{query}&amp;filter=doi%3A{prefix}%2F{query}"}
      }
```


#### Remote URL

When `remoteurl` is defined as a source, the user can add a resource by providing a URL. The `multiple` property allows to specify if multiple URLs can be added for this association type.

```json
      "config": {
        "sources": {
          "remoteurl": {"multiple": false}
```


### Example

```json
"associatedResourcesTypes": [{
      "type": "parent",
      "label": "linkToParent",
      "config": {
        "fields": {"associationType": "partOfSeamlessDatabase", "initiativeType": "" },
        "sources": {
          "metadataStore": {
            "params": {
              "isTemplate": "n"
            }
          },
          "remoteurl": {"multiple": true}
        }
      }
    }, {
      "type": "siblings",
      "label": "linkToSibling",
      "config": {
        "sources": {
          "metadataStore": {
            "params": {
              "isTemplate": "n"
            }
          },
          "remoteurl": {"multiple": true}
        }
      }
    }
```

