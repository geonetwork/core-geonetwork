<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink">

  <!-- Catalogue base URL. Used to build the link to the feature catalogue
     when fcatsUrl is empty. -->
  <xsl:param name="nodeUrl"/>

  <!-- Parameters describing a single feature catalogue.
       Only used when relatedRecords is empty. -->
  <!-- UUID of the feature catalogue to link.
       Set as the uuidref attribute. -->
  <xsl:param name="uuidref"/>
  <!-- Link to the feature catalogue. Optional: when empty an
       api/records/<uuid> URL is built from nodeUrl and uuidref. -->
  <xsl:param name="fcatsUrl" select="''"/>
  <!-- Title of the feature catalogue. Optional: when empty no
       xlink:title attribute is added. -->
  <xsl:param name="fcatsTitle" select="''"/>

  <!-- A list of feature catalogues as a comma separated list of
       uuid#title#url tokens. Used when linking more than one catalogue.
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
      <mdb:contentInfo>
        <mrc:MD_FeatureCatalogueDescription>
          <mrc:includedWithDataset/>
          <xsl:call-template name="make-fcats-links"/>
        </mrc:MD_FeatureCatalogueDescription>
      </mdb:contentInfo>

      <xsl:apply-templates select="mdb:distributionInfo"/>
      <xsl:apply-templates select="mdb:dataQualityInfo"/>
      <xsl:apply-templates select="mdb:resourceLineage"/>
      <xsl:apply-templates select="mdb:portrayalCatalogueInfo"/>
      <xsl:apply-templates select="mdb:metadataConstraints"/>
      <xsl:apply-templates select="mdb:applicationSchemaInfo"/>
      <xsl:apply-templates select="mdb:metadataMaintenance"/>
      <xsl:apply-templates select="mdb:acquisitionInformation"/>
    </xsl:copy>

  </xsl:template>

  <!-- Add one citation per record, either from the single record
       parameters or from the relatedRecords list. -->
  <xsl:template name="make-fcats-links">
    <xsl:choose>
      <xsl:when test="$relatedRecords != ''">
        <xsl:for-each select="tokenize($relatedRecords, ',')">
          <xsl:variable name="token" select="tokenize(., '#')"/>
          <xsl:call-template name="make-fcats-link">
            <xsl:with-param name="uuid" select="$token[1]"/>
            <xsl:with-param name="title" select="gn:unescape(($token[2], '')[1])"/>
            <xsl:with-param name="url" select="gn:unescape(($token[3], '')[1])"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="make-fcats-link"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="make-fcats-link">
    <xsl:param name="uuid" select="$uuidref"/>
    <xsl:param name="title" select="$fcatsTitle"/>
    <xsl:param name="url" select="$fcatsUrl"/>

    <mrc:featureCatalogueCitation uuidref="{$uuid}">
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
    </mrc:featureCatalogueCitation>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="gn:*" priority="2"/>

  <!-- Copy everything. -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
