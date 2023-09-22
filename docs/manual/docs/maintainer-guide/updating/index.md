# Updating the application {#updating}

Since GeoNetwork 4.0.4, passwords stored in the database for the mail server, harvesters, etc. are encrypted with [Jasypt](http://www.jasypt.org/).

By default, a random encryption password is generated when GeoNetwork is started, if it is not already defined, and it is stored in the file **`/geonetwork/WEB-INF/data/config/encryptor/encryptor.properties`**. If you have set the location of the data directory outside of the application, the file will be stored in this external location. Read more at [Customizing the data directory](../../install-guide/customizing-data-directory.md).

The file with the encryption settings **must be copied** to the new installation when upgrading the application; otherwise, it will not be possible to decrypt the existing passwords stored in the database.
