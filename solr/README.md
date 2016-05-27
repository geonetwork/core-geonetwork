## Installing Solr

See [Documentation](/docs/manuals/en/maintainer-guide/installing/installing-solr.rst)

The quick way for developers from the source code:

```
cd solr/solr-config
# Download and install Solr
mvn install -Psolr-download

# Create collection
mvn install -Psolr-init

# Start Solr
mvn exec:exec -Dsolr-start
```

