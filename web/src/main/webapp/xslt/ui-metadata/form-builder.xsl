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

<xsl:stylesheet xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn="http://www.fao.org/geonetwork" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:java-xsl-util="java:org.fao.geonet.util.XslUtil"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://saxon.sf.net/" version="2.0"
                extension-element-prefixes="saxon" exclude-result-prefixes="#all">
  <!-- Build the form for creating HTML elements. -->

  <xsl:import href="../common/base-variables-metadata.xsl"/>

  <xsl:import href="../common/utility-tpl-metadata.xsl"/>

  <xsl:import href="form-builder-xml.xsl"/>

  <xsl:import href="form-configurator.xsl"/>

  <xsl:import href="menu-fn.xsl"/>

  <xsl:import href="menu-builder.xsl"/>

  <!--
    Render an element with a label and a value
  -->
  <xsl:template name="render-element">
    <xsl:param name="label" as="node()?"/>
    <xsl:param name="value"/>
    <xsl:param name="errors" required="no"/>
    <!-- cls may define custom CSS class in order to activate
    custom widgets on client side -->
    <xsl:param name="cls" required="no" as="xs:string"/>
    <!-- XPath is added as data attribute for client side references
    to get help or inline editing ? -->
    <xsl:param name="xpath" required="no" as="xs:string" select="''"/>

    <!-- For editing -->
    <xsl:param name="name" required="no" as="xs:string" select="generate-id()"/>

    <!-- The input type eg. number, date, datetime, email.
    Can also be a directive name when starting with data-.
    Then additional directiveAttributes may be set and are in the directive parameters.-->
    <xsl:param name="type" required="no" as="xs:string" select="''"/>

    <!-- The AngularJS attribute(s) directive.
    The type parameter contains the directive name. -->
    <xsl:param name="directiveAttributes" required="no" select="''"/>

    <xsl:param name="hidden" required="no" as="xs:boolean" select="false()"/>
    <xsl:param name="editInfo" required="no"/>
    <xsl:param name="parentEditInfo" required="no"/>

    <!-- The fields matching all element attributes.
    Rendered hidden in a block below the input. -->
    <xsl:param name="attributesSnippet" required="no"/>

    <!-- Force displaying attributes even if $isDisplayingAttributes
    global variable is not set to true. This could be useful when the attributes
    are important for the element. eg. gmx:FileName -->
    <xsl:param name="forceDisplayAttributes" required="no" as="xs:boolean" select="false()"/>

    <!-- A list of values - could be an helper list for example. -->
    <xsl:param name="listOfValues" select="''"/>

    <!-- Disable all form fields included in a section based on an XLink.
    It could be relevant to investigate if this check should be done on
    only element potentially using XLink and not all of them.
    This may have performance inpact? -->
    <xsl:param name="isDisabled" select="count(ancestor-or-self::node()[@xlink:href]) > 0"/>

    <!-- Define if the language fields should be displayed
    with the selector or below each other. -->
    <xsl:param name="toggleLang" required="no" as="xs:boolean" select="false()"/>
    <!-- A gn-extra-field class is added to non first element.
    This class could be used to customize style of first or following
    element of same kind. eg. do not display label. -->
    <xsl:param name="isFirst" required="no" as="xs:boolean" select="true()"/>

    <xsl:param name="isReadOnly" required="no" as="xs:boolean" select="false()"/>

    <xsl:variable name="isMultilingual" select="count($value/values) > 0"/>

    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:variable name="elementCondition" select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..),$isoType, $xpath)/condition" />

    <!-- Required status is defined in parent element for
    some profiles like ISO19139. If not set, the element
    editing information is used.
    In view mode, always set to false.
    -->
    <xsl:variable name="isRequired" as="xs:boolean">
      <xsl:choose>
        <xsl:when test="$elementCondition = 'mandatory'">
          <xsl:value-of select="true()"/>
        </xsl:when>
        <xsl:when test="$elementCondition = 'optional'">
          <xsl:value-of select="false()"/>
        </xsl:when>
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


    <!-- The form field identified by the element ref.
            This HTML element should be removed when action remove is called.
        -->
    <xsl:variable name="isDivLevelDirective"
                  select="starts-with($type, 'data-') and ends-with($type, '-div')"/>
    <xsl:choose>
      <xsl:when test="$isDivLevelDirective">
        <div class="form-group gn-field" id="gn-el-{$editInfo/@ref}">
          <!-- The DIV directive MUST populate the 11 slot of space available-->
          <span>
            <xsl:choose>
              <xsl:when test="$isMultilingual">
                <xsl:attribute name="{$type}">
                {
                <xsl:for-each select="$value/values/value">
                  "<xsl:value-of select="@lang" />":
                  {"ref" : "<xsl:value-of select="@ref" />", "value": "<xsl:value-of select="." />"}
                  <xsl:if test="position() != last()">,</xsl:if>
                </xsl:for-each>
                }
                </xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="{$type}" select="$value"/>
              </xsl:otherwise>
            </xsl:choose>

            <xsl:attribute name="data-ref" select="concat('_', $editInfo/@ref)"/>
            <xsl:attribute name="data-parent-ref" select="concat('_', $editInfo/@parent)"/>
            <xsl:attribute name="data-label" select="$label/label"/>
            <xsl:attribute name="data-element-name" select="name()"/>
            <xsl:attribute name="data-required" select="$isRequired"/>

            <xsl:if test="$directiveAttributes instance of node()+">
              <xsl:variable name="node" select="." />

              <xsl:for-each select="$directiveAttributes//attribute::*">
                <xsl:choose>
                  <xsl:when test="starts-with(., 'eval#')">
                    <xsl:attribute name="{name()}">
                      <saxon:call-template name="{concat('evaluate-', $schema)}">
                        <xsl:with-param name="base" select="$node"/>
                        <xsl:with-param name="in" select="concat('/', substring-after(., 'eval#'))"/>
                      </saxon:call-template>
                    </xsl:attribute>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:copy-of select="."/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
            </xsl:if>
          </span>
          <div class="col-sm-1 gn-control">
            <xsl:if test="not($isDisabled)">
              <xsl:call-template name="render-form-field-control-remove">
                <xsl:with-param name="editInfo" select="$editInfo"/>
                <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
                <xsl:with-param name="isRequired" select="$isRequired"/>
              </xsl:call-template>
            </xsl:if>
          </div>
        </div>
        <div class="col-sm-offset-2">
          <xsl:call-template name="get-errors"/>
        </div>
      </xsl:when>
      <xsl:otherwise>

        <div
          class="form-group gn-field gn-{substring-after(name(), ':')} {if ($isRequired) then 'gn-required' else ''} {if ($label/condition) then concat('gn-', $label/condition) else ''} {if ($isFirst) then '' else 'gn-extra-field'}"
          id="gn-el-{$editInfo/@ref}"
          data-gn-field-highlight="">
          <label
            for="gn-field-{$editInfo/@ref}"
            class="col-sm-2 control-label">
            <xsl:value-of select="$label/label"/>
          </label>

          <div class="col-sm-9 col-xs-11 gn-value nopadding-in-table">
            <xsl:if test="$isMultilingual">
              <xsl:attribute name="data-gn-multilingual-field"
                             select="$metadataOtherLanguagesAsJson"/>
              <xsl:attribute name="data-main-language" select="java-xsl-util:iso639_2T_to_iso639_2B($metadataLanguage)"/>
              <xsl:attribute name="data-expanded" select="$toggleLang"/>
            </xsl:if>

            <xsl:variable name="mainLangCode"
                          select="upper-case(java-xsl-util:twoCharLangCode($metadataLanguage, substring($metadataLanguage,0,2)))"/>

            <xsl:choose>
              <xsl:when test="$isMultilingual">

                <xsl:variable name="tooltip"
                              select="concat($schema, '|', name(.), '|', name(..), '|', $xpath)"></xsl:variable>

                <!-- Preserve order of the languages as defined in the record. -->
                <xsl:for-each select="$value/values/value">
                  <xsl:if test="@lang != ''">
                    <xsl:call-template name="render-form-field">
                      <xsl:with-param name="name" select="@ref"/>
                      <xsl:with-param name="lang" select="@lang"/>
                      <xsl:with-param name="value" select="."/>
                      <xsl:with-param name="type" select="$type"/>
                      <xsl:with-param name="directiveAttributes" select="$directiveAttributes"/>
                      <xsl:with-param name="tooltip" select="$tooltip"/>
                      <xsl:with-param name="isRequired" select="$isRequired"/>
                      <xsl:with-param name="isReadOnly" select="$isReadOnly"/>
                      <xsl:with-param name="isDisabled" select="$isDisabled"/>
                      <xsl:with-param name="editInfo" select="$editInfo"/>
                      <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
                      <!--  Helpers can't be provided for all languages
                      <xsl:with-param name="listOfValues" select="$listOfValues"/>
                      -->
                      <xsl:with-param name="checkDirective"
                                      select="upper-case(@lang) = $mainLangCode or normalize-space(@lang) = ''"/>
                    </xsl:call-template>
                  </xsl:if>
                </xsl:for-each>

                <!-- Display the helper for a multilingual field below the field.
                 The helper will be used only to populate the main language.
                 It is recommended to use a thesaurus instead of an helper for
                 multilingual records. -->
                <xsl:if test="count($listOfValues/*) > 0">
                  <xsl:call-template name="render-form-field-helper">
                    <xsl:with-param name="elementRef" select="concat('_', $editInfo/@ref)"/>
                    <!-- The @rel attribute in the helper may define a related field
                    to update. Check the related element of the current element
                    which should be in the sibbling axis. -->
                    <xsl:with-param name="relatedElement"
                                    select="concat('_',
                        following-sibling::*[name() = $listOfValues/@rel]/*[1]/gn:element/@ref)"/>
                    <!-- Related attribute name is based on element name
                    _<element_ref>_<attribute_name>. -->
                    <xsl:with-param name="relatedElementRef"
                                    select="concat('_', $editInfo/@ref, '_', $listOfValues/@relAtt)"/>
                    <xsl:with-param name="dataType" select="$type"/>
                    <xsl:with-param name="listOfValues" select="$listOfValues"/>
                    <xsl:with-param name="tooltip" select="$tooltip"/>
                    <xsl:with-param name="multilingualField" select="true()"/>
                  </xsl:call-template>
                </xsl:if>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="render-form-field">
                  <xsl:with-param name="name" select="$name"/>
                  <xsl:with-param name="value" select="$value"/>
                  <xsl:with-param name="type" select="$type"/>
                  <xsl:with-param name="directiveAttributes" select="$directiveAttributes"/>
                  <xsl:with-param name="tooltip"
                                  select="concat($schema, '|', name(.), '|', name(..), '|', $xpath)"/>
                  <xsl:with-param name="isRequired" select="$isRequired"/>
                  <xsl:with-param name="isDisabled" select="$isDisabled"/>
                  <xsl:with-param name="isReadOnly" select="$isReadOnly"/>
                  <xsl:with-param name="editInfo" select="$editInfo"/>
                  <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
                  <xsl:with-param name="listOfValues" select="$listOfValues"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>


            <xsl:call-template name="render-form-field-control-move">
              <xsl:with-param name="elementEditInfo" select="$parentEditInfo"/>
              <xsl:with-param name="domeElementToMoveRef" select="$editInfo/@ref"/>
            </xsl:call-template>

            <xsl:if test="$attributesSnippet and count($attributesSnippet/*) > 0">
              <xsl:variable name="cssDefaultClass" select="'well well-sm'"/>
              <div class="{$cssDefaultClass}
                {if ($forceDisplayAttributes) then 'gn-attr-mandatory' else 'gn-attr'}
                {if ($isDisplayingAttributes = true() or $forceDisplayAttributes = true()) then '' else 'hidden'}">
                <xsl:attribute name="id">gn-attr-div_<xsl:value-of select="$editInfo/@ref"/></xsl:attribute>
                <xsl:copy-of select="$attributesSnippet"/>

              </div>
            </xsl:if>

            <xsl:call-template name="get-errors"/>

          </div>
          <div class="col-sm-1 col-xs-1 gn-control">
            <xsl:if test="not($isDisabled)">
              <xsl:call-template name="render-form-field-control-remove">
                <xsl:with-param name="editInfo" select="$editInfo"/>
                <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
                <xsl:with-param name="isRequired" select="$isRequired"/>
              </xsl:call-template>
            </xsl:if>
          </div>
        </div>
      </xsl:otherwise>
    </xsl:choose>

    <!-- When building the form with an element having cardinality 0..1,
    add a hidden add action in case the element is removed. If removed,
    the client app take care of displaying this control. -->
    <xsl:if test="$service = 'md.edit' and $parentEditInfo and $parentEditInfo/@min = 0 and $parentEditInfo/@max = 1">
      <xsl:variable name="addDirective" select="gn-fn-metadata:getFieldAddDirective($editorConfig, name())"/>

      <xsl:call-template name="render-element-to-add">
        <xsl:with-param name="label" select="$label/label"/>
        <xsl:with-param name="class" select="if ($label/class) then $label/class else ''"/>
        <xsl:with-param name="btnLabel" select="if ($label/btnLabel) then $label/btnLabel else ''"/>
        <xsl:with-param name="btnClass" select="if ($label/btnClass) then $label/btnClass else ''"/>
        <xsl:with-param name="directive" select="$addDirective"/>
        <xsl:with-param name="childEditInfo" select="$parentEditInfo"/>
        <xsl:with-param name="parentEditInfo" select="../gn:element"/>
        <xsl:with-param name="isFirst" select="false()"/>
        <xsl:with-param name="isHidden" select="true()"/>
        <xsl:with-param name="name" select="name()"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <!--
    Render a boxed element in a fieldset.

    Boxed elements are usualy complex element with children.
    The cardinality may be multiple. In that case the metadocument
    contains details in the genet:element child.

    eg.
    <gmd:contact>
      ...
      <geonet:element
        ref="8" parent="1" uuid="gmd:contact_4c081293-de4f-4231-abdc-88952894711d"
        min="1" max="10000" add="true"/>
  -->
  <xsl:template name="render-boxed-element">
    <xsl:param name="label" as="xs:string"/>
    <xsl:param name="value"/>
    <xsl:param name="errors" required="no"/>
    <xsl:param name="editInfo" required="no"/>
    <!-- The content to put into the box -->
    <xsl:param name="subTreeSnippet" required="yes" as="node()"/>
    <!-- cls may define custom CSS class in order to activate
    custom widgets on client side -->
    <xsl:param name="cls" required="no"/>
    <!-- XPath is added as data attribute for client side references
    to get help or inline editing ? -->
    <xsl:param name="xpath" required="no"/>
    <xsl:param name="attributesSnippet" required="no">
      <null/>
    </xsl:param>
    <xsl:param name="isDisabled" select="ancestor::node()[@xlink:href]"/>
    <xsl:param name="collapsible" select="true()" as="xs:boolean" required="no"/>
    <xsl:param name="collapsed" select="false()" as="xs:boolean" required="no"/>


    <xsl:variable name="hasXlink" select="@xlink:href"/>

    <fieldset id="{concat('gn-el-', if ($editInfo) then $editInfo/@ref else generate-id())}"
              data-gn-field-highlight=""
              class="{if ($hasXlink) then 'gn-has-xlink' else ''} gn-{substring-after(name(), ':')}">

      <legend class="{$cls}"
              data-gn-field-tooltip="{$schema}|{name()}|{name(..)}|">
        <xsl:if test="$collapsible">
          <xsl:attribute name="data-gn-slide-toggle" select="$collapsed"/>
        </xsl:if>
        <!--
         The toggle title is in conflict with the element title
         required for the element tooltip
         and bootstrap set the title attribute an higher priority.
         TODO: Could be improved ?
         title="{{{{'toggleSection' | translate}}}}"
        -->
        <xsl:if test="$xpath and $withXPath">
          <xsl:attribute name="data-gn-xpath" select="$xpath"/>
        </xsl:if>

        <xsl:value-of select="$label"/>&#160;

        <xsl:if test="$editInfo and not($isDisabled)">
          <xsl:call-template name="render-boxed-element-control">
            <xsl:with-param name="editInfo" select="$editInfo"/>
          </xsl:call-template>
        </xsl:if>

        <xsl:if test="$editInfo">
          <xsl:call-template name="render-form-field-control-move">
            <xsl:with-param name="elementEditInfo" select="$editInfo"/>
            <xsl:with-param name="domeElementToMoveRef" select="$editInfo/@ref"/>
          </xsl:call-template>
        </xsl:if>
      </legend>

      <xsl:if test="count($attributesSnippet/*) > 0 and name($attributesSnippet/*[1]) != 'null'">

        <xsl:variable name="hasOnlyAttributes"
                      select="count(*[namespace-uri() != 'http://www.fao.org/geonetwork']) = 0
                              and count(@*) > 0"/>
        <div class="well well-sm gn-attr {if ($isDisplayingAttributes = true() or $hasOnlyAttributes)
                                          then '' else 'hidden'}">
          <xsl:copy-of select="$attributesSnippet"/>
        </div>
      </xsl:if>

      <xsl:call-template name="get-errors"/>

      <xsl:if test="$subTreeSnippet">
        <xsl:copy-of select="$subTreeSnippet"/>
      </xsl:if>
    </fieldset>
  </xsl:template>

  <xsl:template name="render-boxed-element-control">
    <xsl:param name="editInfo"/>

    <a class="btn pull-right"
       data-gn-click-and-spin="remove({$editInfo/@ref}, {$editInfo/@parent})"
       data-gn-field-highlight-remove="{$editInfo/@ref}"
       title="{{{{'deleteFieldSet' | translate}}}}">
      <i class="fa fa-times text-danger"></i>
    </a>
  </xsl:template>


  <!-- Render element based on a template defined in config-editor.xml
  -->
  <xsl:template name="render-element-template-field">
    <xsl:param name="name"/>
    <xsl:param name="template"/>
    <xsl:param name="isExisting"/>
    <xsl:param name="id"/>
    <xsl:param name="xpathFieldId" required="no" select="''"/>
    <xsl:param name="keyValues" required="no"/>
    <xsl:param name="hasAddAction" required="no" select="false()"/>
    <!-- The element to delete or null if no delete action -->
    <xsl:param name="refToDelete" required="no"/>
    <!-- Parameters for custom add directive -->
    <xsl:param name="addDirective" required="no"/>
    <xsl:param name="directiveAttributes" required="no"/>
    <xsl:param name="qname" required="no"/>
    <xsl:param name="parentRef" required="no"/>
    <!-- Label to display if element is missing. The field
    is initialized with a default template. -->
    <xsl:param name="isMissingLabel" required="no"/>
    <xsl:param name="isFirst" required="no" as="xs:boolean" select="true()"/>
    <xsl:param name="isAddAction" required="no" as="xs:boolean" select="false()"/>
    <xsl:param name="class" required="no" as="xs:string?" select="''"/>
    <xsl:param name="btnLabel" required="no" as="xs:string?" select="''"/>
    <xsl:param name="btnClass" required="no" as="xs:string?" select="''"/>

    <xsl:variable name="tagId" select="generate-id()"/>

    <!-- <xsl:message>!render-element-template-field <xsl:copy-of select="$keyValues"/>
        <xsl:value-of select="$name"/>/tpl:
        <xsl:copy-of select="$template"/>/
        <xsl:value-of select="$id"/>/
        <xsl:value-of select="$isExisting"/>/
        <xsl:value-of select="$id"/>
      </xsl:message>-->

    <xsl:variable name="firstFieldKey"
                  select="$template/values/key[position() = 1]/@label"/>

    <div
      class="form-group gn-field gn-{$firstFieldKey} {if ($isFirst) then '' else 'gn-extra-field'} {if ($isAddAction) then 'gn-add-field' else ''} {$class}"
      id="gn-el-{if ($refToDelete) then $refToDelete/@ref else generate-id()}"
      data-gn-field-highlight="">

      <label class="col-sm-2 control-label">
        <xsl:value-of select="$name"/>&#160;
      </label>
      <div class="col-sm-9">
        <!-- Create an empty input to contain the data-gn-field-tooltip
        key which is used to check if an element
        is the first element of its kind in the form. The key for a template
        field is {schemaIdentifier}|{firstTemplateFieldKey} -->
        <input type="hidden"
               data-gn-field-tooltip="{$schema}|{$firstFieldKey}"/>

        <!-- Create a title indicating that the element is missing in the current
        record. A checkbox display the template field to be populated. -->
        <xsl:if test="$isMissingLabel != ''">
          <div class="checkbox">
            <label>
              <input type="checkbox"
                     id="gn-template-unset-{$tagId}"
                     checked="checked"/>
              <xsl:value-of select="$isMissingLabel"/>
            </label>
          </div>
        </xsl:if>
        <div id="{$tagId}">
          <xsl:if test="$isMissingLabel != ''">
            <xsl:attribute name="class" select="'hidden'"/>
          </xsl:if>

          <xsl:if test="$hasAddAction">
            <xsl:choose>
              <xsl:when test="$addDirective != ''">
                <!-- The add directive should take care of building the form
                for adding the element. eg. adding a textarea with an XML snippet
                in.
                The default add action (ie. without directive), trigger add based
                on schema info. It may stop on choices (eg. bbox or polygon for extent)
                TODO: add a textarea and
                use the default XML template defined in the editor configuration.
                -->
                <div>
                  <xsl:attribute name="{$addDirective}"/>
                  <xsl:attribute name="data-dom-id" select="$id"/>
                  <xsl:attribute name="data-element-name" select="$qname"/>
                  <xsl:attribute name="data-element-ref" select="$parentRef"/>
                  <xsl:copy-of select="$directiveAttributes/@*"/>
                </div>
              </xsl:when>
              <xsl:otherwise>

                <xsl:variable name="hasMultipleChoice"
                              select="count($template/snippet) gt 1"/>

                <div class="btn-group" data-gn-template-field-add-button="{$id}">
                  <xsl:if test="$hasMultipleChoice">
                    <xsl:attribute name="data-has-choice">true</xsl:attribute>
                  </xsl:if>

                  <button class="btn btn-default {if ($hasMultipleChoice) then 'dropdown-toggle' else ''}">
                    <xsl:if test="$hasMultipleChoice">
                      <xsl:attribute name="data-toggle">dropdown</xsl:attribute>
                      <xsl:attribute name="aria-haspopup">true</xsl:attribute>
                      <xsl:attribute name="aria-expanded">false</xsl:attribute>
                    </xsl:if>
                    <i class="{if ($btnClass != '') then $btnClass else 'fa fa-plus'}"/>
                    <xsl:if test="$btnLabel != ''">&#160;
                      <span>
                        <xsl:value-of select="$btnLabel"/>
                      </span>
                    </xsl:if>

                    <xsl:if test="$hasMultipleChoice">
                      <span class="caret"></span>
                    </xsl:if>
                  </button>
                  <xsl:if test="$hasMultipleChoice">
                    <!-- A combo with the list of snippet available -->
                    <ul class="dropdown-menu">
                      <xsl:for-each select="$template/snippet">
                        <xsl:variable name="label" select="@label"/>
                        <li><a id="{concat($id, $label)}">
                          <xsl:value-of select="if ($strings/*[name() = $label] != '') then $strings/*[name() = $label] else $label"/>
                        </a></li>
                      </xsl:for-each>
                    </ul>
                  </xsl:if>
                </div>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>


          <xsl:if test="not($addDirective)">
            <div>
              <xsl:if test="$hasAddAction">
                <xsl:attribute name="class">hidden</xsl:attribute>
              </xsl:if>
              <!-- For each template field create an input.
              The directive takes care of setting values. -->
              <div class="gn-multi-field">
                <xsl:for-each select="$template/values/key">
                <div class="row">
                  <xsl:variable name="valueLabelKey" select="@label"/>
                  <xsl:variable name="keyRequired" select="@required"/>
                  <xsl:variable name="helper"
                                select="if ($keyValues) then $keyValues/field[@name = $valueLabelKey]/helper else ''"/>
                  <xsl:variable name="codelist"
                                select="if ($keyValues) then $keyValues/field[@name = $valueLabelKey]/codelist else ''"/>
                  <xsl:variable name="readonly"
                                select="if ($keyValues) then $keyValues/field[@name = $valueLabelKey]/readonly else ''"/>
                  <div class="col-sm-11 gn-field">
                    <!-- Only display label if more than one key to match -->
                    <xsl:if test="count($template/values/key) > 1">
                      <label for="{$id}_{@label}">
                        <!-- if key has an attr required="true"-->
                        <xsl:if test="$keyRequired">
                          <xsl:attribute name="class" select="'gn-required'"/>
                        </xsl:if>
                        <xsl:value-of select="$strings/*[name() = $valueLabelKey]"/>
                      </label>
                    </xsl:if>

                    <xsl:choose>
                      <xsl:when test="@use = 'textarea'">
                        <textarea class="form-control"
                                  data-gn-field-tooltip="{$schema}|{@tooltip}"
                                  id="{$id}_{@label}">
                          <xsl:if test="$readonly = 'true'">
                            <xsl:attribute name="disabled"/>
                          </xsl:if>
                        </textarea>
                      </xsl:when>
                      <xsl:when test="$codelist != ''">
                        <select class="form-control"
                                data-gn-field-tooltip="{$schema}|{@tooltip}"
                                id="{$id}_{@label}">
                          <xsl:if test="$readonly = 'true'">
                            <xsl:attribute name="disabled"/>
                          </xsl:if>
                          <option></option>

                          <xsl:for-each select="$codelist/entry">
                            <xsl:sort select="if ($codelist/@sort = 'fixed') then position() else label"/>
                            <option value="{code}" title="{normalize-space(description)}">
                              <xsl:value-of select="label"/>
                            </option>
                          </xsl:for-each>
                        </select>
                      </xsl:when>
                      <xsl:when test="@use = 'checkbox'">
                        <span class="pull-left">
                          <input type="checkbox"
                                data-gn-field-tooltip="{$schema}|{@tooltip}"
                                id="{$id}_{@label}">
                            <xsl:if test="$readonly = 'true'">
                              <xsl:attribute name="disabled"/>
                            </xsl:if>
                          </input>
                          &#160;
                        </span>
                      </xsl:when>
                      <!-- A directive -->
                      <xsl:when test="@use = 'data-gn-language-picker'">
                        <input class="form-control"
                              value="{value}"
                              data-gn-field-tooltip="{$schema}|{@tooltip}"
                              data-gn-language-picker=""
                              id="{$id}_{@label}"/>
                      </xsl:when>
                      <xsl:when test="@use = 'data-gn-keyword-picker'">
                        <!-- To use this directive in template fields should be provided the following attributes:
                               - data-template-field: true to indicate a template field
                               - data-template-field-value: value of the element
                               - data-template-field-concept-id-value: value for the anchor link
                               - data-template-field-element: usually gco:CharacterString
                               - data-template-field-element-with-concept-id: usually gmx:Anchor

                            Example:

                            <key label="nameOfMeasure"
                                xpath="gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure"
                                use="data-gn-keyword-picker"
                                tooltip="gmd:nameOfMeasure">
                             <directiveAttributes data-thesaurus-key="external.theme.httpinspireeceuropaeumetadatacodelistQualityOfServiceCriteria-QualityOfServiceCriteria"
                                                  data-order-by-id="true"
                                                  data-display-definition="true"
                                                  data-template-field="true"
                                                  data-template-field-element="gco:CharacterString"
                                                  data-template-field-element-with-concept-id="gmx:Anchor"
                                                  data-template-field-value="eval#gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/*/text()"
                                                  data-template-field-concept-id-value="eval#gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/gmx:Anchor/@xlink:href"
                                                  data-thesaurus-concept-id-attribute="xlinkCOLONhref"/>
                           </key>

                           The id of the following element should start with: template_, "real" id associated with
                           the template field: {$id}_{@label} is created inside the directive when using template mode
                        -->
                        <input class="form-control"
                               value="{value}"
                               data-gn-field-tooltip="{$schema}|{@tooltip}"
                               data-gn-keyword-picker=""
                               id="template_{$id}_{@label}">

                          <xsl:for-each select="directiveAttributes/attribute::*">
                            <xsl:variable name="directiveAttributeName" select="name()"/>

                            <xsl:attribute name="{$directiveAttributeName}">
                              <xsl:choose>
                                <xsl:when test="$keyValues and
                                                count($keyValues/field[@name = $valueLabelKey]/
                                  directiveAttributes[@name = $directiveAttributeName]) > 0">
                                  <xsl:value-of select="$keyValues/field[@name = $valueLabelKey]/
                                  directiveAttributes[@name = $directiveAttributeName]/text()"/>
                                </xsl:when>
                                <xsl:when test="starts-with(., 'eval#')">
                                  <!-- Empty value for XPath to evaluate. -->
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:value-of select="."/>
                                </xsl:otherwise>
                              </xsl:choose>
                            </xsl:attribute>
                          </xsl:for-each>
                        </input>
                      </xsl:when>
                      <xsl:when test="starts-with(@use, 'gn-')">
                        <input class="form-control"
                              type="hidden"
                              value="{value}"
                              id="{$id}_{@label}"/>

                        <div data-gn-field-tooltip="{$schema}|{@tooltip}"
                            data-id="#{$id}_{@label}">
                          <xsl:attribute name="data-{@use}">
                            <xsl:value-of
                              select="if ($keyValues) then $keyValues/field[@name = $valueLabelKey]/value else ''"/>
                          </xsl:attribute>
                          <xsl:for-each select="directiveAttributes/attribute::*">
                            <xsl:variable name="directiveAttributeName" select="name()"/>

                            <xsl:attribute name="{$directiveAttributeName}">
                              <xsl:choose>
                                <xsl:when test="$keyValues and
                                                count($keyValues/field[@name = $valueLabelKey]/
                                  directiveAttributes[@name = $directiveAttributeName]) > 0">
                                  <xsl:value-of select="$keyValues/field[@name = $valueLabelKey]/
                                  directiveAttributes[@name = $directiveAttributeName]/text()"/>
                                </xsl:when>
                                <xsl:when test="starts-with(., 'eval#')">
                                  <!-- Empty value for XPath to evaluate. -->
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:value-of select="."/>
                                </xsl:otherwise>
                              </xsl:choose>
                            </xsl:attribute>
                          </xsl:for-each>
                        </div>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:variable name="keyIndex" select="position()"/>
                        <input class="form-control"
                              type="{if (@use) then @use else 'text'}"
                              value="{if ($keyValues) then $keyValues/field[$keyIndex]/value/text() else ''}"
                              id="{$id}_{@label}"
                              data-gn-field-tooltip="{$schema}|{@tooltip}">
                          <xsl:if test="$helper">
                            <!-- hide the form field if helper is available, the
                              value is set by the directive which provide customized
                              forms -->
                            <xsl:attribute name="class" select="'hidden'"/>
                          </xsl:if>
                          <xsl:if test="$readonly = 'true'">
                            <xsl:attribute name="disabled"/>
                          </xsl:if>
                        </input>
                      </xsl:otherwise>
                    </xsl:choose>

                    <xsl:if test="$helper">
                      <xsl:variable name="elementName" select="concat($id, '_', @label)"/>
                      <xsl:call-template name="render-form-field-helper">
                        <xsl:with-param name="elementRef" select="$elementName"/>
                        <xsl:with-param name="relatedElement" select="if ($helper/@rel)
                          then concat($elementName, '_', substring-after($helper/@rel, ':'))
                          else ''"/>
                        <xsl:with-param name="dataType" select="'text'"/>
                        <xsl:with-param name="listOfValues" select="$helper"/>
                        <xsl:with-param name="tooltip" select="concat($schema, '|', @tooltip)"/>
                      </xsl:call-template>
                    </xsl:if>
                  </div>
                  <div class="col-sm-1 gn-control"></div>
                </div>
                </xsl:for-each>
              </div>
              <xsl:if test="not($isExisting)">
                <input class="gn-debug" type="text" name="{$xpathFieldId}" value="{@xpath}"/>
              </xsl:if>

              <xsl:variable name="hasMultipleChoice"
                            select="count($template/snippet) gt 1"/>

              <xsl:if test="$hasMultipleChoice">
                <xsl:for-each select="$template/snippet">
                  <textarea id="{concat($id, @label, '-value')}">
                    <xsl:value-of select="saxon:serialize(*,
                                        'default-serialize-mode')"/>
                  </textarea>
                </xsl:for-each>
              </xsl:if>

              <textarea class="form-control gn-debug"
                        name="{$id}"
                        data-gn-template-field="{$id}"
                        data-keys="{string-join($template/values/key/@label, '$$$')}"
                        data-values="{if ($keyValues and count($keyValues/*) > 0)
                          then string-join($keyValues/field/value, '$$$') else ''}">
                <xsl:if test="$isMissingLabel != ''">
                  <xsl:attribute name="data-not-set-check" select="$tagId"/>
                </xsl:if>
                <xsl:value-of select="saxon:serialize($template/snippet[1]/*,
                                      'default-serialize-mode')"/>
              </textarea>
            </div>
          </xsl:if>
        </div>
      </div>
      <xsl:if test="$refToDelete">
        <div class="col-sm-1 gn-control">
          <xsl:call-template name="render-form-field-control-remove">
            <xsl:with-param name="editInfo" select="$refToDelete"/>
          </xsl:call-template>
        </div>
      </xsl:if>
    </div>
  </xsl:template>

  <!--
  Create form for an element which does not exist in the metadata record.
  childInfo parameter contains schema definition for this element. It could
  be a simple element eg.
  <geonet:child name="hierarchyLevelName"
    prefix="gmd"
    namespace="http://www.isotc211.org/2005/gmd"
    uuid="child_gmd:hierarchyLevelName_051ba253-1ae2-4f88-9337-b344db9f10ff"
    min="0" max="10000" action="replace">

    <geonet:child name="CharacterString"
      prefix="gco"
      namespace="http://www.isotc211.org/2005/gco"
      uuid="child_gco:CharacterString_cb89ca70-ae3d-4f58-898d-6fad3dac3a06"
      min="1" max="1" action="replace"/>
  </geonet:child>

  or a choice eg.
  <geonet:child name="hierarchyLevel"
    prefix="gmd" namespace="http://www.isotc211.org/2005/gmd"
    uuid="child_gmd:hierarchyLevel_21fe2fcc-5e71-4c0f-ab1b-d45a94e2df25"
    min="0" max="10000" action="before">

    <geonet:choose name="gmx:MX_ScopeCode"/>
    <geonet:choose name="gmd:MD_ScopeCode"/>
