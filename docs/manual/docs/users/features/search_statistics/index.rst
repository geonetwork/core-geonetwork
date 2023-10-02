.. _search_statistics:

Search Statistics
=================

Searches made through the user interface on the local catalog are logged to the GeoNetwork database. The database holds metadata about the search request (eg. date on which it was made, simple or advanced, query, ip address of the client making the request) and the details of the search terms/parameters and values used in the search query.

Querying search statistics
``````````````````````````

The search statistics page is part of the Administration menu. You need to be logged in as an Administrator to access it **and** search statistics needs to be **enabled** in the System Configuration. See :ref:`search_stats_config` in the 'System Configuration' section of the manual.

.. figure:: search-statistics-administration.png

*Finding search statistics in the Administration page*

As delivered the search statistics page delivers a number of indicators and some reports.

Adding your own search statistics
`````````````````````````````````

The indicators and reports that are needed/desired at your site might be different to those provided with GeoNetwork on the search statistics page. For that reason the search statistics implementation has been designed with extensibility in mind.

There are two types of search statistic services:

- pure XML: you specify a query on the database and a stylesheet to process the XML output from the query and display a web page in HTML - this could be used to add your own indicators to the search statistics page (or your own custom stats page).
- Java+XSLT: this is the more traditional GeoNetwork service, where you need to code a Jeeves service in Java which produces some XML and provide an XSLT to style the output from that service - this could be used in conjunction with the JFreeChart Java API to produce a chart or report from the search statistics

The service definitions for the default search statistics provide examples of both these types of service. You can find the service definitions in the GeoNetwork release at **INSTALL_DIR/web/geonetwork/WEB-INF/config-statistics.xml**. The XSLTs that style the output from these services are in **INSTALL_DIR/web/geonetwork/xsl/statistics**.

Exporting the search statistics as a CSV file
`````````````````````````````````````````````

If you feel so inclined, you may want to export the search statistics from the GeoNetwork database as a CSV file and process them in a spreadsheet. The search statistics page provided with GeoNetwork has this capability.

