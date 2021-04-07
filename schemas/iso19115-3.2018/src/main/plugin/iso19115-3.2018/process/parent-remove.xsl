<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0" xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  exclude-result-prefixes="#all" >
  
  <!-- Remove geonet:* elements and parentMetadata. -->
  <xsl:template match="gn:*|mdb:parentMetadata" priority="2"/>
  
  <!-- Copy everything -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
