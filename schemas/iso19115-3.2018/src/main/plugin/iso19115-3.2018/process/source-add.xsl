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

  <!-- Catalogue base URL. Used to build the link to the source record
     when sourceUrl is empty. -->
  <xsl:param name="nodeUrl"/>

  <!-- Parameters describing a single source record.
       Only used when relatedRecords is empty. -->
  <!-- UUID of the source record to link.
       Set as the uuidref attribute. -->
  <xsl:param name="sourceUuid"/>
  <!-- Link to the source record. Optional: when empty an
       api/records/<uuid> URL is built from nodeUrl and sourceUuid. -->
  <xsl:param name="sourceUrl" select="''"/>
  <!-- Title of the source record. Optional: when empty no
       xlink:title attribute is added. -->
  <xsl:param name="sourceTitle" select="''"/>

  <!-- A list of source records as a comma separated list of
       uuid#title#url tokens. Used when linking more than one source.
       When set, it takes precedence over the parameters above. -->
  <xsl:param name="relatedRecords" select="''"/>

  <!-- Decode the separators escaped by the client in the relatedRecords
       parameter.
       '%25' is decoded last so that a value which already contained a
       percent escape (eg. a title with '%2C' in it) is restored as is. -->
  <xsl:function name="gn:unescape">
    <xsl:param name="value"/>
    <xsl:value-of select="replace(replace(replace($value,
                            '%23', '#'),
                            '%2C', ','),
                            '%25', '%')"/>
  </xsl:function>

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

  <!-- Add one source per record, either from the single record
       parameters or from the relatedRecords list. -->
  <xsl:template name="make-source-link">
    <xsl:choose>
      <xsl:when test="$relatedRecords != ''">
        <xsl:for-each select="tokenize($relatedRecords, ',')">
          <xsl:variable name="token" select="tokenize(., '#')"/>
          <xsl:call-template name="make-one-source-link">
            <xsl:with-param name="uuid" select="$token[1]"/>
            <xsl:with-param name="title" select="gn:unescape(($token[2], '')[1])"/>
            <xsl:with-param name="url" select="gn:unescape(($token[3], '')[1])"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="make-one-source-link">
          <xsl:with-param name="uuid" select="$sourceUuid"/>
          <xsl:with-param name="title" select="$sourceTitle"/>
          <xsl:with-param name="url" select="$sourceUrl"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="make-one-source-link">
    <xsl:param name="uuid"/>
    <xsl:param name="title" select="''"/>
    <xsl:param name="url" select="''"/>

    <mrl:source uuidref="{$uuid}">
      <xsl:if test="$title != ''">
        <xsl:attribute name="xlink:title" select="$title"/>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="$url != ''">
          <xsl:attribute name="xlink:href" select="$url"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="xlink:href"
                         select="concat($nodeUrl, 'api/records/', $uuid)"/>
        </xsl:otherwise>
      </xsl:choose>
    </mrl:source>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="gn:*" priority="2"/>
</xsl:stylesheet>
