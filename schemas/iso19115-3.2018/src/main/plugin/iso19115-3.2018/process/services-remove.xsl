<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to update metadata for a dataset and
detach the related service
-->
<xsl:stylesheet version="2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gn="http://www.fao.org/geonetwork">
  <xsl:param name="uuidref"/>

  <!-- Detach -->

  <xsl:template match="
            srv:operatesOn[@uuidref = $uuidref]|
            srv:coupledResource[srv:SV_CoupledResource/srv:resourceReference/@uuidref = $uuidref]|
            mrd:onLine[cit:CI_OnlineResource/cit:linkage/gco:CharacterString = $uuidref]|
            gn:*" priority="2"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
