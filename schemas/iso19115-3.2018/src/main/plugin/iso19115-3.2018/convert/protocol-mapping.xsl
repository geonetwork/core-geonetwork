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
            <protocol>WWW:DOWNLOAD:x-gis/x-shapefile</protocol>
        </entry>
        <entry>
            <format>json</format>
            <protocol>WWW:DOWNLOAD:application/json</protocol>
        </entry>
        <entry>
            <format>web page</format>
            <protocol>WWW:LINK-1.0-http--link</protocol>
        </entry>
        <entry>
            <format>arcgis geoservices rest api</format>
            <protocol>ESRI:REST</protocol>
        </entry>
    </xsl:variable>

</xsl:stylesheet>
