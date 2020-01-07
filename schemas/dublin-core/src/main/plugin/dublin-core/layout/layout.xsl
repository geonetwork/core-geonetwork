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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/" xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:include href="utility-fn.xsl"/>

  <!-- Get the main metadata languages -->
  <xsl:template name="get-dublin-core-language">
    <xsl:value-of select="$metadata/descendant::node()/dc:language[1]"/>
  </xsl:template>

  <xsl:template name="get-dublin-core-title">
    <xsl:value-of select="$metadata//dc:title"/>
  </xsl:template>

  <!-- No multilingual support in Dublin core -->
  <xsl:template name="get-dublin-core-other-languages-as-json"/>

  <!-- Get the list of other languages -->
  <xsl:template name="get-dublin-core-other-languages"/>

  <xsl:template name="get-dublin-core-online-source-config"/>

  <xsl:template name="get-dublin-core-extents-as-json">[]</xsl:template>

  <!-- Visit all tree -->
  <xsl:template mode="mode-dublin-core" match="dc:*|dct:*">
    <xsl:apply-templates mode="mode-dublin-core" select="*|@*"/>
  </xsl:template>


  <!-- Forget those DC elements -->
  <xsl:template mode="mode-dublin-core"
                match="dc:*[
    starts-with(name(), 'dc:elementContainer') or
    starts-with(name(), 'dc:any')
    ]"
                priority="300">
    <xsl:apply-templates mode="mode-dublin-core" select="*|@*"/>
  </xsl:template>


  <!-- Boxed the root element -->
  <xsl:template mode="mode-dublin-core" priority="200" match="simpledc">
    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(.), $labels)/label"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="subTreeSnippet">
        <xsl:apply-templates mode="mode-dublin-core" select="*"/>
      </xsl:with-param>
      <xsl:with-param name="editInfo" select="gn:element"/>
    </xsl:call-template>
  </xsl:template>


  <!-- Forget all elements ... -->
  <xsl:template mode="mode-dublin-core" match="gn:*|@*"/>

  <!--
    ... but not the one proposing the list of elements to add in DC schema

    Template to display non existing element ie. geonet:child element
    of the metadocument. Display in editing mode only and if
  the editor mode is not flat mode. -->
  <xsl:template mode="mode-dublin-core" match="gn:child[contains(@name, 'CHOICE_ELEMENT')]"
                priority="2000">
    <xsl:if test="$isEditing and
      not($isFlatMode)">

      <!-- Create a new configuration to only create
            a add action for non existing node. The add action for
            the existing one is below the last element. -->
      <xsl:variable name="newElementConfig">
        <xsl:variable name="dcConfig"
                      select="ancestor::node()/gn:child[contains(@name, 'CHOICE_ELEMENT')]"/>
        <xsl:variable name="existingElementNames"
                      select="string-join(../descendant::*/name(), ',')"/>

        <gn:child>
          <xsl:copy-of select="$dcConfig/@*"/>
          <xsl:copy-of select="$dcConfig/gn:choose[not(contains($existingElementNames, @name))]"/>
        </gn:child>
      </xsl:variable>

      <xsl:call-template name="render-element-to-add">
        <xsl:with-param name="childEditInfo" select="$newElementConfig/gn:child"/>
        <xsl:with-param name="parentEditInfo" select="../gn:element"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <!-- Hide from the editor the dct:references pointing to uploaded files -->
  <xsl:template mode="mode-dublin-core" priority="101"
                match="*[(name(.) = 'dct:references' or
                          name(.) = 'dc:relation') and
                         (starts-with(., 'http') or
                          contains(. , 'resources.get') or
                          contains(., 'file.disclaimer'))]"/>


  <!-- the other elements in DC. -->
  <xsl:template mode="mode-dublin-core" priority="100" match="dc:*|dct:*">
    <xsl:variable name="name" select="name(.)"/>
    <xsl:variable name="ref" select="gn:element/@ref"/>
    <xsl:variable name="labelConfig" select="gn-fn-metadata:getLabel($schema, $name, $labels)"/>
    <xsl:variable name="helper" select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>

    <xsl:variable name="added" select="parent::node()/parent::node()/@gn:addedObj"/>
    <xsl:variable name="container" select="parent::node()/parent::node()"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>

    <!-- Add view and edit template-->
    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig"/>
      <xsl:with-param name="value" select="."/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!--<xsl:with-param name="widget"/>
            <xsl:with-param name="widgetParams"/>-->
      <xsl:with-param name="xpath" select="$xpath"/>
      <!--<xsl:with-param name="attributesSnippet" as="node()"/>-->
      <xsl:with-param name="type" select="gn-fn-metadata:getFieldType($editorConfig, name(), '', $xpath)"/>
      <xsl:with-param name="name" select="if ($isEditing) then gn:element/@ref else ''"/>
      <xsl:with-param name="editInfo"
                      select="gn:element"/>
      <xsl:with-param name="parentEditInfo"
                      select="if ($added) then $container/gn:element else element()"/>
      <xsl:with-param name="listOfValues" select="$helper"/>
      <!-- When adding an element, the element container contains
      information about cardinality. -->
      <xsl:with-param name="isFirst"
                      select="if ($added) then
                      (($container/gn:element/@down = 'true' and not($container/gn:element/@up)) or
                      (not($container/gn:element/@down) and not($container/gn:element/@up)))
                      else
                      ((gn:element/@down = 'true' and not(gn:element/@up)) or
                      (not(gn:element/@down) and not(gn:element/@up)))"/>
    </xsl:call-template>

    <!-- Add a control to add this type of element
      if this element is the last element of its kind.
    -->
    <xsl:if
      test="$isEditing and
            (
              not($isFlatMode) or
              gn-fn-metadata:isFieldFlatModeException($viewConfig, $name,  name(..))
            ) and
            $service != 'embedded' and
            count(following-sibling::node()[name() = $name]) = 0">

      <!-- Create configuration to add action button for this element. -->
      <xsl:variable name="dcConfig"
                    select="ancestor::node()/gn:child[contains(@name, 'CHOICE_ELEMENT')]"/>
      <xsl:variable name="newElementConfig">
        <gn:child>
          <xsl:copy-of select="$dcConfig/@*"/>
          <xsl:copy-of select="$dcConfig/gn:choose[@name = $name]"/>
        </gn:child>
      </xsl:variable>

      <xsl:variable name="label"
                    select="gn-fn-metadata:getLabel($schema, $name, $labels, '', '', '')"/>
      <xsl:call-template name="render-element-to-add">
        <xsl:with-param name="label" select="$label/label"/>
        <xsl:with-param name="class" select="if ($label/class) then $label/class else ''"/>
        <xsl:with-param name="btnLabel" select="if ($label/btnLabel) then $label/btnLabel else ''"/>
        <xsl:with-param name="btnClass" select="if ($label/btnClass) then $label/btnClass else ''"/>
        <xsl:with-param name="childEditInfo" select="$newElementConfig/gn:child"/>
        <xsl:with-param name="parentEditInfo" select="$dcConfig/parent::node()/gn:element"/>
        <xsl:with-param name="isFirst" select="false()"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <!-- Readonly elements -->
  <xsl:template mode="mode-dublin-core" priority="200" match="dc:identifier|dct:modified">

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(), $labels)"/>
      <xsl:with-param name="value" select="."/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="type" select="gn-fn-metadata:getFieldType($editorConfig, name(), '', $xpath)"/>
      <xsl:with-param name="name" select="''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="isDisabled" select="true()"/>
    </xsl:call-template>

  </xsl:template>

  <xsl:template mode="mode-dublin-core" match="dc:coverage" priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="coverage" select="."/>
    <xsl:variable name="n" select="substring-after($coverage,'North ')"/>
    <xsl:variable name="north" select="substring-before($n,',')"/>
    <xsl:variable name="s" select="substring-after($coverage,'South ')"/>
    <xsl:variable name="south" select="substring-before($s,',')"/>
    <xsl:variable name="e" select="substring-after($coverage,'East ')"/>
    <xsl:variable name="east" select="substring-before($e,',')"/>
    <xsl:variable name="w" select="substring-after($coverage,'West ')"/>
    <xsl:variable name="west" select="if (contains($w, '. '))
                                      then substring-before($w,'. ') else $w"/>
    <xsl:variable name="place" select="substring-after($coverage,'. ')"/>

    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..),'','')/label"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!-- <xsl:with-param name="attributesSnippet" select="$attributes"/> -->
      <xsl:with-param name="subTreeSnippet">
        <div gn-draw-bbox=""
             data-hleft="{$west}"
             data-hright="{$east}"
             data-hbottom="{$south}"
             data-htop="{$north}"
             data-dc-ref="_{gn:element/@ref}"
             data-lang="lang"
             data-location="{$place}"></div>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


</xsl:stylesheet>
