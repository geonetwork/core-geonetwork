<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                  xmlns:gco="http://www.isotc211.org/2005/gco"
                  xmlns:gmd="http://www.isotc211.org/2005/gmd"
                  xmlns:geonet="http://www.fao.org/geonetwork"
                  exclude-result-prefixes="#all">

  <!-- Remove a DOI in the metadata record. -->
  <xsl:output method="xml" indent="yes"/>

  <xsl:param name="doi"
             select="''"/>

  <!-- Remove online resources matching DOI protocols. -->
  <xsl:template match="gmd:onLine[
                              */gmd:linkage/gmd:URL = $doi]"
                priority="2"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>
</xsl:stylesheet>
