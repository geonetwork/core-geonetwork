<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                exclude-result-prefixes="#all">

    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:variable name="format-mimetype-mapping">
        <entry>
            <format>csv</format>
            <mimetype>text/csv</mimetype>
            <protocol>WWW:DOWNLOAD</protocol>
        </entry>
        <entry>
            <format>geojson</format>
            <mimetype>application/vnd.geo+json</mimetype>
            <protocol>WWW:DOWNLOAD</protocol>
        </entry>
        <entry>
            <format>kml</format>
            <mimetype>application/vnd.google-earth.kml+xml</mimetype>
            <protocol>WWW:DOWNLOAD</protocol>
        </entry>
        <entry>
            <format>zip</format>
            <mimetype>application/zip</mimetype>
            <protocol>WWW:DOWNLOAD</protocol>
        </entry>
        <entry>
            <format>shapefile</format>
            <mimetype>x-gis/x-shapefile</mimetype>
            <protocol>WWW:DOWNLOAD</protocol>
        </entry>
        <entry>
            <format>json</format>
            <mimetype>application/json</mimetype>
            <protocol>WWW:DOWNLOAD</protocol>
        </entry>
        <entry>
            <format>web page</format>
            <mimetype>text/html</mimetype>
            <protocol>WWW:LINK</protocol>
        </entry>
        <entry>
            <format>arcgis geoservices rest api</format>
            <mimetype>application/json</mimetype>
            <protocol>ESRI:REST</protocol>
        </entry>
    </xsl:variable>

</xsl:stylesheet>
