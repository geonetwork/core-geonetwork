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
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all" version="2.0">
  <!--
    Build the form from the schema plugin form configuration.
    -->

  <xsl:template mode="form-builder" match="directive">

    <xsl:variable name="isDisplayed"
                  as="xs:boolean"
                  select="gn-fn-metadata:check-elementandsession-visibility(
                  $schema, $metadata, $serviceInfo, @displayIfRecord, @displayIfServiceInfo)"/>

    <xsl:if test="$isDisplayed">
      <div>
        <xsl:copy-of select="@*"/>
      </div>
    </xsl:if>
  </xsl:template>


  <!-- Create a fieldset in the editor with custom
    legend if attribute name is defined or default
    legend according to the matching element. -->
  <xsl:template mode="form-builder" match="section[@name]|fieldset">
    <xsl:param name="base" as="node()"/>

    <xsl:variable name="isDisplayed"
                  as="xs:boolean"
                  select="gn-fn-metadata:check-elementandsession-visibility(
                  $schema, $metadata, $serviceInfo, @displayIfRecord, @displayIfServiceInfo)"/>

    <xsl:if test="$isDisplayed">
      <xsl:variable name="sectionName" select="@name"/>

      <xsl:choose>
        <xsl:when test="$sectionName">
          <fieldset data-gn-field-highlight="" class="gn-{@name}">
            <!-- Get translation for labels.
            If labels contains ':', search into labels.xml. -->
            <legend>
              <xsl:if test="not(@collapsible)">
                <xsl:attribute name="data-gn-slide-toggle" select="exists(@collapsed)"/>
              </xsl:if>
              <xsl:value-of
                select="if (contains($sectionName, ':'))
                  then gn-fn-metadata:getLabel($schema, $sectionName, $labels)/label
                  else if ($strings/*[name() = $sectionName] != '')
                  then $strings/*[name() = $sectionName]
                  else $sectionName"
              />
            </legend>
            <xsl:apply-templates mode="form-builder" select="@*[name() != 'displayIfRecord']|*">
              <xsl:with-param name="base" select="$base"/>
            </xsl:apply-templates>
          </fieldset>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates mode="form-builder" select="@*[name() != 'displayIfRecord']|*">
            <xsl:with-param name="base" select="$base"/>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>


  <!-- Insert a HTML fragment in the editor from the
  localization files. -->
  <xsl:template mode="form-builder" match="text">
    <xsl:variable name="id" select="@ref"/>
    <xsl:variable name="translation" select="$strings/*[name() = $id]"/>
    <xsl:variable name="text"
                  select="if ($translation) then $translation else ."/>

    <xsl:variable name="isDisplayed"
                  as="xs:boolean"
                  select="gn-fn-metadata:check-elementandsession-visibility(
                  $schema, $metadata, $serviceInfo, @if, @displayIfServiceInfo)"/>

    <xsl:if test="$isDisplayed and $text">
      <xsl:copy-of select="$text/*" copy-namespaces="no"/>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="form-builder" match="action">

    <xsl:variable name="isDisplayed"
                  as="xs:boolean"
                  select="gn-fn-metadata:check-elementandsession-visibility(
                  $schema, $metadata, $serviceInfo, @if, @displayIfServiceInfo)"/>

    <xsl:if test="$isDisplayed">
      <xsl:choose>
        <xsl:when test="@type = 'process' and @process">
          <xsl:call-template name="render-batch-process-button">
            <xsl:with-param name="process-label-key" select="if (@labelKey) then @labelKey else @process"/>
            <xsl:with-param name="process-name" select="@process"/>
            <xsl:with-param name="process-params" select="@params"/>
            <xsl:with-param name="btnClass" select="@btnClass"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="@type = 'suggest' and @process">
          <xsl:call-template name="render-suggest-button">
            <xsl:with-param name="process-name" select="@process"/>
            <xsl:with-param name="process-params" select="@params"/>
            <xsl:with-param name="btnClass" select="@btnClass"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="@type = 'associatedResource'">
          <xsl:variable name="labelKey" select="@name"/>
          <xsl:variable name="label" select="$strings/*[name() = $labelKey]"/>
          <xsl:call-template name="render-associated-resource-button">
            <xsl:with-param name="type" select="@process"/>
            <xsl:with-param name="options" select="directiveAttributes"/>
            <xsl:with-param name="label" select="if ($label != '') then $label else $labelKey"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>


  <!-- Call an XSL template by name.  -->
  <xsl:template mode="form-builder" match="xsl">
    <xsl:param name="base" as="node()"/>

    <xsl:variable name="nodes">
      <saxon:call-template name="{concat('evaluate-', $schema)}">
        <xsl:with-param name="base" select="$base"/>
        <xsl:with-param name="in" select="concat('/../', @xpath)"/>
      </saxon:call-template>
    </xsl:variable>

    <xsl:variable name="mode" select="@mode"/>
    <xsl:variable name="config" select="."/>
    <xsl:for-each select="$nodes/*">
      <xsl:variable name="originalNode"
                    select="gn-fn-metadata:getOriginalNode($base, .)"/>

      <xsl:for-each select="$originalNode">
        <saxon:call-template name="{$mode}">
          <xsl:with-param name="base" select="$base"/>
          <xsl:with-param name="config" select="$config"/>
        </saxon:call-template>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>



  <!-- Element to ignore in that mode -->
  <xsl:template mode="form-builder" match="@name|@collapsed|@collapsible"/>

  <!-- For each field, fieldset and section, check the matching xpath
    is in the current document. In that case dispatch to the schema mode
    or create an XML snippet editor for non matching document based on the
    template element. -->
  <xsl:template mode="form-builder" match="field|fieldset|section[@xpath]">
    <!-- The XML document to edit -->
    <xsl:param name="base" as="node()"/>

    <xsl:if test="@xpath">
      <xsl:variable name="config" select="."/>

      <!-- Seach any nodes in the metadata matching the XPath.

      We could have called saxon-evaluate from here like:
      <xsl:variable name="nodes"
        select="saxon:evaluate(concat('$p1/..', @xpath), $base)"/>
      but this does not work here because namespace of the context
      (ie. this XSLT) are used to resolve the xpath.
      It needs to be in a profile specific XSL which declare all
      profile's namespaces used in XPath expression.

      That's why each schema should define its evaluate-<schemaid> template. -->
      <xsl:variable name="nodes">
        <saxon:call-template name="{concat('evaluate-', $schema)}">
          <xsl:with-param name="base" select="$base"/>
          <xsl:with-param name="in" select="concat('/../', @xpath)"/>
        </saxon:call-template>
      </xsl:variable>

      <!-- Match any gn:child nodes from the metadocument which
      correspond to non existing node but available in the schema. -->
      <xsl:variable name="nonExistingChildParent">
        <xsl:if test="@or and @in">
          <saxon:call-template name="{concat('evaluate-', $schema)}">
            <xsl:with-param name="base" select="$base"/>
            <xsl:with-param name="in"
                            select="concat('/../', @in, '[gn:child/@name=''', @or, ''']')"/>
          </saxon:call-template>
        </xsl:if>
      </xsl:variable>


      <!-- Check if this field is controlled by a condition
          (eg. display that field for service metadata record only).
          If @if expression return false, the field is not displayed. -->
      <xsl:variable name="isDisplayed"
                    as="xs:boolean"
                    select="gn-fn-metadata:check-elementandsession-visibility(
                  $schema, $base, $serviceInfo, @if, @displayIfServiceInfo)"/>

      <!--
      <xsl:message> Field: <xsl:value-of select="@name"/></xsl:message>
      <xsl:message>Xpath: <xsl:copy-of select="@xpath"/></xsl:message>
      <xsl:message>TemplateModeOnly: <xsl:value-of select="@templateModeOnly"/></xsl:message>
      <xsl:message>Display: <xsl:copy-of select="$isDisplayed"/></xsl:message>
      <xsl:message><xsl:value-of select="count($nodes/*)"/> matching nodes: <xsl:copy-of select="$nodes"/></xsl:message>
      <xsl:message>Non existing child path: <xsl:value-of select="concat(@in, '/gn:child[@name = ''', @or, ''']')"/></xsl:message>
      <xsl:message>Non existing child: <xsl:copy-of select="$nonExistingChildParent"/></xsl:message>
      -->


      <xsl:variable name="del" select="@del"/>


      <!-- For non existing node create a XML snippet to be edited
        No match in current document. 2 scenario here:
        1) the requested element is a direct child of a node of the document.
        In that case, a geonet:child element should exist in the document.
        -->
      <xsl:choose>
        <xsl:when test="$isDisplayed and not(@templateModeOnly)">
          <xsl:variable name="configName" select="@name"/>


          <!-- Display the matching node using standard editor mode
          propagating to the schema mode ... -->
          <xsl:for-each select="$nodes">
            <xsl:variable name="translation"
                          select="$strings/*[name() = $configName]"/>
            <xsl:variable name="overrideLabel"
                          select="if ($translation != '')
                                  then $translation
                                  else $configName"/>

            <xsl:if test="$configName != '' and not($overrideLabel)">
              <xsl:message>Label not defined for field name <xsl:value-of select="$configName"/> in loc/{language}/strings.xml.</xsl:message>
            </xsl:if>


            <xsl:choose>
              <xsl:when test="count($nodes/*) = 1">
                <xsl:variable name="originalNode"
                              select="gn-fn-metadata:getOriginalNode($metadata, $nodes/node())"/>


                <!-- Get the reference of the element to delete if delete is allowed. -->
                <xsl:variable name="refToDelete">
                  <xsl:call-template name="get-ref-element-to-delete">
                    <xsl:with-param name="node" select="$originalNode"/>
                    <xsl:with-param name="delXpath" select="$del"/>
                  </xsl:call-template>
                </xsl:variable>

                <saxon:call-template name="{concat('dispatch-', $schema)}">
                  <xsl:with-param name="base" select="$originalNode"/>
                  <xsl:with-param name="overrideLabel"
                                  select="if ($configName != '' and $overrideLabel != '')
                                        then $overrideLabel
                                        else ''"/>
                  <xsl:with-param name="refToDelete" select="$refToDelete/gn:element"/>
                  <xsl:with-param name="config" select="$config"/>
                </saxon:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:for-each select="$nodes/*">
                  <xsl:variable name="originalNode"
                                select="gn-fn-metadata:getOriginalNode($metadata, .)"/>


                  <!-- Get the reference of the element to delete if delete is allowed. -->
                  <xsl:variable name="refToDelete">
                    <xsl:call-template name="get-ref-element-to-delete">
                      <xsl:with-param name="node" select="$originalNode"/>
                      <xsl:with-param name="delXpath" select="$del"/>
                    </xsl:call-template>
                  </xsl:variable>

                  <saxon:call-template name="{concat('dispatch-', $schema)}">
                    <xsl:with-param name="base" select="$originalNode"/>
                    <xsl:with-param name="overrideLabel"
                                    select="if ($configName != '' and $overrideLabel != '')
                                        then $overrideLabel
                                        else ''"/>
                    <xsl:with-param name="refToDelete" select="$refToDelete/gn:element"/>
                    <xsl:with-param name="config" select="$config"/>
                  </saxon:call-template>
                </xsl:for-each>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>


          <!-- Display the matching non existing child node with a + control to
          add it if :
            * a gn:child element is found and the ifNotExist attribute is not set
            in the editor configuration.
            or
            * a gn:child element is found, the matching node does not exist and
            the ifNotExist attribute is set - restrict cardinality to 0..1 even
            if element is 0..n in the schema.
           -->
          <xsl:if test="($nonExistingChildParent/* and not(@ifNotExist)) or
            ($nonExistingChildParent/* and count($nodes/*) = 0 and @ifNotExist)">
            <xsl:variable name="childName" select="@or"/>

            <xsl:for-each select="$nonExistingChildParent/*/gn:child[@name = $childName]">
              <xsl:variable name="name" select="concat(@prefix, ':', @name)"/>

              <xsl:variable name="labelConfig"
                            select="gn-fn-metadata:getLabel($schema, $name, $labels)"/>

              <saxon:call-template name="{concat('dispatch-', $schema)}">
                <xsl:with-param name="base" select="."/>
                <xsl:with-param name="overrideLabel"
                                select="if ($configName != '')
                                        then $strings/*[name() = $configName]
                                        else $labelConfig/label"/>
                <xsl:with-param name="config" select="$config"/>
              </saxon:call-template>
            </xsl:for-each>
          </xsl:if>

        </xsl:when>
        <xsl:when test="$isDisplayed and (@templateModeOnly or template)">
          <!--
              templateModeOnly

              or the requested element is a subchild and is not described in the
            metadocument. This mode will probably take precedence over the others
            if defined in a view.
            -->
          <xsl:variable name="name" select="@name"/>
          <xsl:variable name="del" select="@del"/>
          <xsl:variable name="template" select="template"/>
          <xsl:variable name="forceLabel" select="@forceLabel"/>
          <xsl:for-each select="$nodes/*">
            <!-- Retrieve matching key values
              Only text values are supported. Separator is #.
              -->

            <!--
              When existing, the template should be combined with
              the existing node to add element not available in the template
              and available in the source XML document.

              eg. editing a format which may be defined with the following
              <gmd:distributionFormat>
                  <gmd:MD_Format>
                    <gmd:name>
                      <gco:CharacterString>{{format}}</gco:CharacterString>
                    </gmd:name>
                    <gmd:version>
                      <gco:CharacterString>{{format_version}}</gco:CharacterString>
                    </gmd:version>
                  </gmd:MD_Format>
                </gmd:distributionFormat>
                extra elements (eg. specification) will not be part of the
                templates and removed.

              -->
            <xsl:variable name="currentNode" select="."/>

            <!-- Check if template field values should be in
            readonly mode in the editor.-->
            <xsl:variable name="readonly">
              <xsl:choose>
                <xsl:when test="$template/values/@readonlyIf">
                  <saxon:call-template name="{concat('evaluate-', $schema, '-boolean')}">
                    <xsl:with-param name="base" select="$currentNode"/>
                    <xsl:with-param name="in" select="concat('/', $template/values/@readonlyIf)"/>
                  </saxon:call-template>
                </xsl:when>
              </xsl:choose>
            </xsl:variable>

            <xsl:variable name="templateCombinedWithNode" as="node()">
              <template>
                <xsl:copy-of select="$template/values"/>
                <snippet>
                  <xsl:apply-templates mode="gn-merge" select="$template/snippet/*">
                    <xsl:with-param name="node-to-merge" select="$currentNode"/>
                  </xsl:apply-templates>
                </snippet>
              </template>
            </xsl:variable>

            <xsl:variable name="keyValues">
              <xsl:call-template name="build-key-value-configuration">
                <xsl:with-param name="template" select="$template"/>
                <xsl:with-param name="currentNode" select="$currentNode"/>
                <xsl:with-param name="readonly" select="$readonly"/>
              </xsl:call-template>
            </xsl:variable>

            <xsl:variable name="originalNode"
                          select="gn-fn-metadata:getOriginalNode($metadata, .)"/>

            <xsl:variable name="refToDelete">
              <xsl:call-template name="get-ref-element-to-delete">
                <xsl:with-param name="node" select="$originalNode"/>
                <xsl:with-param name="delXpath" select="$del"/>
              </xsl:call-template>
            </xsl:variable>


            <!-- If the element exist, use the _X<ref> mode which
                  insert the snippet for the element if not use the
                  XPATH mode which will create the new element at the
                  correct location. -->
            <xsl:variable name="id" select="concat('_X', gn:element/@ref, '_replace')"/>
            <xsl:call-template name="render-element-template-field">
              <xsl:with-param name="name" select="$strings/*[name() = $name]"/>
              <xsl:with-param name="id" select="$id"/>
              <xsl:with-param name="isExisting" select="true()"/>
              <xsl:with-param name="template" select="$templateCombinedWithNode"/>
              <xsl:with-param name="keyValues" select="$keyValues"/>
              <xsl:with-param name="refToDelete" select="$refToDelete/gn:element"/>
              <xsl:with-param name="isFirst" select="$forceLabel or position() = 1"/>
            </xsl:call-template>
          </xsl:for-each>


          <!-- The element does not exist in current record.
          Create an empty field with a template. -->
          <xsl:if test="count($nodes/*) = 0 and not(@notDisplayedIfMissing)">
            <!-- If the element exist, use the _X<ref> mode which
            insert the snippet for the element if not use the
            XPATH mode which will create the new element at the
            correct location. -->
            <xsl:variable name="xpathFieldId" select="concat('_P', generate-id())"/>
            <xsl:variable name="id" select="concat($xpathFieldId, '_xml')"/>
            <xsl:variable name="isMissingLabel" select="@isMissingLabel"/>

            <xsl:variable name="currentNode">
              <xsl:apply-templates mode="gn-element-cleaner"
                                   select="$template/snippet/*"/>
            </xsl:variable>

            <xsl:variable name="keyValues">
              <xsl:call-template name="build-key-value-configuration">
                <xsl:with-param name="template" select="$template"/>
                <xsl:with-param name="currentNode" select="$currentNode"/>
                <xsl:with-param name="readonly" select="'false'"/>
              </xsl:call-template>
            </xsl:variable>

            <!-- Node does not exist, stripped gn:copy element from template. -->
            <xsl:variable name="templateWithoutGnCopyElement" as="node()">
              <template>
                <xsl:copy-of select="$template/values"/>
                <snippet>
                  <xsl:copy-of select="$currentNode"/>
                </snippet>
              </template>
            </xsl:variable>

            <xsl:call-template name="render-element-template-field">
              <xsl:with-param name="name" select="$strings/*[name() = $name]"/>
              <xsl:with-param name="id" select="$id"/>
              <xsl:with-param name="xpathFieldId" select="$xpathFieldId"/>
              <xsl:with-param name="isExisting" select="false()"/>
              <xsl:with-param name="keyValues" select="$keyValues"/>
              <xsl:with-param name="template" select="$templateWithoutGnCopyElement"/>
              <xsl:with-param name="isMissingLabel" select="$strings/*[name() = $isMissingLabel]"/>
            </xsl:call-template>
          </xsl:if>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>


  <!-- Get the reference of the element to delete if delete is allowed. -->
  <xsl:template name="get-ref-element-to-delete">
    <xsl:param name="node" as="node()?"/>
    <xsl:param name="delXpath" as="xs:string?"/>
    <xsl:choose>
      <xsl:when test="$delXpath = '.'">
        <xsl:copy-of select="$node/gn:element"/>
      </xsl:when>
      <xsl:when test="$delXpath != ''">
        <!-- Search in the context of the metadata (current context is a node with no parent due to the saxon eval selection. -->
        <xsl:variable name="ancestor">
          <saxon:call-template name="{concat('evaluate-', $schema)}">
            <xsl:with-param name="base" select="$node"/>
            <xsl:with-param name="in"
                            select="concat('/descendant-or-self::node()[gn:element/@ref = ''', $node/gn:element/@ref, ''']/', $delXpath)"/>
          </saxon:call-template>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="exists($ancestor/*/gn:element)">
            <xsl:copy-of select="$ancestor/*/gn:element"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="$node/gn:element"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="$node/gn:element"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="build-key-value-configuration">
    <xsl:param name="template" as="node()"/>
    <xsl:param name="currentNode" as="node()?"/>
    <xsl:param name="readonly"/>

    <xsl:for-each select="$template/values/key">
      <field name="{@label}">
        <xsl:if test="$readonly = 'true'">
          <readonly>true</readonly>
        </xsl:if>

        <xsl:variable name="matchingNodeValue">
          <saxon:call-template name="{concat('evaluate-', $schema)}">
            <xsl:with-param name="base" select="$currentNode"/>
            <xsl:with-param name="in" select="concat('/', @xpath)"/>
          </saxon:call-template>
        </xsl:variable>
        <value>
          <xsl:value-of select="normalize-space($matchingNodeValue)"/>
        </value>

        <!--
        Directive attribute are usually string but could be an XPath
        to evaluate. In that case, the attribute starts with eval#.

        This could be useful when a directive takes care of setting
        more than one value for an element. Eg. a date and an attribute
        like indeterminate position.

        <directiveAttributes
            data-tag-name="gml:endPosition"
            data-indeterminate-position="eval#gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/*/gml:endPosition/@indeterminatePosition"/>
        -->
        <xsl:for-each select="directiveAttributes/attribute::*">
          <xsl:if test="starts-with(., 'eval#')">
            <directiveAttributes name="{name()}">
              <saxon:call-template name="{concat('evaluate-', $schema)}">
                <xsl:with-param name="base" select="$currentNode"/>
                <xsl:with-param name="in" select="concat('/', substring-after(., 'eval#'))"/>
              </saxon:call-template>
            </directiveAttributes>
          </xsl:if>
        </xsl:for-each>

        <!-- If an helper element defined the path to an helper list to
        get from the loc files -->
        <xsl:if test="helper">
          <!-- Get them, it may contains multiple helpers with context (eg. different for service and dataset) -->
          <xsl:variable name="helper"
                        select="gn-fn-metadata:getHelper($schema, helper/@name, helper/@context, helper/@xpath)"/>
          <xsl:variable name="node"
                        select="$metadata/descendant::*[gn:element/@ref = $matchingNodeValue/*/gn:element/@parent]"/>

          <!-- propose the helper matching the current node type -->
          <xsl:choose>
            <xsl:when test="count($helper) > 1 and $node">
              <xsl:variable name="originalNode"
                            select="gn-fn-metadata:getOriginalNode($metadata, $node)"/>
              <!-- If more than one, get the one matching the context of the matching element. -->
              <xsl:variable name="chooseHelperBasedOnElement"
                            select="gn-fn-metadata:getHelper($helper, $originalNode)"/>
              <xsl:copy-of select="$chooseHelperBasedOnElement"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- Return the first helper as the node
              does not exist yet in the record. -->
              <xsl:copy-of select="$helper[1]"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>


        <xsl:if test="codelist">
          <xsl:variable name="listOfValues"
                        select="gn-fn-metadata:getCodeListValues($schema, codelist/@name, $codelists)"/>
          <xsl:copy-of select="$listOfValues"/>
        </xsl:if>

      </field>
    </xsl:for-each>
  </xsl:template>


  <xsl:template mode="form-builder" match="section[@template]">
    <saxon:call-template name="{@template}"/>
  </xsl:template>

  <xsl:template mode="form-builder" match="action[@type='add']">
    <xsl:param name="base" as="node()"/>


    <!-- Match any gn:child nodes from the metadocument which
      correspond to non existing node but available in the schema. -->
    <xsl:variable name="nonExistingChildParent">
      <xsl:if test="@or and @in">
        <saxon:call-template name="{concat('evaluate-', $schema)}">
          <xsl:with-param name="base" select="$base"/>
          <xsl:with-param name="in" select="concat('/../', @in, '[gn:child/@name=''', @or, ''']')"/>
        </saxon:call-template>
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="elementOfSameKind">
      <xsl:if test="@or and @in">
        <saxon:call-template name="{concat('evaluate-', $schema)}">
          <xsl:with-param name="base" select="$base"/>
          <xsl:with-param name="in"
                          select="concat('/../', @in,
                            '/*[local-name() = ''', @or, ''']')"/>
        </saxon:call-template>
      </xsl:if>
    </xsl:variable>

    <!-- Check if this field is controlled by a condition (eg. display that field for
              service metadata record only).
              If @if expression return false, the field is not displayed. -->
    <xsl:variable name="isDisplayed"
                  as="xs:boolean"
                  select="gn-fn-metadata:check-elementandsession-visibility(
                  $schema, $base, $serviceInfo, @if, @displayIfServiceInfo)"/>

    <!--<xsl:message>## Add action</xsl:message>
    <xsl:message><xsl:copy-of select="."/></xsl:message>
    <xsl:message>Is displayed: <xsl:copy-of select="$isDisplayed"/> because no if provided or if attribute XPath '<xsl:value-of select="@if"/>' expression found a match.</xsl:message>
    <xsl:message> = Display action <xsl:value-of select="$nonExistingChildParent/* and $isDisplayed = 'true'"/></xsl:message>-->

    <xsl:if test="$nonExistingChildParent/* and $isDisplayed">
      <xsl:variable name="childName" select="@or"/>

      <!-- Get label from action or from gn:child -->
      <xsl:variable name="elementName"
                    select="$nonExistingChildParent/*/gn:child[@name = $childName]/concat(@prefix, ':', @name)"/>
      <xsl:variable name="btnOverrideName"
                    select="@name"/>
      <xsl:variable name="btnName"
                    select="if ($btnOverrideName)
                            then $strings/*[name() = $btnOverrideName]
                            else ''"/>

      <!-- If multiple elements $elementName contains multiple values. Use the first one in getLabel to avoid failure. -->
      <xsl:variable name="labelConfig"
                    select="gn-fn-metadata:getLabel($schema, $elementName[1], $labels)"/>
      <xsl:variable name="name"
                    select="if ($btnName != '')
                            then $btnName
                            else $labelConfig/label"/>
      <xsl:variable name="class" select="if (@class != '') then @class else $labelConfig/class"/>
      <xsl:variable name="btnLabel" select="if (@btnLabel != '') then @btnLabel else $labelConfig/btnLabel"/>
      <xsl:variable name="btnClass" select="if (@btnClass != '') then @btnLabel else $labelConfig/btnClass"/>
      <xsl:variable name="btnLabelTranslation" select="$strings/*[name() = $btnLabel]"/>

      <xsl:choose>
        <xsl:when test="template">
          <!-- TODO: render-element-to-add should contains all
          logic for add field (based on geonet:child/geonet:choose
          and also when having directives or templates. -->
          <xsl:call-template name="render-element-template-field">
            <xsl:with-param name="name" select="$name"/>
            <xsl:with-param name="id" select="concat('_X',
     $nonExistingChildParent/*[position() = last()]/gn:element/@ref, '_',
     $nonExistingChildParent/*[position() = last()]/gn:child[@name = $childName]/@prefix, 'COLON', @or)"/>
            <xsl:with-param name="isExisting" select="false()"/>
            <xsl:with-param name="template" select="template"/>
            <xsl:with-param name="hasAddAction" select="true()"/>
            <xsl:with-param name="addDirective" select="@addDirective"/>
            <xsl:with-param name="directiveAttributes" select="directiveAttributes"/>
            <xsl:with-param name="parentRef"
                            select="$nonExistingChildParent/*[position() = last()]/gn:element/@ref"/>
            <xsl:with-param name="qname"
                            select="concat($nonExistingChildParent/*[position() = last()]/gn:child[@name = $childName]/@prefix, ':', @or)"/>
            <xsl:with-param name="isFirst" select="@forceLabel or count($elementOfSameKind/*) = 0"/>
            <xsl:with-param name="isAddAction" select="true()"/>
            <xsl:with-param name="btnLabel"
                            select="if ($btnLabelTranslation != '') then $btnLabelTranslation else $btnLabel"/>
            <xsl:with-param name="btnClass" select="@btnClass"/>
            <xsl:with-param name="class" select="@class"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="directive" select="."/>

          <xsl:for-each select="$nonExistingChildParent/*/gn:child[@name = $childName]">
            <xsl:call-template name="render-element-to-add">
              <xsl:with-param name="label" select="$name"/>
              <xsl:with-param name="directive" select="$directive"/>
              <xsl:with-param name="childEditInfo" select="."/>
              <xsl:with-param name="parentEditInfo" select="../gn:element"/>
              <xsl:with-param name="class" select="$class"/>
              <xsl:with-param name="btnClass" select="$btnClass"/>
              <xsl:with-param name="btnLabel" select="$btnLabelTranslation"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

  </xsl:template>
</xsl:stylesheet>
