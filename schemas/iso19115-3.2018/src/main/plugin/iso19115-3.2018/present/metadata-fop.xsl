<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:gml="http://www.opengis.net/gml/3.2" 
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
  xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
  xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:date="http://exslt.org/dates-and-times" 
  xmlns:exslt="http://exslt.org/common"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  exclude-result-prefixes="xs" version="2.0">

  <xsl:template name="metadata-fop-iso19115-3.2018-unused">
    <xsl:param name="schema"/>
    
    <!-- TODO improve block level element using mode -->
    <xsl:for-each select="*[namespace-uri(.)!=$geonetUri]">
      
      <xsl:call-template name="blockElementFop">
        <xsl:with-param name="block">
          <xsl:choose>
            <xsl:when test="count(*/*) > 1">
              <xsl:for-each select="*">
                <xsl:call-template name="blockElementFop">
                  <xsl:with-param name="label">
                    <xsl:call-template name="getTitle">
                      <xsl:with-param name="name"   select="name()"/>
                      <xsl:with-param name="schema" select="$schema"/>
                    </xsl:call-template>
                  </xsl:with-param>
                  <xsl:with-param name="block">
                    <xsl:apply-templates mode="elementFop" select=".">
                      <xsl:with-param name="schema" select="$schema"/>
                    </xsl:apply-templates>
                  </xsl:with-param>
                </xsl:call-template>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates mode="elementFop" select=".">
                <xsl:with-param name="schema" select="$schema"/>
              </xsl:apply-templates>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>


  <xsl:template name="metadata-fop-iso19115-3.2018">
    <xsl:param name="schema"/>

    <!-- Title -->
    <xsl:variable name="title">
      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/mri:citation/cit:CI_Citation/cit:title">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$title"/>
    </xsl:call-template>

    <!-- Date -->
    <xsl:variable name="date">
      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/mri:citation/cit:CI_Citation/cit:date/cit:CI_Date/cit:date |
                ./mds:identificationInfo/*/mri:citation/cit:CI_Citation/cit:date/cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$date"/>
    </xsl:call-template>

    <!-- Abstract -->
    <xsl:variable name="abstract">
      <xsl:apply-templates mode="elementFop" select="./mds:identificationInfo/*/mri:abstract">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$abstract"/>
    </xsl:call-template>

    <!-- Service Type -->
    <xsl:variable name="serviceType">
      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/srv:serviceType/gco:LocalName ">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$serviceType"/>
    </xsl:call-template>

    <!-- Service Type Version -->
    <xsl:variable name="srvVersion">
      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/srv:serviceTypeVersion">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$srvVersion"/>
    </xsl:call-template>

    <!-- Coupling Type -->
    <xsl:variable name="couplingType">
      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/srv:couplingType/srv:SV_CouplingType/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$couplingType"/>
    </xsl:call-template>

    <!-- Code -->
    <xsl:variable name="code">
      <xsl:apply-templates mode="elementFop"
        select="mds:identificationInfo/*/mri:citation/cit:CI_Citation/cit:identifier/mcc:MD_Identifier/mcc:code">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$code"/>
    </xsl:call-template>

    <!-- Language -->
    <xsl:variable name="lang">
      <xsl:apply-templates mode="elementFop" select="./mds:identificationInfo/*/mri:defaultLocale/lan:PT_Locale/lan:language/lan:languageCode/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$lang"/>
    </xsl:call-template>

    <!-- metadataScope Level -->
    <xsl:variable name="hierarchy">
      <xsl:apply-templates mode="elementFop"
        select="./mds:metadataScope/mcc:MD_ScopeCode/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$hierarchy"/>
    </xsl:call-template>

    <!-- Source Online -->
    <xsl:variable name="online">
      <xsl:apply-templates mode="elementFop"
        select="./mds:distributionInfo/mrd:MD_Distribution/mrd:transferOptions/mrd:MD_DigitalTransferOptions/mrd:onLine/cit:CI_OnlineResource/cit:linkage |
                                  ./mds:distributionInfo/mrd:MD_Distribution/mrd:transferOptions/mrd:MD_DigitalTransferOptions/mrd:onLine/cit:CI_OnlineResource/cit:protocol">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$online"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='mrd:onLine']/label"/>
      </xsl:with-param>
    </xsl:call-template>

    <!-- Contact -->
    <xsl:variable name="poc">
      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/mri:pointOfContact/cit:CI_Responsibility/cit:party/*/cit:name    |
                ./mds:identificationInfo/*/mri:pointOfContact/cit:CI_Responsibility/cit:role/cit:CI_RoleCode/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$poc"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='mri:pointOfContact']/label"
        />
      </xsl:with-param>
    </xsl:call-template>

    <!-- Topic category -->
    <xsl:variable name="topicCat">
      <xsl:apply-templates mode="elementFop" select="./mds:identificationInfo/*/mri:topicCategory">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$topicCat"/>
    </xsl:call-template>

    <!-- Keywords -->
    <xsl:variable name="keyword">
      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/mri:descriptiveKeywords/mri:MD_Keywords/mri:keyword | 
              ./mds:identificationInfo/*/mri:descriptiveKeywords/mri:MD_Keywords/mri:type/mri:MD_KeywordTypeCode/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$keyword"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='mri:keyword']/label"/>
      </xsl:with-param>
    </xsl:call-template>

    <!-- Geographical extent -->
    <xsl:variable name="geoDesc">
      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/mri:extent/gex:EX_Extent/gex:description |
                ./mds:identificationInfo/*/srv:extent/gex:EX_Extent/gex:description">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:variable name="geoBbox">
      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/mri:extent/gex:EX_Extent/gex:geographicElement/gex:EX_GeographicBoundingBox |
              ./mds:identificationInfo/*/srv:extent/gex:EX_Extent/gex:geographicElement/gex:EX_GeographicBoundingBox">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:variable name="timeExtent">
      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/mri:extent/gex:EX_Extent/gex:temporalElement/gex:EX_TemporalExtent/gex:extent/gml:TimeInstant/gml:timePosition">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:variable name="geoExtent">
      <xsl:call-template name="blockElementFop">
        <xsl:with-param name="block" select="$geoDesc"/>
      </xsl:call-template>
      <xsl:call-template name="blockElementFop">
        <xsl:with-param name="block" select="$geoBbox"/>
        <xsl:with-param name="label">
          <xsl:value-of
            select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='gex:EX_GeographicBoundingBox']/label"
          />
        </xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="blockElementFop">
        <xsl:with-param name="block" select="$timeExtent"/>
        <xsl:with-param name="label">
          <xsl:value-of
            select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='gex:temporalElement']/label"
          />
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$geoExtent"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='gex:EX_Extent']/label"/>
      </xsl:with-param>
    </xsl:call-template>

    <!-- Spatial resolution -->
    <xsl:variable name="spatialResolution">
      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/mri:spatialResolution">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$spatialResolution"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='mri:spatialResolution']/label"
        />
      </xsl:with-param>
    </xsl:call-template>

    <!-- Temporal resolution -->
    <xsl:variable name="temporalResolution">
      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/mri:temporalResolution">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$temporalResolution"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='mri:temporalResolution']/label"
        />
      </xsl:with-param>
    </xsl:call-template>

    <!-- Lineage -->
    <xsl:if test="./mds:identificationInfo/*[name(.)!='srv:SV_ServiceIdentification']">
      <xsl:variable name="qual">
        <xsl:apply-templates mode="elementFop"
          select="./mds:dataQualityInfo/dqm:DQ_DataQuality/dqm:lineage/dqm:LI_Lineage/dqm:statement">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="elementFop"
          select="./mds:dataQualityInfo/dqm:DQ_DataQuality/dqm:lineage/dqm:LI_Lineage/dqm:source">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
      </xsl:variable>
      <xsl:call-template name="blockElementFop">
        <xsl:with-param name="block" select="$qual"/>
        <xsl:with-param name="label">
          <xsl:value-of
            select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='dqm:lineage']/label"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>

    <!-- Constraints -->
    <xsl:variable name="constraints">
      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/mri:resourceConstraints/*/mco:useLimitation/gco:CharacterString">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>

      <xsl:apply-templates mode="elementFop"
        select="./mds:identificationInfo/*/mri:resourceConstraints/*/mco:classification">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$constraints"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='mri:resourceConstraints']/label"
        />
      </xsl:with-param>
    </xsl:call-template>

    <!-- Identifier -->
    <xsl:variable name="identifier">
      <xsl:apply-templates mode="elementFop" select="./mds:metadataIdentifier[position() = 1]/mcc:MD_Identifier/mcc:code/*">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$identifier"/>
    </xsl:call-template>

    <!-- Language -->
    <xsl:variable name="language">
      <xsl:apply-templates mode="elementFop" select="./mds:defaultLocale/lan:PT_Locale/lan:language/lan:languageCode/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$language"/>
    </xsl:call-template>

    <!-- Contact -->
    <xsl:variable name="contact">
      <xsl:apply-templates mode="elementFop"
        select="./mds:contact/cit:CI_Responsibility/cit:party/cit:CI_Organisation/cit:name">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="elementFop"
        select="./mds:contact/cit:CI_Responsibility/cit:party/cit:CI_Individual/cit:name">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="elementFop"
        select="./mds:contact/cit:CI_Responsibility/cit:role/cit:CI_RoleCode/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$contact"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='cit:contact' and not(@context)]/label"/>
      </xsl:with-param>
    </xsl:call-template>

    <!-- Modification date -->
    <xsl:variable name="dateInfo">
      <xsl:apply-templates mode="elementFop" select="./mds:dateInfo/cit:date/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode/@codeListValue='revision']/cit:date/*">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$dateInfo"/>
    </xsl:call-template>

  </xsl:template>

</xsl:stylesheet>
