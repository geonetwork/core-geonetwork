# Search Service

GeoNetwork provides a access to ***Elasticsearch*** `/srv/api/search/records/_search` and `/srv/api/search/records/_msearch` end-points. These endpoints accept `POST` requests, with request body containing an Elasticsearch JSON query.

Reference

-   [Search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html) ( Elasticsearch Guide )
-   [Multi search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html) (Elasticsearch Guide)

## Search API examples

This section provides some query `POST` examples of `/srv/api/search/records/_search` end-point:

1.  To test examples navigate Swagger API documentation at `/srv/api/index.html`

2.  Locate the *search* heading, and the `/search/records/_search` `POST` end-point

3.  Use the *Try it out* button with:
     
     * **bucket**: `metadata`
     * **relatedType**: 
     * **Request body**: Chosen from the examples below

4.  Press **Execute** to run the example.

     ![](img/swagger-search-endpoint.png)

### Text search query

Query with any field for metadata containing the string `infrastructure`, using a query with Lucene syntax and excluding metadata templates:

```json
{
  "query": {
    "bool": {
      "must": [
        {
          "query_string": {
            "query": "+anytext:infrastructure "
          }
        }
      ],
      "filter": [
        {
          "term": {
            "isTemplate": {
              "value": "n"
            }
          }
        }
      ]
    }
  }
}
```

### Subset results

Query with any field for metadata containing the string `infrastructure`, using a query with Lucene syntax and excluding metadata templates, returning a subset of the information:

```json
{
  "query": {
    "bool": {
      "must": [
        {
          "query_string": {
            "query": "+anytext:infrastructure "
          }
        }
      ],
      "filter": [
        {
          "term": {
            "isTemplate": {
              "value": "n"
            }
          }
        }
      ]
    }
  },
  "_source": {
    "includes": [
      "uuid",
      "id",
      "resourceType",
      "resourceTitle*",
      "resourceAbstract*"
    ]
  }
}
```

### Dataset query

Query datasets with title containing the string `infrastructure`, using a query with Lucene syntax and excluding metadata templates:

```json
{
  "query": {
    "bool": {
      "must": [
        {
          "query_string": {
            "query": "+anytext:infrastructure +resourceType:dataset"
          }
        }
      ],
      "filter": [
        {
          "term": {
            "isTemplate": {
              "value": "n"
            }
          }
        }
      ]
    }
  }
}
```

### Revision date query

Query datasets with a revision date in June 2019 and excluding metadata templates:

```json
{
  "query": {
    "bool": {
      "must": [
        {
          "term": {
            "resourceType": {
              "value": "dataset"
            }
          }
        },
        {
          "range": {
            "resourceTemporalDateRange": {
              "gte": "2019-06-01",
              "lte": "2019-06-30",
              "relation": "intersects"
            }
          }
        }
      ],
      "filter": [
        {
          "term": {
            "isTemplate": {
              "value": "n"
            }
          }
        }
      ]
    }
  }
}
```
