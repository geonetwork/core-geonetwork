<?xml version="1.0" encoding="UTF-8"?>
<!-- FIXME: Use latest mcc:linkage to hold reference to browse graphic -->
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" 
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
  xmlns:gn="http://www.fao.org/geonetwork">

  <xsl:template match="/root">
    <xsl:apply-templates select="mdb:MD_Metadata|*[contains(@gco:isoType, 'MD_Metadata')]"/>
  </xsl:template>

  <xsl:template match="mri:MD_DataIdentification|
    *[contains(@gco:isoType, 'MD_DataIdentification')]|
    srv:SV_ServiceIdentification|
    *[contains(@gco:isoType, 'SV_ServiceIdentification')]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="mri:citation"/>
      <xsl:apply-templates select="mri:abstract"/>
      <xsl:apply-templates select="mri:purpose"/>
      <xsl:apply-templates select="mri:credit"/>
      <xsl:apply-templates select="mri:status"/>
      <xsl:apply-templates select="mri:pointOfContact"/>
      <xsl:apply-templates select="mri:spatialRepresentationType"/>
      <xsl:apply-templates select="mri:spatialResolution"/>
      <xsl:apply-templates select="mri:temporalResolution"/>
      <xsl:apply-templates select="mri:topicCategory"/>
      <xsl:apply-templates select="mri:extent"/>
      <xsl:apply-templates select="mri:additionalDocumentation"/>
      <xsl:apply-templates select="mri:processingLevel"/>
      <xsl:apply-templates select="mri:resourceMaintenance"/>
      <xsl:apply-templates select="mri:graphicOverview"/>

      <xsl:call-template name="fill"/>

      <xsl:apply-templates select="mri:resourceFormat"/>
      <xsl:apply-templates select="mri:descriptiveKeywords"/>
      <xsl:apply-templates select="mri:resourceSpecificUsage"/>
      <xsl:apply-templates select="mri:resourceConstraints"/>
      <xsl:apply-templates select="mri:associatedResource"/>
      <xsl:apply-templates select="mri:defaultLocale"/>
      <xsl:apply-templates select="mri:otherLocale"/>
      <xsl:apply-templates select="mri:environmentDescription"/>
      <xsl:apply-templates select="mri:supplementalInformation"/>

      <xsl:apply-templates select="srv:*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="fill">
    <mri:graphicOverview>
      <mcc:MD_BrowseGraphic>
        <mcc:fileName>
          <xsl:variable name="metadataId"
            select="/root/*/mdb:metadataIdentifier/mcc:MD_Identifier/mcc:code/gco:CharacterString/text()"/>
          <gco:CharacterString>
            <xsl:value-of select="concat(
                                    /root/env/url, '/resources.get?',
                                    'uuid=', $metadataId, 
                                    '&amp;fname=', /root/env/file)"/>
          </gco:CharacterString>
        </mcc:fileName>

        <mcc:fileDescription>
          <gco:CharacterString>
            <xsl:value-of select="/root/env/type"/>
          </gco:CharacterString>
        </mcc:fileDescription>
        <mcc:fileType>
          <gco:CharacterString>
            <xsl:value-of select="/root/env/ext"/>
          </gco:CharacterString>
        </mcc:fileType>
      </mcc:MD_BrowseGraphic>
    </mri:graphicOverview>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="gn:info" priority="2"/>
</xsl:stylesheet>
