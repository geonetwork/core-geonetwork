# Search Service {#q-search}

## OpenAPI Search

!!! note

    GeoNetwork 4


The Q Search endpoint is replaced by the ``/srv/api/search/records/_search`` endpoint.

Parameter Reference:

-   [Search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html) (Elasticsearch)

## Q Search

!!! note

    GeoNetwork 3


The Q Search endpoint allows you to query the catalog programmatically. It is available in the local catalog at `http://localhost:8080/geonetwork/srv/eng/q` (otherwise substitute your catalog URL).

### Query results parameters

The following parameters can be appended to your request to format the results:

-   `_content_type=json`: returns results in json format. If this parameter is not provided, then the results are returned in xml format.
-   `sortBy`: sorts the results by different criteria (example: `sortBy=relevance`):
    -   `relevance` (default sorting method if not provided)
    -   `title` (metadata title)
    -   `changeDate` (metadata datestamp)
    -   `rating`
    -   `popularity`
    -   `denominatorDesc`
    -   `denominatorAsc`
-   `sortOrder=reverse`: Used to sort alphabetically. Note this will sort in **ASCENDING** order (eg A - Z)
-   `from`, `to`: Used to return a subset of the results, usually for pagination (example: `from=1&to=20`)
-   `fast`: Used to indicate the information to return. Possible values:
    -   `index`: returns the metadata information from the Lucene index (a subset of the information). In most cases this is the best option as the retrieval of information from the Lucene index is very fast.

        The fields returned are configured in the `dumpFields` section in <https://github.com/geonetwork/core-geonetwork/blob/master/web/src/main/webapp/WEB-INF/config-lucene.xml#L107>

    -   `false`: returns the raw (full) metadata. This is slower as it will retrieve every metadata attribute from the database. If this parameter is not provided, it returns a minimal set of information for each record: uuid, internal id, metadata schema, create/change dates
-   `buildSummary`: Returns a summary element with search facets that can be used to filter the metadata, typically used to provide quick filters (facets) on the search results page. Values:
    -   `true` (default, if the parameter is not provided).
    -   `false`: does not return the summary.
-   `summaryOnly`: Returns the summary (depending on the value of the parameter `buildSummary`) and results. Values:
    -   `0` (default, if the parameter is not provided).
    -   Any other value returns the summary only.
-   `resultType`: type of summary to return. Summaries are configured in the `summaryTypes` section in <https://github.com/geonetwork/core-geonetwork/blob/master/web/src/main/webapp/WEB-INF/config-summary.xml#L132-L249>
    -   `hits` (default value if not provided), returns the fields configured in the `hits` section in <https://github.com/geonetwork/core-geonetwork/blob/master/web/src/main/webapp/WEB-INF/config-summary.xml#L185>
    -   `details` (recommended value to send), returns the fields configured in the `details` section in <https://github.com/geonetwork/core-geonetwork/blob/master/web/src/main/webapp/WEB-INF/config-summary.xml#L133>
-   `extraDumpFields`: a comma-separated list of additional fields that you wish to return alongside the fields returned according to the resultType you have chosen. The wildcard character `*` can be used to match multiple fields. For example `extraDumpFields=mycustomfield*` would match mycustomfield1 and mycustomfield2.
-   Other values in the summaries section are allowed

### Query filter parameters

You can search on any field(s) indexed in Lucene. For a complete reference see <https://github.com/geonetwork/core-geonetwork/blob/master/schemas/iso19139/src/main/plugin/iso19139/index-fields/default.xsl>

Note you can query the Lucene index graphically,using a Java-based graphical tool such as [Luke](https://github.com/DmitryKey/luke). Version [4.10.4](https://github.com/DmitryKey/luke/releases/tag/luke-4.10.4.1/) is required to work with the version of Lucene bundled with GeoNetwork. Download the jar file where you can access the GeoNetwork index files, then execute with:

`java -jar luke-with-deps.jar`

Then follow the instructions in the tool.

Most relevant fields:

-   `any`: A special Lucene field that indexes all the text content in the metadata. Example: <http://localhost:8080/geonetwork/srv/eng/q?any=water&from=1&to=20&resultType=details&fast=index&_content_type=json>

There are some additional query fields, that use the content from the Lucene field `any`.

-   `or`: extract the tokens of the query parameter to return the results that contain at least 1 of the tokens
-   `without`: extract the tokens of the query parameter to return the results that don't contain any of the tokens.
-   `phrase`: return the results that contain the exact text as provided in the search query parameter.
-   `title`: metadata title.
-   `abstract`: metadata abstract.
-   `topicCat`: metadata topic categories.
-   `keyword`: metadata keywords.
-   `type`: hierarchyLevel (dataset, service, etc.)

If several tokens are included in the query, an AND query with all the tokens is executed. For example, `title=roads&topicCat=biota`. This query will return the results that contain roads in the title AND have the topic category biota.

An OR query of several fields can be executed using the format: `field1_OR_field2_OR_... =value`. For example, `title_OR_abstract=roads` returns the metadata that contain roads in the title OR the abstract.

Additionally an OR query of several values for a single field can be executed, if the Lucene configuration for that field allows it, with the following format: `field=value1 or value2 or ...` For example `topicCat=biota or farming`, returns the metadata where the topic category is either biota OR farming. If the query was executed as `topicCat=biota&topicCat=farming` then only the metadata with BOTH topic categories would be returned.

### Date Searches

There are a number of ways that you can search by date. Date searches should be of the form YYYY-MM-DD

-   dateFrom/dateTo: uses the changeDate parameter in the index.
-   creationDateFrom/To: uses the creation date.
-   revisionDateFrom/To: uses the revision date.

### Query examples

Query with any field for metadata containing the string 'infrastructure', returning json, using the fast index to return results, and returning the fields configured in `config-summary.xml`:

<http://localhost:8080/geonetwork/srv/eng/q?any=infrastructure&_content_type=json&fast=index&from=1&resultType=details&sortBy=relevance&to=20>

Query datasets with title containing the string 'infrastructure', returning json, using the fast index to return results, returning the fields configured in `config-summary.xml` and returning only the first 20 results (ordered by relevance):

<http://localhost:8080/geonetwork/srv/eng/q?title=infrastructure&type=dataset&_content_type=json&fast=index&from=1&resultType=details&sortBy=relevance&to=20>

Query datasets with a revision date in June 2019 using the fast index to return results, returning the fields configured in `config-summary.xml` and returning only the first 20 results (ordered by relevance):

<http://localhost:8080/geonetwork/srv/eng/q?_content_type=json&revisionDateFrom=2019-06-01&revisionDateTo=2019-06-30&fast=index&from=1&resultType=details&sortBy=relevance&to=20>
