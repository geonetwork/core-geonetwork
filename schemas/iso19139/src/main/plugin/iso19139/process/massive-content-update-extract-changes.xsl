<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                  xmlns:geonet="http://www.fao.org/geonetwork"
                  xmlns:gmd="http://www.isotc211.org/2005/gmd"
                  exclude-result-prefixes="gmd xsl geonet">

  <xsl:template match="/">
    <changes>
      <xsl:apply-templates select="@*|node()"/>
    </changes>
  </xsl:template>

  <xsl:template match="*[@geonet:change]">
    <change>
      <fieldid><xsl:value-of select="@geonet:change"/></fieldid>
      <originalval><xsl:value-of select="@geonet:original"/></originalval>
      <changedval><xsl:value-of select="@geonet:new"/></changedval>
    </change>
  </xsl:template>


  <xsl:template match="@*|node()">
      <xsl:apply-templates select="@*|node()"/>
  </xsl:template>

</xsl:stylesheet>
