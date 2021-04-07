<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to remove a reference to a source record.
-->
<xsl:stylesheet version="2.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gn="http://www.fao.org/geonetwork">

  <!-- Source metadata record UUID -->
  <xsl:param name="sourceUuid"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements and the target source. -->
  <xsl:template match="gn:*|mrl:source[@uuidref = $sourceUuid]" priority="2"/>
</xsl:stylesheet>
