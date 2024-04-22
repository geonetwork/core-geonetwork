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
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:locn="http://www.w3.org/ns/locn#"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-dcat2="http://geonetwork-opensource.org/xsl/functions/profiles/dcat2"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                extension-element-prefixes="saxon"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:variable name="dcatKeywordConfig">
    <xsl:for-each select="$editorConfig/editor/fields/for[@use='thesaurus-list-picker']">
      <element>
        <xsl:attribute name="name" select="./@name"/>
        <xsl:if test="./@xpath">
          <xsl:attribute name="parent" select="./@xpath"/>
        </xsl:if>
        <thesaurus>
          <xsl:value-of select="./directiveAttributes/@thesaurus"/>
        </thesaurus>
        <xpath>
          <xsl:value-of select="./directiveAttributes/@xpath"/>
        </xpath>
        <max>
          <xsl:value-of select="./directiveAttributes/@max"/>
        </max>
        <labelKey>
          <xsl:value-of select="./directiveAttributes/@labelKey"/>
        </labelKey>
        <useReference>
          <xsl:value-of select="./directiveAttributes/@useReference"/>
        </useReference>
      </element>
    </xsl:for-each>
  </xsl:variable>

  <!-- Theme can only be set by thesaurus eu.europa.data-theme.

  Catch all elements first.
  On gn:child, build the keyword picker directive using XPath mode.
  TODO: How to deal with value not in the thesaurus ?
  -->
  <xsl:template mode="mode-dcat2" priority="4000"
                match="*[gn-fn-dcat2:getThesaurusConfig(name(), name(..))]">
    <xsl:variable name="name" select="name()"/>
    <xsl:variable name="hasGnChild" select="count(../gn:child[concat(@prefix, ':', @name) = $name]) > 0"/>

    <xsl:if test="not($hasGnChild)">
      <xsl:variable name="isFirst"
                    select="count(preceding-sibling::*[name() = $name]) &lt; 1"/>
      <xsl:if test="$isFirst">
        <xsl:variable name="config" select="gn-fn-dcat2:getThesaurusConfig(name(), name(..))"/>

        <xsl:call-template name="thesaurus-picker-list">
          <xsl:with-param name="config" select="$config"/>
          <xsl:with-param name="ref" select="../gn:element/@ref"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="mode-dcat2" priority="4000"
                match="gn:child[gn-fn-dcat2:getThesaurusConfig(concat(@prefix, ':', @name), name(..))]">
    <xsl:variable name="config" select="gn-fn-dcat2:getThesaurusConfig(concat(@prefix, ':', @name), name(..))"/>
    <xsl:call-template name="thesaurus-picker-list">
      <xsl:with-param name="config" select="$config"/>
      <xsl:with-param name="ref" select="../gn:element/@ref"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="thesaurus-picker-list">
    <xsl:param name="config" as="node()"/>
    <xsl:param name="ref" as="xs:string"/>

    <xsl:variable name="values">
      <xsl:choose>
        <xsl:when test="$config/useReference = 'true' and ../*[name() = $config/@name]/@rdf:resource">
          <xsl:for-each select="../*[name() = $config/@name]">
           <xsl:variable name="v" select="replace(java:getKeywordValueByUri(@rdf:resource, $config/thesaurus, $lang), ',', ',,')" />

            <xsl:if test="string($v)">
              <xsl:value-of select="$v" /><xsl:if test="position() != last()">,</xsl:if>
            </xsl:if>

          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="string-join(
                          if ($metadataLanguage and ../*[name() = $config/@name]//skos:prefLabel[@xml:lang = $metadataLanguage])
                          then
                          ../*[name() = $config/@name]//skos:prefLabel[@xml:lang = $metadataLanguage]/replace(text(), ',', ',,')
                          else ../*[name() = $config/@name]//skos:prefLabel[not(@xml:lang)]/replace(text(), ',', ',,'), ',')" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="transformation" select="if ($config/useReference = 'true')
                                                then 'to-dcat2-concept-reference'
                                                else 'to-dcat2-concept'" />

    <div
      data-gn-keyword-selector="tagsinput"
      data-metadata-id=""
      data-element-ref="{concat('_P', $ref, '_', replace($config/@name, ':', 'COLON'))}"
      data-element-xpath="{concat(if ($isDcatService) then './dcat:DataService' else './dcat:Dataset', $config/xpath)}"
      data-wrapper="{$config/@name}"
      data-thesaurus-title="{$strings/*[name() = $config/labelKey]}"
      data-thesaurus-key="{$config/thesaurus}"
      data-keywords="{$values}"
      data-transformations="{$transformation}"
      data-current-transformation="{$transformation}"
      data-max-tags="{$config/max}"
      data-lang="{$metadataOtherLanguagesAsJson}"
      data-textgroup-only="false"
      class="">
    </div>
  </xsl:template>

  <xsl:function name="gn-fn-dcat2:getThesaurusConfig">
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="parent" as="xs:string"/>
    <xsl:copy-of select="if ($dcatKeywordConfig/*[@name = $name and @parent = $parent]) then
                                $dcatKeywordConfig/*[@name = $name and @parent = $parent] else
                                $dcatKeywordConfig/*[@name = $name and not(@parent)]"/>

  </xsl:function>
</xsl:stylesheet>
