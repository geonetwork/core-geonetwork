# Configuring printing of the map {# map-print-setup}

This section describes how to configure the options to print maps. Printing a map generates a pdf file on the server which is downloaded by the client to be send to a printer. During pdf creation map data is downloaded from various sources to be included in the pdf.

GeoNetwork needs to be able to access the external resource. Set up a webproxy in `system settings` if your network requires a webproxy to be set up to access the internet.

Locate the file ``WEB-INF/config-print/print-config.yaml``, this configuration file has a lot of options to customise the print options. Read more about the various parameters at <http://www.mapfish.org/doc/print/configuration.html>

The folder contains 3 template files:

-   ``template.pdf`` and ``template-landscape.pdf`` which are used to generate the map viewer pdf
-   ``template-thumbnail.pdf`` which is used to build a thumbnail in the metadata editor (see [Generating a thumbnail using WMS layers](../user-guide/associating-resources/linking-thumbnail.md#linking-thumbnail-from-wms)).

These templates are created by exporting pdf from the included ``template.pdf`` file in the folder.
