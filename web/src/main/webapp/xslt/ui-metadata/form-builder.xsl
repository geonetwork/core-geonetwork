<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="#all">
  <!-- Build the form for creating HTML elements. -->

  <xsl:import href="../common/base-variables-metadata.xsl"/>

  <xsl:import href="../common/utility-tpl-metadata.xsl"/>

  <xsl:import href="../layout-xml.xsl"/>

  <xsl:import href="form-configurator.xsl"/>
  
  <xsl:import href="menu-builder.xsl"/>

  <!-- 
    Render an element with a label and a value
  -->
  <xsl:template name="render-element">
    <xsl:param name="label" as="xs:string"/>
    <xsl:param name="value"/>
    <!-- cls may define custom CSS class in order to activate
    custom widgets on client side -->
    <xsl:param name="cls" required="no" as="xs:string"/>
    <!-- widget may define custom information in order to activate
    custom widgets on client side. Eg. calendar, bboxMap -->
    <xsl:param name="widget" required="no" as="xs:string" select="''"/>
    <xsl:param name="widgetParams" required="no" as="xs:string" select="''"/>
    <!-- XPath is added as data attribute for client side references 
    to get help or inline editing ? -->
    <xsl:param name="xpath" required="no" as="xs:string" select="''"/>

    <!-- For editing -->
    <xsl:param name="name" required="no" as="xs:string" select="generate-id()"/>
    <xsl:param name="type" required="no" as="xs:string" select="'input'"/>
    <xsl:param name="hidden" required="no" as="xs:boolean" select="false()"/>
    <xsl:param name="editInfo" required="no"/>
    <xsl:param name="parentEditInfo" required="no"/>
    <xsl:param name="attributesSnippet" required="no"/>
    <xsl:param name="listOfValues" select="''"/>
    <xsl:param name="isDisabled" select="false()"/>

    <!-- Required status is defined in parent element for
    some profiles like ISO19139. If not set, the element
    editing information is used. 
    In view mode, always set to false.
    -->
    <xsl:variable name="isRequired" as="xs:boolean">
      <xsl:choose>
        <xsl:when test="$isEditing">
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
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="false()"/>
        </xsl:otherwise>
      </xsl:choose>

    </xsl:variable>


    <!-- The form field identified by the element ref.
            This HTML element should be removed when action remove is called.
        -->
    
    <div class="form-group" id="gn-el-{$editInfo/@ref}">
      <label for="gn-field-{$editInfo/@ref}"
        class="col-lg-2 control-label {if ($isRequired) then 'gn-required' else ''}">
        <xsl:if test="$xpath and $withXPath">
          <xsl:attribute name="data-gn-xpath" select="$xpath"/>
        </xsl:if>
        <xsl:if test="$widget != ''">
          <xsl:attribute name="data-gn-widget" select="$widget"/>
          <xsl:if test="$widgetParams != ''">
            <xsl:attribute name="data-gn-widget-params" select="$widgetParams"/>
          </xsl:if>
        </xsl:if>
        <xsl:value-of select="$label"/>
      </label>



      <xsl:choose>
        <xsl:when test="$isEditing">
          <!-- TODO : Add custom fields -->
          <div class="col-lg-8 gn-value">
            <xsl:call-template name="render-form-field">
              <xsl:with-param name="name" select="$name"/>
              <xsl:with-param name="value" select="$value"/>
              <xsl:with-param name="type" select="$type"/>
              <xsl:with-param name="isRequired" select="$isRequired"/>
              <xsl:with-param name="isDisabled" select="$isDisabled"/>
              <xsl:with-param name="editInfo" select="$editInfo"/>
              <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
              <xsl:with-param name="listOfValues" select="$listOfValues"/>
            </xsl:call-template>
          </div>
          <div class="col-lg-2 gn-control">
            <xsl:if test="not($isDisabled)">
              <xsl:call-template name="render-form-field-control-remove">
                <xsl:with-param name="name" select="name(.)"/>
                <xsl:with-param name="isRequired" select="$isRequired"/>
                <xsl:with-param name="editInfo" select="$editInfo"/>
                <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
              </xsl:call-template>
            </xsl:if>
            <!-- TODO: Add the set on save text ? -->
          </div>

          <!-- Next line display the add element control
                    the geonet:child element is taking care of that
                  <xsl:if test="not($isDisabled)">
                    <div class="col-lg-10"> </div>
                    <div class="col-lg-2">
                        <xsl:call-template name="render-form-field-control-add">
                            <xsl:with-param name="name" select="name(.)"/>
                            <xsl:with-param name="isRequired" select="$isRequired"/>
                            <xsl:with-param name="editInfo" select="$editInfo"/>
                            <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
                        </xsl:call-template>
                    </div>
                  </xsl:if> -->

        </xsl:when>
        <xsl:otherwise>
          <div class="col-lg-10 gn-value">
            <xsl:value-of select="$value"/>
          </div>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:if test="$attributesSnippet">
        <xsl:copy-of select="$attributesSnippet"/>
      </xsl:if>
    </div>
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
    <xsl:param name="editInfo" required="no"/>
    <!-- The content to put into the box -->
    <xsl:param name="subTreeSnippet" required="yes" as="node()"/>
    <!-- cls may define custom CSS class in order to activate
    custom widgets on client side -->
    <xsl:param name="cls" required="no"/>
    <!-- XPath is added as data attribute for client side references 
    to get help or inline editing ? -->
    <xsl:param name="xpath" required="no"/>
    <xsl:param name="attributesSnippet" required="no"/>


    <fieldset id="{concat('gn-el-', $editInfo/@ref)}">

      <legend class="{$cls}">
        <xsl:if test="$xpath and $withXPath">
          <xsl:attribute name="data-gn-xpath" select="$xpath"/>
        </xsl:if>

        <xsl:value-of select="$label"/>

        <xsl:if test="$editInfo">
          <xsl:call-template name="render-boxed-element-control">
            <xsl:with-param name="editInfo" select="$editInfo"/>
          </xsl:call-template>
        </xsl:if>

        <xsl:if test="$attributesSnippet">
          <xsl:copy-of select="$attributesSnippet"/>
        </xsl:if>
      </legend>

      <xsl:if test="$subTreeSnippet">
        <xsl:copy-of select="$subTreeSnippet"/>
      </xsl:if>
    </fieldset>
  </xsl:template>

  <xsl:template name="render-boxed-element-control">
    <xsl:param name="editInfo"/>

    <!--<textarea><xsl:copy-of select="$editInfo"/></textarea>
        <textarea><xsl:copy-of select="$parentEditInfo"/></textarea>
        -->

    <button class="btn icon-remove text-danger pull-right"
      data-ng-click="remove({$editInfo/@ref}, {$editInfo/@parent})"
      data-ng-mouseenter="highlightRemove({$editInfo/@ref})"
      data-ng-mouseleave="unhighlightRemove({$editInfo/@ref})"/>

    <!--
      Add a box element from here or after the current one ?
      
      <xsl:if test="$parentEditInfo and $parentEditInfo/@del = 'true'">
      <button class="btn icon-remove btn-danger gn-remove"
        data-ng-click="remove({$editInfo/@ref}, {$editInfo/@parent})"/>
    </xsl:if>-->
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
    <xsl:param name="label"/>
    <xsl:param name="childEditInfo"/>
    <xsl:param name="parentEditInfo"/>

    <xsl:variable name="id" select="generate-id()"/>

    <!-- This element is replaced by the content received when clicking add -->
    <div class="form-group" id="gn-el-{$id}">
      <label class="col-lg-2 control-label">
        <xsl:value-of select="$label"/>
      </label>
      <div class="col-lg-10">
        <xsl:choose>
          <!-- When element have different types, provide
                a list of those types to be selected. The type list
                is defined by the schema and optionaly overriden by
                the schema suggestion.
                
                TODO: Could be nice to select a type by default - a recommended type -->
          <xsl:when test="$childEditInfo/gn:choose">
            <div class="btn-group">
              <button type="button" class="btn dropdown-toggle icon-plus" data-toggle="dropdown">
                <span/>
                <span class="caret"/>
              </button>
              <ul class="dropdown-menu">
                <xsl:for-each select="$childEditInfo/gn:choose">
                  <li>
                    <a
                      data-ng-click="addChoice({$parentEditInfo/@ref}, '{concat($childEditInfo/@prefix, ':', $childEditInfo/@name)}', '{@name}', '{$id}', 'before');">
                      <xsl:value-of select="gn-fn-metadata:getLabel($schema, @name, $labels)"/>
                    </a>
                  </li>
                </xsl:for-each>
              </ul>
            </div>
          </xsl:when>
          <xsl:otherwise>
            <i class="btn icon icon-plus"
              data-ng-click="add({$parentEditInfo/@ref}, '{concat(@prefix, ':', @name)}', '{$id}', 'before');"
            />
          </xsl:otherwise>
        </xsl:choose>

      </div>
    </div>
  </xsl:template>

  <!-- Create a form field ie. a textarea, an input, a select, ...
    
    This could be a directive which take care of rendering form elements ?
    
    -->
  <xsl:template name="render-form-field">
    <xsl:param name="name"/>
    <xsl:param name="value"/>
    <xsl:param name="hidden"/>
    <xsl:param name="type"/>
    <xsl:param name="isRequired"/>
    <xsl:param name="isDisabled"/>
    <xsl:param name="editInfo"/>
    <xsl:param name="parentEditInfo"/>
    <!-- 
        May contain a codelist or a helper list.
        -->
    <xsl:param name="listOfValues" select="''"/>

    <!-- Get variable from attribute (eg. codelist) or node (eg. gco:CharacterString) -->
    <xsl:variable name="valueToEdit"
      select="if ($value/*) then normalize-space($value/text()) else $value"/>
    
    <xsl:choose>
      <xsl:when test="$type = 'textarea'">
        <textarea class="form-control" id="gn-field-{$editInfo/@ref}" name="_{$name}">
          <xsl:if test="$isRequired">
            <xsl:attribute name="required" select="'required'"/>
          </xsl:if>
          <xsl:if test="$hidden">
            <xsl:attribute name="display" select="'none'"/>
          </xsl:if>
          <xsl:value-of select="$valueToEdit"/>
        </textarea>
      </xsl:when>
      <xsl:when test="$type = 'select'">
        <select class="form-control" id="gn-field-{$editInfo/@ref}" name="_{$name}">
          <xsl:if test="$isRequired">
            <xsl:attribute name="required" select="'required'"/>
          </xsl:if>
          <xsl:if test="$hidden">
            <xsl:attribute name="display" select="'none'"/>
          </xsl:if>
          <!-- Build list from list of values.
                    
                    TODO : for enum -->
          <xsl:for-each select="$listOfValues/entry">
            <xsl:sort select="label"/>
            <option value="{code}" title="{normalize-space(description)}">
              <xsl:if test="$valueToEdit = code">
                <xsl:attribute name="selected"/>
              </xsl:if>
              <xsl:value-of select="label"/>
            </option>
          </xsl:for-each>
          <!--<option value="{$valueToEdit}">
                        <xsl:value-of select="$value"/>
                    </option>-->
        </select>

      </xsl:when>
      <xsl:otherwise>
        <input class="form-control" id="gn-field-{$editInfo/@ref}" name="_{$name}"
          value="{$valueToEdit}">
          <xsl:if test="$isRequired">
            <xsl:attribute name="required" select="'required'"/>
          </xsl:if>
          <xsl:if test="$isDisabled">
            <xsl:attribute name="disabled" select="'disabled'"/>
          </xsl:if>
          <xsl:if test="$type != ''">
            <xsl:attribute name="type" select="$type"/>
          </xsl:if>
          <xsl:if test="$hidden">
            <xsl:attribute name="hidden"/>
          </xsl:if>
        </input>

        <!-- Helpers -->
        <xsl:if test="$listOfValues">
          <select class="form-control">
            <xsl:copy-of select="$listOfValues/*"/>
          </select>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


  <xsl:template name="render-form-field-control-remove">
    <xsl:param name="name"/>
    <xsl:param name="isRequired"/>
    <xsl:param name="editInfo"/>
    <xsl:param name="parentEditInfo"/>

    <!--<textarea><xsl:copy-of select="$editInfo"/></textarea>
        <textarea><xsl:copy-of select="$parentEditInfo"/></textarea>
        -->
    <xsl:if test="$parentEditInfo and $parentEditInfo/@del = 'true'">
      <button class="btn icon-remove text-danger gn-remove"
        data-ng-click="remove({$editInfo/@ref}, {$editInfo/@parent})"
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
      <button class="btn icon-plus gn-add"
        data-ng-click="add({$parentEditInfo/@parent}, '{$name}', {$editInfo/@ref})"/>
    </xsl:if>
  </xsl:template>


  <!-- Nav bars -->
  <xsl:template name="scroll-spy-nav-bar">
    <div id="navbarExample" class="navbar navbar-static navbar-fixed-bottom">
      <div class="navbar-inner">
        <div class="container" style="width: auto;">
          <ul class="nav">
            <li class="active">
              <a href="#identificationInfo">@gmd:identificationInfo</a>
            </li>
            <li>
              <a href="#spatialRepresentationInfo">@gmd:spatialRepresentationInfo</a>
            </li>
            <li>
              <a href="#distributionInfo">@gmd:distributionInfo</a>
            </li>
            <li>
              <a href="#dataQualityInfo">@gmd:dataQualityInfo</a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>
