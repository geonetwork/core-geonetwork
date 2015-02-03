<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
  xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="#all" version="2.0">

  <!-- 
      Usage: 
        thumbnail-from-url-remove?thumbnail_url=http://geonetwork.org/thumbnails/image.png
    -->

  <xsl:param name="thumbnail_url"/>

  <!-- Remove the thumbnail define in thumbnail_url parameter -->
  <xsl:template
    match="gmd:graphicOverview[gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString= $thumbnail_url]"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
