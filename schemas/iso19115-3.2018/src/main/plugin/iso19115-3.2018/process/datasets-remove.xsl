<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to update metadata for a service and
detached it to the metadata for data.
-->
<xsl:stylesheet version="2.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gn="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all">

  <xsl:param name="uuidref"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template
          match="gn:*|
          srv:coupledResource[normalize-space(srv:SV_CoupledResource/srv:resourceReference/@uuidref) = $uuidref]|
          srv:operatesOn[@uuidref = $uuidref]"
          priority="2"/>

</xsl:stylesheet>
