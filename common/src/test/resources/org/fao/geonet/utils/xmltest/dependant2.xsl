<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:include href="xsl/nested/dependant3.xsl" />
    <xsl:template name="dep2">
        <dep2>dep2</dep2>
        <xsl:call-template name="dep3" />
    </xsl:template>

</xsl:stylesheet>