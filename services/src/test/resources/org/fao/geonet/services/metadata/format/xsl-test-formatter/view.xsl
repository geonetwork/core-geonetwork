<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:tr="java:org.fao.geonet.services.metadata.format.SchemaLocalizations"
    xmlns:gnf="http://www.fao.org/geonetwork/functions"
    exclude-result-prefixes="tr gnf">

    <xsl:include href="sharedFormatterDir/functions.xsl" />

    <xsl:template match="/" >
        <html>
            <body>
                <div class="tr"><xsl:value-of select="tr:node-label(tr:create('iso19139'), 'gmd:title', 'gmd:parent')"/> </div>
                <xsl:copy-of select="gnf:p()" />
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
