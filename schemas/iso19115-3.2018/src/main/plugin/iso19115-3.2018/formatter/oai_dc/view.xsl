<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                exclude-result-prefixes="#all">

  <xsl:output indent="yes"/>

  <xsl:template match="mdb:MD_Metadata">
    <oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
               xmlns:dc   ="http://purl.org/dc/elements/1.1/"
               xmlns:xsi  ="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">

      <xsl:for-each select="mdb:metadataIdentifier[position() = 1]/mcc:MD_Identifier/mcc:code">
        <dc:identifier><xsl:value-of select="gco:CharacterString"/></dc:identifier>
      </xsl:for-each>

      <dc:date><xsl:value-of select="/root/env/changeDate"/></dc:date>

      <!-- DataIdentification - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="mdb:identificationInfo/mri:MD_DataIdentification">

        <xsl:for-each select="mri:citation/cit:CI_Citation">
          <xsl:for-each select="cit:title/gco:CharacterString">
            <dc:title><xsl:value-of select="."/></dc:title>
          </xsl:for-each>

          <xsl:for-each select="cit:citedResponsibleParty/cit:CI_Responsibility[cit:role/cit:CI_RoleCode/@codeListValue='originator']/cit:party/cit:CI_Organisation/cit:name/gco:CharacterString">
            <dc:creator><xsl:value-of select="."/></dc:creator>
          </xsl:for-each>

          <xsl:for-each select="cit:citedResponsibleParty/cit:CI_Responsibility[cit:role/cit:CI_RoleCode/@codeListValue='publisher']/cit:party/cit:CI_Organisation/cit:name/gco:CharacterString">
            <dc:publisher><xsl:value-of select="."/></dc:publisher>
          </xsl:for-each>

          <xsl:for-each select="cit:citedResponsibleParty/cit:CI_Responsibility[cit:role/cit:CI_RoleCode/@codeListValue='author']/cit:party/cit:CI_Organisation/cit:name/gco:CharacterString">
            <dc:contributor><xsl:value-of select="."/></dc:contributor>
          </xsl:for-each>
        </xsl:for-each>

        <!-- subject -->

        <xsl:for-each select="mri:descriptiveKeywords/mri:MD_Keywords/mri:keyword/gco:CharacterString">
          <dc:subject><xsl:value-of select="."/></dc:subject>
        </xsl:for-each>

        <!-- description -->

        <xsl:for-each select="mri:abstract/gco:CharacterString">
          <dc:description><xsl:value-of select="."/></dc:description>
        </xsl:for-each>

        <!-- rights -->

        <xsl:for-each select="mri:resourceConstraints/mco:MD_LegalConstraints">
          <xsl:for-each select="*/mco:MD_RestrictionCode/@codeListValue">
            <dc:rights><xsl:value-of select="."/></dc:rights>
          </xsl:for-each>

          <xsl:for-each select="mco:otherConstraints/gco:CharacterString">
            <dc:rights><xsl:value-of select="."/></dc:rights>
          </xsl:for-each>
        </xsl:for-each>

        <!-- language -->

        <xsl:for-each select="mri:defaultLocale/lan:PT_Locale/lan:language/lan:languageCode">
          <dc:language><xsl:value-of select="."/></dc:language>
        </xsl:for-each>

        <!-- bounding box -->

        <xsl:for-each select="mri:extent/gex:EX_Extent/gex:geographicElement/gex:EX_GeographicBoundingBox">
          <dc:coverage>
            <xsl:value-of select="concat('North ', gex:northBoundLatitude/gco:Decimal, ', ')"/>
            <xsl:value-of select="concat('South ', gex:southBoundLatitude/gco:Decimal, ', ')"/>
            <xsl:value-of select="concat('East ' , gex:eastBoundLongitude/gco:Decimal, ', ')"/>
            <xsl:value-of select="concat('West ' , gex:westBoundLongitude/gco:Decimal, '.')"/>
          </dc:coverage>
        </xsl:for-each>
      </xsl:for-each>

      <!-- Type - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue">
        <dc:type><xsl:value-of select="."/></dc:type>
      </xsl:for-each>

      <!-- Distribution - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="mdb:distributionInfo/mrd:MD_Distribution">
        <xsl:for-each select="mrd:distributionFormat/mrd:MD_Format/mrd:name/gco:CharacterString">
          <dc:format><xsl:value-of select="."/></dc:format>
        </xsl:for-each>
      </xsl:for-each>
    </oai_dc:dc>
  </xsl:template>

  <xsl:template match="*">
    <xsl:apply-templates select="*"/>
  </xsl:template>
</xsl:stylesheet>
