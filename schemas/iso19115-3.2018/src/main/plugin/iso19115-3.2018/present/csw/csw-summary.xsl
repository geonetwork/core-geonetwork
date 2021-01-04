<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
  xmlns:dc ="http://purl.org/dc/elements/1.1/"
  xmlns:dct="http://purl.org/dc/terms/"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
  xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
  xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:geonet="http://www.fao.org/geonetwork"
  exclude-result-prefixes="#all">
  
  <xsl:param name="displayInfo"/>
  <xsl:param name="lang"/>
  
  <xsl:include href="../metadata-utils.xsl"/>
  
  <xsl:template match="mdb:MD_Metadata|*[contains(@gco:isoType,'MD_Metadata')]">
    
    <xsl:variable name="info" select="geonet:info"/>
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId19115-3.2018">
        <xsl:with-param name="langGui" select="$lang"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>
    
    <csw:SummaryRecord>
      
      <xsl:for-each select="mdb:metadataIdentifier">
        <dc:identifier><xsl:value-of select="mcc:MD_Identifier/mcc:code/gco:CharacterString"/></dc:identifier>
      </xsl:for-each>
      
      <!-- Identification -->
      <xsl:for-each select="mdb:identificationInfo/mri:MD_DataIdentification|
        mdb:identificationInfo/*[contains(@gco:isoType, 'MD_DataIdentification')]|
        mdb:identificationInfo/srv:SV_ServiceIdentification|
        mdb:identificationInfo/*[contains(@gco:isoType, 'SV_ServiceIdentification')]">
        
        <xsl:for-each select="mri:citation/cit:CI_Citation/cit:title">
          <dc:title>
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </dc:title>
        </xsl:for-each>
        
        <!-- Type -->
        <xsl:for-each select="../../mdb:metadataScope/mdb:MD_MetadataScope/
          mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue">
          <dc:type><xsl:value-of select="."/></dc:type>
        </xsl:for-each>
        
        
        <xsl:for-each select="mri:descriptiveKeywords/mri:MD_Keywords/mri:keyword[not(@gco:nilReason)]">
          <dc:subject>
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </dc:subject>
        </xsl:for-each>
        <xsl:for-each select="mri:topicCategory/mri:MD_TopicCategoryCode">
          <dc:subject><xsl:value-of select="."/></dc:subject><!-- TODO : translate ? -->
        </xsl:for-each>
        
        
        <!-- Distribution -->
        <xsl:for-each select="../../mdb:distributionInfo/mrd:MD_Distribution">
          <xsl:for-each select="mrd:distributionFormat/
            mrd:MD_Format/mrd:formatSpecificationCitation/
            cit:CI_Citation/cit:title/gco:CharacterString">
            <dc:format>
              <xsl:apply-templates mode="localised" select=".">
                <xsl:with-param name="langId" select="$langId"/>
              </xsl:apply-templates>
            </dc:format>
          </xsl:for-each>
        </xsl:for-each>
        
        
        <!-- Parent Identifier -->
        <xsl:for-each select="../../mdb:parentMetadata">
          <dc:relation><xsl:value-of select="cit:CI_Citation/
            cit:identifier/mcc:MD_Identifier/mcc:code/gco:CharacterString|@uuidref"/></dc:relation>
        </xsl:for-each>
        
        
        <!-- Resource modification date (metadata modification date is in 
          mdb:MD_Metadata/mdb:dateInfo  -->
        <xsl:for-each select="mri:citation/cit:CI_Citation/
          cit:date/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode/@codeListValue='revision']/
          cit:date/*">
          <dct:modified><xsl:value-of select="."/></dct:modified>
        </xsl:for-each>
        
        
        <xsl:for-each select="mri:abstract">
          <dct:abstract>
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </dct:abstract>
        </xsl:for-each>
        
      </xsl:for-each>
      
      <!-- Lineage 
        
        <xsl:for-each select="../../mdb:dataQualityInfo/dqm:DQ_DataQuality/dqm:lineage/dqm:LI_Lineage/dqm:statement/gco:CharacterString">
        <dc:source><xsl:value-of select="."/></dc:source>
        </xsl:for-each>-->
      
      
      <!-- GeoNetwork elements added when resultType is equal to results_with_summary -->
      <xsl:if test="$displayInfo = 'true'">
        <xsl:copy-of select="$info"/>
      </xsl:if>
      
    </csw:SummaryRecord>
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:apply-templates select="*"/>
  </xsl:template>
</xsl:stylesheet>
