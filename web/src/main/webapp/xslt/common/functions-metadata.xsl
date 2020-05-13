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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0" extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">
  <!-- Provides XSL function related to metadata management like
  retrieving labels, helper, -->


  <!--
      Return the original node with its context when a node
       is retrieved using the evaluate template (which cause the
       element to lost its context. Only works on an enumerated
       metadocument (ie. in editing mode).
    -->
  <xsl:function name="gn-fn-metadata:getOriginalNode" as="node()">
    <xsl:param name="metadata" as="node()"/>
    <xsl:param name="evaluatedNode" as="node()"/>

    <xsl:variable name="nodeRef" select="$evaluatedNode/gn:element/@ref"/>
    <xsl:variable name="node" select="$metadata//*[gn:element/@ref = $nodeRef][not(ancestor::svrl:*)]"/>

    <!--<xsl:message>#getOriginalNode ==================</xsl:message>
    <xsl:message><xsl:value-of select="$evaluatedNode/*/gn:element/@ref"/></xsl:message>
    <xsl:message>Match with ref: <xsl:value-of select="$node/gn:element/@ref"/></xsl:message>
    <xsl:message><xsl:copy-of select="$node"/></xsl:message>-->

    <xsl:sequence select="if ($node) then $node else $evaluatedNode"/>
  </xsl:function>

  <!--
    Return the label of an element looking in <schema>/loc/<lang>/labels.xml
  -->
  <xsl:function name="gn-fn-metadata:getLabel" as="node()">
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="labels" as="node()"/>
    <xsl:copy-of select="gn-fn-metadata:getLabel($schema, $name, $labels, '', '', '')"/>
  </xsl:function>


  <!-- Return an element label taking care of the profil,
  the context (ie. parent element) or a complete xpath.
  -->
  <xsl:function name="gn-fn-metadata:getLabel" as="node()">
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="labels" as="node()"/>
    <xsl:param name="parent" as="xs:string?"/>
    <xsl:param name="parentIsoType" as="xs:string?"/>
    <xsl:param name="xpath" as="xs:string?"/>

    <!-- TODO : add fallback schema
    Add try/catch block to log out when a label id duplicated
    in loc files. XSLv3 could be useful for that.
    -->
    <!--
    <xsl:message>#gn-fn-metadata:getLabel</xsl:message>
    <xsl:message>#Element name: <xsl:value-of select="$name"/></xsl:message>
    <xsl:message>#XPath: <xsl:value-of select="$xpath"/></xsl:message>
    <xsl:message>#Parent: <xsl:value-of select="$parent"/></xsl:message>
    -->

    <xsl:variable name="escapedName">
      <xsl:choose>
        <xsl:when test="matches($name, '.*CHOICE_ELEMENT.*')">
          <xsl:value-of select="substring-before($name, 'CHOICE_ELEMENT')"/>
        </xsl:when>
        <xsl:when test="matches($name, '.*GROUP_ELEMENT.*')">
          <xsl:value-of select="substring-before($name, 'GROUP_ELEMENT')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$name"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- Name with context in current schema -->
    <xsl:variable name="schemaLabelWithContextCollection"
                  select="$labels/element[@name=$escapedName and (@context=$xpath or @context=$parent or @context=$parentIsoType)]"/>
    <xsl:variable name="schemaLabelWithContext" select="$schemaLabelWithContextCollection[1]"/>
    <xsl:if test="count($schemaLabelWithContextCollection) > 1">
      <xsl:message>WARNING: gn-fn-metadata:getLabel | multiple labels found for element '<xsl:value-of select="$escapedName"/>' with context=('<xsl:value-of select="$xpath"/>' or '<xsl:value-of select="$parent"/>' or '<xsl:value-of select="$parentIsoType"/>') in schema <xsl:value-of select="$schema"/></xsl:message>
    </xsl:if>

    <!-- Name in current schema -->
    <xsl:variable name="schemaLabel"
                  select="$labels/element[@name=$escapedName and not(@context)]"/>

    <xsl:choose>
      <xsl:when test="$schemaLabelWithContext">
        <xsl:copy-of select="$schemaLabelWithContext" copy-namespaces="no"/>
      </xsl:when>
      <xsl:when test="$schemaLabel">
        <xsl:copy-of select="$schemaLabel" copy-namespaces="no"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with($schema, 'iso19139.')">
            <xsl:copy-of select="gn-fn-metadata:getLabel('iso19139', $name, $iso19139labels,
              $parent, $parentIsoType, $xpath)"/>
          </xsl:when>
          <xsl:otherwise>
            <element>
              <label>
                <xsl:value-of select="$escapedName"/>
              </label>
            </element>
            <xsl:message>gn-fn-metadata:getLabel | missing translation in schema <xsl:value-of select="$schema"/> for <xsl:value-of select="$name"/>.</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:function>


  <!-- Return the list of values for a codelist or <null/>.
  Codelist are defined in codelist.xml.

  One element may have more than one codelists. In that case, the
  conditional codelist define a displayIf attribute with an XPath
  expression.
  eg. Only for services:
  <codelist name="gmd:MD_ScopeCode"
    displayIf="/ancestor::node()[name()='gmd:MD_Metadata']/gmd:identificationInfo/srv:SV_ServiceIdentification">


  The response is:
  <codelist name="gmd:MD_ScopeCode">
    <entry>
      <code>attribute</code>
      <label>Attribute</label>
      <description>Information applies to the attribute class</description>
    </entry>
  -->

  <xsl:function name="gn-fn-metadata:getCodeListValues" as="node()">
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="codelists" as="node()"/>
    <xsl:copy-of select="gn-fn-metadata:getCodeListValues($schema, $name, $codelists, false())"/>
  </xsl:function>

  <xsl:function name="gn-fn-metadata:getCodeListValues" as="node()">
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="codelists" as="node()"/>
    <xsl:param name="node" as="item()?"/>

    <xsl:variable name="codelists" select="$codelists/codelist[@name=$name]"
                  exclude-result-prefixes="#all"/>

    <!--
    <xsl:message>#gn-fn-metadata:getCodeListValues</xsl:message>
    <xsl:message>#Schema: <xsl:value-of select="$schema"/> </xsl:message>
    <xsl:message>#Element name: <xsl:value-of select="$name"/> </xsl:message>
    <xsl:message>#Codelist found: <xsl:copy-of select="$codelists"/> </xsl:message>
    -->

    <!-- Conditional helpers which may define an xpath expression to evaluate
        if the xpath match. Check all codelists if one define an expression.
        If the expression return a node, this codelist will be returned. -->
    <xsl:variable name="conditionalCodelist">
      <xsl:if test="$node">
        <xsl:for-each select="$codelists">
          <xsl:if test="@displayIf">
            <xsl:variable name="match">
              <saxon:call-template name="{concat('evaluate-', $schema)}">
                <xsl:with-param name="base"
                                select="$metadata/descendant-or-self::node()[gn:element/@ref = $node/gn:element/@ref]"/>
                <xsl:with-param name="in" select="concat('/', @displayIf)"/>
              </saxon:call-template>
            </xsl:variable>
            <xsl:if test="$match != ''">
              <xsl:copy-of select="."/>
            </xsl:if>
          </xsl:if>
        </xsl:for-each>
      </xsl:if>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$conditionalCodelist/*">
        <xsl:copy-of select="$conditionalCodelist/codelist"/>
      </xsl:when>
      <xsl:when test="$codelists">
        <!-- Return the default -->
        <codelist>
          <xsl:copy-of select="$codelists[not(@displayIf)]/@*"/>
          <xsl:copy-of select="$codelists[not(@displayIf)]/*[not(@hideInEditMode)]"/>
        </codelist>
      </xsl:when>
      <xsl:otherwise>
        <null/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!--
    Return the list of helper for an element looking in <schema>/loc/<lang>/labels.xml
    helper tag.


      In editing mode, for gco:CharacterString elements (with no codelist
      or enumeration defined in the schema) an helper list could be defined
      in loc files (labels.xml) using the helper tag.
      <element name="gmd:denominator" id="57.0">
          <label>Denominator</label>
          <helper>
            <option value="5000">1:5´000</option>
            <option value="10000">1:10´000</option>
            ...

      Then a list of values is displayed next to the input field.

      The helper list could be sorted if stort attribute is set to true:
      <helper sort="true" ...

      By default, the list of suggestion is displayed in a combo next to the
      element. The layout may be customized by setting the editorMode. Supported
      modes are:
      * radio
      * radio_withdesc
      * radio_linebreak

      <helper editorMode="radio_withdesc" ...

      One related element (sibbling) could be link to current element using the @rel attribute.
      This related element is updated with the title value of the selected option.
  -->
  <xsl:function name="gn-fn-metadata:getHelper" as="node()">
    <xsl:param name="helper" as="node()*"/>
    <xsl:param name="node" as="node()"/>

    <xsl:choose>
      <xsl:when test="$helper[@displayIf]">

        <!-- Search for the related element identifier -->
        <xsl:variable name="relatedElementRef"
                      select="$node/../*[name()=$helper/@rel]/*/gn:element/@ref"/>

        <helper>
          <xsl:attribute name="relElementRef" select="$relatedElementRef"/>
          <xsl:copy-of select="$helper/@*"/>

          <xsl:variable name="helpersMatchingCurrentRecord">
            <xsl:for-each select="$helper[@displayIf]">
              <xsl:variable name="match">
                <saxon:call-template name="{concat('evaluate-', $schema)}">
                  <xsl:with-param name="base"
                                  select="$metadata/descendant-or-self::node()[gn:element/@ref = $node/gn:element/@ref]"/>
                  <xsl:with-param name="in" select="concat('/', @displayIf)"/>
                </saxon:call-template>
              </xsl:variable>

              <xsl:if test="$match/*">
                <xsl:copy-of select="option"/>
              </xsl:if>
            </xsl:for-each>
          </xsl:variable>

          <xsl:choose>
            <xsl:when
              test="count($helpersMatchingCurrentRecord/*) > 0">
              <xsl:copy-of select="$helpersMatchingCurrentRecord"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- The default helper is the one with no condition. -->
              <xsl:copy-of select="$helper[not(@displayIf)]/*"/>
            </xsl:otherwise>
          </xsl:choose>
        </helper>
      </xsl:when>

      <xsl:when test="$helper">
        <!-- Search for the related element identifier -->
        <xsl:variable name="relatedElementRef"
                      select="$node/../*[name()=$helper/@rel]/*/gn:element/@ref"/>

        <helper>
          <xsl:attribute name="relElementRef" select="$relatedElementRef"/>
          <xsl:copy-of select="$helper/@*"/>
          <xsl:copy-of select="$helper/*"/>
        </helper>
      </xsl:when>
      <xsl:otherwise>
        <null/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!-- Get helper -->
  <xsl:function name="gn-fn-metadata:getHelper" as="node()*">
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="context" as="xs:string?"/>
    <xsl:param name="xpath" as="xs:string?"/>

    <!-- Name with context in current schema -->
    <xsl:variable name="helper"
                  select="if (string($xpath) or string($context))
                          then $labels/element[@name=$name and (@context=$xpath or @context=$context)]/helper
                          else $labels/element[@name=$name and not(string(@context))]/helper"/>

    <xsl:choose>
      <xsl:when test="$helper">
        <xsl:copy-of select="$helper" copy-namespaces="no"/>
      </xsl:when>
      <xsl:otherwise>
        <null/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:function>


  <!-- Get field type based on editor configuration.
  Search by element name or the child element name (the one
  containing the value).

  The child element take priority if defined.
  -->
  <xsl:function name="gn-fn-metadata:getFieldType" as="xs:string">
    <xsl:param name="configuration" as="node()"/>
    <!-- The container element -->
    <xsl:param name="name" as="xs:string"/>
    <!-- The element containing the value eg. gco:Date -->
    <xsl:param name="childName" as="xs:string?"/>
    <xsl:param name="xpath" as="xs:string?"/>

    <xsl:variable name="childType"
                  select="normalize-space($configuration/editor/fields/for[@name = $childName]/@use)"/>
    <xsl:variable name="childTypeXpath"
                  select="normalize-space($configuration/editor/fields/for[@name = $childName and @xpath = $xpath]/@use)"/>
    <xsl:variable name="type"
                  select="normalize-space($configuration/editor/fields/for[@name = $name and not(@xpath)]/@use)"/>
    <xsl:variable name="typeXpath"
                  select="normalize-space($configuration/editor/fields/for[@name = $name and @xpath = $xpath]/@use)"/>

    <xsl:value-of
      select="if ($childTypeXpath != '')
      then $childTypeXpath
      else if ($childType != '')
      then $childType
      else if ($typeXpath != '')
      then $typeXpath
      else if ($type != '')
      then $type
      else $defaultFieldType"
    />

  </xsl:function>

  <xsl:function name="gn-fn-metadata:getAttributeFieldType" as="xs:string">
    <xsl:param name="configuration" as="node()"/>
    <!-- The container element gmx:fileName/@src-->
    <xsl:param name="attributeNameWithParent" as="xs:string"/>

    <xsl:variable name="type"
                  select="normalize-space($configuration/editor/fields/for[@name = $attributeNameWithParent]/@use)"/>

    <xsl:value-of
      select="if ($type != '')
      then $type
      else $defaultFieldType"
    />
  </xsl:function>


  <!-- Return the directive to use for editing. -->
  <xsl:function name="gn-fn-metadata:getFieldDirective" as="node()">
    <xsl:param name="configuration" as="node()"/>
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="xpath" as="xs:string?"/>

    <xsl:variable name="type"
                  select="$configuration/editor/fields/for[@name = $name and starts-with(@use, 'data-') and not(@xpath)]"/>
    <xsl:variable name="typeWithXpath"
                  select="$configuration/editor/fields/for[@name = $name and starts-with(@use, 'data-') and @xpath = $xpath]"/>
    <xsl:choose>
      <xsl:when test="$typeWithXpath">
        <xsl:element name="directive">
          <xsl:attribute name="data-directive-name" select="$typeWithXpath/@use"/>
          <xsl:copy-of select="$typeWithXpath/directiveAttributes/@*"/>
        </xsl:element>
      </xsl:when>
      <xsl:when test="$type">
        <xsl:element name="directive">
          <xsl:attribute name="data-directive-name" select="$type/@use"/>
          <xsl:copy-of select="$type/directiveAttributes/@*"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <null/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>


  <!-- Return the directive to use for add control if a custom one
  is defined. Eg. Adding from a thesaurus propose a list of available
  thesaurus. -->
  <xsl:function name="gn-fn-metadata:getFieldAddDirective" as="node()">
    <xsl:param name="configuration" as="node()"/>
    <xsl:param name="name" as="xs:string"/>

    <xsl:variable name="type"
                  select="$configuration/editor/fields/for[@name = $name and @addDirective]"/>
    <xsl:choose>
      <xsl:when test="$type">
        <xsl:copy-of select="$type" copy-namespaces="no"/>
      </xsl:when>
      <xsl:otherwise>
        <null/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="gn-fn-metadata:getFieldAddDirectiveAttributes"
                as="attribute()*">
    <xsl:param name="configuration" as="node()"/>
    <xsl:param name="name" as="xs:string"/>

    <xsl:copy-of select="$configuration/editor/fields/
          for[@name = $name and @addDirective]/
          directiveAttributes/@*"/>
  </xsl:function>

  <!-- Return if a flat mode exception has been defined in the current view for a field. -->
  <xsl:function name="gn-fn-metadata:isFieldFlatModeException" as="xs:boolean">
    <xsl:param name="configuration" as="node()?"/>
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="parent" as="xs:string?" />

    <xsl:choose>
      <xsl:when test="not($configuration)">
        <xsl:value-of select="false()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="exception"
                      select="if (string($parent))
                  then count($configuration/flatModeExceptions/for[@name = $name and (not(@excludeFrom) or (@excludeFrom and not(contains(@excludeFrom, $parent))))])
                  else count($configuration/flatModeExceptions/for[@name = $name])"/>

        <xsl:value-of select="if ($exception > 0)
                      then true()
                      else false()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="gn-fn-metadata:getXPath" as="xs:string">
    <xsl:param name="node" as="node()"/>

    <xsl:value-of select="gn-fn-metadata:getXPath($node, false())"/>
  </xsl:function>

  <xsl:function name="gn-fn-metadata:positionOfType" as="xs:string">
    <xsl:param name="node" as="node()"/>
    <xsl:variable name="nodePosition" select="$node/position()"/>
    <xsl:variable name="allPrecedingSiblings"
                  select="$node/preceding-sibling::*[name() = name($node)]"/>
    <!--<xsl:value-of select="count($node/../*[name = name($node) and position() &lt; $nodePosition]) + 1"/>-->
    <xsl:value-of select="count($allPrecedingSiblings) + 1"/>
  </xsl:function>

  <!--
    Return the xpath of a node.
  -->
  <xsl:function name="gn-fn-metadata:getXPath" as="xs:string">
    <xsl:param name="node" as="node()"/>
    <xsl:param name="withPosition" as="xs:boolean"/>

    <!-- Avoid root element. -->
    <xsl:variable name="untilIndex" select="1"/>
    <xsl:variable name="xpathSeparator">/</xsl:variable>
    <xsl:variable name="elementName" select="name($node)"/>
    <xsl:variable name="isAttribute" select="$node/../attribute::*[name() = $elementName]"/>
    <xsl:variable name="ancestors" select="$node/ancestor::*"/>

    <xsl:variable name="xpath">
      <xsl:for-each select="$ancestors[position() != $untilIndex]">
        <xsl:value-of select="if ($withPosition)
          then concat($xpathSeparator, name(.), '[', gn-fn-metadata:positionOfType(.), ']')
          else concat($xpathSeparator, name(.))"/>
      </xsl:for-each>
    </xsl:variable>

    <xsl:value-of
      select="if ($isAttribute)
      then concat($xpath, $xpathSeparator, '@', $elementName)
      else if ($withPosition)
        then concat($xpath, $xpathSeparator, $elementName, '[', gn-fn-metadata:positionOfType($node), ']')
        else concat($xpath, $xpathSeparator, $elementName)
      "
    />
  </xsl:function>

  <xsl:function name="gn-fn-metadata:getXPathByRef" as="xs:string">
    <xsl:param name="nodeRef" as="xs:string"/>
    <xsl:param name="md" as="node()"/>
    <xsl:param name="withPosition" as="xs:boolean"/>

    <!-- when walking thru expanded document with validation report info, ignore report info avoid ing multiple matches-->
    <xsl:variable name="node" select="$md/descendant::node()[gn:element/@ref = $nodeRef][not(ancestor::*[name() = 'geonet:report'])]"/>

    <xsl:value-of select="gn-fn-metadata:getXPath($node, $withPosition)"/>
  </xsl:function>


</xsl:stylesheet>
