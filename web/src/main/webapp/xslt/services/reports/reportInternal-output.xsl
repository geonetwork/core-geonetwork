<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet 	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                    xmlns:geonet="http://www.fao.org/geonetwork"
                    xmlns:exslt= "http://exslt.org/common"
                    exclude-result-prefixes="xsl exslt geonet">

    <xsl:output method="text" version="1.0" encoding="utf-8" indent="no"/>

    <xsl:include href="reportCommon-output.xsl"/>

    <xsl:template name="csvHeader">
        <xsl:param name="record"/>

        <xsl:for-each select="$record/*">
            <xsl:choose>
                <xsl:when test="name(.) = name(following-sibling::node())">
                </xsl:when>
                <xsl:otherwise>
                    <!--<xsl:value-of select="normalize-space(name(.))"/> -->
                    <xsl:variable name="columnKey" select="normalize-space(name(.))"/>
                    <xsl:value-of select="/root/gui/reports/internalRecords/*[name() = $columnKey]"/>

                    <xsl:value-of select="$sep"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>

        <xsl:call-template name="newLine"/>
    </xsl:template>
</xsl:stylesheet>