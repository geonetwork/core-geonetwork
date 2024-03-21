<?xml version="1.0" encoding="UTF-8" ?>
<!--
  ~ Copyright (C) 2001-2020 Food and Agriculture Organization of the
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

<xsl:stylesheet version="2.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn-fn-index="http://geonetwork-opensource.org/xsl/functions/index"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#">


  <xsl:import href="common/index-utils.xsl"/>

  <!-- Subtemplate indexing

  Add the [count(ancestor::node()) =  1] to only match element at the root of the document.
  This is the method to identify a subtemplate.
  -->
  <xsl:param name="id"/>
  <xsl:param name="uuid"/>
  <xsl:param name="title"/>


  <xsl:variable name="isMultilingual" select="count(distinct-values(*//lan:LocalisedCharacterString/@locale)) > 0"/>

  <xsl:variable name="mainLanguage" as="xs:string?"
                select="util:getLanguage()"/>

  <xsl:variable name="otherLanguages"
                select="distinct-values(//lan:LocalisedCharacterString/@locale)"/>

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
      <root>
        <xsl:value-of select="name(*)"/>
      </root>
      <xsl:copy-of select="gn-fn-index:add-field('mainLanguage', $isoDocLangId)"/>
      <xsl:for-each select="$allLanguages/lang[@id != 'default']">
        <otherLanguage>
          <xsl:value-of select="@value"/>
        </otherLanguage>
      </xsl:for-each>

      <xsl:apply-templates mode="index" select="*"/>
    </doc>
  </xsl:template>


  <xsl:template mode="index"
                match="cit:CI_Responsibility[count(ancestor::node()) =  1]">

    <xsl:variable name="org"
                  select="normalize-space(cit:party/cit:CI_Organisation/cit:name/gco:CharacterString)"/>
    <xsl:variable name="name"
                  select="string-join(.//cit:individual/cit:CI_Individual/cit:name/gco:CharacterString, ', ')"/>
    <xsl:variable name="mail"
                  select="string-join(.//cit:CI_Address/cit:electronicMailAddress/gco:CharacterString, ', ')"/>

    <xsl:variable name="contactInfo"
                  select="if ($name != '') then $name
                          else if ($mail != '') then $mail else ''"/>
    <xsl:variable name="orgContactInfoSuffix"
                  select="if ($contactInfo != '')
                          then concat(' (', $contactInfo, ')') else ''"/>

    <resourceTitleObject type="object">{
      "default": "<xsl:value-of select="util:escapeForJson(
                                          concat($org, $orgContactInfoSuffix))"/>"
      <xsl:for-each
        select="cit:party/cit:CI_Organisation/cit:name/lan:PT_FreeText/*/lan:LocalisedCharacterString[. != '']">
        ,"lang<xsl:value-of select="$allLanguages/lang[
                                      @id = current()/@locale/substring(., 2, 2)
                                    ]/@value"/>": "<xsl:value-of select="util:escapeForJson(
                                               concat(., $orgContactInfoSuffix))"/>"
      </xsl:for-each>
      }
    </resourceTitleObject>

    <xsl:copy-of select="gn-fn-index:add-field('Org', $org)"/>

    <any type="object">{"common": "<xsl:value-of
      select="util:escapeForJson(normalize-space(.))"/>"}
    </any>

    <xsl:for-each
      select=".//cit:CI_Address/cit:electronicMailAddress/gco:CharacterString">
      <xsl:copy-of select="gn-fn-index:add-field('email', .)"/>
    </xsl:for-each>

    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>


  <xsl:template mode="index" match="cit:CI_Organisation">
    <xsl:variable name="org" select="normalize-space(cit:name/gco:CharacterString)"/>

    <resourceTitleObject type="object">{
      "default": "<xsl:value-of select="util:escapeForJson($org)"/>"
      <xsl:for-each select="cit:name/lan:PT_FreeText/*/lan:LocalisedCharacterString[. != '']">
        ,"lang<xsl:value-of select="$allLanguages/lang[
                                      @id = current()/@locale/substring(., 2, 2)
                                    ]/@value"/>": "<xsl:value-of select="util:escapeForJson(.)"/>"
      </xsl:for-each>
      }
    </resourceTitleObject>

    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>



  <xsl:template mode="index"
                match="mrs:MD_ReferenceSystem[count(ancestor::node()) =  1]"
                priority="2">
    <xsl:variable name="type" select="local-name(.)"/>

    <xsl:variable name="code"
                  select="mrs:referenceSystemIdentifier/*/mcc:code/*/text()"/>
    <xsl:variable name="description"
                  select="mrs:referenceSystemIdentifier/*/mcc:description/*/text()"/>
    <resourceTitleObject type="object">{
      "default": "<xsl:value-of select="util:escapeForJson(if ($description != '')
                                          then concat($description, ' (', $code, ')')
                                          else $code)"/>"
      }
    </resourceTitleObject>

    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>



  <!-- Indexing DQ report -->
  <xsl:template mode="index"
                match="mdq:*[mdq:result and count(ancestor::node()) =  1]"
                priority="2">
    <xsl:variable name="type" select="local-name(.)"/>
    <xsl:variable name="measures"
                  select="string-join(mdq:measure/*/mdq:nameOfMeasure/*/text(), ', ')"/>
    <xsl:variable name="specifications"
                  select="string-join(mdq:result/*/mdq:specification/*/cit:title/*/text(), ', ')"/>

    <resourceTitleObject type="object">{
      "default": "<xsl:value-of select="if ($specifications != '' )
                                        then util:escapeForJson($specifications)
                                        else if ($measures != '' )
                                        then util:escapeForJson($measures)
                                        else normalize-space(.)"/>"
      }
    </resourceTitleObject>

    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>


  <!-- Indexing constraints -->
  <xsl:template mode="index"
                match="mco:MD_Constraints[count(ancestor::node()) =  1]|
                       mco:MD_LegalConstraints[count(ancestor::node()) =  1]|
                       mco:MD_SecurityConstraints[count(ancestor::node()) =  1]">

    <xsl:variable name="type" select="local-name(.)"/>
    <xsl:variable name="references"
                  select="string-join(mco:reference/cit:CI_Citation/cit:title/*/text(), ', ')"/>
    <xsl:variable name="others"
                  select="string-join(mco:otherConstraints/*/text(), ', ')"/>
    <xsl:variable name="uses"
                  select="string-join(mco:useLimitations/*/text(), ', ')"/>

    <resourceTitleObject type="object">{
      "default": "<xsl:value-of select="util:escapeForJson(
                    if ($references != '')
                    then $references else if ($others != '')
                    then $others
                    else $uses)"/>"
      }
    </resourceTitleObject>
    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>


  <!-- Indexing constraints -->
  <xsl:template mode="index"
                match="gex:EX_Extent[count(ancestor::node()) =  1]">

    <xsl:variable name="desc"
                  select="gex:description"/>
    <xsl:variable name="name"
                  select="concat('S:', .//gex:southBoundLatitude/*/text(), ', W:', .//gex:westBoundLongitude/*/text(), ', N:', .//gex:northBoundLatitude/*/text(), ', E:',.//gex:eastBoundLongitude/*/text())"/>

    <resourceTitleObject type="object">{
      "default": "<xsl:value-of select="util:escapeForJson(
                    if ($desc != '')
                    then $desc
                    else $name)"/>"
      }
    </resourceTitleObject>
    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>


  <xsl:template name="subtemplate-common-fields">
    <any type="object">{"common": "<xsl:value-of
      select="util:escapeForJson(normalize-space(.))"/>"}
    </any>
  </xsl:template>

</xsl:stylesheet>
