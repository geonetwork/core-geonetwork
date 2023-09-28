# Metadata Select services {#metadata_xml_select}

These services are for creating and managing a set of selected metadata records. The selected set is normally used by the metadata.batch services eg. `metadata.batch.update.privileges`{.interpreted-text role="ref"}, `metadata.batch.newowner`{.interpreted-text role="ref"}, `metadata.batch.update.status`{.interpreted-text role="ref"}, `metadata.batch.update.categories`{.interpreted-text role="ref"}, `metadata.batch.version`{.interpreted-text role="ref"}, `metadata.batch.processing`{.interpreted-text role="ref"} and `metadata.batch.delete`{.interpreted-text role="ref"}.

## Select metadata records (xml.metadata.select) {#metadata.select}

This service can be used to build and manage a selected set of metadata.

### Request

Parameters:

-   **id**: Identifier of metadata to select (can be more than one)
-   **selected**: Selection state. Values: add, add-all, remove, remove-all

Select all metadata example:

    Url:
    http://localhost:8080/geonetwork/srv/eng/metadata.select

    Mime-type:
    application/xml

    Post request:
    <?xml version="1.0" encoding="UTF-8"?>
    <request>
      <selected>add-all</selected>
    </request>

Select a metadata record example:

    Url:
    http://localhost:8080/geonetwork/srv/eng/metadata.select

    Mime-type:
    application/xml

    Post request:
    <?xml version="1.0" encoding="UTF-8"?>
    <request>
      <id>2</id>
      <selected>add</selected>
    </request>

Clear metadata selection example:

    Url:
    http://localhost:8080/geonetwork/srv/eng/metadata.select

    Mime-type:
    application/xml

    Post request:
    <?xml version="1.0" encoding="UTF-8"?>
    <request>
      <selected>remove-all</selected>
    </request>

### Response

The XML response from this service *always* contains the number of metadata records selected after applying the select operation.

Example:

    <?xml version="1.0" encoding="UTF-8"?>
    <request>
      <Selected>10</Selected>
    </request>
