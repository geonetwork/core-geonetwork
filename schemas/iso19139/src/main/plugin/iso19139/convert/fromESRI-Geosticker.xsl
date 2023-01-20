<?xml version="1.0" encoding="UTF-8"?>
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

Transformation to produce ISO19139 documents from input documents that
combine "extra" elements (Esri-specific or from ISO19115 DTD) with an included
MD_Metadata element.

The current version ignores all elements except for MD_Metadata, which is copied to the output verbatim.

author Heikki Doeleman
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:fn="http://www.w3.org/2005/xpath-functions"
xmlns:gmd="http://www.isotc211.org/2005/gmd">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <xsl:strip-space elements="*" />

  <xsl:param name="publishLocalResources">False</xsl:param>

  <xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

  <!-- the input document is supposed to have either <metadata> or <Metadata> as root element -->
  <xsl:template match="/metadata|/Metadata">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- the wrapped <MD_Metadata> is copied in its entirety to the output -->
  <!--<xsl:template match="gmd:MD_Metadata|MD_Metadata">
    <xsl:copy-of select="."/>
  </xsl:template>-->

  <xsl:template match="gmd:MD_Metadata|MD_Metadata">
    <xsl:copy>
      <xsl:apply-templates mode="iso" select="@*"/>
      <xsl:apply-templates mode="iso" select="gmd:fileIdentifier|fileIdentifier"/>
      <xsl:apply-templates mode="iso" select="gmd:language|language"/>
      <xsl:apply-templates mode="iso" select="gmd:characterSet|characterSet"/>
      <xsl:apply-templates mode="iso" select="gmd:parentIdentifier|parentIdentifier"/>
      <xsl:apply-templates mode="iso" select="gmd:hierarchyLevel|hierarchyLevel"/>
      <xsl:apply-templates mode="iso" select="gmd:hierarchyLevelName|hierarchyLevelName"/>
      <xsl:apply-templates mode="iso" select="gmd:contact|contact"/>
      <xsl:apply-templates mode="iso" select="gmd:dateStamp|dateStamp"/>
      <xsl:apply-templates mode="iso" select="gmd:metadataStandardName|metadataStandardName"/>
      <xsl:apply-templates mode="iso" select="gmd:metadataStandardVersion|metadataStandardVersion"/>
      <xsl:apply-templates mode="iso" select="gmd:dataSetURI|dataSetURI"/>
      <xsl:apply-templates mode="iso" select="gmd:locale|locale"/>
      <xsl:apply-templates mode="iso" select="gmd:spatialRepresentationInfo|spatialRepresentationInfo"/>
      <xsl:apply-templates mode="iso" select="gmd:referenceSystemInfo|referenceSystemInfo"/>
      <xsl:apply-templates mode="iso" select="gmd:metadataExtensionInfo|metadataExtensionInfo"/>
      <xsl:apply-templates mode="iso" select="gmd:identificationInfo|identificationInfo"/>
      <xsl:apply-templates mode="iso" select="gmd:contentInfo|contentInfo"/>
      <xsl:apply-templates mode="iso" select="gmd:distributionInfo|distributionInfo"/>
      <xsl:apply-templates mode="iso" select="gmd:dataQualityInfo"/>
      <xsl:apply-templates mode="iso" select="gmd:portrayalCatalogueInfo|dataQualityInfo"/>
      <xsl:apply-templates mode="iso" select="gmd:metadataConstraints|metadataConstraints"/>
      <xsl:apply-templates mode="iso" select="gmd:applicationSchemaInfo|applicationSchemaInfo"/>
      <xsl:apply-templates mode="iso" select="gmd:metadataMaintenance|metadataMaintenance"/>
      <xsl:apply-templates mode="iso" select="gmd:series|series"/>
      <xsl:apply-templates mode="iso" select="gmd:describes|describes"/>
      <xsl:apply-templates mode="iso" select="gmd:propertyType|propertyType"/>
      <xsl:apply-templates mode="iso" select="gmd:featureType|featureType"/>
      <xsl:apply-templates mode="iso" select="gmd:featureAttribute|featureAttribute"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="iso" match="gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine|MD_Distribution/transferOptions/MD_DigitalTransferOptions/onLine" priority="1">
    <xsl:variable name="linkage_lower" select="translate(gmd:CI_OnlineResource/gmd:linkage/gmd:URL, $uppercase, $smallcase)" />

    <xsl:choose>
      <xsl:when test="$publishLocalResources = 'True'">

        <xsl:copy>
          <xsl:apply-templates mode="iso" select="@*|node()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="not(starts-with($linkage_lower, 'server=')) and not(starts-with($linkage_lower, 'file://')) and not(starts-with($linkage_lower, 'http://localhost')) and not(starts-with($linkage_lower, 'https://localhost')) and not(starts-with($linkage_lower, 'http://127.0.0.1')) and not(starts-with($linkage_lower, 'https://127.0.0.1'))">
            <xsl:copy>
              <xsl:apply-templates mode="iso" select="@*|node()"/>
          </xsl:copy>
          </xsl:when>

          <xsl:otherwise>

            <xsl:copy>
              <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="iso" match="MD_Distribution/transferOptions/MD_DigitalTransferOptions/onLine" priority="1">
    <xsl:variable name="linkage_lower" select="translate(CI_OnlineResource/linkage/URL, $uppercase, $smallcase)" />

    <xsl:choose>
      <xsl:when test="$publishLocalResources = 'True'">

        <xsl:copy>
          <xsl:apply-templates mode="iso" select="@*|node()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="not(starts-with($linkage_lower, 'server=')) and not(starts-with($linkage_lower, 'file://')) and not(starts-with($linkage_lower, 'http://localhost')) and not(starts-with($linkage_lower, 'https://localhost')) and not(starts-with($linkage_lower, 'http://127.0.0.1')) and not(starts-with($linkage_lower, 'https://127.0.0.1'))">
            <xsl:copy>
              <xsl:apply-templates mode="iso" select="@*|node()"/>
          </xsl:copy>
          </xsl:when>

          <xsl:otherwise>

            <xsl:copy>
              <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>

          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*|node()" mode="iso">

    <xsl:copy>
      <xsl:apply-templates mode="iso"  select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- all other elements are ignored -->
  <xsl:template match="*"/>

</xsl:stylesheet>