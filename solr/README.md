## Installing Solr

Download Solr from http://lucene.apache.org/solr/mirrors-solr-latest-redir.html
and unzip the file.

```
cd solr
wget http://apache.crihan.fr/dist/lucene/solr/5.3.1/solr-5.3.1.tgz
tar xvfz solr-5.3.1.tgz
```
Additional libraries need to be installed to activate features like 
* spatial search support. Download JTS from https://sourceforge.net/projects/jts-topo-suite/files/jts/1.13/
  and copy it to the Solr lib folder ie. ``server/solr-webapp/webapp/WEB-INF/lib``
* XSL v2 support. Download Saxon from https://sourceforge.net/projects/saxon/files/Saxon-HE/9.6/
  and copy it to the Solr lib folder ie. ``server/solr-webapp/webapp/WEB-INF/lib``


Running from the source code, use maven to download Solr and additional libraries.
```
mvn install -Psolr-download
```


## Creating the default collection 


Start Solr and then create the default collection:
```
solr-5.2.1/bin/solr create -p 8984 -c catalog -d src/main/solr-cores/catalog
```

or use maven when running from the source code:

```
mvn install -Psolr-init
```
This will start Solr, create the collection and stop Solr.

TODO: One collection per node should be created


## Starting and stopping Solr

Manually start and stop Solr using:

```
solr-5.3.1/bin/solr start -c -p 8984
solr-5.3.1/bin/solr stop 
```


or use maven when running from the source code:

```
mvn exec:exec -Dsolr-start
```

Then access Solr admin page from http://localhost:8984/solr.

To stop Solr when using maven, simply stop the process as Solr is started in
foreground mode.


## Secure Solr server

TODO


## WFS Feature Indexing

Start the sample Camel application:

```
cd wfsfeature-harvester
mvn camel:run
```


## Deleting document

```
curl http://localhost:8984/solr/catalog/update \
    --data '<delete><query>*:*</query></delete>' \
    -H 'Content-type:text/xml; charset=utf-8'
    
curl http://localhost:8984/solr/catalog/update \
    --data '<commit/>' \
    -H 'Content-type:text/xml; charset=utf-8'

```
