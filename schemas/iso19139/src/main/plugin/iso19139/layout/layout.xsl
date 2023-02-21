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
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:include href="utility-fn.xsl"/>
  <xsl:include href="utility-tpl.xsl"/>
  <xsl:include href="layout-custom-fields.xsl"/>
  <xsl:include href="layout-custom-fields-date.xsl"/>
  <xsl:include href="layout-custom-tpl.xsl"/>

  <!-- Ignore all gn element -->
  <xsl:template mode="mode-iso19139"
                match="gn:*|@gn:*|@*"
                priority="1000"/>

  <!-- Ignore group element. -->
  <xsl:template mode="mode-iso19139"
                match="gml:*[
                    starts-with(local-name(.), 'TimePeriodTypeGROUP_ELEMENT') or
                    starts-with(local-name(.), 'TimeInstantTypeGROUP_ELEMENT')
                ]|gml320:*[
                    starts-with(local-name(.), 'TimePeriodTypeGROUP_ELEMENT') or
                    starts-with(local-name(.), 'TimeInstantTypeGROUP_ELEMENT')
                ]"
                priority="1000"/>


  <!-- Template to display non existing element ie. geonet:child element
  of the metadocument. Display in editing mode only and if
  the editor mode is not flat mode. -->
  <xsl:template mode="mode-iso19139" match="gn:child" priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="name" select="concat(@prefix, ':', @name)"/>
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(..)"/>
    <xsl:variable name="flatModeException"
                  select="gn-fn-metadata:isFieldFlatModeException($viewConfig, $name,  name(..), $xpath)"/>


    <xsl:if test="$name = 'gmd:descriptiveKeywords' and count(../gmd:descriptiveKeywords) = 0">
      <xsl:call-template name="addAllThesaurus">
        <xsl:with-param name="ref" select="concat('_X', ../gn:element/@ref, '_', replace('gmd:descriptiveKeywords', ':', 'COLON'))"/>
      </xsl:call-template>
    </xsl:if>

    <xsl:if test="$isEditing and
      (not($isFlatMode) or $flatModeException)">

      <xsl:variable name="directive"
                    select="gn-fn-metadata:getFieldAddDirective($editorConfig, $name)"/>
      <xsl:variable name="label"
                    select="gn-fn-metadata:getLabel($schema, $name, $labels, name(..), '', '')"/>


      <xsl:choose>
        <!-- Specifc case when adding a new keyword using the gn-thesaurus-selector
        in a view where descriptiveKeyword is a flat mode exception. In this case
        the "Add keyword" button will add a new descriptiveKeyword block if none exists
        and it will insert a keyword in the first descriptiveKeyword block (not referencing thesaurus)
        ie. free text keyword block.

        The goal here is to avoid to have multiple free text descriptiveKeyword sections.
        -->
        <xsl:when test="$flatModeException and $name = 'gmd:descriptiveKeywords'">
          <xsl:variable name="freeTextKeywordBlocks"
                        select="../gmd:descriptiveKeywords[not(*/gmd:thesaurusName)]"/>
          <xsl:variable name="isFreeTextKeywordBlockExist"
                        select="count($freeTextKeywordBlocks) > 0"/>
          <xsl:variable name="freeTextKeywordTarget"
                        select="if ($isFreeTextKeywordBlockExist) then $freeTextKeywordBlocks[1]/*/gn:child[@name = 'keyword'] else ."/>

          <xsl:variable name="directive" as="node()?">
            <xsl:for-each select="$directive">
              <xsl:copy>
                <xsl:copy-of select="@*"/>
                <directiveAttributes data-freekeyword-element-ref="{$freeTextKeywordTarget/../gn:element/@ref}"
                                     data-freekeyword-element-name="{concat($freeTextKeywordTarget/@prefix, ':', $freeTextKeywordTarget/@name)}">
                  <xsl:copy-of select="directiveAttributes/@*"/>
                </directiveAttributes>
              </xsl:copy>
            </xsl:for-each>
          </xsl:variable>

          <xsl:call-template name="render-element-to-add">
            <xsl:with-param name="label" select="$label/label"/>
            <xsl:with-param name="class" select="if ($label/class) then $label/class else ''"/>
            <xsl:with-param name="btnLabel" select="if ($label/btnLabel) then $label/btnLabel else ''"/>
            <xsl:with-param name="btnClass" select="if ($label/btnClass) then $label/btnClass else ''"/>
            <xsl:with-param name="directive" select="$directive"/>
            <xsl:with-param name="childEditInfo" select="."/>
            <xsl:with-param name="parentEditInfo" select="../gn:element"/>
            <xsl:with-param name="isFirst" select="count(preceding-sibling::*[name() = $name]) = 0"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="render-element-to-add">
            <xsl:with-param name="label" select="$label/label"/>
            <xsl:with-param name="class" select="if ($label/class) then $label/class else ''"/>
            <xsl:with-param name="btnLabel" select="if ($label/btnLabel) then $label/btnLabel else ''"/>
            <xsl:with-param name="btnClass" select="if ($label/btnClass) then $label/btnClass else ''"/>
            <xsl:with-param name="directive" select="$directive"/>
            <xsl:with-param name="childEditInfo" select="."/>
            <xsl:with-param name="parentEditInfo" select="../gn:element"/>
            <xsl:with-param name="isFirst" select="count(preceding-sibling::*[name() = $name]) = 0"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- Visit all XML tree recursively -->
  <xsl:template mode="mode-iso19139" match="gmd:*|gmx:*|gml:*|gml320:*|srv:*|gts:*">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="refToDelete" required="no"/>
    <xsl:param name="overrideLabel" required="no"/>



    <!-- In flat mode, block level may contains
    validation report. Display them when traversing the tree. -->
    <xsl:if test="$isFlatMode">
      <xsl:call-template name="get-errors"/>
    </xsl:if>


    <xsl:apply-templates mode="mode-iso19139" select="*|@*">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="labels" select="$labels"/>
      <xsl:with-param name="refToDelete" select="$refToDelete"/>
      <xsl:with-param name="overrideLabel" select="$overrideLabel"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Boxed element

      Details about the last line :
      * namespace-uri(.) != $gnUri: Only take into account profile's element
      * and $isFlatMode = false(): In flat mode, don't box any
      * and gmd:*: Match all elements having gmd child elements
      * and not(gco:CharacterString): Don't take into account those having gco:CharacterString (eg. multilingual elements)
  -->
  <xsl:template mode="mode-iso19139" priority="200"
                match="*[name() = $editorConfig/editor/fieldsWithFieldset/name
    or @gco:isoType = $editorConfig/editor/fieldsWithFieldset/name]|
      gmd:report/*|
      gmd:result/*|
      gmd:extent[name(..)!='gmd:EX_TemporalExtent']|
      *[namespace-uri(.) != $gnUri and $isFlatMode = false() and gmd:* and not(gco:CharacterString) and not(gmd:URL)]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="refToDelete" required="no"/>
    <xsl:param name="config" required="no"/>


    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:variable name="attributes">
      <!-- Create form for all existing attribute (not in gn namespace)
      and all non existing attributes not already present. -->
      <xsl:apply-templates mode="render-for-field-for-attribute"
                           select="
        @*|
        gn:attribute[not(@name = parent::node()/@*/name())]">
        <xsl:with-param name="ref" select="gn:element/@ref"/>
        <xsl:with-param name="insertRef" select="gn:element/@ref"/>
      </xsl:apply-templates>
    </xsl:variable>

    <xsl:variable name="label" select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>
    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label" select="$label/label"/>
      <xsl:with-param name="editInfo" select="if ($refToDelete) then $refToDelete else gn:element"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="subTreeSnippet">
        <!-- Process child of those element. Propagate schema
        and labels to all subchilds (eg. needed like iso19110 elements
        contains gmd:* child. -->
        <xsl:apply-templates mode="mode-iso19139" select="*">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="labels" select="$labels"/>
        </xsl:apply-templates>
      </xsl:with-param>
      <xsl:with-param name="collapsible"
                      select="if ($config and $config/@collapsible != '')
                              then xs:boolean($config/@collapsible) else true()"/>
      <xsl:with-param name="collapsed"
                      select="if ($config and $config/@collapsed != '')
                              then xs:boolean($config/@collapsed) else false()"/>
    </xsl:call-template>

  </xsl:template>




  <!-- Render simple element which usually match a form field -->
  <xsl:template mode="mode-iso19139" priority="200"
                match="*[gco:CharacterString|gmx:Anchor|gco:Integer|gco:Decimal|
       gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gmx:FileName|
       gco:Scale|gco:Record|gco:RecordType|gmx:MimeFileType|gmd:URL|gco:LocalName|gmd:PT_FreeText|
       gts:TM_PeriodDuration|gml:duration|gml320:duration]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>
    <xsl:param name="refToDelete" required="no"/>
    <xsl:param name="config" required="no"/>

    <xsl:variable name="elementName" select="name()"/>

    <xsl:variable name="excluded"
                  select="gn-fn-iso19139:isNotMultilingualField(., $editorConfig)"/>

    <xsl:variable name="hasPTFreeText"
                  select="count(gmd:PT_FreeText) > 0"/>
    <xsl:variable name="hasOnlyPTFreeText"
                  select="count(gmd:PT_FreeText) > 0 and count(gco:CharacterString|gmx:Anchor) = 0"/>
    <xsl:variable name="isMultilingualElement"
                  select="$metadataIsMultilingual and $excluded = false()"/>
    <xsl:variable name="isMultilingualElementExpanded"
                  select="$isMultilingualElement and count($editorConfig/editor/multilingualFields/expanded[name = $elementName]) > 0"/>

    <!-- For some fields, always display attributes.
    TODO: move to editor config ? -->
    <xsl:variable name="forceDisplayAttributes" select="count(gmx:FileName|gmx:Anchor) > 0"/>

    <!-- TODO: Support gmd:LocalisedCharacterString -->
    <xsl:variable name="monoLingualValue" select="gco:CharacterString|gmx:Anchor|gco:Integer|gco:Decimal|
      gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gmx:FileName|
      gco:Scale|gco:Record|gco:RecordType|gmx:MimeFileType|gmd:URL|gco:LocalName|
       gts:TM_PeriodDuration|gml:duration|gml320:duration"/>
    <xsl:variable name="theElement"
                  select="if ($isMultilingualElement and $hasOnlyPTFreeText or not($monoLingualValue))
                          then gmd:PT_FreeText
                          else $monoLingualValue"/>
    <!--
      This may not work if node context is lost eg. when an element is rendered
      after a selection with copy-of.
      <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>-->
    <xsl:variable name="xpath"
                  select="gn-fn-metadata:getXPathByRef(gn:element/@ref, $metadata, false())"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig"
                  select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>
    <xsl:variable name="helper" select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>

    <xsl:variable name="attributes">

      <!-- Create form for all existing attribute (not in gn namespace)
      and all non existing attributes not already present for the
      current element and its children (eg. @uom in gco:Distance).
      A list of exception is defined in form-builder.xsl#render-for-field-for-attribute. -->
      <xsl:apply-templates mode="render-for-field-for-attribute"
                           select="@*">
        <xsl:with-param name="ref" select="gn:element/@ref"/>
        <xsl:with-param name="insertRef" select="$theElement/gn:element/@ref"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="render-for-field-for-attribute"
                           select="*/@*">
        <xsl:with-param name="ref" select="$theElement/gn:element/@ref"/>
        <xsl:with-param name="insertRef" select="$theElement/gn:element/@ref"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="render-for-field-for-attribute"
                           select="gn:attribute[not(@name = parent::node()/@*/name())]">
        <xsl:with-param name="ref" select="gn:element/@ref"/>
        <xsl:with-param name="insertRef" select="$theElement/gn:element/@ref"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="render-for-field-for-attribute"
                           select="*/gn:attribute[not(@name = parent::node()/@*/name())]">
        <xsl:with-param name="ref" select="$theElement/gn:element/@ref"/>
        <xsl:with-param name="insertRef" select="$theElement/gn:element/@ref"/>
      </xsl:apply-templates>
    </xsl:variable>


    <xsl:variable name="values">
      <xsl:if test="$isMultilingualElement">
        <xsl:variable name="text"
                      select="normalize-space(gco:CharacterString|gmx:Anchor)"/>

        <values>
          <!--
          CharacterString is not edited anymore, but it's PT_FreeText
          counterpart is.

          Or the PT_FreeText element matching the main language
          <xsl:if test="gco:CharacterString">
            <value ref="{$theElement/gn:element/@ref}" lang="{$metadataLanguage}">
              <xsl:value-of select="gco:CharacterString"/>
            </value>
          </xsl:if>-->

          <!-- the existing translation -->
          <xsl:for-each select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
            <value ref="{gn:element/@ref}" lang="{substring-after(@locale, '#')}">
              <xsl:value-of select="."/>
            </value>
          </xsl:for-each>

          <!-- and create field for none translated language -->
          <xsl:for-each select="$metadataOtherLanguages/lang">
            <xsl:variable name="code" select="@code"/>
            <xsl:variable name="currentLanguageId" select="@id"/>
            <xsl:variable name="ptFreeElementDoesNotExist"
                          select="count($theElement/parent::node()/
                                        gmd:PT_FreeText/*/
                                        gmd:LocalisedCharacterString[
                                          @locale = concat('#', $currentLanguageId)]) = 0"/>

            <xsl:choose>
              <xsl:when test="$ptFreeElementDoesNotExist and
                              $text != '' and
                              $code = $metadataLanguage">
                <value ref="lang_{@id}_{$theElement/parent::node()/gn:element/@ref}"
                       lang="{@id}">
                  <xsl:value-of select="$text"/>
                </value>
              </xsl:when>
              <xsl:when test="$ptFreeElementDoesNotExist">
                <value ref="lang_{@id}_{$theElement/parent::node()/gn:element/@ref}"
                       lang="{@id}"></value>
              </xsl:when>
            </xsl:choose>
          </xsl:for-each>
        </values>
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="labelConfig">
      <xsl:choose>
        <xsl:when test="$overrideLabel != ''">
          <element>
            <label><xsl:value-of select="$overrideLabel"/></label>
          </element>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$labelConfig"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>


    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="$labelConfig/*"/>
      <xsl:with-param name="value" select="if ($isMultilingualElement) then $values else *[namespace-uri(.) != $gnUri]"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!--<xsl:with-param name="widget"/>
        <xsl:with-param name="widgetParams"/>-->
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="type"
                      select="if ($config and $config/@use != '')
                              then $config/@use
                              else gn-fn-metadata:getFieldType($editorConfig, name(),
        name($theElement), $xpath)"/>
      <xsl:with-param name="directiveAttributes">
        <xsl:choose>
          <xsl:when test="$config and $config/@use != ''">
            <xsl:element name="directive">
              <xsl:attribute name="data-directive-name" select="$config/@use"/>
              <xsl:copy-of select="$config/directiveAttributes/@*"/>
            </xsl:element>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="gn-fn-metadata:getFieldDirective($editorConfig, name(), name($theElement), $xpath)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
      <xsl:with-param name="name" select="$theElement/gn:element/@ref"/>
      <xsl:with-param name="editInfo" select="$theElement/gn:element"/>
      <xsl:with-param name="parentEditInfo"
                      select="if ($refToDelete) then $refToDelete else gn:element"/>
      <!-- TODO: Handle conditional helper -->
      <xsl:with-param name="listOfValues" select="$helper"/>
      <xsl:with-param name="toggleLang" select="$isMultilingualElementExpanded"/>
      <xsl:with-param name="forceDisplayAttributes" select="$forceDisplayAttributes"/>
      <xsl:with-param name="isFirst"
                      select="count(preceding-sibling::*[name() = $elementName]) = 0"/>
      <!-- Children of an element having an XLink using the directory
      is in readonly mode. Search by reference because this template may be
      called without context eg. render-table. -->
      <xsl:with-param name="isDisabled"
                      select="count($metadata//*[gn:element/@ref = $theElement/gn:element/@ref]/ancestor-or-self::node()[contains(@xlink:href, 'api/registries/entries')]) > 0"/>
    </xsl:call-template>

  </xsl:template>

  <!-- Display UUIDREF attribute with the parent element name
   as read only. The associated resource panel is used to edit
    those values. -->
  <xsl:template mode="mode-iso19139" match="@uuidref" priority="2000">
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(..), $labels)"/>
      <xsl:with-param name="value" select="."/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="type" select="gn-fn-metadata:getFieldType($editorConfig, name(), '', $xpath)"/>
      <xsl:with-param name="name" select="''"/>
      <xsl:with-param name="editInfo" select="../gn:element"/>
      <xsl:with-param name="parentEditInfo" select="../gn:element"/>
      <xsl:with-param name="isDisabled" select="true()"/>
    </xsl:call-template>
  </xsl:template>



  <xsl:template mode="mode-iso19139" match="gco:ScopedName|gco:LocalName">

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(.), $labels)"/>
      <xsl:with-param name="value" select="."/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="gn-fn-metadata:getXPath(.)"/>
      <xsl:with-param name="type" select="gn-fn-metadata:getFieldType($editorConfig, name(), '', $xpath)"/>
      <xsl:with-param name="name" select="gn:element/@ref"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="parentEditInfo" select="../gn:element"/>
    </xsl:call-template>
  </xsl:template>


  <!--
    <xsl:template mode="mode-iso19139" priority="200"
      match="*[gco:Date|gco:DateTime]">
      <xsl:param name="schema" select="$schema" required="no"/>
      <xsl:param name="labels" select="$labels" required="no"/>
      <xsl:param name="editInfo" required="no"/>
      <xsl:param name="parentEditInfo" required="no"/>

      <xsl:variable name="isRequired" as="xs:boolean">
        <xsl:choose>
          <xsl:when
            test="($parentEditInfo and $parentEditInfo/@min = 1 and $parentEditInfo/@max = 1) or
            (not($parentEditInfo) and $editInfo and $editInfo/@min = 1 and $editInfo/@max = 1)">
            <xsl:value-of select="true()"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="false()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="labelConfig"
        select="gn-fn-metadata:getLabel($schema, name(), $labels)"/>

      <div data-gn-date-picker="{gco:Date|gco:DateTime}"
        data-label="{$labelConfig/label}"
        data-element-name="{name(gco:Date|gco:DateTime)}"
        data-element-ref="{concat('_X', gn:element/@ref)}"
        data-required="{$isRequired}"
        data-hide-time="{if ($viewConfig/@hideTimeInCalendar = 'true') then 'true' else 'false'}">
      </div>
    </xsl:template>-->


  <!-- Match codelist values.

  eg.
  <gmd:CI_RoleCode codeList="./resources/codeList.xml#CI_RoleCode" codeListValue="pointOfContact">
    <geonet:element ref="42" parent="41" uuid="gmd:CI_RoleCode_e75c8ec6-b994-4e98-b7c8-ecb48bda3725" min="1" max="1"/>
    <geonet:attribute name="codeList"/>
    <geonet:attribute name="codeListValue"/>
    <geonet:attribute name="codeSpace" add="true"/>

  -->
  <xsl:template mode="mode-iso19139" priority="200" match="*[*/@codeList]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="codelists" select="$iso19139codelists" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>
    <xsl:param name="refToDelete" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="elementName" select="name()"/>
    <xsl:variable name="labelConfig">
      <xsl:choose>
        <xsl:when test="$overrideLabel != ''">
          <element>
            <label><xsl:value-of select="$overrideLabel"/></label>
          </element>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), '', $xpath)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="ref"
                  select="*/gn:element/@ref"/>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig/*"/>
      <xsl:with-param name="value" select="*/@codeListValue"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="type" select="gn-fn-iso19139:getCodeListType(name())"/>
      <xsl:with-param name="name"
                      select="if ($isEditing) then concat(*/gn:element/@ref, '_codeListValue') else ''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo"
                      select="if ($refToDelete) then $refToDelete else gn:element"/>
      <xsl:with-param name="listOfValues"
                      select="gn-fn-metadata:getCodeListValues($schema, name(*[@codeListValue]), $codelists, .)"/>
      <xsl:with-param name="isFirst"
                      select="count(preceding-sibling::*[name() = $elementName]) = 0"/>
      <!-- Children of an element having an XLink using the directory
      is in readonly mode. Search by reference because this template may be
      called without context eg. render-table. -->
      <xsl:with-param name="isDisabled"
                      select="count($metadata//*[gn:element/@ref = $ref]/ancestor-or-self::node()[contains(@xlink:href, 'api/registries/entries')]) > 0"/>
    </xsl:call-template>

  </xsl:template>


  <!--
    Take care of enumerations.

    In the metadocument an enumeration provide the list of possible values:
  <gmd:topicCategory>
    <gmd:MD_TopicCategoryCode>
    <geonet:element ref="69" parent="68" uuid="gmd:MD_TopicCategoryCode_0073afa8-bc8f-4c52-94f3-28d3aa686772" min="1" max="1">
      <geonet:text value="farming"/>
      <geonet:text value="biota"/>
      <geonet:text value="boundaries"/
  -->
  <xsl:template mode="mode-iso19139" match="*[gn:element/gn:text]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="codelists" select="$iso19139codelists" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)" />

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), '', $xpath)"/>
      <xsl:with-param name="value" select="text()"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="type" select="gn-fn-iso19139:getCodeListType(name())"/>
      <xsl:with-param name="name" select="gn:element/@ref"/>
      <xsl:with-param name="editInfo" select="../gn:element"/>
      <xsl:with-param name="listOfValues"
                      select="gn-fn-metadata:getCodeListValues($schema, name(), $codelists, .)"/>
      <xsl:with-param name="xpath" select="$xpath"/>
    </xsl:call-template>
  </xsl:template>


  <!-- the gml element having no child eg. gml:name. -->
  <xsl:template mode="mode-iso19139" priority="100"
                match="gml:*[count(.//gn:element) = 1]|gml320:*[count(.//gn:element) = 1]">
    <xsl:variable name="name" select="name(.)"/>

    <xsl:variable name="labelConfig" select="gn-fn-metadata:getLabel($schema, $name, $labels)"/>
    <xsl:variable name="helper" select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>

    <xsl:variable name="added" select="parent::node()/parent::node()/@gn:addedObj"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig"/>
      <xsl:with-param name="value" select="."/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="type" select="gn-fn-metadata:getFieldType($editorConfig, name(), '', $xpath)"/>
      <xsl:with-param name="name" select="if ($isEditing) then gn:element/@ref else ''"/>
      <xsl:with-param name="editInfo"
                      select="gn:element"/>
      <xsl:with-param name="listOfValues" select="$helper"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template mode="mode-iso19139"
                match="gmd:topicCategory[1]"
                priority="2100">

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig" select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>


    <xsl:variable name="elementName" select="name()"/>

    <xsl:variable name="topicCategories">
      <xsl:value-of select="string-join(
                            ../*[name() = $elementName]/gmd:MD_TopicCategoryCode/text(),
                            ',')"/>
    </xsl:variable>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="$labelConfig"/>
      <xsl:with-param name="value" select="$topicCategories"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="type" select="'data-gn-topiccategory-selector-div'"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="parentEditInfo" select="../gn:element"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Ignore the following topic categories-->
  <xsl:template mode="mode-iso19139"
                match="gmd:topicCategory[
                        preceding-sibling::*[1]/name() = name()]"
                priority="2100"/>

  <xsl:template mode="mode-iso19139"
                match="gn:child[@name = 'topicCategory' and count(../gmd:topicCategory) > 0]"
                priority="2100" />

</xsl:stylesheet>
