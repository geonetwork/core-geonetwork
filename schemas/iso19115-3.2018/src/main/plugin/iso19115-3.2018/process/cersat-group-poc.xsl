<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all" version="2.0">

  <xsl:template match="mri:MD_DataIdentification|
                      *[@gco:isoType='mri:MD_DataIdentification']|
                      srv:SV_ServiceIdentification">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:variable name="element-before"
                    select="mri:citation|mri:abstract|mri:purpose|mri:credit|mri:status|mri:pointOfContact"/>

      <xsl:apply-templates select="$element-before"/>
      <xsl:apply-templates select="* except $element-before"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mri:pointOfContact[count(*) = 0]"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="gn:*"
                priority="2"/>
</xsl:stylesheet>
