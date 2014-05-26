<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output encoding="UTF-8" method="text" />
    <xsl:template match="/">

        <xsl:value-of select="/root/data"/>
    </xsl:template>
</xsl:stylesheet>