<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                exclude-result-prefixes="#all">

    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:variable name="format-protocol-mapping">
        <entry>
            <format>csv</format>
            <protocol>WWW:DOWNLOAD:text/csv</protocol>
        </entry>
        <entry>
            <format>geojson</format>
            <protocol>WWW:DOWNLOAD:application/vnd.geo+json</protocol>
        </entry>
        <entry>
            <format>kml</format>
            <protocol>WWW:DOWNLOAD:application/vnd.google-earth.kml+xml</protocol>
        </entry>
        <entry>
            <format>zip</format>
            <protocol>WWW:DOWNLOAD:application/zip</protocol>
        </entry>
        <entry>
            <format>shapefile</format>
            <format>shp</format>
            <protocol>WWW:DOWNLOAD:x-gis/x-shapefile</protocol>
        </entry>
        <entry>
            <format>json</format>
            <protocol>WWW:DOWNLOAD:application/json</protocol>
        </entry>
        <entry>
            <format>pdf</format>
            <protocol>WWW:DOWNLOAD:application/pdf</protocol>
        </entry>
        <entry>
            <format>xls</format>
            <protocol>WWW:DOWNLOAD:application/vnd.ms-excel</protocol>
        </entry>
        <entry>
            <format>xlsx</format>
            <format>excel</format>
            <protocol>WWW:DOWNLOAD:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet</protocol>
        </entry>
        <entry>
            <format>rtf</format>
            <protocol>WWW:DOWNLOAD:application/rtf</protocol>
        </entry>
        <entry>
            <format>web page</format>
            <format>html</format>
            <format>arcgis</format>
            <protocol>WWW:LINK-1.0-http--link</protocol>
        </entry>
        <entry>
            <format>wms</format>
            <protocol>OGC:WMS</protocol>
        </entry>
        <entry>
            <format>wfs</format>
            <protocol>OGC:WFS</protocol>
        </entry>
    </xsl:variable>

</xsl:stylesheet>
