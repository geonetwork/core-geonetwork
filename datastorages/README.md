# Data storage

Extensions for data storage (used for storage resources such as record attachments and thumbnails).

Core provides the default implementation for a data directory:

* org.fao.geonet.resource.FileResources

For more information see:

* org.fao.geonet.resource.Resources

## Maven Profiles

Maven profiles are used to enable the optional data storage modules:

```
mvn install -Pdatastorage-s3
```

These build profiles are also included in the `release` profile for distribution.
