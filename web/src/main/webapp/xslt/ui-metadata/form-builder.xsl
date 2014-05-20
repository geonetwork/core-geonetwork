<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:java-xsl-util="java:org.fao.geonet.util.XslUtil"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saxon="http://saxon.sf.net/"
  extension-element-prefixes="saxon" exclude-result-prefixes="#all">
  <!-- Build the form for creating HTML elements. -->

  <xsl:import href="../common/base-variables-metadata.xsl"/>

  <xsl:import href="../common/utility-tpl-metadata.xsl"/>

  <xsl:import href="form-builder-xml.xsl"/>

  <xsl:import href="form-configurator.xsl"/>

  <xsl:import href="menu-builder.xsl"/>

  <!-- 
    Render an element with a label and a value
  -->
  <xsl:template name="render-element">
    <xsl:param name="label" as="xs:string"/>
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

    <!-- The input type eg. number, date, datetime, email-->
    <xsl:param name="type" required="no" as="xs:string" select="''"/>

    <!-- The AngularJS directive name eg. gn-field-duration -->
    <xsl:param name="directive" required="no" as="xs:string" select="''"/>

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
    <xsl:param name="isDisabled" select="ancestor-or-self::node()[@xlink:href]"/>
    
    <!-- Define if the language fields should be displayed 
    with the selector or below each other. -->
    <xsl:param name="toggleLang" required="no" as="xs:boolean" select="false()"/>
    <!-- A gn-extra-field class is added to non first element.
    This class could be used to customize style of first or following
    element of same kind. eg. do not display label. -->
    <xsl:param name="isFirst" required="no" as="xs:boolean" select="true()"/>


    <xsl:variable name="isMultilingual" select="count($value/values) > 0"/>

    <!-- Required status is defined in parent element for
    some profiles like ISO19139. If not set, the element
    editing information is used. 
    In view mode, always set to false.
    -->
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


    <!-- The form field identified by the element ref.
            This HTML element should be removed when action remove is called.
        -->
    <xsl:choose>
      <xsl:when test="$directive != ''">
        <div class="form-group" id="gn-el-{$editInfo/@ref}">
          <div class="col-lg-10">
            <xsl:attribute name="data-{$directive}" select="$value"/>
            <xsl:attribute name="data-ref" select="concat('_', $editInfo/@ref)"/>
            <xsl:attribute name="data-label" select="$label"/>
          </div>
          <div class="col-lg-2 gn-control">
            <xsl:if test="not($isDisabled)">
              <xsl:call-template name="render-form-field-control-remove">
                <xsl:with-param name="editInfo" select="$editInfo"/>
                <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
              </xsl:call-template>
            </xsl:if>
          </div>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <div class="form-group gn-field {if ($isRequired) then 'gn-required' else ''} {if ($isFirst) then '' else 'gn-extra-field'}"
            id="gn-el-{$editInfo/@ref}">
          <label
              for="gn-field-{$editInfo/@ref}"
              class="col-sm-2 control-label">
            <xsl:value-of select="$label"/>
          </label>

          <div class="col-sm-9 gn-value">
            <xsl:if test="$isMultilingual">
              <xsl:attribute name="data-gn-multilingual-field" select="$metadataOtherLanguagesAsJson"/>
              <xsl:attribute name="data-main-language" select="$metadataLanguage"/>
              <xsl:attribute name="data-expanded" select="$toggleLang"/>
            </xsl:if>
            
            <xsl:choose>
              <xsl:when test="$isMultilingual">
                <xsl:for-each select="$value/values/value">
                  <xsl:sort select="@lang"/>
                  
                  <xsl:call-template name="render-form-field">
                    <xsl:with-param name="name" select="@ref"/>
                    <xsl:with-param name="lang" select="@lang"/>
                    <xsl:with-param name="value" select="."/>
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="tooltip" select="concat($schema, '|', name(.), '|', name(..), '|', $xpath)"/>
                    <xsl:with-param name="isRequired" select="$isRequired"/>
                    <xsl:with-param name="isDisabled" select="$isDisabled"/>
                    <xsl:with-param name="editInfo" select="$editInfo"/>
                    <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
                    <xsl:with-param name="listOfValues" select="$listOfValues"/>
                  </xsl:call-template>
                </xsl:for-each>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="render-form-field">
                  <xsl:with-param name="name" select="$name"/>
                  <xsl:with-param name="value" select="$value"/>
                  <xsl:with-param name="type" select="$type"/>
                  <xsl:with-param name="tooltip" select="concat($schema, '|', name(.), '|', name(..), '|', $xpath)"/>
                  <xsl:with-param name="isRequired" select="$isRequired"/>
                  <xsl:with-param name="isDisabled" select="$isDisabled"/>
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
            
            
            <xsl:if test="$attributesSnippet">
              <xsl:variable name="cssDefaultClass" select="'well well-sm'"/>
              <div class="{$cssDefaultClass} 
                {if ($forceDisplayAttributes) then 'gn-attr-mandatory' else 'gn-attr'}
                {if ($isDisplayingAttributes or $forceDisplayAttributes) then '' else 'hidden'}">
                <xsl:copy-of select="$attributesSnippet"/>
              </div>
            </xsl:if>
            
            <xsl:if test="$errors">
              <xsl:for-each select="$errors/errors/error">
                <span class="help-block text-danger"><xsl:value-of select="."/></span>
              </xsl:for-each>
            </xsl:if>
          </div>
          <div class="col-sm-1 gn-control">
            <xsl:if test="not($isDisabled)">
              <xsl:call-template name="render-form-field-control-remove">
                <xsl:with-param name="editInfo" select="$editInfo"/>
                <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
              </xsl:call-template>
            </xsl:if>
          </div>
        </div>
      </xsl:otherwise>
    </xsl:choose>

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
    <xsl:param name="attributesSnippet" required="no"><null/></xsl:param>
    <xsl:param name="isDisabled" select="ancestor::node()[@xlink:href]"/>


    <xsl:variable name="hasXlink" select="@xlink:href"/>

    <fieldset id="{concat('gn-el-', $editInfo/@ref)}" 
      class="{if ($hasXlink) then 'gn-has-xlink' else ''}">

      <legend class="{$cls}" data-gn-field-tooltip="{$schema}|{name()}|{name(..)}|">
        <xsl:if test="$xpath and $withXPath">
          <xsl:attribute name="data-gn-xpath" select="$xpath"/>
        </xsl:if>

        <xsl:value-of select="$label"/>

        <xsl:if test="$editInfo and not($isDisabled)">
          <xsl:call-template name="render-boxed-element-control">
            <xsl:with-param name="editInfo" select="$editInfo"/>
          </xsl:call-template>
        </xsl:if>
        
        
        <xsl:call-template name="render-form-field-control-move">
          <xsl:with-param name="elementEditInfo" select="$editInfo"/>
          <xsl:with-param name="domeElementToMoveRef" select="$editInfo/@ref"/>
        </xsl:call-template>
      </legend>

      <xsl:if test="count($attributesSnippet/*) > 0">
        <div class="well well-sm gn-attr {if ($isDisplayingAttributes) then '' else 'hidden'}">
          <xsl:copy-of select="$attributesSnippet"/>
        </div>
      </xsl:if>

      <xsl:if test="normalize-space($errors) != ''">
        <xsl:for-each select="$errors/errors/error">
          <div class="alert alert-danger">
            <xsl:value-of select="."/>
          </div>
        </xsl:for-each>
      </xsl:if>

      <xsl:if test="$subTreeSnippet">
        <xsl:copy-of select="$subTreeSnippet"/>
      </xsl:if>
    </fieldset>
  </xsl:template>

  <xsl:template name="render-boxed-element-control">
    <xsl:param name="editInfo"/>

    <i class="btn fa fa-times text-danger pull-right"
      data-ng-click="remove({$editInfo/@ref}, {$editInfo/@parent})"
      data-ng-mouseenter="highlightRemove({$editInfo/@ref})"
      data-ng-mouseleave="unhighlightRemove({$editInfo/@ref})"/>

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
    <xsl:param name="qname" required="no"/>
    <xsl:param name="parentRef" required="no"/>
    <!-- Label to display if element is missing. The field
    is initialized with a default template. -->
    <xsl:param name="isMissingLabel" required="no"/>
    <xsl:param name="isFirst" required="no" as="xs:boolean" select="true()"/>
    <xsl:param name="isAddAction" required="no" as="xs:boolean" select="false()"/>

    <xsl:variable name="tagId" select="generate-id()"/>

  <!-- <xsl:message>!render-element-template-field <xsl:copy-of select="$keyValues"/>
      <xsl:value-of select="$name"/>/tpl:
      <xsl:copy-of select="$template"/>/
      <xsl:value-of select="$id"/>/
      <xsl:value-of select="$isExisting"/>/
      <xsl:value-of select="$id"/>
    </xsl:message>-->
    <div class="form-group gn-field {if ($isFirst) then '' else 'gn-extra-field'} {if ($isAddAction) then 'gn-add-field' else ''}"
         id="gn-el-{if ($refToDelete) then $refToDelete/@ref else generate-id()}">

      <label class="col-sm-2 control-label">
        <xsl:value-of select="$name"/>
      </label>
      <div class="col-sm-9">
        <!-- Create an empty input to contain the data-gn-field-tooltip
        key which is used to check if an element
        is the first element of its kind in the form. The key for a template
        field is {schemaIdentifier}|{firstTemplateFieldKey} -->
        <input type="hidden"
               data-gn-field-tooltip="{$schema}|{$template/values/key[position() = 1]/@label}"/>

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
                  <xsl:attribute name="data-template-add-action" select="'true'"/>
                </div>
              </xsl:when>
              <xsl:otherwise>
                <i class="btn btn-default fa fa-plus" data-gn-template-field-add-button="{$id}"/>
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
              <xsl:for-each select="$template/values/key">
                <xsl:variable name="valueLabelKey" select="@label"/>
                <xsl:variable name="helper" select="if ($keyValues) then $keyValues/field[@name = $valueLabelKey]/helper else ''"/>
                <xsl:variable name="codelist" select="if ($keyValues) then $keyValues/field[@name = $valueLabelKey]/codelist else ''"/>
                <xsl:variable name="readonly" select="if ($keyValues) then $keyValues/field[@name = $valueLabelKey]/readonly else ''"/>

                <!-- Only display label if more than one key to match -->
                <xsl:if test="count($template/values/key) > 1">
                  <label for="{$id}_{@label}">
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
                    <select class="form-control input-sm"
                            data-gn-field-tooltip="{$schema}|{@tooltip}"
                            id="{$id}_{@label}">
                      <xsl:if test="$readonly = 'true'">
                        <xsl:attribute name="disabled"/>
                      </xsl:if>
                      <option></option>
                      <xsl:for-each select="$codelist/entry">
                        <xsl:sort select="label"/>
                        <option value="{code}" title="{normalize-space(description)}">
                          <xsl:value-of select="label"/>
                        </option>
                      </xsl:for-each>
                    </select>
                  </xsl:when>
                  <xsl:when test="@use = 'checkbox'">
                    <span class="pull-left" >
                      <input type="checkbox"
                             data-gn-field-tooltip="{$schema}|{@tooltip}"
                             id="{$id}_{@label}">
                        <xsl:if test="$readonly = 'true'">
                          <xsl:attribute name="disabled"/>
                        </xsl:if>
                       </input>&#160;</span>
                  </xsl:when>
                  <xsl:when test="@use = 'gn-date-picker'">
                    <input class="form-control"
                           type="hidden"
                           value=""
                           id="{$id}_{@label}"/>

                    <div data-gn-field-tooltip="{$schema}|{@tooltip}"
                         data-gn-date-picker="{if ($keyValues) then $keyValues/field[@name = $valueLabelKey]/value else ''}"
                         data-id="#{$id}_{@label}">
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
                    <input class="form-control"
                           type="{if (@use) then @use else 'text'}"
                           value="" id="{$id}_{@label}"
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

              </xsl:for-each>

              <xsl:if test="not($isExisting)">
                <input class="gn-debug" type="text" name="{$xpathFieldId}" value="{@xpath}"/>
              </xsl:if>
              <textarea class="form-control gn-debug" name="{$id}"
                        data-gn-template-field="{$id}"
                        data-keys="{string-join($template/values/key/@label, '$$$')}"
                        data-values="{if ($keyValues and count($keyValues/*) > 0)
                          then string-join($keyValues/field/value, '$$$') else ''}">
                <xsl:if test="$isMissingLabel != ''">
                  <xsl:attribute name="data-not-set-check" select="$tagId"/>
                </xsl:if>
                <xsl:copy-of select="$template/snippet/*"/>
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
    <xsl:param name="directive" as="xs:string?"/>
    <xsl:param name="childEditInfo"/>
    <xsl:param name="parentEditInfo"/>
    <!-- Hide add element if child of an XLink section. -->
    <xsl:param name="isDisabled" select="ancestor::node()[@xlink:href]"/>
    <xsl:param name="isFirst" required="no" as="xs:boolean" select="true()"/>

    <xsl:if test="not($isDisabled)">
      <xsl:variable name="id" select="generate-id()"/>
      <xsl:variable name="qualifiedName" select="concat($childEditInfo/@prefix, ':', $childEditInfo/@name)"/>
  
      <!-- This element is replaced by the content received when clicking add -->
      <div class="form-group gn-field {if ($isFirst) then '' else 'gn-extra-field'} gn-add-field"
           id="gn-el-{$id}">
        <label class="col-sm-2 control-label"
          data-gn-field-tooltip="{$schema}|{$qualifiedName}|{name(..)}|">
          <xsl:if test="normalize-space($label) != ''">
                  <xsl:value-of select="$label"/>
          </xsl:if>
        </label>
        <div class="col-sm-9">
          
          <xsl:choose>
            <!-- When element have different types, provide
                  a list of those types to be selected. The type list
                  is defined by the schema and optionaly overriden by
                  the schema suggestion.
                  
                  TODO: Could be nice to select a type by default - a recommended type 
                  
                  If only one choice, make a simple button
            -->
            <xsl:when test="count($childEditInfo/gn:choose) = 1">
                  <xsl:for-each select="$childEditInfo/gn:choose">
                    <xsl:variable name="label" select="gn-fn-metadata:getLabel($schema, @name, $labels)"/>
                    
                    <i type="button" class="btn fa fa-plus gn-add" 
                    title="{$label/description}"
                    data-ng-click="addChoice({$parentEditInfo/@ref}, '{$qualifiedName}', '{@name}', '{$id}', 'replaceWith');">
                    </i>
                  </xsl:for-each>
            </xsl:when>
            <!-- 
                  If many choices, make a dropdown button -->
            <xsl:when test="count($childEditInfo/gn:choose) > 1">
              <div class="btn-group">
                <button type="button" class="btn dropdown-toggle fa fa-plus gn-add" data-toggle="dropdown">
                  <span/>
                  <span class="caret"/>
                </button>
                <ul class="dropdown-menu">
                  <xsl:for-each select="$childEditInfo/gn:choose">
                    <xsl:variable name="label" select="gn-fn-metadata:getLabel($schema, @name, $labels)"/>
                    
                    <li title="{$label/description}">
                      <a
                        data-ng-click="addChoice({$parentEditInfo/@ref}, '{$qualifiedName}', '{@name}', '{$id}', 'before');">
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
              <xsl:choose>
                <xsl:when test="$directive != ''">
                  <div>
                    <xsl:attribute name="{$directive}"/>
                    <xsl:attribute name="data-dom-id" select="$id"/>
                    <xsl:attribute name="data-element-name" select="$qualifiedName"/>
                    <xsl:attribute name="data-element-ref" select="$parentEditInfo/@ref"/>
                  </div>
                </xsl:when>
                <xsl:otherwise>
                  <i class="btn fa fa-plus gn-add"
                    data-ng-click="add({$parentEditInfo/@ref}, '{concat(@prefix, ':', @name)}', '{$id}', 'before');"
                  />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </div>
    </xsl:if>
  </xsl:template>

  <!-- Create a form field ie. a textarea, an input, a select, ...
    
    This could be a directive which take care of rendering form elements ?
    
    -->
  <xsl:template name="render-form-field">
    <xsl:param name="name"/>
    <xsl:param name="value"/>
    <xsl:param name="lang" required="no"/>
    <xsl:param name="hidden"/>
    <xsl:param name="type"/>
    <xsl:param name="tooltip" required="no"/>
    <xsl:param name="isRequired"/>
    <xsl:param name="isDisabled"/>
    <xsl:param name="editInfo"/>
    <xsl:param name="parentEditInfo"/>
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
          <xsl:if test="$tooltip">
            <xsl:attribute name="data-gn-field-tooltip" select="$tooltip"/>
          </xsl:if>
          <xsl:if test="$lang">
            <xsl:attribute name="lang" select="$lang"/>
          </xsl:if>
          <xsl:if test="$hidden or $hasHelper">
            <xsl:attribute name="class" select="'hidden'"/>
          </xsl:if>
          <xsl:value-of select="$valueToEdit"/>
        </textarea>
      </xsl:when>
      <xsl:when test="$type = 'select'">
        <select class="" id="gn-field-{$editInfo/@ref}" name="_{$name}">
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
            <xsl:sort select="label"/>
            <option value="{code}" title="{normalize-space(description)}">
              <xsl:if test="$valueToEdit = code">
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
          <!--
            Add the value if not defined in the codelist to not lose it
            <option value="{$valueToEdit}">
                        <xsl:value-of select="$value"/>
                    </option>-->
        </select>

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
        <!-- FIXME : some JS here. Move to a directive ?-->
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
        
        <xsl:variable name="input">
          <input class="form-control {if ($lang) then 'hidden' else ''}" 
            id="gn-field-{$editInfo/@ref}" 
            name="_{$name}" 
            value="{normalize-space($valueToEdit)}">
            <!-- If type is a directive -->
            <xsl:if test="$isDirective">
              <xsl:attribute name="{$type}"/>
            </xsl:if>
            <xsl:if test="$tooltip">
              <xsl:attribute name="data-gn-field-tooltip" select="$tooltip"/>
            </xsl:if>
            <xsl:if test="$isRequired">
              <xsl:attribute name="required" select="'required'"/>
              <xsl:attribute name="data-gn-check" select="concat('#gn-el-', $editInfo/@ref)"/>
            </xsl:if>
            <xsl:if test="$isDisabled">
              <xsl:attribute name="disabled" select="'disabled'"/>
            </xsl:if>
            <xsl:if test="$lang">
              <xsl:attribute name="lang" select="$lang"/>
            </xsl:if>
            <xsl:if test="$type != ''">
              <xsl:attribute name="type" select="if ($isDirective) then 'text' else $type"/>
            </xsl:if>
            <xsl:if test="$hidden or $hasHelper">
              <!-- hide the form field if helper is available, the 
              value is set by the directive which provide customized 
              forms -->
              <xsl:attribute name="class" select="'hidden'"/>
            </xsl:if>
          </input>
        </xsl:variable>

        <xsl:copy-of select="$input"/>

      </xsl:otherwise>
    </xsl:choose>

    <!-- 
        Create an helper list for the current input element.
        Current input could be an element or an attribute (eg. uom). 
        -->
    <xsl:if test="$hasHelper">
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


  <xsl:template name="render-form-field-helper">
    <xsl:param name="elementRef" as="xs:string"/>
    <xsl:param name="relatedElement" as="xs:string" required="no" select="''"/>
    <xsl:param name="relatedElementRef" as="xs:string" required="no" select="''"/>
    <xsl:param name="dataType" as="xs:string" required="no" select="'text'"/>
    <xsl:param name="listOfValues" as="node()"/>
    <xsl:param name="tooltip" as="xs:string" required="no" select="''"/>
    
    <!-- 
    The helper config to pass to the directive in JSON format
    -->
    <textarea id="{$elementRef}_config" class="hidden">
      <xsl:copy-of select="java-xsl-util:xmlToJson(
        saxon:serialize($listOfValues, 'default-serialize-mode'))"/></textarea>
    <div 
      data-gn-editor-helper="{$listOfValues/@editorMode}"
      data-ref="{$elementRef}"
      data-type="{$dataType}"
      data-related-element="{if ($listOfValues/@rel != '')
      then $relatedElement else ''}"
      data-related-attr="{if ($listOfValues/@relAtt) 
      then $relatedElementRef else ''}"
      data-tooltip="{$tooltip}">
    </div>
  </xsl:template>

  <!-- Display the remove control 
  if parent info is not defined and element is not 
  mandatory. 
  -->
  <xsl:template name="render-form-field-control-remove">
    <xsl:param name="editInfo"/>
    <xsl:param name="parentEditInfo" required="no"/>
    <xsl:if
      test="($parentEditInfo and 
                     ($parentEditInfo/@del = 'true' or 
                     $parentEditInfo/@min != 1)
                   ) or 
                   (not($parentEditInfo) and ($editInfo and 
                   ($editInfo/@del = 'true' or 
                   $editInfo/@min != 1)
                   ))">
      
      <xsl:variable name="elementToRemove" select="if ($parentEditInfo) then 
        $parentEditInfo else $editInfo"/>
      
      <i class="btn fa fa-times text-danger gn-control pull-right"
        data-ng-click="remove({$elementToRemove/@ref}, {$elementToRemove/@parent}, {$editInfo/@ref})"
        data-ng-mouseenter="highlightRemove({$editInfo/@ref})"
        data-ng-mouseleave="unhighlightRemove({$editInfo/@ref})"/>
    </xsl:if>
  </xsl:template>


  <xsl:template name="render-form-field-control-add">
    <xsl:param name="name"/>
    <xsl:param name="isRequired"/>
    <xsl:param name="editInfo"/>
    <xsl:param name="parentEditInfo"/>


    <!-- Add icon for last element of its kind -->
    <xsl:if test="$parentEditInfo and $parentEditInfo/@add = 'true' and not($parentEditInfo/@down)">
      <i class="btn fa fa-plus gn-add"
        data-ng-click="add({$parentEditInfo/@parent}, '{$name}', {$editInfo/@ref})"/>
    </xsl:if>
  </xsl:template>




  <!-- Template to render up and down control. -->
  <xsl:template name="render-form-field-control-move">
    <xsl:param name="elementEditInfo"/>
    <xsl:param name="domeElementToMoveRef" required="no" select="''"/>
    
    <xsl:if test="not($viewConfig/@upAndDownControlHidden)">
      <div class="gn-move">
        <xsl:variable name="elementToMoveRef" select="if ($elementEditInfo) then $elementEditInfo/@ref else ''"/>
        <a class="fa fa-angle-up {if ($elementEditInfo and $elementEditInfo/@up = 'true') then '' else 'invisible'}" 
          data-gn-editor-control-move="{$elementToMoveRef}"
          data-domelement-to-move="{$domeElementToMoveRef}"
          data-direction="up" href=""></a>
        <a class="fa fa-angle-down {if ($elementEditInfo and $elementEditInfo/@down = 'true') then '' else 'invisible'}" 
          data-gn-editor-control-move="{$elementToMoveRef}"
          data-domelement-to-move="{$domeElementToMoveRef}"
          data-direction="down" href=""></a>
      </div>
    </xsl:if>
  </xsl:template>


  <!-- 
    Render attribute as select list or simple output 
  -->
  <xsl:template mode="render-for-field-for-attribute" match="@*">
    <xsl:param name="ref"/>
    
    <xsl:variable name="attributeName" select="name()"/>
    <xsl:variable name="attributeValue" select="."/>
    <xsl:variable name="attributeSpec" select="../gn:attribute[@name = $attributeName]"/>
    
    <xsl:variable name="directive"
      select="gn-fn-metadata:getFieldType($editorConfig, name(), 
      name(..))"/>
    
    <!-- Form field name escaping ":" which will be invalid character for
    Jeeves request parameters. -->
    <xsl:variable name="fieldName" select="concat('_', $ref, '_', replace($attributeName, ':', 'COLON'))"/>
    
    <div class="form-group" id="gn-attr-{$fieldName}">
      <label class="col-sm-4">
        <xsl:value-of select="gn-fn-metadata:getLabel($schema, $attributeName, $labels)/label"/>
      </label>
      <div class="col-sm-7">
        <xsl:if test="$directive">
          <xsl:attribute name="{$directive}"/>
        </xsl:if>
        
        <xsl:choose>
          <xsl:when test="$attributeSpec/gn:text">
            <xsl:variable name="attributeCodeList" select="gn-fn-metadata:getCodeListValues($schema, $attributeName, $codelists)"/>

            <select class="" name="{$fieldName}">
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
            </select>
          </xsl:when>
          <xsl:otherwise>
            <input type="text" class="" name="{$fieldName}" value="{$attributeValue}">
            </input>
          </xsl:otherwise>
        </xsl:choose>
      </div>
      <div class="col-sm-1">
        <i class="btn pull-right fa fa-times text-danger" data-ng-click="removeAttribute('{$fieldName}')"/>
      </div>
    </div>
  </xsl:template>
  
  
  <!-- 
  Ignore some internal attributes and do not allow to apply this mode
  to a node (only for gn:attribute, see next template).
  -->
  <xsl:template mode="render-for-field-for-attribute" 
    match="@gn:xsderror|@gn:addedObj|
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
    'xlink:show', 'xlink:actuate', 'xlink:arcrole', 'xlink:role', 'xlink:title', 'xlink:href'))]" priority="4">
    <xsl:param name="ref"/>
    <xsl:param name="insertRef" select="''"/>

    <xsl:variable name="attributeLabel" select="gn-fn-metadata:getLabel($schema, @name, $labels)"/>
    <button type="button" class="btn btn-link btn-xs"
      data-ng-click="add('{$ref}', '{@name}', '{$insertRef}', null, true)"
      title="{$attributeLabel/description}">
      <i class="fa fa-plus"/>
      <xsl:value-of select="$attributeLabel/label"/>
    </button>
  </xsl:template>
  

  <!-- Render batch process directive action -->
  <xsl:template name="render-batch-process-button">
    <xsl:param name="process-name"/>
    <xsl:param name="process-params"/>
    <!-- TODO: Could be relevant to only apply process to the current thesaurus -->
    
    <div class="row form-group gn-field gn-extra-field">
      <div class="col-xs-10 col-xs-offset-2">
        <span data-gn-batch-process-button="{$process-name}"
          data-params="{$process-params}"
          data-name="{$strings/*[name() = $process-name]}"
          data-help="{$strings/*[name() = concat($process-name, 'Help')]}"/>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>