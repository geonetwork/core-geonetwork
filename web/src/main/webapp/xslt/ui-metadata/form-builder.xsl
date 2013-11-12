<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:java-xsl-util="java:org.fao.geonet.util.XslUtil"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saxon="http://saxon.sf.net/"
  extension-element-prefixes="saxon" exclude-result-prefixes="#all">
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

    <!-- The input type eg. number, date, datetime, email-->
    <xsl:param name="type" required="no" as="xs:string" select="''"/>

    <!-- The AngularJS directive name eg. gn-field-duration -->
    <xsl:param name="directive" required="no" as="xs:string" select="''"/>

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
                <xsl:with-param name="name" select="name(.)"/>
                <xsl:with-param name="isRequired" select="$isRequired"/>
                <xsl:with-param name="editInfo" select="$editInfo"/>
                <xsl:with-param name="parentEditInfo" select="$parentEditInfo"/>
              </xsl:call-template>
            </xsl:if>
          </div>
        </div>
      </xsl:when>
      <xsl:otherwise>
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
                
                
                <xsl:if test="$attributesSnippet">
                  <div class="well well-sm gn-attr {if ($isDisplayingAttributes) then '' else 'hidden'}">
                    <xsl:copy-of select="$attributesSnippet"/>
                  </div>
                </xsl:if>
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
      </legend>


      <xsl:if test="count($attributesSnippet/*) > 0">
        <div class="well well-sm gn-attr {if ($isDisplayingAttributes) then '' else 'hidden'}">
          <xsl:copy-of select="$attributesSnippet"/>
        </div>
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
    
    <!--<xsl:message>!render-element-template-field <xsl:copy-of select="$keyValues"/>
      <xsl:value-of select="$name"/>/
      <xsl:copy-of select="$template"/>/
      <xsl:value-of select="$id"/>/
      <xsl:value-of select="$isExisting"/>/
      <xsl:value-of select="$id"/>
    </xsl:message>-->
    <div class="form-group">
      <label class="col-lg-2 control-label">
        <!-- TODO: get label i18n -->
        <xsl:value-of select="$name"/>
      </label>
      <div class="col-lg-8">
        <xsl:for-each select="$template/values/key">
          <!-- Only display label if more than one key to match -->
          <xsl:if test="count($template/values/key) > 1">
            <label>
              <xsl:value-of select="@label"/>
            </label>
          </xsl:if>
          
          <xsl:choose>
            <xsl:when test="@use = 'textarea'">
              <textarea class="form-control" id="{$id}_{@label}"></textarea>
            </xsl:when>
            <xsl:otherwise>
              <input class="form-control" type="{@use}" value="" id="{$id}_{@label}"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
        
        <xsl:if test="not($isExisting)">
          <input class="form-control gn-debug" type="text" name="{$xpathFieldId}" value="{@xpath}"/>
        </xsl:if>
        <textarea class="form-control gn-debug" name="{$id}" data-gn-template-field="{$id}"
          data-keys="{string-join($template/values/key/@label, '#')}"
          data-values="{if ($keyValues) then string-join($keyValues/value, '#') else ''}">
          <xsl:copy-of select="$template/snippet/*"/>
        </textarea>
      </div>
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
    <xsl:param name="childEditInfo"/>
    <xsl:param name="parentEditInfo"/>

    <xsl:variable name="id" select="generate-id()"/>

    <!-- This element is replaced by the content received when clicking add -->
    <div class="form-group" id="gn-el-{$id}">
      <label class="col-lg-2 control-label">
        <xsl:if test="normalize-space($label) != ''">
                <xsl:value-of select="$label"/>
        </xsl:if>
      </label>
      <div class="col-lg-10">
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
                  data-ng-click="addChoice({$parentEditInfo/@ref}, '{concat($childEditInfo/@prefix, ':', $childEditInfo/@name)}', '{@name}', '{$id}', 'replaceWith');">
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
                      data-ng-click="addChoice({$parentEditInfo/@ref}, '{concat($childEditInfo/@prefix, ':', $childEditInfo/@name)}', '{@name}', '{$id}', 'before');">
                      <xsl:value-of select="$label/label"/>
                    </a>
                  </li>
                </xsl:for-each>
              </ul>
            </div>
          </xsl:when>
          <xsl:otherwise>
            <i class="btn fa fa-plus gn-add"
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
        <xsl:variable name="hasHelper" select="$listOfValues and count($listOfValues/*) > 0"/>
        
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
          <xsl:if test="$hidden or $hasHelper">
            <!-- hide the form field if helper is available, the 
            value is set by the directive which provide customized 
            forms -->
            <xsl:attribute name="class" select="'hidden'"/>
          </xsl:if>
        </input>


        <!-- 
        Create an helper list for the current input element.
        Current input could be an element or an attribute (eg. uom). 
        -->
        <xsl:if test="$hasHelper">
          <!-- 
            The helper config to pass to the directive in JSON format
          -->
          <textarea id="_{$editInfo/@ref}_config" class="hidden">
            <xsl:copy-of select="java-xsl-util:xmlToJson(
                                    saxon:serialize($listOfValues, 'default-serialize-mode'))"/></textarea>
          <div 
            data-gn-editor-helper="{$listOfValues/@editorMode}"
            data-ref="_{$editInfo/@ref}"
            data-type="{$type}"
            data-related-element="{if ($listOfValues/@relElementRef != '') 
                                    then concat('_', $listOfValues/@relElementRef) else ''}"
            data-related-attr="{if ($listOfValues/@relAtt) 
                                    then concat('_', $editInfo/@ref, '_', $listOfValues/@relAtt) else ''}">
          </div>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


  <!-- Display the remove control 
  if parent info is not defined and element is not 
  mandatory. 
  -->
  <xsl:template name="render-form-field-control-remove">
    <xsl:param name="name"/>
    <xsl:param name="isRequired"/>
    <xsl:param name="editInfo"/>
    <xsl:param name="parentEditInfo"/>

    <!--<textarea><xsl:copy-of select="$editInfo"/></textarea>
    <textarea><xsl:copy-of select="$parentEditInfo"/></textarea>
    -->
    <xsl:if
      test="($parentEditInfo and 
                     ($parentEditInfo/@del = 'true' or 
                     $parentEditInfo/@min != 1)
                   ) or 
                   not($parentEditInfo)">
      <i class="btn fa fa-times text-danger gn-control pull-right"
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
      <i class="btn fa fa-plus gn-add"
        data-ng-click="add({$parentEditInfo/@parent}, '{$name}', {$editInfo/@ref})"/>
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
    <!-- Form field name escaping ":" which will be invalid character for
    Jeeves request parameters. -->
    <xsl:variable name="fieldName" select="concat('_', $ref, '_', replace($attributeName, ':', 'COLON'))"/>
    
    <div class="form-group">
      <label class="col-lg-4">
        <xsl:value-of select="gn-fn-metadata:getLabel($schema, $attributeName, $labels)/label"/>
      </label>
      <div class="col-lg-8">
        <xsl:choose>
          <xsl:when test="$attributeSpec/gn:text">
            
            <xsl:variable name="attributeCodeList" select="gn-fn-metadata:getCodeListValues($schema, $attributeName, $codelists)"/>
            
            <select class="form-control" name="{$fieldName}">
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
            <input type="text" class="form-control" name="{$fieldName}" value="{$attributeValue}"/>
          </xsl:otherwise>
        </xsl:choose>
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
