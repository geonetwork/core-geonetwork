# Setup Kibana {#statistics_kibana}

This section describes how to setup Kibana to be used in GeoNetwork to visualize the search/content statistics:

-   Download Kibana from <http://www.elastic.co/downloads/past-releases/>. For Geonetwork 3.8.x version 7.2.x is recommended.

-   Unzip the file, for example to ``/opt/kibana``

-   Configure Kibana to use it in GeoNetwork:

    ``` shell
    $ cd opt/kibana
    $ vi config/kibana.yml
        server.basePath: "/geonetwork/dashboards"
        kibana.index: “.dashboards"
    ```

-   Execute Kibana:

    ``` shell
    $ cd /opt/kibana/bin
    $ ./kibana &
    ```

    !!! note

        Usually you'll want to configure Kibana to start automatically when the server is startup, this is not covered in this guide.


-   Verify in a browser that Kibana is running: <http://localhost:5601/app/kibana>

-   Kibana should also be visible in Geonetwork at <http://localhost:8080/geonetwork/dashboards>

## Load Kibana data

Visit Kibana in a browser using one of the above links and go to 'Saved Objects'.

Import export.json from <https://github.com/geonetwork/core-geonetwork/blob/master/es/es-dashboards/data/export.json>
