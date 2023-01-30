<?xml version="1.0" encoding="UTF-8" ?>
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

<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn-fn-index="http://geonetwork-opensource.org/xsl/functions/index"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                version="2.0"
>

  <xsl:import href="common/index-utils.xsl"/>

  <xsl:param name="id"/>
  <xsl:param name="uuid"/>
  <xsl:param name="title"/>


  <xsl:variable name="isMultilingual" select="count(distinct-values(*//gmd:LocalisedCharacterString/@locale)) > 0"/>

  <xsl:variable name="mainLanguage" as="xs:string?"
                select="util:getLanguage()"/>

  <xsl:variable name="otherLanguages"
                select="distinct-values(//gmd:LocalisedCharacterString/@locale)"/>

  <xsl:variable name="allLanguages">
    <lang id="default" value="{$mainLanguage}"/>
    <xsl:for-each select="$otherLanguages">
      <lang id="{substring(., 2, 2)}" value="{util:threeCharLangCode(substring(., 2, 2))}"/>
    </xsl:for-each>
  </xsl:variable>

  <!-- Subtemplate indexing -->
  <xsl:template match="/">
    <xsl:variable name="isoDocLangId" select="util:getLanguage()"></xsl:variable>

    <doc>
      <root><xsl:value-of select="name(*)"/></root>
      <xsl:copy-of select="gn-fn-index:add-field('mainLanguage', $isoDocLangId)"/>
      <xsl:for-each select="$allLanguages/lang[@id != 'default']">
        <otherLanguage><xsl:value-of select="@value"/></otherLanguage>
      </xsl:for-each>

      <xsl:apply-templates mode="index" select="*"/>
    </doc>
  </xsl:template>

  <!-- Indexing Contacts & Organisations -->
  <xsl:template mode="index"
                match="gmd:CI_ResponsibleParty[count(ancestor::node()) =  1]|
                       *[@gco:isoType='gmd:CI_ResponsibleParty'][count(ancestor::node()) = 1]">
    <xsl:variable name="org"
                  select="normalize-space((
                          gmd:organisationName/gco:CharacterString[. != '']
                          |gmd:organisationName/gmd:PT_FreeText/*/gmd:LocalisedCharacterString[. != '']
                          )[1])"/>
    <xsl:variable name="name"
                  select="normalize-space(gmd:individualName/gco:CharacterString)"/>
    <xsl:variable name="mail"
                  select="normalize-space(gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress[1]/gco:CharacterString)"/>
    <xsl:variable name="contactInfo"
                  select="if ($name != '') then $name
                          else if ($mail != '') then $mail else ''"/>
    <xsl:variable name="orgContactInfoSuffix"
                  select="if ($contactInfo != '')
                          then concat(' (', $contactInfo, ')') else ''"/>

    <resourceTitleObject type="object">{
      "default": "<xsl:value-of select="gn-fn-index:json-escape(
                                          concat($org, $orgContactInfoSuffix))"/>"
      <xsl:for-each select="gmd:organisationName/gmd:PT_FreeText/*/gmd:LocalisedCharacterString[. != '']">
        ,"lang<xsl:value-of select="$allLanguages/lang[
                                      @id = current()/@locale/substring(., 2, 2)
                                    ]/@value"/>": "<xsl:value-of select="gn-fn-index:json-escape(
                                       concat(., $orgContactInfoSuffix))"/>"
      </xsl:for-each>
      }</resourceTitleObject>

    <xsl:copy-of select="gn-fn-index:add-field('Org', $org)"/>

    <xsl:for-each
      select="gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString">
      <xsl:copy-of select="gn-fn-index:add-field('email', .)"/>
    </xsl:for-each>

    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>


  <!-- Indexing extent descriptions  -->
  <xsl:template mode="index" match="gmd:EX_Extent[count(ancestor::node()) =  1]">
    <xsl:param name="locale"/>
    <xsl:choose>
      <xsl:when test="normalize-space(gmd:description) != ''">
        <xsl:variable name="description"
                      select="normalize-space((
                          gmd:description/gco:CharacterString[. != '']
                          |gmd:description/gmd:PT_FreeText/*/gmd:LocalisedCharacterString[. != '']
                          )[1])"/>
        <resourceTitleObject type="object">{
          "default": "<xsl:value-of select="gn-fn-index:json-escape($description)"/>"
          <xsl:for-each select="gmd:description/gmd:PT_FreeText/*/gmd:LocalisedCharacterString[. != '']">
            ,"lang<xsl:value-of select="$allLanguages/lang[
                                      @id = current()/@locale/substring(., 2, 2)
                                    ]/@value"/>": "<xsl:value-of select="gn-fn-index:json-escape(.)"/>"
          </xsl:for-each>
          }</resourceTitleObject>

      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="name"
                      select="concat('S:', .//gmd:southBoundLatitude/*/text(), ', W:', .//gmd:westBoundLongitude/*/text(), ', N:', .//gmd:northBoundLatitude/*/text(), ', E:',.//gmd:eastBoundLongitude/*/text())"/>

        <resourceTitleObject type="object">{
          "default": "<xsl:value-of select="gn-fn-index:json-escape($name)"/>"
          }
        </resourceTitleObject>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>


  <!-- Indexing distribution formats -->
  <xsl:template mode="index" match="gmd:MD_Format[count(ancestor::node()) =  1]">
    <xsl:variable name="title"
                  select="if (gmd:version/gco:CharacterString = '' or gmd:version/gco:CharacterString = '-')
                        then gmd:name/gco:CharacterString
                        else concat(gmd:name/gco:CharacterString, ' ', gmd:version/gco:CharacterString)"/>
    <resourceTitleObject type="object">{
      "default": "<xsl:value-of select="gn-fn-index:json-escape($title)"/>"
      }</resourceTitleObject>

    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>


  <!-- Indexing constraints -->
  <xsl:template mode="index" match="gmd:resourceConstraints[count(ancestor::node()) =  1]">
    <xsl:variable name="constraint" select="concat(
                        string-join(gmd:MD_LegalConstraints/*/gmd:MD_RestrictionCode/@codeListValue[. != 'otherConstraints'], ', '),
                        ' ',
                        string-join(gmd:MD_LegalConstraints/gmd:otherConstraints/*/text(), ', '))"/>

    <resourceTitleObject type="object">{
      "default": "<xsl:value-of select="gn-fn-index:json-escape($constraint)"/>"
      }
    </resourceTitleObject>

    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>


  <!-- Indexing DQ report -->
  <xsl:template mode="index" match="gmd:DQ_DomainConsistency[count(ancestor::node()) =  1]">
    <xsl:variable name="title"
                  select="gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/(gco:CharacterString|gmx:Anchor)"/>
    <resourceTitleObject type="object">{
      "default": "<xsl:value-of select="gn-fn-index:json-escape($title)"/>"
      }</resourceTitleObject>

    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>

  <xsl:template name="subtemplate-common-fields"/>
</xsl:stylesheet>
