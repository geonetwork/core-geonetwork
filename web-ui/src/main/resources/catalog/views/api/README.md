This folder contains a simple HTML page using the API mode.


Configure a custom host to load the API:

* In apache config add a new virtual host

```
<VirtualHost gnapi:80>
    ServerName gnapi
    DocumentRoot /data/dev/geonetwork/web-ui/src/main/resources/catalog/views/api/

    ErrorLog ${APACHE_LOG_DIR}/error.log
    CustomLog ${APACHE_LOG_DIR}/access.log combined

    <Directory /data/dev/geonetwork/web-ui/src/main/resources/catalog/>
      Options Indexes FollowSymLinks
      AllowOverride None
      Require all granted
    </Directory>
</VirtualHost>

```


* In ```/etc/hosts``` add the new host

```
127.0.0.1 gnapi
```

Load ```http://gnapi```.
