<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gnf="http://www.fao.org/geonetwork/functions">

    <xsl:include href="@@formatterDir@@/functions.xsl" />

    <xsl:template match="/" >
        <html>
            <body>
                <xsl:copy-of select="gnf:p()" />
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
