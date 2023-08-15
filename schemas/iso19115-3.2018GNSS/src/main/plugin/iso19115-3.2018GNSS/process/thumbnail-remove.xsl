<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:gn="http://www.fao.org/geonetwork" exclude-result-prefixes="#all" version="2.0">

  <!-- 
      Usage: 
        thumbnail-from-url-remove?thumbnail_url=http://geonetwork.org/thumbnails/image.png
    -->
  <xsl:param name="thumbnail_url"/>

  <!-- Remove the thumbnail define in thumbnail_url parameter -->
  <xsl:template
    match="mri:graphicOverview[mcc:MD_BrowseGraphic/mcc:fileName/gco:CharacterString= $thumbnail_url]"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="gn:*" priority="2"/>

</xsl:stylesheet>
