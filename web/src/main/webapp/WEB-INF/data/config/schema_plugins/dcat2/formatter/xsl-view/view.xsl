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
                xmlns:adms="http://www.w3.org/ns/adms#" xmlns:dct="http://purl.org/dc/terms/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:locn="http://www.w3.org/ns/locn#" xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:schema="http://schema.org/"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-dcat2="http://geonetwork-opensource.org/xsl/functions/profiles/dcat2"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:strip-space elements="*"/>

  <!-- Load the editor configuration to be able to render the different views -->
  <xsl:variable name="configuration"
                select="document('../../layout/config-editor.xml')"/>

  <!-- Some utility -->
  <xsl:include href="common/functions-metadata.xsl"/>
  <xsl:include href="../../layout/utility-tpl-multilingual.xsl" />
  <xsl:include href="../../convert/functions.xsl"/>
  <xsl:include href="../../layout/evaluate.xsl"/>

  <!-- The core formatter XSL layout based on the editor configuration -->
  <xsl:include href="sharedFormatterDir/xslt/render-layout.xsl"/>

  <!-- The stylesheet 'common/functions-metadata.xsl' relies on two variables
    'iso19139labels' and 'defaultFieldType' -->
  <xsl:variable name="iso19139labels" select="dummy"/>
  <xsl:variable name="defaultFieldType" select="'text'"/>


  <!-- Define the metadata to be loaded for this schema plugin -->
  <xsl:variable name="metadata" select="/root/rdf:RDF"/>
  <xsl:variable name="langId" select="/root/gui/language"/>
  <xsl:variable name="nodeUrl" select="/root/gui/nodeUrl"/>
  <xsl:variable name="langId-2char">
    <xsl:call-template name="langId3to2">
      <xsl:with-param name="langId-3char" select="$langId"/>
    </xsl:call-template>
  </xsl:variable>
  <!-- Create a SchemaLocalizations object to look up nodeLabels with function
    tr:node-label($schemaLocalizations, name(), name(..)). This is no longer
    used -->
  <!-- xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations" -->
  <!-- <xsl:variable name="schemaLocalizations" select="tr:create($schema)"
    /> -->

  <!-- The labels and their translations -->
  <xsl:variable name="schemaInfo" select="/root/schemas/*[name(.)=$schema]"/>
  <xsl:variable name="labels" select="$schemaInfo/labels"/>

  <xsl:variable name="allLanguages">
    <xsl:call-template name="get-dcat2-other-languages"/>
  </xsl:variable>

  <!-- Specific schema rendering -->
  <xsl:template mode="getMetadataTitle" match="rdf:RDF">
    <!--
     Language priority order:
       1) Field in the language of the interface
       2) Field in default language ($defaultLang-2char)
       3) First field of the list
     -->
    <xsl:choose>
      <xsl:when test="//dcat:Dataset/dct:title[@xml:lang = $langId-2char]">
        <xsl:value-of select="//dcat:Dataset/dct:title[@xml:lang = $langId-2char][1]"/>
      </xsl:when>
      <xsl:when test="//dcat:Dataset/dct:title[@xml:lang = $defaultLang-2char]">
        <xsl:value-of
          select="concat(//dcat:Dataset/dct:title[@xml:lang = $defaultLang-2char][1], ' (', normalize-space(//dcat:Dataset/dct:title[@xml:lang = $defaultLang-2char][1]/@xml:lang), ')')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="//dcat:Dataset/dct:title[1]"/>
        <xsl:if
          test="//dcat:Dataset/dct:title[1]/@xml:lang and normalize-space(//dcat:Dataset/dct:title[1]/@xml:lang) != '' ">
          <xsl:value-of select="concat(' (',//dcat:Dataset/dct:title[1]/@xml:lang,')')"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="getMetadataAbstract" match="rdf:RDF">
    <xsl:value-of select="//dcat:Dataset/dct:description"/>
  </xsl:template>

  <xsl:template mode="getMetadataHeader" match="rdf:RDF">
  </xsl:template>

  <xsl:template mode="getMetadataThumbnail" match="rdf:RDF">
  </xsl:template>


  <!-- Field with lang : display only field of current lang or first one if not exist -->
  <xsl:template mode="render-field"
                match="dct:title|dct:description|foaf:name|adms:versionNotes">
    <xsl:param name="fieldName"/>

    <xsl:if test="normalize-space(string-join(., '')) != ''">
      <dl>
        <dt>
          <xsl:call-template name="render-field-label">
            <xsl:with-param name="fieldName" select="$fieldName"/>
            <xsl:with-param name="languages" select="$allLanguages"/>
          </xsl:call-template>
        </dt>
        <dd><xsl:comment select="name()"/>
          <xsl:apply-templates mode="render-value" select="."/>
          <xsl:apply-templates mode="render-value" select="@*"/>
        </dd>
      </dl>
    </xsl:if>
  </xsl:template>

  <!-- Field with no lang : display all -->
  <xsl:template mode="render-field"
                match="dcat:accessURL|dcat:downloadURL|dcat:landingPage|dct:created|dct:issued|dct:modified|dct:identifier|skos:notation|schema:startDate|schema:endDate|vcard:street-address|vcard:locality|vcard:postal-code|vcard:country-name|vcard:hasTelephone|vcard:fn|vcard:organization-name|skos:prefLabel|owl:versionInfo|adms:versionNotes|dcat:byteSize">
    <xsl:param name="fieldName"/>

    <xsl:variable name="stringValue" select="string()"/>
    <xsl:if test="normalize-space($stringValue) != ''">
      <dl>
        <dt>
          <xsl:call-template name="render-field-label">
            <xsl:with-param name="fieldName" select="$fieldName"/>
            <xsl:with-param name="languages" select="$allLanguages"/>
          </xsl:call-template>
        </dt>
        <dd><xsl:comment select="name()"/>
          <xsl:apply-templates mode="render-value" select="."/>
        </dd>
      </dl>
    </xsl:if>
  </xsl:template>

  <!-- Some major sections are boxed -->
  <xsl:template mode="render-field"
                match="*[name() = $configuration/editor/fieldsWithFieldset/name]">
    <div class="entry name">
      <h2>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
      </h2>
      <div class="target"><xsl:comment select="name()"/>
        <xsl:choose>
          <xsl:when test="count(*) > 0">
            <xsl:apply-templates mode="render-field" select="*"/>
          </xsl:when>
          <xsl:otherwise>
            No information provided.
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>

  </xsl:template>

  <!-- Traverse the tree -->
  <xsl:template mode="render-field"
                match="*">
    <xsl:apply-templates mode="render-field"/>
  </xsl:template>

  <xsl:template name="render-field-label">
    <xsl:param name="fieldName" select="''" as="xs:string" required="no"/>
    <xsl:param name="languages" as="node()*" required="no"/>
    <xsl:param name="contextLabel" as="attribute()?" required="no"/>

    <xsl:variable name="name"
                  select="name()"/>

    <xsl:variable name="context"
                  select="name(..)"/>

    <xsl:choose>
      <!-- eg. for codelist, display label in all record languages -->
      <xsl:when test="$fieldName = '' and $language = 'all' and count($languages/lang) > 0">
        <xsl:for-each select="$languages/lang">
          <div xml:lang="{@code}">
            <xsl:value-of select="tr:nodeLabel(tr:create($schema, @code), $name, $context)"/>
            <xsl:if test="$contextLabel">
              <xsl:variable name="extraLabel">
                <xsl:apply-templates mode="render-value"
                                     select="$contextLabel">
                  <xsl:with-param name="forcedLanguage" select="@code"/>
                </xsl:apply-templates>
              </xsl:variable>
              <xsl:value-of select="concat(' (', $extraLabel, ')')"/>
            </xsl:if>
          </div>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <!-- Overriden label or element name in current UI language. -->
        <xsl:value-of select="if ($fieldName)
                                then $fieldName
                                else tr:nodeLabel(tr:create($schema), $name, $context)"/>
        <xsl:if test="$contextLabel">
          <xsl:variable name="extraLabel">
            <xsl:apply-templates mode="render-value"
                                 select="$contextLabel"/>
          </xsl:variable>
          <xsl:value-of select="concat(' (', $extraLabel, ')')"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- ########################## -->
  <!-- Render values for text with clickable URL ... -->
  <xsl:template mode="render-value"
                match="dct:title|dct:description|owl:versionInfo|adms:versionNotes|dct:LicenseDocument/dct:identifier">
    <xsl:call-template name="addLineBreaksAndHyperlinks">
      <xsl:with-param name="txt" select="."/>
    </xsl:call-template>
  </xsl:template>

  <!-- Render values for text -->
  <xsl:template mode="render-value" match="*">
    <xsl:value-of select="."/>
  </xsl:template>

  <!-- Render values for URL -->
  <xsl:template mode="render-url" match="*|@*">
    <a href="{.}" style="color=#06c; text-decoration: underline;">
      <xsl:value-of select="."/>
    </a>
  </xsl:template>

  <!-- ... Dates -->
  <xsl:template mode="render-value"
                match="*[matches(., '^[0-9]{4}-[0-1][0-9]-[0-3][0-9](Z|(\+|-)[0-1][0-9]:[0-6][0-9])?$')]">
    <span data-gn-humanize-time="{.}" data-format="DD MMM YYYY">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="*[matches(., '^[0-9]{4}-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-6][0-9]:[0-6][0-9](Z|(\+|-)[0-1][0-9]:[0-6][0-9])?$')]">
    <span data-gn-humanize-time="{.}" data-format="DD MMM YYYY HH:mm">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-url" match="*[../name() = 'vcard:hasEmail']|@*[../name() = 'vcard:hasEmail']">
    <xsl:choose>
      <xsl:when test="starts-with(normalize-space(.), 'mailto:')">
        <a href="{normalize-space(.)}" style="color=#06c; text-decoration: underline;">
          <xsl:value-of select="substring-after(normalize-space(.), 'mailto:')"/>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <a href="{concat('mailto:', normalize-space(.))}" style="color=#06c; text-decoration: underline;">
          <xsl:value-of select="."/>
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="render-url"
                match="*[name(..) = 'dct:relation' or name(..) = 'dct:source' or name(..) = 'dct:isVersionOf' or name(..) = 'dct:hasVersion']|
                       @*[name(..) = 'dct:relation' or name(..) = 'dct:source' or name(..) = 'dct:isVersionOf' or name(..) = 'dct:hasVersion']">
    <a
      href="{concat($nodeUrl,$langId,'/catalog.search#/search?resultType=details&amp;sortBy=relevance&amp;from=1&amp;to=20&amp;fast=index&amp;_content_type=json&amp;any=',.)}"
      style="color=#06c; text-decoration: underline;">
      <xsl:value-of select="."/>
    </a>
  </xsl:template>
</xsl:stylesheet>
