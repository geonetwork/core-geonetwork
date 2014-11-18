<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:include href="dependant1.xsl"/>
    <xsl:include href="../dependant2.xsl"/>

    <xsl:template match="/">
        <root>
            <xsl:call-template name="dep1"/>
            <xsl:call-template name="dep2"/>
        </root>
    </xsl:template>

</xsl:stylesheet>