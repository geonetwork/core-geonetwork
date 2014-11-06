<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:saxon="http://saxon.sf.net/" extension-element-prefixes="saxon"
  exclude-result-prefixes="#all" version="2.0">
  <!-- 
    Build the form from the schema plugin form configuration.
    -->


  <!-- Create a fieldset in the editor with custom
    legend if attribute name is defined or default 
    legend according to the matching element. -->
  <xsl:template mode="form-builder" match="section[@name]|fieldset">
    <xsl:param name="base" as="node()"/>

    <xsl:variable name="sectionName" select="@name"/>

    <xsl:choose>
      <xsl:when test="$sectionName">
        <fieldset>
          <!-- Get translation for labels.
          If labels contains ':', search into labels.xml. -->
          <legend>
            <xsl:value-of
              select="if (contains($sectionName, ':')) 
                then gn-fn-metadata:getLabel($schema, $sectionName, $labels)/label 
                else $strings/*[name() = $sectionName]"
            />
          </legend>
          <xsl:apply-templates mode="form-builder" select="@*|*">
            <xsl:with-param name="base" select="$base"/>
          </xsl:apply-templates>
        </fieldset>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="form-builder" select="@*|*">
          <xsl:with-param name="base" select="$base"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Insert a HTML fragment in the editor from the
  localization files. -->
  <xsl:template mode="form-builder" match="text">
    <xsl:variable name="id" select="@ref"/>
    <xsl:variable name="text" select="$strings/*[name() = $id]"/>
    <xsl:if test="$text">
      <xsl:copy-of select="$text/*" copy-namespaces="no"/>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="form-builder" match="action">
    <xsl:variable name="match">
      <xsl:choose>
        <xsl:when test="@if">
          <saxon:call-template name="{concat('evaluate-', $schema, '-boolean')}">
            <xsl:with-param name="base" select="$metadata"/>
            <xsl:with-param name="in" select="concat('/../', @if)"/>
          </saxon:call-template>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="false()"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:if test="$match = true()">
      <xsl:call-template name="render-batch-process-button">
        <xsl:with-param name="process-name" select="@process"/>
        <xsl:with-param name="process-params" select="@params"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <!-- Element to ignore in that mode -->
  <xsl:template mode="form-builder" match="@name"/>

  <!-- For each field, fieldset and section, check the matching xpath
    is in the current document. In that case dispatch to the schema mode
    or create an XML snippet editor for non matching document based on the
    template element. -->
  <xsl:template mode="form-builder" match="field|fieldset|section[@xpath]">
    <!-- The XML document to edit -->
    <xsl:param name="base" as="node()"/>

    <xsl:if test="@xpath">
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
            <xsl:with-param name="in" select="concat('/../', @in, '[gn:child/@name=''', @or, ''']')"/>
          </saxon:call-template>
        </xsl:if>
      </xsl:variable>




      <!-- Check if this field is controlled by a condition
          (eg. display that field for service metadata record only).
          If @if expression return false, the field is not displayed. -->
      <xsl:variable name="isDisplayed">
        <xsl:choose>
          <xsl:when test="@if">
            <saxon:call-template name="{concat('evaluate-', $schema, '-boolean')}">
              <xsl:with-param name="base" select="$base"/>
              <xsl:with-param name="in" select="concat('/../', @if)"/>
            </saxon:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="true()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      
      <!--
      <xsl:message> Field: <xsl:value-of select="@name"/></xsl:message>
      <xsl:message>Xpath: <xsl:copy-of select="@xpath"/></xsl:message>
      <xsl:message>TemplateModeOnly: <xsl:value-of select="@templateModeOnly"/></xsl:message>
      <xsl:message>If: <xsl:copy-of select="@if"/></xsl:message>
      <xsl:message>Display: <xsl:copy-of select="$isDisplayed"/></xsl:message>
      <xsl:message>Matching nodes: <xsl:copy-of select="$nodes"/></xsl:message>
      <xsl:message>Non existing child path: <xsl:value-of select="concat(@in, '/gn:child[@name = ''', @or, ''']')"/></xsl:message>
      <xsl:message>Non existing child: <xsl:copy-of select="$nonExistingChildParent"/></xsl:message>
      -->




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
            <saxon:call-template name="{concat('dispatch-', $schema)}">
              <xsl:with-param name="base" select="."/>
              <xsl:with-param name="overrideLabel"
                              select="if ($configName != '')
                                      then $strings/*[name() = $configName]
                                      else ''"/>
            </saxon:call-template>
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
              
              <xsl:variable name="directive" select="gn-fn-metadata:getFieldAddDirective($editorConfig, $name)"/>
              <xsl:call-template name="render-element-to-add">
                <xsl:with-param name="label"
                  select="if ($configName != '') 
                          then $strings/*[name() = $configName] 
                          else gn-fn-metadata:getLabel($schema, $name, $labels)/label"/>
                <xsl:with-param name="directive" select="$directive"/>
                <xsl:with-param name="childEditInfo" select="."/>
                <xsl:with-param name="parentEditInfo" select="../gn:element"/>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:if>

        </xsl:when>
        <xsl:when test="$isDisplayed = 'true' and (@templateModeOnly or template)">
          <!-- 
              templateModeOnly 
              
              or the requested element is a subchild and is not described in the
            metadocument. This mode will probably take precedence over the others
            if defined in a view.
            -->
          <xsl:variable name="name" select="@name"/>
          <xsl:variable name="del" select="@del"/>
          <xsl:variable name="template" select="template"/>
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
                  <value><xsl:value-of select="normalize-space($matchingNodeValue)"/></value>

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
                    <xsl:variable name="helper" select="gn-fn-metadata:getHelper($schema, helper/@name, helper/@context, helper/@xpath)"/>
                    
                    <xsl:choose>
                      <xsl:when test="count($helper) > 1">
                        <!-- If more than one, get the one matching the context of the matching element. -->
                        <xsl:variable name="chooseHelperBasedOnElement" 
                          select="gn-fn-metadata:getHelper($helper, 
                          $metadata/descendant::*[gn:element/@ref = $matchingNodeValue/*/gn:element/@parent])"/>
                        <xsl:copy-of select="$chooseHelperBasedOnElement"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:copy-of select="$helper"/>
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
            </xsl:variable>
            
            <!-- Get the reference of the element to delete if delete is allowed. -->
            <xsl:variable name="refToDelete">
              <xsl:if test="$del != ''">
                <xsl:choose>
                  <xsl:when test="$del = '.'">
                    <xsl:copy-of select="$currentNode/gn:element"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <!-- Search in the context of the metadata (current context is a node with no parent
                due to the saxon eval selection. -->
                    <xsl:variable name="ancestor">
                      <saxon:call-template name="{concat('evaluate-', $schema)}">
                        <xsl:with-param name="base" select="$base"/>
                        <xsl:with-param name="in" select="concat('/descendant-or-self::node()[gn:element/@ref = ''', $currentNode/gn:element/@ref, ''']/', $del)"/>
                      </saxon:call-template>
                    </xsl:variable>
                    
                    <xsl:copy-of select="$ancestor/*/gn:element"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:if>
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
              <xsl:with-param name="refToDelete" select="if ($refToDelete) then $refToDelete/gn:element else ''"/>
              <xsl:with-param name="isFirst" select="position() = 1"/>
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

            <!-- Node does not exist, stripped gn:copy element from template. -->
            <xsl:variable name="templateWithoutGnCopyElement" as="node()">
              <template>
                <xsl:copy-of select="$template/values"/>
                <snippet>
                  <xsl:apply-templates mode="gn-element-cleaner"
                                       select="$template/snippet/*"/>
                </snippet>
              </template>
            </xsl:variable>

            <xsl:call-template name="render-element-template-field">
              <xsl:with-param name="name" select="$strings/*[name() = $name]"/>
              <xsl:with-param name="id" select="$id"/>
              <xsl:with-param name="xpathFieldId" select="$xpathFieldId"/>
              <xsl:with-param name="isExisting" select="false()"/>
              <xsl:with-param name="template" select="$templateWithoutGnCopyElement"/>
              <xsl:with-param name="isMissingLabel" select="$strings/*[name() = $isMissingLabel]"/>
            </xsl:call-template>
          </xsl:if>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
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
    <xsl:variable name="isDisplayed">
      <xsl:choose>
        <xsl:when test="@if">
          <saxon:call-template name="{concat('evaluate-', $schema, '-boolean')}">
            <xsl:with-param name="base" select="$base"/>
            <xsl:with-param name="in" select="concat('/../', @if)"/>
          </saxon:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="true()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:if test="$nonExistingChildParent/* and $isDisplayed = 'true'">
      <!-- The element does not exist in current record. 
          Add an action to add an element. -->
      <xsl:variable name="name" select="@name"/>
      <xsl:variable name="childName" select="@or"/>

      <xsl:call-template name="render-element-template-field">
        <xsl:with-param name="name" select="$strings/*[name() = $name]"/>
        <xsl:with-param name="id" select="concat('_X', 
          $nonExistingChildParent/*[position() = last()]/gn:element/@ref, '_', 
          $nonExistingChildParent/*[position() = last()]/gn:child[@name = $childName]/@prefix, 'COLON', @or)"/>
        <xsl:with-param name="isExisting" select="false()"/>
        <xsl:with-param name="template" select="template"/>
        <xsl:with-param name="hasAddAction" select="true()"/>
        <xsl:with-param name="addDirective" select="@addDirective"/>
        <xsl:with-param name="directiveAttributes" select="directiveAttributes"/>
        <xsl:with-param name="parentRef" select="$nonExistingChildParent/*[position() = last()]/gn:element/@ref"/>
        <xsl:with-param name="qname" select="concat($nonExistingChildParent/*[position() = last()]/gn:child[@name = $childName]/@prefix, ':', @or)"/>
        <xsl:with-param name="isFirst" select="@forceLabel or count($elementOfSameKind/*) = 0"/>
        <xsl:with-param name="isAddAction" select="true()"/>
      </xsl:call-template>
    </xsl:if>
    
  </xsl:template>
  
</xsl:stylesheet>
