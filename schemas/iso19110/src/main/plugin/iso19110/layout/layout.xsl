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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gfc="http://www.isotc211.org/2005/gfc"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-iso19110="http://geonetwork-opensource.org/xsl/functions/profiles/iso19110"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:include href="utility-fn.xsl"/>
  <xsl:include href="layout-custom-fields.xsl"/>

  <!-- Ignore all gn element -->
  <xsl:template mode="mode-iso19110" match="gn:*|@gn:*" priority="1000"/>


  <!-- Template to display non existing element ie. geonet:child element
  of the metadocument. Display in editing mode only and if
  the editor mode is not flat mode. -->
  <xsl:template mode="mode-iso19110" match="gn:child" priority="2000">
    <!-- TODO: this should be common to all schemas -->

    <xsl:variable name="name" select="concat(@prefix, ':', @name)"/>
    <xsl:variable name="directive"
                  select="gn-fn-metadata:getFieldAddDirective($editorConfig, $name)"/>

    <xsl:variable name="flatModeException"
                  select="gn-fn-metadata:isFieldFlatModeException($viewConfig, $name,  name(..))"/>

    <xsl:if test="$isEditing and (not($isFlatMode) or $flatModeException)">

      <xsl:variable name="label"
                    select="gn-fn-metadata:getLabel($schema, $name, $labels)"/>
      <xsl:call-template name="render-element-to-add">
        <xsl:with-param name="label" select="$label/label"/>
        <xsl:with-param name="class" select="if ($label/class) then $label/class else ''"/>
        <xsl:with-param name="btnLabel" select="if ($label/btnLabel) then $label/btnLabel else ''"/>
        <xsl:with-param name="btnClass" select="if ($label/btnClass) then $label/btnClass else ''"/>
        <xsl:with-param name="directive" select="$directive"/>
        <xsl:with-param name="childEditInfo" select="."/>
        <xsl:with-param name="parentEditInfo" select="../gn:element"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <!-- Visit all XML tree recursively -->
  <xsl:template mode="mode-iso19110" match="gfc:*">
    <xsl:apply-templates mode="mode-iso19110" select="*|@*"/>
  </xsl:template>

  <xsl:template mode="mode-iso19110" match="gmd:*|gmx:*">
    <xsl:apply-templates mode="mode-iso19139" select="*|@*">
      <xsl:with-param name="schema" select="'iso19139'"/>
      <xsl:with-param name="labels" select="$iso19139labels"/>
    </xsl:apply-templates>
  </xsl:template>


  <!-- Boxed element -->
  <xsl:template mode="mode-iso19110" priority="200"
                match="gfc:*[gfc:FC_FeatureType]|
    gfc:*[gfc:FC_AssociationRole]|
    gfc:*[gfc:FC_AssociationOperation]|
    *[name() = $editorConfig/editor/fieldsWithFieldset/name]|
    *[namespace-uri(.) != $gnUri and $isFlatMode = false() and gfc:*]">

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:variable name="attributes">
      <xsl:if test="$isEditing">
        <!-- Create form for all existing attribute (not in gn namespace)
        and all non existing attributes not already present. -->
        <xsl:apply-templates mode="render-for-field-for-attribute"
                             select="
          @*|
          gn:attribute[not(@name = parent::node()/@*/name())]">
          <xsl:with-param name="ref" select="gn:element/@ref"/>
          <xsl:with-param name="insertRef" select="gn:element/@ref"/>
        </xsl:apply-templates>
      </xsl:if>
    </xsl:variable>

    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="subTreeSnippet">
        <!-- Process child of those element -->
        <xsl:apply-templates mode="mode-iso19110" select="*"/>
      </xsl:with-param>
    </xsl:call-template>

  </xsl:template>

  <!-- Render simple element wi9th gmx:Anchor which usually match a form field -->
  <xsl:template mode="mode-iso19110" priority="1000"
                match="*[gmx:Anchor]">

    <xsl:variable name="elementSchema"
                  select="if(namespace-uri() = 'http://www.isotc211.org/2005/gfc')
      then $labels else $iso19139labels"/>
    <xsl:variable name="elementSchemaIdentifier"
                  select="if(namespace-uri() = 'http://www.isotc211.org/2005/gfc')
      then $schema else 'iso19139'"/>
    <xsl:variable name="elementName" select="name()"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig"
                  select="gn-fn-metadata:getLabel($elementSchemaIdentifier, name(), $elementSchema, name(..), $isoType, $xpath)"/>
    <xsl:variable name="helper" select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>

    <xsl:variable name="attributes">
      <xsl:if test="$isEditing">

        <!-- Create form for all existing attribute (not in gn namespace)
        and all non existing attributes not already present for the
        current element and its children (eg. @uom in gco:Distance).
        A list of exception is defined in form-builder.xsl#render-for-field-for-attribute. -->
        <xsl:apply-templates mode="render-for-field-for-attribute"
                             select="
              @*|
              gn:attribute[not(@name = parent::node()/@*/name())]">
          <xsl:with-param name="ref" select="gn:element/@ref"/>
          <xsl:with-param name="insertRef" select="*/gn:element/@ref"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="render-for-field-for-attribute"
                             select="
          */@*|
          */gn:attribute[not(@name = parent::node()/@*/name())]">
          <xsl:with-param name="ref" select="*/gn:element/@ref"/>
          <xsl:with-param name="insertRef" select="*/gn:element/@ref"/>
        </xsl:apply-templates>
      </xsl:if>
    </xsl:variable>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig"/>
      <xsl:with-param name="value" select="*"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!--<xsl:with-param name="widget"/>
      <xsl:with-param name="widgetParams"/>-->
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="forceDisplayAttributes" select="true()" />
      <xsl:with-param name="type"
                      select="gn-fn-iso19110:getFieldType(name(),
            name(gmx:Anchor), $xpath)"/>
      <xsl:with-param name="name" select="if ($isEditing) then */gn:element/@ref else ''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <!-- TODO: Handle conditional helper -->
      <xsl:with-param name="listOfValues" select="$helper"/>
      <xsl:with-param name="isFirst"
                      select="count(preceding-sibling::*[name() = $elementName]) = 0"/>
    </xsl:call-template>
  </xsl:template>


  <!-- Render simple element which usually match a form field -->
  <xsl:template mode="mode-iso19110" priority="100"
                match="*[gco:CharacterString|gco:Date|gco:DateTime|gco:Integer|gco:UnlimitedInteger|
                        gco:Decimal|gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|
                        gco:Scale|gco:RecordType|gmx:MimeFileType|gmd:URL|gco:LocalName]">

    <xsl:variable name="elementSchema"
                  select="if(namespace-uri() = 'http://www.isotc211.org/2005/gfc')
      then $labels else $iso19139labels"/>
    <xsl:variable name="elementSchemaIdentifier"
                  select="if(namespace-uri() = 'http://www.isotc211.org/2005/gfc')
      then $schema else 'iso19139'"/>
    <xsl:variable name="elementName" select="name()"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig"
                  select="gn-fn-metadata:getLabel($elementSchemaIdentifier, name(), $elementSchema, name(..), $isoType, $xpath)"/>
    <xsl:variable name="helper" select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>

    <xsl:variable name="attributes">
      <xsl:if test="$isEditing">

        <!-- Create form for all existing attribute (not in gn namespace)
        and all non existing attributes not already present for the
        current element and its children (eg. @uom in gco:Distance).
        A list of exception is defined in form-builder.xsl#render-for-field-for-attribute. -->
        <xsl:apply-templates mode="render-for-field-for-attribute"
                             select="
              @*|
              gn:attribute[not(@name = parent::node()/@*/name())]">
          <xsl:with-param name="ref" select="gn:element/@ref"/>
          <xsl:with-param name="insertRef" select="*/gn:element/@ref"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="render-for-field-for-attribute"
                             select="
          */@*|
          */gn:attribute[not(@name = parent::node()/@*/name())]">
          <xsl:with-param name="ref" select="*/gn:element/@ref"/>
          <xsl:with-param name="insertRef" select="*/gn:element/@ref"/>
        </xsl:apply-templates>
      </xsl:if>
    </xsl:variable>


    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig"/>
      <xsl:with-param name="value" select="*"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!--<xsl:with-param name="widget"/>
      <xsl:with-param name="widgetParams"/>-->
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="type"
                      select="gn-fn-iso19110:getFieldType(name(),
            name(gco:CharacterString|gco:Date|gco:DateTime|gco:Integer|gco:Decimal|
                gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|
                gco:Scale|gco:RecordType|gmx:MimeFileType|gmd:URL), $xpath)"/>
      <xsl:with-param name="name" select="if ($isEditing) then */gn:element/@ref else ''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <!-- TODO: Handle conditional helper -->
      <xsl:with-param name="listOfValues" select="$helper"/>
      <xsl:with-param name="isFirst"
                      select="count(preceding-sibling::*[name() = $elementName]) = 0"/>
    </xsl:call-template>

  </xsl:template>


  <!--
    ISOI19139 dependent
  -->
  <xsl:template mode="mode-iso19110" match="*[gmx:FileName]">
    <xsl:apply-templates mode="mode-iso19139"/>
  </xsl:template>

  <xsl:template mode="mode-iso19110" priority="2000" match="gmd:*[*/@codeList]">
    <xsl:apply-templates mode="mode-iso19139">
      <xsl:with-param name="schema" select="'iso19139'"/>
      <xsl:with-param name="labels" select="$iso19139labels"/>
      <xsl:with-param name="codelists" select="$iso19139codelists"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Match codelist values.
  -->
  <xsl:template mode="mode-iso19110" priority="200" match="gfc:*[*/@codeList]">

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>
      <xsl:with-param name="value" select="*/@codeListValue"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!--<xsl:with-param name="widget"/>
            <xsl:with-param name="widgetParams"/>-->
      <xsl:with-param name="xpath" select="$xpath"/>
      <!--<xsl:with-param name="attributesSnippet" as="node()"/>-->
      <xsl:with-param name="type" select="gn-fn-iso19139:getCodeListType(name())"/>
      <xsl:with-param name="name"
                      select="if ($isEditing) then concat(*/gn:element/@ref, '_codeListValue') else ''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="listOfValues"
                      select="gn-fn-metadata:getCodeListValues($schema, name(*[@codeListValue]), $codelists)"/>
    </xsl:call-template>

  </xsl:template>


  <!-- Get the main metadata languages - none for ISO19110 -->
  <xsl:template name="get-iso19110-language"/>

  <!-- Get the list of other languages -->
  <xsl:template name="get-iso19110-other-languages"/>

  <xsl:template name="get-iso19110-other-languages-as-json"/>

  <xsl:template name="get-iso19110-online-source-config"/>

  <xsl:template name="get-iso19110-title">
    <xsl:value-of select="$metadata/gfc:FC_FeatureCatalogue/gmx:name/gco:CharacterString|
                          $metadata/gfc:FC_FeatureCatalogue/gfc:name/gco:CharacterString|
                          $metadata/gfc:FC_FeatureType/gfc:typeName/gco:LocalName"/>
  </xsl:template>

  <xsl:template name="get-iso19110-extents-as-json">[]</xsl:template>

</xsl:stylesheet>
