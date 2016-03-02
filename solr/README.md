## Installing Solr

### Manual installation

Download Solr from http://lucene.apache.org/solr/mirrors-solr-latest-redir.html
and unzip the file.

```
cd solr/solr-config
wget http://apache.crihan.fr/dist/lucene/solr/5.4.1/solr-5.4.1.tgz
tar xvfz solr-5.4.1.tgz
```

Additional libraries need to be installed to activate features like
* spatial search support. Download JTS from https://sourceforge.net/projects/jts-topo-suite/files/jts/1.13/
  and copy it to the Solr lib folder ie. ``server/solr-webapp/webapp/WEB-INF/lib``
* XSL v2 support. Download Saxon from https://sourceforge.net/projects/saxon/files/Saxon-HE/9.6/
  and copy it to the Solr lib folder ie. ``server/solr-webapp/webapp/WEB-INF/lib``


Manually start and stop Solr using:

```
solr-5.4.1/bin/solr start -c -p 8984
```

Then create the default collection:

```
solr-5.4.1/bin/solr create -p 8984 -c catalog_srv -d src/main/solr-cores/catalog
```

Stop Solr using

```
solr-5.4.1/bin/solr stop
```


### Install using maven

Running from the source code, use maven to download Solr and additional libraries.
```
cd solr/solr-config
mvn install -Psolr-download
mvn install -Psolr-init
mvn exec:exec -Dsolr-start
```

To stop Solr when using maven, simply stop the process as Solr is started in
foreground mode.


### Check Solr installation

Access Solr admin page from http://localhost:8984/solr.


## Secure Solr server

TODO

## Deleting document

```
curl http://localhost:8984/solr/catalog/update \
    --data '<delete><query>*:*</query></delete>' \
    -H 'Content-type:text/xml; charset=utf-8'
    
curl http://localhost:8984/solr/catalog/update \
    --data '<commit/>' \
    -H 'Content-type:text/xml; charset=utf-8'

or

cd solr/solr-config
./solrdeletedoc.sh
```
