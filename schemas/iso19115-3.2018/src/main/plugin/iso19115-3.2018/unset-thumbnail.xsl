<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:gn="http://www.fao.org/geonetwork" 
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0">
  
  <xsl:template match="/root">
    <xsl:apply-templates select="mdb:MD_Metadata|*[contains(@gco:isoType, 'MD_Metadata')]"/>
  </xsl:template>
  
  <xsl:template match="mri:graphicOverview[mcc:MD_BrowseGraphic/mcc:fileDescription/gco:CharacterString = /root/env/type]"/>
  
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="gn:info" priority="2"/>
</xsl:stylesheet>
