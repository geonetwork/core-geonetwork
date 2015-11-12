<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:dct="http://purl.org/dc/terms/" xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:gfc="http://www.isotc211.org/2005/gfc" xmlns:ows="http://www.opengis.net/ows"
  xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="#all">
  
  <xsl:param name="displayInfo"/>
  
  <xsl:template match="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType">
    <xsl:variable name="info" select="geonet:info"/>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="gfc:name|gfc:typeName"/>
      <xsl:apply-templates select="gfc:scope"/>
      <xsl:apply-templates select="gfc:producer"/>
      <!-- GeoNetwork elements added when resultType is equal to results_with_summary -->
      <xsl:if test="$displayInfo = 'true'">
        <xsl:copy-of select="$info"/>
      </xsl:if>
      
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
