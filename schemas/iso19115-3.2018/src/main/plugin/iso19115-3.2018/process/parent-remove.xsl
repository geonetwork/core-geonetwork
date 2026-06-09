<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                exclude-result-prefixes="#all">
  <xsl:param name="parentUuid"/>

  <!-- Parent may be encoded using an associatedResource.
  Define which association type should be considered as parent. -->
  <xsl:variable name="parentAssociatedResourceType" select="'partOfSeamlessDatabase'"/>

  <!-- Remove geonet:* elements and parentMetadata. -->
  <xsl:template match="gn:*
                  |mdb:parentMetadata[@uuidref = $parentUuid]
                  |mri:associatedResource[
                    $parentAssociatedResourceType != ''
                    and */mri:associationType/*/@codeListValue = $parentAssociatedResourceType
                    and */mri:metadataReference/@uuidref = $parentUuid]" priority="2"/>

  <!-- Copy everything -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
