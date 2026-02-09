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
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:java-xsl-util="java:org.fao.geonet.util.XslUtil"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:include href="layout-custom-fields-keywords.xsl"/>
  <xsl:include href="layout-custom-fields-sds.xsl"/>

  <!-- Readonly elements -->
  <xsl:template mode="mode-iso19139" priority="2100" match="gmd:fileIdentifier|gmd:dateStamp">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="fieldLabelConfig"
                  select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>

    <xsl:variable name="labelConfig">
      <xsl:choose>
        <xsl:when test="$overrideLabel != ''">
          <element>
            <label><xsl:value-of select="$overrideLabel"/></label>
          </element>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$fieldLabelConfig"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>


    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="$labelConfig/*"/>
      <xsl:with-param name="value" select="*"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="type" select="gn-fn-metadata:getFieldType($editorConfig, name(), '', $xpath)"/>
      <xsl:with-param name="name" select="''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="isDisabled" select="true()"/>
    </xsl:call-template>

  </xsl:template>
<!--

  &lt;!&ndash; Measure elements, gco:Distance, gco:Angle, gco:Scale, gco:Length, ... &ndash;&gt;
  <xsl:template mode="mode-iso19139" priority="2000" match="*[gco:*/@uom]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>
    <xsl:param name="refToDelete" select="gn:element" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig"
                  select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>

    <xsl:variable name="labelMeasureType"
                  select="gn-fn-metadata:getLabel($schema, name(gco:*), $labels, name(), '', '')"/>

    <xsl:variable name="isRequired" as="xs:boolean">
      <xsl:choose>
        <xsl:when
          test="($refToDelete and $refToDelete/@min = 1 and $refToDelete/@max = 1) or
          (not($refToDelete) and gn:element/@min = 1 and gn:element/@max = 1)">
          <xsl:value-of select="true()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="false()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>


    <div class="form-group gn-field gn-title {if ($isRequired) then 'gn-required' else ''} {if ($labelConfig/condition) then concat('gn-', $labelConfig/condition) else ''}"
         id="gn-el-{*/gn:element/@ref}"
         data-gn-field-highlight="">
      <label class="col-sm-2 control-label">
        <xsl:value-of select="if ($overrideLabel != '') then $overrideLabel else $labelConfig/label"/>
        <xsl:if test="$labelMeasureType != '' and
                      $labelMeasureType/label != $labelConfig/label">&#10;
          (<xsl:value-of select="$labelMeasureType/label"/>)
        </xsl:if>
      </label>
      <div class="col-sm-9 col-xs-11 gn-value nopadding-in-table">
        <xsl:variable name="elementRef"
                      select="gco:*/gn:element/@ref"/>
        <xsl:variable name="helper"
                      select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>
        <div data-gn-measure="{gco:*/text()}"
             data-uom="{gco:*/@uom}"
             data-ref="{concat('_', $elementRef)}">
        </div>

        <textarea id="_{$elementRef}_config" class="hidden">
          <xsl:copy-of select="java-xsl-util:xmlToJson(
              saxon:serialize($helper, 'default-serialize-mode'))"/>
        </textarea>
      </div>
      <div class="col-sm-1 col-xs-1 gn-control">
        <xsl:call-template name="render-form-field-control-remove">
          <xsl:with-param name="editInfo" select="*/gn:element"/>
          <xsl:with-param name="parentEditInfo" select="$refToDelete"/>
        </xsl:call-template>
      </div>

      <div class="col-sm-offset-2 col-sm-9">
        <xsl:call-template name="get-errors"/>
      </div>
    </div>
  </xsl:template>

