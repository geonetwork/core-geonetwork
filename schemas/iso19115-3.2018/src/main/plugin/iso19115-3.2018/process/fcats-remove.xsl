<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to update metadata for a service and 
detach a dataset metadata
-->
<xsl:stylesheet version="2.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gn="http://www.fao.org/geonetwork">

  <xsl:param name="uuidref"/>

  <!-- Detach ref or links -->
  <xsl:template
      match="mrc:featureCatalogueCitation[@uuidref = $uuidref or
                                          */cit:onlineResource/*/cit:linkage/* = $uuidref]"
      priority="20"/>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="gn:*" priority="2"/>

</xsl:stylesheet>
