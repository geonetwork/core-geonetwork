<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


    <xsl:output
            omit-xml-declaration="yes"
            method="xml"
            indent="yes"
            encoding="UTF-8"/>

    <xsl:include href="banner.xsl"/>

    <xsl:template mode="css" match="/">
    </xsl:template>

    <xsl:template mode="script" match="/">

    </xsl:template>

    <xsl:template match="/">
        <div id="header">
            <xsl:call-template name="banner"/>
        </div>

    </xsl:template>


</xsl:stylesheet>