-->

  <xsl:template mode="mode-iso19139"
                match="gml320:TimeInstant[gml320:timePosition]|gml:TimeInstant[gml:timePosition]"
                priority="50000">

    <xsl:variable name="configName" select="concat(local-name(..),'Position')"/>
        <!-- 'temporalRangeSection' this is the name of the section (defined elsewhere)
             and uses to find the translated tag for the section in the UI -->
        <xsl:variable name="translation"
                      select="$strings/*[name() = $configName]"/>
        <xsl:variable name="name"
                      select="if ($translation != '')
                                    then $translation
                                    else $configName"/>

        <xsl:variable name="id" select="concat('_X', gn:element/@ref, '_replace')"/>

    <xsl:variable name="isFirst" select="position() = 1"/>
    <xsl:variable name="originalNode"
                  select="gn-fn-metadata:getOriginalNode($metadata, .)"/>
    <xsl:variable name="del" select="./@del"/>

    <xsl:variable name="refToDelete">
      <xsl:call-template name="get-ref-element-to-delete">
        <xsl:with-param name="node" select="$originalNode"/>
        <xsl:with-param name="delXpath" select="$del"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="parent_parent" select="."/>

    <!-- look at the actual XML's gml namespace URI
         if its GML 32, then that matches most of the implicit
         assumptions inside GN.  Especially the JS, which assumes that
         an element with a "gml:" prefix is GML 3.2.
         However, this is a really bad assumption - all the iso19139 sample data
         has "gml:" as the old (NOT GML 3.2).
         Also, in this XSLT;
         "gml:" - version 3.2
         "gml320:" - OLD VERSION (!!!!!!!!)
     -->
    <xsl:variable name="element_ns_uri" select="namespace-uri()"/>
    <xsl:variable name="element_ns_prefix" select="prefix-from-QName(node-name(.))"/>
    <xsl:variable name="gml_is_32" select="$element_ns_uri = 'http://www.opengis.net/gml/3.2'"/>
    <xsl:variable name="timeinstance_id" select="@*[local-name() = 'id']"/>

    <xsl:variable name="prefix_to_use">gml</xsl:variable>

    <xsl:variable name="prefix_to_use_select">
      <xsl:choose>
        <xsl:when test="$gml_is_32">gml</xsl:when>
        <xsl:otherwise>gml320</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>


    <xsl:variable name="label_node" select="local-name(..)"/>


    <xsl:variable name="template">
          <template  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gn="http://www.fao.org/geonetwork" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv"  xmlns:xlink="http://www.w3.org/1999/xlink">
            <values>
              <key
                   xpath="."
                   use="gn-date-picker"
                   tooltip="gmd:extent|gmd:EX_TemporalExtent">
                <xsl:attribute name="label" select="$label_node"/>

                <directiveAttributes
                        data-assume-gml-ns="true"
                >
                  <xsl:attribute name="data-indeterminate-position" select="concat('eval#',$prefix_to_use_select,':timePosition/@indeterminatePosition')"/>
                  <xsl:attribute name="data-tag-name" select="concat($prefix_to_use,':timePosition')"/>

                </directiveAttributes>
              </key>
            </values>
            <snippet>
              <!-- this is more explicitly done, below -->
              <gml:TimeInstant>
              {{<xsl:copy-of select="local-name(..)"/>}}
              </gml:TimeInstant>
            </snippet>
          </template>
        </xsl:variable>

    <xsl:variable name="keyValues">
          <xsl:call-template name="build-key-value-configuration">
            <xsl:with-param name="template" select="$template/template"/>
            <xsl:with-param name="currentNode" select="$parent_parent"/>
            <xsl:with-param name="readonly" select="'false'"/>
          </xsl:call-template>
        </xsl:variable>

    <xsl:variable name="templateCombinedWithNode" as="node()">
      <template>
        <xsl:copy-of select="$template/template/values"/>

        <xsl:element name="snippet" inherit-namespaces="no">
          <xsl:attribute name="data-remove-gn-ns-def">true</xsl:attribute>

            <xsl:element name="gml:TimeInstant"  namespace="{$element_ns_uri}" inherit-namespaces="no">
                <xsl:attribute  namespace="{$element_ns_uri}" name="gml:id"><xsl:value-of select="$timeinstance_id"/></xsl:attribute>
              {{<xsl:copy-of select="local-name(..)"/>}}
            </xsl:element>


        </xsl:element>
      </template>
    </xsl:variable>


    <xsl:call-template name="render-element-template-field">
      <xsl:with-param name="name" select="$name"/>
      <xsl:with-param name="id" select="$id"/>
      <xsl:with-param name="isExisting" select="true()"/>
      <xsl:with-param name="template" select="$templateCombinedWithNode"/>
      <xsl:with-param name="keyValues" select="$keyValues"/>
      <xsl:with-param name="refToDelete" select="$refToDelete/gn:element"/>
      <xsl:with-param name="isFirst" select="$isFirst"/>
    </xsl:call-template>
  </xsl:template>
 
  <!---
    For temporal extent, we want a more complicated control.
    This will allow the <beginPosition   indeterminatePosition="now"></beginPosition>
    It also sets up the DatePicker so it has the indeterminatePosition drop-down, plus
    a calendar date selector, a time selector, a timezone selector, and "mode" selector (i.e. only year, year + month, date & time).

    This is basically a replacement for https://github.com/geonetwork/core-geonetwork/blob/5f6571e72e2bd39237394caf1f076a6cc0dc8758/schemas/iso19139/src/main/plugin/iso19139/layout/config-editor.xml#L1471-L1513
    See the "template" variable, below.

    This is basically a stand-in for the  <field name="temporalRangeSection"> and uses the same template to do the controls.
  -->
  <xsl:template mode="mode-iso19139"
                match="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement[//gml:beginPosition]"
                priority="30000">
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>


    <xsl:variable name="configName" select="'temporalRangeSection'"/>
    <!-- 'temporalRangeSection' this is the name of the section (defined elsewhere)
         and uses to find the translated tag for the section in the UI -->
    <xsl:variable name="translation"
                  select="$strings/*[name() = $configName]"/>
    <xsl:variable name="name"
                  select="if ($translation != '')
                                then $translation
                                else $configName"/>

    <xsl:variable name="id" select="concat('_X', gn:element/@ref, '_replace')"/>

    <xsl:variable name="template">
      <template  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gn="http://www.fao.org/geonetwork" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:xlink="http://www.w3.org/1999/xlink">
      <values>
        <!-- -Need a * for gml:TimePeriodTypeCHOICE_ELEMENT2 added by editor enumerated tree
              but this will make the XSLT formatter fails. Using // to access the element in both case. -->
        <key label="beginPosition"
             xpath="gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod//gml:beginPosition"
             use="gn-date-picker"
             tooltip="gmd:extent|gmd:EX_TemporalExtent">
          <directiveAttributes
            data-tag-name="gml:beginPosition"
            data-indeterminate-position="eval#gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/*/gml:beginPosition/@indeterminatePosition"/>
        </key>
        <key label="endPosition"
             xpath="gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod//gml:endPosition"
             use="gn-date-picker"
             tooltip="gmd:extent|gmd:EX_TemporalExtent">
          <directiveAttributes
            data-tag-name="gml:endPosition"
            data-indeterminate-position="eval#gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/*/gml:endPosition/@indeterminatePosition"/>
        </key>
      </values>
      <snippet>
        <gmd:temporalElement>
          <gmd:EX_TemporalExtent>
            <gmd:extent>
              <gml:TimePeriod gml:id="">
                {{beginPosition}}
                {{endPosition}}
              </gml:TimePeriod>
            </gmd:extent>
          </gmd:EX_TemporalExtent>
        </gmd:temporalElement>
      </snippet>
    </template>
    </xsl:variable>
    <xsl:variable name="isFirst" select="position() = 1"/>
    <xsl:variable name="originalNode"
                  select="gn-fn-metadata:getOriginalNode($metadata, .)"/>
    <xsl:variable name="del" select="./@del"/>

    <xsl:variable name="refToDelete">
      <xsl:call-template name="get-ref-element-to-delete">
        <xsl:with-param name="node" select="$originalNode"/>
        <xsl:with-param name="delXpath" select="$del"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="parent_parent" select="."/>

    <xsl:variable name="keyValues">
      <xsl:call-template name="build-key-value-configuration">
        <xsl:with-param name="template" select="$template/template"/>
        <xsl:with-param name="currentNode" select="$parent_parent"/>
        <xsl:with-param name="readonly" select="'false'"/>
      </xsl:call-template>
    </xsl:variable>


    <xsl:variable name="templateCombinedWithNode" as="node()">
      <template>
        <xsl:copy-of select="$template/template/values"/>
        <snippet>
          <xsl:apply-templates mode="gn-merge" select="$template/template/snippet/*|$editorConfig/editor/snippets/list[@name = $template/template/snippets/@name]/snippet/*">
            <xsl:with-param name="node-to-merge" select="$parent_parent"/>
          </xsl:apply-templates>
        </snippet>
      </template>
    </xsl:variable>

    <xsl:call-template name="render-element-template-field">
      <xsl:with-param name="name" select="$name"/>
      <xsl:with-param name="id" select="$id"/>
      <xsl:with-param name="isExisting" select="true()"/>
      <xsl:with-param name="template" select="$templateCombinedWithNode"/>
      <xsl:with-param name="keyValues" select="$keyValues"/>
      <xsl:with-param name="refToDelete" select="$refToDelete/gn:element"/>
      <xsl:with-param name="isFirst" select="$isFirst"/>
    </xsl:call-template>

  </xsl:template>


  <!-- ===================================================================== -->
  <!-- gml:TimePeriod (format = %Y-%m-%dThh:mm:ss) -->
  <!-- ===================================================================== -->

  <xsl:template mode="mode-iso19139"
                match="gml:beginPosition|gml:endPosition|
                       gml320:beginPosition|gml320:endPosition"
                priority="200">

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="value" select="normalize-space(text())"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig" select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>


    <xsl:variable name="attributes">
      <xsl:if test="$isEditing">
        <!-- Create form for all existing attribute (not in gn namespace)
        and all non existing attributes not already present. -->
        <xsl:apply-templates mode="render-for-field-for-attribute"
                             select="             @*|           gn:attribute[not(@name = parent::node()/@*/name())]">
          <xsl:with-param name="ref" select="gn:element/@ref"/>
          <xsl:with-param name="insertRef" select="gn:element/@ref"/>
        </xsl:apply-templates>
      </xsl:if>
    </xsl:variable>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="$labelConfig"/>
      <xsl:with-param name="name" select="gn:element/@ref"/>
      <xsl:with-param name="value" select="text()"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>

      <!--
          Default field type is Date.

          TODO : Add the capability to edit those elements as:
           * xs:time
           * xs:dateTime
           * xs:anyURI
           * xs:decimal
           * gml:CalDate
          See http://trac.osgeo.org/geonetwork/ticket/661
        -->
      <xsl:with-param name="type"
                      select="if (string-length($value) = 10 or $value = '') then 'date' else 'datetime'"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="mode-iso19139" match="gmd:EX_GeographicBoundingBox" priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig" select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>

    <xsl:variable name="labelVal">
      <xsl:choose>
        <xsl:when test="$overrideLabel != ''">
          <xsl:value-of select="$overrideLabel"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$labelConfig/label"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="requiredClass" select="if ($labelConfig/condition = 'mandatory') then 'gn-required' else ''" />

    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label"
                      select="$labelVal"/>
      <xsl:with-param name="editInfo" select="../gn:element"/>
      <xsl:with-param name="cls" select="concat(local-name(), ' ', $requiredClass)"/>
      <xsl:with-param name="subTreeSnippet">

        <xsl:variable name="identifier"
                      select="../following-sibling::gmd:geographicElement[1]/gmd:EX_GeographicDescription/
                                  gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/(gmx:Anchor|gco:CharacterString)"/>
        <xsl:variable name="description"
                      select="../preceding-sibling::gmd:description/gco:CharacterString"/>
        <xsl:variable name="readonly" select="ancestor-or-self::node()[@xlink:href] != ''"/>

        <div gn-draw-bbox=""
             data-hleft="{gmd:westBoundLongitude/gco:Decimal}"
             data-hright="{gmd:eastBoundLongitude/gco:Decimal}"
             data-hbottom="{gmd:southBoundLatitude/gco:Decimal}"
             data-htop="{gmd:northBoundLatitude/gco:Decimal}"
             data-hleft-ref="_{gmd:westBoundLongitude/gco:Decimal/gn:element/@ref}"
             data-hright-ref="_{gmd:eastBoundLongitude/gco:Decimal/gn:element/@ref}"
             data-hbottom-ref="_{gmd:southBoundLatitude/gco:Decimal/gn:element/@ref}"
             data-htop-ref="_{gmd:northBoundLatitude/gco:Decimal/gn:element/@ref}"
             data-lang="lang"
             data-read-only="{$readonly}">
          <xsl:if test="$identifier and $isFlatMode">
            <xsl:attribute name="data-identifier"
                           select="$identifier"/>
            <xsl:attribute name="data-identifier-ref"
                           select="concat('_', $identifier/gn:element/@ref)"/>
          </xsl:if>
          <xsl:if test="$description and $isFlatMode and not($metadataIsMultilingual)">
            <xsl:attribute name="data-description"
                           select="$description"/>
            <xsl:attribute name="data-description-ref"
                           select="concat('_', $description/gn:element/@ref)"/>
          </xsl:if>
        </div>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="mode-iso19139" match="gmd:EX_BoundingPolygon" priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig" select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>

    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label"
                      select="$labelConfig/label"/>
      <xsl:with-param name="editInfo" select="../gn:element"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="subTreeSnippet">

        <xsl:variable name="geometry">
          <xsl:apply-templates select="gmd:polygon/gml:MultiSurface|gmd:polygon/gml:LineString|
                                       gmd:polygon/gml320:MultiSurface|gmd:polygon/gml320:LineString"
                               mode="gn-element-cleaner"/>
        </xsl:variable>

        <xsl:variable name="identifier"
                      select="concat('_X', gmd:polygon/gn:element/@ref, '_replace')"/>
        <xsl:variable name="readonly" select="ancestor-or-self::node()[@xlink:href] != ''"/>

        <br />
        <gn-bounding-polygon polygon-xml="{saxon:serialize($geometry, 'default-serialize-mode')}"
                             identifier="{$identifier}"
                             read-only="{$readonly}">
        </gn-bounding-polygon>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- In flat mode do not display geographic identifier and description
  because it is part of the map widget - see previous template. -->
  <xsl:template mode="mode-iso19139"
                match="gmd:extent/*/gmd:description[$isFlatMode and not($metadataIsMultilingual)]|
                       gmd:geographicElement[
                          $isFlatMode and
                          preceding-sibling::gmd:geographicElement/gmd:EX_GeographicBoundingBox
                        ]/gmd:EX_GeographicDescription"
                priority="2000"/>


  <!-- Do not display other local declaring also the main language
  which is added automatically by update-fixed-info. -->
  <xsl:template mode="mode-iso19139"
                match="gmd:locale[*/gmd:languageCode/*/@codeListValue =
                                  ../gmd:language/*/@codeListValue]"
                priority="2000"/>
</xsl:stylesheet>
