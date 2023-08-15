<?xml version="1.0" encoding="UTF-8"?>
<!--
Stylesheet used to update metadata adding a reference to a source record.
-->
<xsl:stylesheet version="2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="#all">

  <!-- Source metadata record UUID -->
  <xsl:param name="sourceUuid"/>
  <xsl:param name="nodeUrl"/>
  <xsl:param name="sourceUrl" select="''"/>
  <xsl:param name="sourceTitle" select="''"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/mdb:MD_Metadata|*[@gco:isoType='mdb:MD_Metadata']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:apply-templates select="mdb:metadataIdentifier"/>
      <xsl:apply-templates select="mdb:defaultLocale"/>
      <xsl:apply-templates select="mdb:parentMetadata"/>
      <xsl:apply-templates select="mdb:metadataScope"/>
      <xsl:apply-templates select="mdb:contact"/>
      <xsl:apply-templates select="mdb:dateInfo"/>
      <xsl:apply-templates select="mdb:metadataStandard"/>
      <xsl:apply-templates select="mdb:metadataProfile"/>
      <xsl:apply-templates select="mdb:alternativeMetadataReference"/>
      <xsl:apply-templates select="mdb:otherLocale"/>
      <xsl:apply-templates select="mdb:metadataLinkage"/>
      <xsl:apply-templates select="mdb:spatialRepresentationInfo"/>
      <xsl:apply-templates select="mdb:referenceSystemInfo"/>
      <xsl:apply-templates select="mdb:metadataExtensionInfo"/>
      <xsl:apply-templates select="mdb:identificationInfo"/>
      <xsl:apply-templates select="mdb:contentInfo"/>
      <xsl:apply-templates select="mdb:distributionInfo"/>
      <xsl:apply-templates select="mdb:dataQualityInfo"/>

      <xsl:choose>
        <!-- Add to existing resourceLineage section or create a new one -->
        <xsl:when
            test="mdb:resourceLineage">
          <xsl:for-each select="mdb:resourceLineage/mrl:LI_Lineage">
            <mdb:resourceLineage>
              <mrl:LI_Lineage>
                <xsl:apply-templates select="mrl:statement"/>
                <xsl:apply-templates select="mrl:scope"/>
                <xsl:apply-templates select="mrl:additionalDocumentation"/>
                <xsl:apply-templates select="mrl:source"/>
                <xsl:if test="position() = 1">
                  <xsl:call-template name="make-source-link"/>
                </xsl:if>
                <xsl:apply-templates select="mrl:processStep"/>
              </mrl:LI_Lineage>
            </mdb:resourceLineage>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <mdb:resourceLineage>
            <mrl:LI_Lineage>
              <xsl:call-template name="make-source-link"/>
            </mrl:LI_Lineage>
          </mdb:resourceLineage>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates select="mdb:portrayalCatalogueInfo"/>
      <xsl:apply-templates select="mdb:metadataConstraints"/>
      <xsl:apply-templates select="mdb:applicationSchemaInfo"/>
      <xsl:apply-templates select="mdb:metadataMaintenance"/>
      <xsl:apply-templates select="mdb:acquisitionInformation"/>
    </xsl:copy>

  </xsl:template>

  <xsl:template name="make-source-link">
    <mrl:source uuidref="{$sourceUuid}">
      <xsl:if test="$sourceTitle != ''">
        <xsl:attribute name="xlink:title" select="$sourceTitle"/>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="$sourceUrl != ''">
          <xsl:attribute name="xlink:href" select="$sourceUrl"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="xlink:href"
                         select="concat($nodeUrl, 'api/records/', $sourceUuid)"/>
        </xsl:otherwise>
      </xsl:choose>
    </mrl:source>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="gn:*" priority="2"/>
</xsl:stylesheet>
