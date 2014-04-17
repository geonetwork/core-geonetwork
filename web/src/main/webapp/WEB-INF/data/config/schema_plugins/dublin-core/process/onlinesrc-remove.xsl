<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to remove a reference to a parent record.
-->
<xsl:stylesheet version="2.0"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork">

  <xsl:param name="url"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*|dct:references[text() = $url]"
                priority="2"/>
</xsl:stylesheet>
