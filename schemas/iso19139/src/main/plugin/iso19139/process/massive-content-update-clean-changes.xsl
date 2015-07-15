<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                  xmlns:geonet="http://www.fao.org/geonetwork"
                  xmlns:gmd="http://www.isotc211.org/2005/gmd" exclude-result-prefixes="gmd geonet">

  <xsl:param name="removeEmptyElement" select="'false'"/>

  <xsl:template match="/">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[$removeEmptyElement = 'true' and @geonet:new = '']"
                priority="2"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>
  <xsl:template match="@geonet:*" priority="2"/>

</xsl:stylesheet>
