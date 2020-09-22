<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs"
                version="2.0">

  <xsl:template name="metadata-fop-iso19139-unused">
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
                      <xsl:with-param name="name" select="name()"/>
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


  <xsl:template name="metadata-fop-iso19139">
    <xsl:param name="schema"/>

    <!-- Title -->
    <xsl:variable name="title">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$title"/>
    </xsl:call-template>

    <!-- Date -->
    <xsl:variable name="date">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date |
                ./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$date"/>
    </xsl:call-template>

    <!-- Abstract -->
    <xsl:variable name="abstract">
      <xsl:apply-templates mode="elementFop" select="./gmd:identificationInfo/*/gmd:abstract">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$abstract"/>
    </xsl:call-template>

    <!-- Service Type -->
    <xsl:variable name="serviceType">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:identificationInfo/*/srv:serviceType/gco:LocalName ">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$serviceType"/>
    </xsl:call-template>

    <!-- Service Type Version -->
    <xsl:variable name="srvVersion">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:identificationInfo/*/srv:serviceTypeVersion">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$srvVersion"/>
    </xsl:call-template>

    <!-- Coupling Type -->
    <xsl:variable name="couplingType">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:identificationInfo/*/srv:couplingType/srv:SV_CouplingType/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$couplingType"/>
    </xsl:call-template>

    <!-- Code -->
    <xsl:variable name="code">
      <xsl:apply-templates mode="elementFop"
                           select="gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$code"/>
    </xsl:call-template>

    <!-- Language -->
    <xsl:variable name="lang">
      <xsl:apply-templates mode="elementFop" select="./gmd:identificationInfo/*/gmd:language">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$lang"/>
    </xsl:call-template>

    <!-- Charset Encoding -->
    <xsl:variable name="characterSet">
      <xsl:apply-templates mode="elementFop" select="./gmd:identificationInfo/*/gmd:characterSet">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$characterSet"/>
    </xsl:call-template>

    <!-- Hierarchy Level -->
    <xsl:variable name="hierarchy">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$hierarchy"/>
    </xsl:call-template>

    <!-- Source Online -->
    <xsl:variable name="online">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage |
                                  ./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$online"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:onLine']/label"/>
      </xsl:with-param>
    </xsl:call-template>

    <!-- Contact -->
    <xsl:variable name="poc">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualName    |
                                  ./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName  |
                                  ./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:positionName      |
                                  ./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$poc"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:pointOfContact']/label"
        />
      </xsl:with-param>
    </xsl:call-template>

    <!-- Topic category -->
    <xsl:variable name="topicCat">
      <xsl:apply-templates mode="elementFop" select="./gmd:identificationInfo/*/gmd:topicCategory">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$topicCat"/>
    </xsl:call-template>

    <!-- Keywords -->
    <xsl:variable name="keyword">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword |
              ./gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$keyword"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:keyword']/label"/>
      </xsl:with-param>
    </xsl:call-template>

    <!-- Geographical extent -->
    <xsl:variable name="geoDesc">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:description |
                ./gmd:identificationInfo/*/srv:extent/gmd:EX_Extent/gmd:description">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:variable name="geoBbox">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox |
              ./gmd:identificationInfo/*/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:variable name="timeExtent">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimeInstant/gml:timePosition">
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
            select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:EX_GeographicBoundingBox']/label"
          />
        </xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="blockElementFop">
        <xsl:with-param name="block" select="$timeExtent"/>
        <xsl:with-param name="label">
          <xsl:value-of
            select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:temporalElement']/label"
          />
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$geoExtent"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:EX_Extent']/label"/>
      </xsl:with-param>
    </xsl:call-template>

    <!-- Spatial resolution -->
    <xsl:variable name="spatialResolution">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:identificationInfo/*/gmd:spatialResolution">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$spatialResolution"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:spatialResolution']/label"
        />
      </xsl:with-param>
    </xsl:call-template>

    <!-- "Généalogie" -->
    <xsl:if test="./gmd:identificationInfo/*[name(.)!='srv:SV_ServiceIdentification']">
      <xsl:variable name="qual">
        <xsl:apply-templates mode="elementFop"
                             select="./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="elementFop"
                             select="./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
      </xsl:variable>
      <xsl:call-template name="blockElementFop">
        <xsl:with-param name="block" select="$qual"/>
        <xsl:with-param name="label">
          <xsl:value-of
            select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:lineage']/label"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>

    <!-- Constraints -->
    <xsl:variable name="constraints">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:useLimitation/gco:CharacterString">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>

      <xsl:apply-templates mode="elementFop"
                           select="./gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:classification">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$constraints"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:resourceConstraints']/label"
        />
      </xsl:with-param>
    </xsl:call-template>

    <!-- Identifier -->
    <xsl:variable name="identifier">
      <xsl:apply-templates mode="elementFop" select="./gmd:fileIdentifier">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$identifier"/>
    </xsl:call-template>

    <!-- Language -->
    <xsl:variable name="language">
      <xsl:apply-templates mode="elementFop" select="./gmd:language">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$language"/>
    </xsl:call-template>

    <!-- Encoding -->
    <xsl:variable name="charset">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$charset"/>
    </xsl:call-template>

    <!-- Contact -->
    <xsl:variable name="contact">
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:contact/gmd:CI_ResponsibleParty/gmd:individualName">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="elementFop"
                           select="./gmd:contact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$contact"/>
      <xsl:with-param name="label">
        <xsl:value-of
          select="/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:contact' and not(@context)]/label"/>
      </xsl:with-param>
    </xsl:call-template>

    <!-- Date stamp -->
    <xsl:variable name="dateStamp">
      <xsl:apply-templates mode="elementFop" select="./gmd:dateStamp">
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:call-template name="blockElementFop">
      <xsl:with-param name="block" select="$dateStamp"/>
    </xsl:call-template>

    <!-- Conformance -->
    <xsl:if
      test="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult[contains(gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString, 'INSPIRE')]">
      <xsl:variable name="conf">
        <xsl:apply-templates mode="elementFop"
                             select="./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="elementFop"
                             select="./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="elementFop"
                             select="./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="elementFop"
                             select="./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:explanation">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="elementFop"
                             select="./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:pass">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
      </xsl:variable>
      <xsl:call-template name="blockElementFop">
        <xsl:with-param name="block" select="$conf"/>
        <xsl:with-param name="label">INSPIRE</xsl:with-param>
      </xsl:call-template>
    </xsl:if>

  </xsl:template>

</xsl:stylesheet>
