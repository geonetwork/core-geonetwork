# Characterset

By default the character set of GeoNetwork is ``UTF-8``. This works well for many locales in the world and is compatible with ASCII that is typically used in US and Canada. However, if ``UTF-8`` is not a compatible characterset in your environment you can change the default.

To change it within GeoNetwork simply start the application with the system property geonetwork.file.encoding set to the desired character set name.

For example if you are running Tomcat you can set

``` xml
JAVA_OPTS="-Dgeonetwork.file.encoding=UTF-16"
```

to the startup script and the default codec in GeoNetwork will be ``UTF-16``.

It is also recommended to set the file.encoding parameter to the same codec as this dictates to the default encoding used in Java and the Web Server may reference at times use the default codec.

Finally, by default the URL parameters are typically interpreted as ASCII characters which can be a problem when searching for metadata that are not in the english language. Each Web Server will have a method for configuring the encoding used when reading the parameters. For example, in Tomcat the encoding/charset configuration is in the **`server.xml`** Connector element.