</geonet:child>

  -->
  <xsl:template name="render-element-to-add">
    <xsl:param name="label" as="xs:string?"/>
    <xsl:param name="directive" as="node()?"/>
    <xsl:param name="childEditInfo"/>
    <!-- If not provided, add element can't define where to add element.
    In such case, the add action is not rendered. eg. in table mode
    elements can't be added in the same col and the parentEditInfo is
    not provided to not render that part of the form. -->
    <xsl:param name="parentEditInfo"/>
    <!-- Hide add element if child of an XLink section. -->
    <xsl:param name="isDisabled" select="ancestor::node()[@xlink:href]"/>
    <xsl:param name="isFirst" required="no" as="xs:boolean" select="true()"/>
    <xsl:param name="isHidden" required="no" as="xs:boolean" select="false()"/>
    <xsl:param name="name" required="no" as="xs:string" select="''"/>
    <xsl:param name="class" required="no" as="xs:string?" select="''"/>
    <xsl:param name="btnLabel" required="no" as="xs:string?" select="''"/>
    <xsl:param name="btnClass" required="no" as="xs:string?" select="''"/>


    <xsl:if test="not($isDisabled) and $parentEditInfo/@ref != ''">
      <xsl:variable name="id" select="generate-id()"/>
      <xsl:variable name="qualifiedName"
                    select="concat($childEditInfo/@prefix, ':', $childEditInfo/@name)"/>
      <xsl:variable name="parentName"
                    select="name(ancestor::*[not(contains(name(), 'CHOICE_ELEMENT'))][1])"/>
      <xsl:variable name="isRequired" select="$childEditInfo/@min = 1"/>

      <!-- This element is replaced by the content received when clicking add -->
      <div
        class="form-group gn-field {if ($isRequired) then 'gn-required' else ''} {if ($isFirst) then '' else 'gn-extra-field'} gn-add-field {if ($isHidden) then 'hidden' else ''} {$class}"
        id="gn-el-{$id}"
        data-gn-cardinality="{$childEditInfo/@min}-{$childEditInfo/@max}"
        data-gn-field-highlight="">
        <label class="col-sm-2 control-label"
               data-gn-field-tooltip="{$schema}|{$qualifiedName}|{$parentName}|">
          <xsl:value-of select="if (normalize-space($label) != '')
                                then $label else '&#160;'"/>
        </label>
        <div class="col-sm-9">

          <xsl:variable name="addDirective" select="$directive/@addDirective != ''"/>

          <xsl:variable name="addActionDom">
            <xsl:choose>
              <!-- When element have different types, provide
                    a list of those types to be selected. The type list
                    is defined by the schema and optionaly overriden by
                    the schema suggestion.

                    TODO: Could be nice to select a type by default - a recommended type

                    If only one choice, make a simple button
              -->
              <xsl:when test="$qualifiedName = 'gfc:code' and $schema='iso19110'">

                <div class="btn-group">
                  <button type="button" class="btn btn-default dropdown-toggle fa fa-plus gn-add" data-toggle="dropdown" title="{$i18n/addA} {$label}">
                    <span/>
                    <span class="caret"/>
                  </button>
                  <ul class="dropdown-menu">
                    <xsl:variable name="name" select="'gco:CharacterString'"/>
                    <xsl:variable name="label" select="gn-fn-metadata:getLabel($schema, $name, $labels)"/>
                    <li title="{$label/description}">
                      <a data-gn-click-and-spin="addChoice({$parentEditInfo/@ref}, '{$qualifiedName}', '{$name}', '{$id}', 'replaceWith');">
                        <xsl:value-of select="$label/label"/>
                      </a>
                    </li>
                    <xsl:variable name="name2" select="'gmx:Anchor'"/>
                    <xsl:variable name="label2" select="gn-fn-metadata:getLabel($schema, $name2, $labels)"/>
                    <li title="{$label2/description}">
                      <a data-gn-click-and-spin="addChoice({$parentEditInfo/@ref}, '{$qualifiedName}', '{$name2}', '{$id}', 'replaceWith');">
                        <xsl:value-of select="$label2/label"/>
                      </a>
                    </li>
                  </ul>
                </div>
              </xsl:when>

              <xsl:when test="count($childEditInfo/gn:choose) = 1">
                <xsl:for-each select="$childEditInfo/gn:choose">
                  <xsl:variable name="label"
                                select="gn-fn-metadata:getLabel($schema, @name, $labels, $parentName, '', '')"/>

                  <a class="btn btn-default"
                     title="{$i18n/addA} {$label/label}"
                     data-gn-click-and-spin="addChoice({$parentEditInfo/@ref}, '{$qualifiedName}', '{@name}', '{$id}', 'replaceWith');">
                    <i type="button"
                       class="{if ($btnClass != '') then $btnClass else 'fa fa-plus'} gn-add"
                       title="{$label/description}">
                    </i>
                    <xsl:if test="$btnLabel != ''">&#160;
                      <span>
                        <xsl:value-of select="$btnLabel"/>
                      </span>
                    </xsl:if>
                  </a>
                </xsl:for-each>
              </xsl:when>
              <!--
                    If many choices, make a dropdown button -->
              <xsl:when test="count($childEditInfo/gn:choose) > 1">
                <div class="btn-group">
                  <button type="button"
                          class="btn btn-default dropdown-toggle {if ($btnClass != '') then $btnClass else 'fa fa-plus'} gn-add"
                          data-toggle="dropdown"
                          title="{$i18n/addA} {$label}">
                    <span/>
                    <xsl:if test="$btnLabel != ''">&#160;
                      <span>
                        <xsl:value-of select="$btnLabel"/>
                      </span>
                    </xsl:if>
                    <span class="caret"/>
                  </button>
                  <ul class="dropdown-menu">
                    <xsl:for-each select="$childEditInfo/gn:choose">
                      <xsl:sort
                        select="gn-fn-metadata:getLabel($schema, @name, $labels, $parentName, '', '')"/>
                      <xsl:variable name="label"
                                    select="gn-fn-metadata:getLabel($schema, @name, $labels, $parentName, '', '')"/>

                      <li title="{$label/description}">
                        <a
                          data-gn-click-and-spin="addChoice({$parentEditInfo/@ref}, '{$qualifiedName}', '{@name}', '{$id}', 'before');">
                          <xsl:value-of select="$label/label"/>
                        </a>
                      </li>
                    </xsl:for-each>
                  </ul>
                </div>
              </xsl:when>
              <xsl:otherwise>
                <!-- Add custom widget to add element.
                  This could be a subtemplate (if one available), or a helper
                  like for projection.
                  The directive is in charge of displaying the default add button if needed.
                -->
                <a class="btn btn-default"
                   title="{$i18n/addA} {$label}"
                   data-gn-click-and-spin="add({$parentEditInfo/@ref}, '{if ($name != '') then $name else concat(@prefix, ':', @name)}', '{$id}', 'before');">
                  <i class="{if ($btnClass != '') then $btnClass else 'fa fa-plus'} gn-add"/>
                  <xsl:if test="$btnLabel != ''">&#160;
                    <span>
                      <xsl:value-of select="$btnLabel"/>
                    </span>
                  </xsl:if>
                </a>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <xsl:choose>
            <xsl:when test="$addDirective">
              <div>
                <xsl:attribute name="{$directive/@addDirective}"/>
                <xsl:attribute name="data-dom-id" select="$id"/>
                <xsl:attribute name="data-element-name" select="$qualifiedName"/>
                <xsl:attribute name="data-element-ref" select="$parentEditInfo/@ref"/>
                <xsl:choose>
                  <xsl:when test="$directive/directiveAttributes">
                    <xsl:for-each select="$directive/directiveAttributes/@*">
                      <xsl:choose>
                        <xsl:when test="starts-with(., 'xpath::')">
                          <xsl:variable name="xpath" select="substring-after(., 'xpath::')"/>


                          <xsl:attribute name="{name(.)}">
                            <saxon:call-template name="{concat('evaluate-', $schema)}">
                              <xsl:with-param name="base" select="$metadata//*[gn:element/@ref = $parentEditInfo/@ref]"/>
                              <xsl:with-param name="in"
                                              select="concat('/../', $xpath)"/>
                            </saxon:call-template>
                          </xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:copy-of select="."/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:copy-of
                      select="gn-fn-metadata:getFieldAddDirectiveAttributes($editorConfig,
                                    $qualifiedName)"/>
                  </xsl:otherwise>
                </xsl:choose>
                <xsl:copy-of select="$addActionDom"/>
              </div>
            </xsl:when>
            <xsl:otherwise>
              <xsl:copy-of select="$addActionDom"/>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </div>
    </xsl:if>
    <xsl:call-template name="get-errors-for-child"/>

  </xsl:template>

  <!-- Create a form field ie. a textarea, an input, a select, ...

    This could be a directive which take care of rendering form elements
    See type parameter.

    -->
  <xsl:template name="render-form-field">
    <xsl:param name="name"/>
    <xsl:param name="value"/>
    <xsl:param name="lang" required="no"/>
    <xsl:param name="hidden"/>
    <xsl:param name="type"/>
    <xsl:param name="directiveAttributes"/>
    <xsl:param name="tooltip" required="no"/>
    <xsl:param name="isRequired"/>
    <xsl:param name="isDisabled"/>
    <xsl:param name="isReadOnly"/>
    <xsl:param name="editInfo"/>
    <xsl:param name="parentEditInfo"/>
    <xsl:param name="checkDirective" select="$isRequired"/>
    <!--
        May contain a codelist or a helper list.
        -->
    <xsl:param name="listOfValues" select="''"/>

    <!-- Get variable from attribute (eg. codelist) or node (eg. gco:CharacterString).-->
    <xsl:variable name="valueToEdit"
                  select="if ($value/*) then $value/text() else $value"/>
    <!-- If a form field has suggestion list in helper
    then the element is hidden and the helper directive is added.
    ListOfValues could be a codelist (with entry children) or
    helper (with option).
    -->
    <xsl:variable name="hasHelper" select="$listOfValues and count($listOfValues/option) > 0"/>

    <xsl:choose>
      <xsl:when test="$type = 'textarea'">
        <textarea class="form-control {if ($lang) then 'hidden' else ''}"
                  id="gn-field-{$editInfo/@ref}" name="_{$name}"
                  data-gn-autogrow="">
          <xsl:if test="$isRequired">
            <xsl:attribute name="required" select="'required'"/>
          </xsl:if>
          <xsl:if test="$isDisabled">
            <xsl:attribute name="disabled" select="'disabled'"/>
          </xsl:if>
          <xsl:if test="$isReadOnly">
            <xsl:attribute name="readonly" select="'readonly'"/>
          </xsl:if>
          <xsl:if test="$tooltip">
            <xsl:attribute name="data-gn-field-tooltip" select="$tooltip"/>
          </xsl:if>
          <xsl:if test="$lang">
            <xsl:attribute name="lang" select="$lang"/>
          </xsl:if>
          <xsl:if test="$hidden or ($hasHelper and not($isDisabled))">
            <xsl:attribute name="class" select="'hidden'"/>
          </xsl:if>
          <xsl:value-of select="$valueToEdit"/>
        </textarea>
      </xsl:when>
      <xsl:when test="$type = 'select'">

        <!-- Codelist could be displayed using 2 modes:
        1) simple combo box with list of values from codelist.xml.
        If the record value is not in the codelist.xml, an extra element
        is created with this value in order to preserve it.

        2) radio more.
        -->
        <xsl:choose>
          <xsl:when test="$listOfValues/@editorMode = 'radio'">

            <xsl:for-each select="$listOfValues/entry">
              <xsl:sort select="if ($listOfValues/@sort = 'fixed') then position() else label"/>

              <div class="radio row">
                <div class="col-xs-12">
                  <label title="{normalize-space(description)}">
                    <input type="radio"
                           name="_{$name}"
                           value="{code}">
                      <xsl:if test="$valueToEdit = code">
                        <xsl:attribute name="checked"/>
                      </xsl:if>
                    </input>
                    <xsl:value-of select="label"/>
                  </label>
                </div>
              </div>

            </xsl:for-each>
            <!-- Add the value if not defined in the codelist to not lose it
               -->
            <xsl:if test="count($listOfValues/entry[code = $valueToEdit]) = 0">

              <div class="radio row">
                <div class="col-xs-12">
                  <label>
                    <input type="radio"
                           name="_{$name}"
                           value="{$valueToEdit}"
                           checked="checked"/>
                    <xsl:value-of select="$valueToEdit"/>
                  </label>
                </div>
              </div>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="elementRef" select="$editInfo/@ref"/>

            <xsl:call-template name="render-codelist-as-select">
              <xsl:with-param name="listOfValues" select="$listOfValues"/>
              <xsl:with-param name="lang" select="$lang"/>
              <xsl:with-param name="isDisabled" select="$isDisabled"/>
              <xsl:with-param name="elementRef" select="$elementRef"/>
              <xsl:with-param name="isRequired" select="$isRequired"/>
              <xsl:with-param name="hidden" select="$hidden"/>
              <xsl:with-param name="valueToEdit" select="$valueToEdit"/>
              <xsl:with-param name="name" select="$name"/>
              <xsl:with-param name="tooltip" select="$tooltip"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$type = 'checkbox'">
        <!-- Checkbox field is composed of an
        hidden input to host the checked or unchecked state
        and a checkbox which updates the hidden field.
        If only a checkbox is used, unchecked state is not post
        by forms.
        -->
        <xsl:variable name="elementId" select="concat('gn-field-', $editInfo/@ref)"/>
        <input
          id="{$elementId}"
          name="_{$name}"
          type="hidden"
          value="{$valueToEdit}"/>
        <input class=""
               onclick="$('#{$elementId}').val(this.checked)"
               type="checkbox">
          <xsl:if test="$valueToEdit = 'true'">
            <xsl:attribute name="checked">checked</xsl:attribute>
          </xsl:if>
          <xsl:if test="$tooltip">
            <xsl:attribute name="data-gn-field-tooltip" select="$tooltip"/>
          </xsl:if>
        </input>
      </xsl:when>
      <xsl:otherwise>

        <xsl:variable name="isDirective" select="starts-with($type, 'data-')"/>
        <!-- Some directives needs to support values having carriage return in the
        value. In that case a textarea field should be used with the value in it. -->
        <xsl:variable name="isTextareaDirective"
                      select="$isDirective and contains($type, '-textarea')"/>

        <!-- TODO: Standardize the naming to use div container, currently used for data-gn-checkbox-with-nilreason -->
        <xsl:variable name="isDivDirective"
                      select="$isDirective and contains($type, '-checkbox')"/>

        <xsl:variable name="contentSnippet">
          <xsl:element name="{if ($isDivDirective) then 'div' else if ($isTextareaDirective) then 'textarea' else 'input'}">

            <xsl:attribute name="class"
                           select="concat(if ($isDivDirective) then '' else 'form-control ', if ($lang) then 'hidden' else '')"/>

            <xsl:attribute name="id"
                           select="concat('gn-field-', $editInfo/@ref)"/>

            <xsl:attribute name="name"
                           select="concat('_', $name)"/>

            <xsl:if test="$isDirective">
             <xsl:attribute name="data-element-ref"
                             select="concat('_X', $editInfo/@parent, '_replace')"/>

              <xsl:attribute name="{$type}">
                <xsl:value-of
                  select="normalize-space($valueToEdit)"/>
              </xsl:attribute>

              <xsl:if test="$directiveAttributes instance of node()+">

                <xsl:variable name="node" select="." />

                <xsl:for-each select="$directiveAttributes//attribute::*">
                  <xsl:choose>
                    <xsl:when test="starts-with(., 'eval#')">
                      <xsl:attribute name="{name()}">
                        <saxon:call-template name="{concat('evaluate-', $schema)}">
                          <xsl:with-param name="base" select="$node"/>
                          <xsl:with-param name="in" select="concat('/', substring-after(., 'eval#'))"/>
                        </saxon:call-template>
                      </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:copy-of select="."/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:for-each>

              </xsl:if>
            </xsl:if>
            <xsl:if test="$tooltip">
              <xsl:attribute name="data-gn-field-tooltip" select="$tooltip"/>
            </xsl:if>
            <xsl:if test="$isRequired">
              <xsl:attribute name="required" select="'required'"/>
            </xsl:if>
            <xsl:if test="$checkDirective">
              <xsl:attribute name="data-gn-check" select="concat('#gn-el-', $editInfo/@ref)"/>
            </xsl:if>
            <xsl:if test="$isDisabled">
              <xsl:attribute name="disabled" select="'disabled'"/>
            </xsl:if>
            <xsl:if test="$isReadOnly">
              <xsl:attribute name="readonly" select="'readonly'"/>
            </xsl:if>

            <xsl:if test="$lang">
              <xsl:attribute name="lang" select="$lang"/>
            </xsl:if>
            <xsl:if test="$hidden or ($hasHelper and not($isDisabled))">
              <!-- hide the form field if helper is available, the
              value is set by the directive which provide customized
              forms -->
              <xsl:attribute name="class" select="'hidden'"/>
            </xsl:if>
            <xsl:choose>
              <xsl:when test="$isTextareaDirective">
                <xsl:value-of select="$valueToEdit"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:if test="$type != ''">
                  <xsl:attribute name="type" select="if ($isDirective) then 'text' else $type"/>
                </xsl:if>
                <xsl:attribute name="value"
                               select="normalize-space($valueToEdit)"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>
        </xsl:variable>

        <xsl:copy-of select="$contentSnippet"/>

      </xsl:otherwise>
    </xsl:choose>

    <!--
        Create an helper list for the current input element.
        Current input could be an element or an attribute (eg. uom).
        -->
    <xsl:if test="$hasHelper and not($isDisabled)">

      <xsl:call-template name="render-form-field-helper">
        <xsl:with-param name="elementRef" select="concat('_', $editInfo/@ref)"/>
        <!-- The @rel attribute in the helper may define a related field
        to update. Check the related element of the current element
        which should be in the sibbling axis. -->
        <xsl:with-param name="relatedElement"
                        select="concat('_',
                        following-sibling::*[name() = $listOfValues/@rel]/*/gn:element/@ref)"/>
        <!-- Related attribute name is based on element name
        _<element_ref>_<attribute_name>. -->
        <xsl:with-param name="relatedElementRef"
                        select="concat('_', $editInfo/@ref, '_', $listOfValues/@relAtt)"/>
        <xsl:with-param name="dataType" select="$type"/>
        <xsl:with-param name="listOfValues" select="$listOfValues"/>
        <xsl:with-param name="tooltip" select="$tooltip"/>
      </xsl:call-template>
    </xsl:if>

  </xsl:template>


  <xsl:template name="render-codelist-as-select">
    <xsl:param name="listOfValues"/>
    <xsl:param name="lang"/>
    <xsl:param name="isDisabled"/>
    <xsl:param name="elementRef"/>
    <xsl:param name="isRequired"/>
    <xsl:param name="hidden"/>
    <xsl:param name="valueToEdit"/>
    <xsl:param name="name"/>
    <xsl:param name="tooltip"/>
    <select class="" id="gn-field-{$elementRef}" name="_{$name}">
      <xsl:if test="$isRequired">
        <xsl:attribute name="required" select="'required'"/>
      </xsl:if>
      <xsl:if test="$isDisabled">
        <xsl:attribute name="disabled" select="'disabled'"/>
      </xsl:if>
      <xsl:if test="$tooltip">
        <xsl:attribute name="data-gn-field-tooltip" select="$tooltip"/>
      </xsl:if>
      <xsl:if test="$lang">
        <xsl:attribute name="lang" select="$lang"/>
      </xsl:if>
      <xsl:if test="$hidden">
        <xsl:attribute name="display" select="'none'"/>
      </xsl:if>
      <xsl:for-each select="$listOfValues/entry">
        <xsl:sort select="if ($listOfValues/@sort = 'fixed') then position() else label"/>

        <option value="{code}" title="{normalize-space(description)}">
          <xsl:if test="code = $valueToEdit">
            <xsl:attribute name="selected"/>
          </xsl:if>
          <xsl:value-of select="label"/>
        </option>
      </xsl:for-each>
      <!-- Add the value if not defined in the codelist to not lose it
         -->
      <xsl:if test="count($listOfValues/entry[code = $valueToEdit]) = 0">
        <option value="{$valueToEdit}" selected="selected">
          <xsl:value-of select="$valueToEdit"/>
        </option>
      </xsl:if>
    </select>
  </xsl:template>


  <xsl:template name="render-form-field-helper">
    <xsl:param name="elementRef" as="xs:string"/>
    <xsl:param name="relatedElement" as="xs:string" required="no" select="''"/>
    <xsl:param name="relatedElementRef" as="xs:string" required="no" select="''"/>
    <xsl:param name="dataType" as="xs:string" required="no" select="'text'"/>
    <xsl:param name="listOfValues" as="node()"/>
    <xsl:param name="tooltip" as="xs:string" required="no" select="''"/>
    <xsl:param name="multilingualField" as="xs:boolean" required="no" select="false()"/>

    <!--
    The helper config to pass to the directive in JSON format
    -->
    <textarea id="{$elementRef}_config" class="hidden">
      <xsl:copy-of select="java-xsl-util:xmlToJson(
        saxon:serialize($listOfValues, 'default-serialize-mode'))"/>
    </textarea>
    <div
      data-gn-editor-helper="{$listOfValues/@editorMode}"
      data-ref="{$elementRef}"
      data-type="{$dataType}"
      data-related-element="{if ($listOfValues/@rel != '')
      then $relatedElement else ''}"
      data-related-attr="{if ($listOfValues/@relAtt)
      then $relatedElementRef else ''}"
      data-tooltip="{$tooltip}"
      data-multilingual-field="{$multilingualField}">
    </div>
  </xsl:template>

  <!-- Display the remove control
  if parent info is not defined and element is not
  mandatory.
  -->
  <xsl:template name="render-form-field-control-remove">
    <xsl:param name="editInfo"/>
    <xsl:param name="parentEditInfo" required="no"/>
    <xsl:param name="isRequired" required="no"/>

    <xsl:if
      test="(($parentEditInfo and (
              $parentEditInfo/@del = 'true' or
              $parentEditInfo/@min != 1)
            ) or (
              not($parentEditInfo) and $editInfo and (
                $editInfo/@del = 'true' or
                $editInfo/@min != 1
              )
            )) and not($isRequired and ($editInfo and $editInfo/@max and $editInfo/@max = 1) and ($parentEditInfo and $parentEditInfo/@max and $parentEditInfo/@max = 1))">

      <xsl:variable name="elementToRemove" select="if ($parentEditInfo) then
        $parentEditInfo else $editInfo"/>

      <a class="btn pull-right"
         data-gn-click-and-spin="remove({$elementToRemove/@ref}, {$elementToRemove/@parent}, {$editInfo/@ref})"
         data-gn-field-highlight-remove="{$editInfo/@ref}"
         data-toggle="tooltip" data-placement="top" title="{{{{'deleteField' | translate}}}}">
        <i class="fa fa-times text-danger gn-control"/>
      </a>
    </xsl:if>
  </xsl:template>


  <xsl:template name="render-form-field-control-add">
    <xsl:param name="name"/>
    <xsl:param name="isRequired"/>
    <xsl:param name="editInfo"/>
    <xsl:param name="parentEditInfo"/>


    <!-- Add icon for last element of its kind -->
    <xsl:if test="$parentEditInfo and $parentEditInfo/@add = 'true' and not($parentEditInfo/@down)">
      <a class="btn btn-default"
         title="{$i18n/addA} {$name}"
         data-gn-click-and-spin="add({$parentEditInfo/@parent}, '{$name}', {$editInfo/@ref})">
        <i class="fa fa-plus gn-add"/>
      </a>
    </xsl:if>
  </xsl:template>


  <!-- Template to render up and down control. -->
  <xsl:template name="render-form-field-control-move">
    <xsl:param name="elementEditInfo"/>
    <xsl:param name="domeElementToMoveRef" required="no" select="''"/>

    <xsl:if test="not($viewConfig/@upAndDownControlHidden)">
      <div class="gn-move">
        <xsl:variable name="elementToMoveRef"
                      select="if ($elementEditInfo) then $elementEditInfo/@ref else ''"/>
        <a
          class="fa fa-angle-up {if ($elementEditInfo and $elementEditInfo/@up = 'true') then '' else 'hidden'}"
          data-gn-editor-control-move="{$elementToMoveRef}"
          data-domelement-to-move="{$domeElementToMoveRef}"
          data-direction="up" href="" tabindex="-1"></a>
        <a
          class="fa fa-angle-down {if ($elementEditInfo and $elementEditInfo/@down = 'true') then '' else 'hidden'}"
          data-gn-editor-control-move="{$elementToMoveRef}"
          data-domelement-to-move="{$domeElementToMoveRef}"
          data-direction="down" href="" tabindex="-1"></a>
      </div>
    </xsl:if>
  </xsl:template>


  <!--
    Render attribute as select list or simple output
  -->
  <xsl:template mode="render-for-field-for-attribute" match="@*">
    <xsl:param name="ref"/>
    <xsl:param name="class" select="''"/>

    <xsl:variable name="attributeName" select="name()"/>
    <xsl:variable name="attributeValue" select="."/>
    <xsl:variable name="attributeSpec" select="../gn:attribute[@name = $attributeName]"/>

    <xsl:variable name="attributeKey"
                  select="concat(name(..), '/@', name())"/>
    <xsl:variable name="directive"
                  select="gn-fn-metadata:getAttributeFieldType($editorConfig, $attributeKey)"/>
    <xsl:variable name="directiveAttributes"
                  select="$editorConfig/editor/fields/for[
                            @name = $attributeKey and @use = $directive]
                              /directiveAttributes/@*"/>

    <!-- Form field name escaping ":" which will be invalid character for
    Jeeves request parameters. -->
    <xsl:variable name="fieldName"
                  select="concat('_', $ref, '_', replace($attributeName, ':', 'COLON'))"/>


    <div class="form-group {$class} gn-attr-{replace($attributeName, ':', '_')}" id="gn-attr-{$fieldName}">
      <label class="col-sm-4">
        <xsl:if test="$attributeName = 'xlink:href'">
          <i class="fa fa-link fa-fw"/>
        </xsl:if>
        <xsl:value-of select="gn-fn-metadata:getLabel($schema, $attributeName, $labels, name(..), '', gn-fn-metadata:getXPath(.))/label"/>
      </label>
      <div class="col-sm-7">
        <xsl:variable name="isDivLevelDirective"
                      select="$directive = 'data-gn-logo-picker'"/>
        <xsl:if test="$directive and $isDivLevelDirective">
          <xsl:attribute name="{$directive}"/>
          <xsl:copy-of select="$directiveAttributes"/>
        </xsl:if>

        <xsl:choose>
          <xsl:when test="$attributeSpec/gn:text">
            <xsl:variable name="attributeCodeList"
                          select="gn-fn-metadata:getCodeListValues($schema, $attributeName, $codelists)"/>

            <select class="" name="{$fieldName}">
              <xsl:if test="$directive and not($isDivLevelDirective)">
                <xsl:attribute name="{$directive}"/>
                <xsl:copy-of select="$directiveAttributes"/>
              </xsl:if>

              <xsl:for-each select="$attributeSpec/gn:text">
                <xsl:variable name="optionValue" select="@value"/>

                <!-- Check if a translation is available for the attribute value -->
                <xsl:variable name="label"
                              select="$attributeCodeList/entry[code = $optionValue]/label"/>

                <option value="{$optionValue}">
                  <xsl:if test="$optionValue = $attributeValue">
                    <xsl:attribute name="selected"/>
                  </xsl:if>
                  <xsl:value-of select="if ($label) then $label else $optionValue"/>
                </option>
              </xsl:for-each>


              <xsl:if test="count($attributeSpec/gn:text[@value = $attributeValue]) = 0">
                <option value="{$attributeValue}" selected="">
                  <xsl:value-of select="$attributeValue"/>
                </option>

              </xsl:if>
            </select>
          </xsl:when>
          <xsl:otherwise>
            <input type="text" class="" name="{$fieldName}" value="{$attributeValue}">
              <xsl:if test="$directive and not($isDivLevelDirective)">
                <xsl:attribute name="{$directive}"/>
                <xsl:copy-of select="$directiveAttributes"/>
              </xsl:if>
            </input>
          </xsl:otherwise>
        </xsl:choose>
      </div>
      <div class="col-sm-1">
        <a class="btn pull-right"
           data-gn-click-and-spin="removeAttribute('{$fieldName}')" data-toggle="tooltip"
           data-placement="top" title="{{{{'deleteField' | translate}}}}">
          <i class="fa fa-times text-danger"></i>
        </a>
      </div>
    </div>
  </xsl:template>


  <!--
  Ignore some internal attributes and do not allow to apply this mode
  to a node (only for gn:attribute, see next template).
  -->
  <xsl:template mode="render-for-field-for-attribute"
                match="@gn:addedObj|@xsi:type|
          @min|@max|@name|@del|@add|@id|@uuid|@ref|@parent|@up|@down" priority="2"/>

  <!--
    Add attribute control
  <geonet:attribute
                  name="gco:nilReason"
                  add="true">
                  <geonet:text value="inapplicable"/>
                  <geonet:text value="missing"/>
                  ...


   TODO: externalize exception ?
  -->
  <xsl:template mode="render-for-field-for-attribute"
                match="gn:attribute[not(@name = ('ref', 'parent', 'id', 'uuid', 'type', 'uuidref',
    'xlink:show', 'xlink:actuate', 'xlink:arcrole', 'xlink:role', 'xlink:title', 'xlink:href'))]"
                priority="4">
    <xsl:param name="ref"/>
    <xsl:param name="insertRef" select="''"/>

    <xsl:variable name="attributeLabel" select="gn-fn-metadata:getLabel($schema, @name, $labels)"/>
    <xsl:variable name="fieldName"
                  select="concat(replace(@name, ':', 'COLON'), '_', $insertRef)"/>
    <button type="button" class="btn btn-default btn-xs btn-attr btn-xs gn-attr-{replace(@name, ':', '_')}"
            id="gn-attr-add-button-{$fieldName}"
            data-gn-click-and-spin="add('{$ref}', '{@name}', '{$insertRef}', null, true)"
            title="{$attributeLabel/description}">
      <i class="fa fa-plus fa-fw"/>
      <xsl:value-of select="$attributeLabel/label"/>
    </button>
  </xsl:template>


  <!-- Render batch process directive action -->
  <xsl:template name="render-batch-process-button">
    <xsl:param name="process-label-key"/>
    <xsl:param name="process-name"/>
    <xsl:param name="process-params"/>
    <xsl:param name="btnClass" required="no"/>

    <div class="row form-group gn-field gn-extra-field gn-process-{$process-name}">
      <div class="col-xs-10 col-xs-offset-2">
        <span data-gn-batch-process-button="{$process-name}"
              data-params="{$process-params}"
              data-icon="{$btnClass}"
              data-name="{normalize-space($strings/*[name() = $process-label-key])}"
              data-help="{normalize-space($strings/*[name() = concat($process-name, 'Help')])}"/>
      </div>
    </div>
  </xsl:template>

  <!-- Render suggest directive action -->
  <xsl:template name="render-suggest-button">
    <xsl:param name="process-name"/>
    <xsl:param name="process-params"/>
    <xsl:param name="btnClass" required="no"/>

    <div class="row form-group gn-field gn-extra-field gn-process-{$process-name}">
      <div class="col-xs-10 col-xs-offset-2">
        <span data-gn-suggest-button="{$process-name}"
              data-params="{$process-params}"
              data-icon="{$btnClass}"
              data-name="{normalize-space($strings/*[name() = $process-name])}"
              data-help="{normalize-space($strings/*[name() = concat($process-name, 'Help')])}"/>
      </div>
    </div>
  </xsl:template>


  <!-- Render associated resource action -->
  <xsl:template name="render-associated-resource-button">
    <xsl:param name="type"/>
    <xsl:param name="options"/>
    <xsl:param name="label"/>

    <div class="row form-group gn-field gn-extra-field">
      <div class="col-xs-10 col-xs-offset-2">
        <a class="btn gn-associated-resource-btn"
           data-ng-click="gnOnlinesrc.onOpenPopup('{$type}'{if ($options != '') then concat(', ''', $options, '''') else ''})">
          <i class="fa gn-icon-{$type}"></i>&#160;
          <span data-translate="">
            <xsl:choose>
              <xsl:when test="$label">
                <xsl:value-of select="$label"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$type"/>
              </xsl:otherwise>
            </xsl:choose>
          </span>
        </a>
      </div>
    </div>
  </xsl:template>



  <!-- Create a table based on the values param which contains a table structure.

  values structure should be:
  <header>
   <col>colName</col>
   <col..
  </header>
  <row (title='')>
   <col (readonly='')
        (title='xs:string')
        (colspan='xs:int')
        (type='textarea|Real|Integer|Percentage')>
     text value for readonly cols or element for editing.
   </col>
   <col remove=''>
     Element containing gn:element containing removal config.
   </col>
   <col...
  </row>
  <row...

  -->
  <xsl:template name="render-table">
    <xsl:param name="values" as="node()"/>
    <xsl:param name="addControl" as="node()?"/>

    <table class="table table-striped gn-table">
      <xsl:for-each select="$values/header">
        <thead>
          <tr>
            <xsl:for-each select="col">
              <th>
                <xsl:if test="@class">
                  <xsl:attribute name="class" select="@class"/>
                </xsl:if>
                <div class="th-inner ">
                  <xsl:value-of select="."/>
                </div>
              </th>
            </xsl:for-each>
          </tr>
        </thead>
      </xsl:for-each>
      <tbody>
        <xsl:for-each select="$values/row">
          <tr id="gn-el-{col[@remove]/gn:element/@ref}">
            <xsl:if test="@title != ''">
              <xsl:attribute name="title" select="@title"/>
            </xsl:if>
            <xsl:for-each select="col">
              <td>
                <xsl:if test="@colspan">
                  <xsl:attribute name="colspan" select="@colspan"/>
                </xsl:if>
                <xsl:if test="@class">
                  <xsl:attribute name="class" select="@class"/>
                </xsl:if>
                <xsl:if test="@title">
                  <xsl:attribute name="title" select="@title"/>
                </xsl:if>
                <xsl:if test="@withLabel">
                  <xsl:attribute name="class" select="'gn-table-label'"/>
                </xsl:if>

                <xsl:variable name="ref"
                              select="*/gn:element/@ref"/>

                <!-- TODO: Add move up/down control? -->
                <xsl:choose>
                  <xsl:when test="@remove">
                    <xsl:call-template name="render-form-field-control-remove">
                      <xsl:with-param name="editInfo" select="gn:element"/>
                    </xsl:call-template>
                  </xsl:when>
                  <!-- Form is inserted directly in the row. -->
                  <xsl:when test="@type = 'form'">
                   <xsl:copy-of select="*"/>
                  </xsl:when>
                  <xsl:when test="@readonly">
                    <xsl:value-of select="."/>
                  </xsl:when>
                  <xsl:when test="count(*) = 0">
                    <!-- Empty col -->
                  </xsl:when>
                  <xsl:otherwise>

                    <!-- Children of an element having an XLink using the directory
                                is in readonly mode. -->
                    <xsl:variable name="isReadonlyDueToXlink"
                                  select="count($metadata//*[gn:element/@ref = $ref]/ancestor-or-self::node()[contains(@xlink:href, 'api/registries/entries')]) > 0"/>

                    <xsl:choose>
                      <xsl:when test="@type">
                        <xsl:variable name="name"
                                      select="if (@name != '')
                                              then @name
                                              else concat('_', */gn:element/@ref)"/>

                        <xsl:choose>
                          <xsl:when test="@type = 'select'">
                            <select class="form-control"
                                      name="{$name}">
                              <xsl:if test="$isReadonlyDueToXlink">
                                <xsl:attribute name="disabled" select="'disabled'"/>
                              </xsl:if>
                              <xsl:variable name="value"
                                            select="*/text()"/>
                              <option></option>
                              <xsl:for-each select="options/option">
                                <option value="{@value}">
                                  <xsl:if test="@value = $value">
                                    <xsl:attribute name="selected">selected</xsl:attribute>
                                  </xsl:if>
                                  <xsl:value-of select="."/>
                                </option>
                              </xsl:for-each>
                              <xsl:if test="count(options/option[@value = $value]) = 0">
                                <option value="{$value}">
                                  <xsl:value-of select="$value"/>
                                </option>
                              </xsl:if>
                            </select>
                          </xsl:when>
                          <xsl:when test="@type = 'textarea'">
                            <!-- TODO: Multilingual, codelist, date ... -->
                            <textarea class="form-control"
                                      name="{$name}">
                              <xsl:value-of select="*/text()"/>
                            </textarea>
                          </xsl:when>
                          <xsl:otherwise>
                            <input class="form-control"
                                   type="{if (@type = 'Real' or @type = 'Integer' or @type = 'Percentage')
                                  then 'number'
                                  else 'text'}"
                                   name="{$name}"
                                   value="{*/normalize-space()}">
                              <xsl:if test="@min">
                                <xsl:attribute name="min" select="@min"/>
                              </xsl:if>
                              <xsl:if test="@max">
                                <xsl:attribute name="max" select="@max"/>
                              </xsl:if>
                              <xsl:if test="@step">
                                <xsl:attribute name="step" select="@step"/>
                              </xsl:if>
                              <xsl:if test="$isReadonlyDueToXlink">
                                <xsl:attribute name="disabled" select="'disabled'"/>
                              </xsl:if>
                              <xsl:if test="@pattern">
                                <xsl:attribute name="pattern" select="@pattern"/>
                              </xsl:if>
                            </input>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:when>
                      <xsl:otherwise>
                        <!-- Call schema render mode of the field without label and controls.-->
                        <saxon:call-template name="{concat('dispatch-', $schema)}">
                          <xsl:with-param name="base" select=".[name() != 'directiveAttributes']"/>
                          <xsl:with-param name="config" as="node()?">
                            <xsl:if test="@use">
                              <field>
                                <xsl:copy-of select="@use|directiveAttributes"/>
                              </field>
                            </xsl:if>
                          </xsl:with-param>
                        </saxon:call-template>
                      </xsl:otherwise>
                    </xsl:choose>

                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </xsl:for-each>
          </tr>
        </xsl:for-each>

        <!-- Add an extra row for adding new one -->
        <xsl:if test="$addControl">
          <tr>
            <td colspan="{max($values/row/count(col))}">
              <xsl:copy-of select="$addControl"/>
            </td>
          </tr>
        </xsl:if>
      </tbody>
    </table>
    <!--<textarea><xsl:copy-of select="$values"/></textarea>-->
    <!--<textarea><xsl:copy-of select="$addControl"/></textarea>-->
  </xsl:template>
</xsl:stylesheet>
