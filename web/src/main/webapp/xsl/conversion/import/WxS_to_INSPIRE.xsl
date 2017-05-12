<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:srv="http://www.isotc211.org/2005/srv"
    >

  <!-- Modify harvested WxS capabilities to conform to INSPIRE specifications -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()[name(self::*)!='geonet:info']"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="srv:serviceType">
    <xsl:message>XSL: replacing <xsl:value-of select="normalize-space(gco:LocalName/text())"/> in <xsl:value-of select="name()"/></xsl:message>
    <xsl:copy>
        <xsl:choose>
            <xsl:when test="contains(gco:LocalName/text(), 'WMS')">
                <gco:LocalName>view</gco:LocalName>
            </xsl:when>
            <xsl:when test="contains(gco:LocalName/text(), 'WFS')">
                <gco:LocalName>download</gco:LocalName>
            </xsl:when>
            <xsl:otherwise>
                <!-- whoa, what's that? let's put in some random value, view will just do -->
                <gco:LocalName>view</gco:LocalName>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
