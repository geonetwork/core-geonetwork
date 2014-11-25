<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:saxon="http://saxon.sf.net/" extension-element-prefixes="saxon"
  exclude-result-prefixes="#all">
  <!-- Provides XSL function related to metadata management like
  retrieving labels, helper, -->

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
    <!--<xsl:message>#<xsl:value-of select="$name"/></xsl:message>
    <xsl:message>#<xsl:value-of select="$xpath"/></xsl:message>
    <xsl:message>#<xsl:value-of select="$parent"/></xsl:message>-->
    
    <xsl:variable name="escapedName">
      <xsl:choose>
        <xsl:when test="matches($name, '.*CHOICE_ELEMENT.*')">
          <xsl:value-of select="substring-before($name, 'CHOICE_ELEMENT')"/>
        </xsl:when>
        <xsl:when test="matches($name, '.*GROUP_ELEMENT.*')">
          <xsl:value-of select="substring-before($name, 'GROUP_ELEMENT')"/>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="$name"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <!-- Name with context in current schema -->
    <xsl:variable name="schemaLabelWithContext"
      select="$labels/element[@name=$escapedName and (@context=$xpath or @context=$parent or @context=$parentIsoType)]"/>
    
    <!-- Name in current schema -->
    <xsl:variable name="schemaLabel" select="$labels/element[@name=$escapedName and not(@context)]"/>

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
            <xsl:message>gn-fn-metadata:getLabel | missing translation in schema <xsl:value-of
              select="$schema"/> for <xsl:value-of select="$name"/>.</xsl:message>
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
    
    <!-- Conditional helpers which may define an xpath expression to evaluate 
        if the xpath match. Check all codelists if one define an expression.
        If the expression return a node, this codelist will be returned. -->
    <xsl:variable name="conditionalCodelist">
      <xsl:if test="$node">
        <xsl:for-each select="$codelists">
          <xsl:if test="@displayIf">
            <xsl:variable name="match">
              <xsl:call-template name="evaluate-iso19139">
                <xsl:with-param name="base" select="$metadata/descendant-or-self::node()[gn:element/@ref = $node/gn:element/@ref]"/>
                <xsl:with-param name="in" select="concat('/', @displayIf)"/>
              </xsl:call-template>
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
          <xsl:for-each select="$helper[@displayIf]">
            <xsl:variable name="match">
              <saxon:call-template name="{concat('evaluate-', $schema)}">
                <xsl:with-param name="base" select="$metadata/descendant-or-self::node()[gn:element/@ref = $node/gn:element/@ref]"/>
                <xsl:with-param name="in" select="concat('/', @displayIf)"/>
              </saxon:call-template>
            </xsl:variable>

            <xsl:choose>
              <xsl:when test="$match/*">
                <xsl:copy-of select="option"/>
              </xsl:when>
              <xsl:when test="$helper[not(@displayIf)]">
                <!-- The defautl helper is the one with no condition. -->
                <xsl:copy-of select="$helper[not(@displayIf)]/*"/>
              </xsl:when>
              <xsl:otherwise>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
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
      select="$labels/element[@name=$name and (@context=$xpath or @context=$context)]/helper"/>
    
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
    
    <xsl:variable name="childType" select="normalize-space($configuration/editor/fields/for[@name = $childName]/@use)"/>
    <xsl:variable name="type" select="normalize-space($configuration/editor/fields/for[@name = $name]/@use)"/>
    
    <xsl:value-of
      select="if ($childType != '') 
      then $childType 
      else if ($type != '')
      then $type 
      else $defaultFieldType"
    />
  </xsl:function>


  <!-- Return the directive to use for add control if a custom one 
  is defined. Eg. Adding from a thesaurus propose a list of available
  thesaurus. -->
  <xsl:function name="gn-fn-metadata:getFieldAddDirective" as="node()">
    <xsl:param name="configuration" as="node()"/>
    <xsl:param name="name" as="xs:string"/>
    
    <xsl:variable name="type" select="$configuration/editor/fields/for[@name = $name and @addDirective]"/>
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
    <xsl:param name="configuration" as="node()"/>
    <xsl:param name="name" as="xs:string"/>

    <xsl:variable name="exception" select="count($configuration/flatModeExceptions/for[@name = $name])"/>
   
    <xsl:value-of
        select="if ($exception > 0)
      then true()
      else false()"
        />
  </xsl:function>

  <xsl:function name="gn-fn-metadata:getXPath" as="xs:string">
    <xsl:param name="node" as="node()"/>
    
    <xsl:value-of select="gn-fn-metadata:getXPath($node, false())"/>
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
          then concat($xpathSeparator, name(.), '[', position(), ']')
          else concat($xpathSeparator, name(.))"/>
      </xsl:for-each>
    </xsl:variable>

    <xsl:value-of
      select="if ($isAttribute) 
      then concat($xpath, $xpathSeparator, '@', $elementName) 
      else if ($withPosition) 
        then concat($xpath, $xpathSeparator, $elementName, '[', $node/position(), ']')
        else concat($xpath, $xpathSeparator, $elementName)
      "
    />
  </xsl:function>

  <xsl:function name="gn-fn-metadata:getXPathByRef" as="xs:string">
    <xsl:param name="nodeRef" as="xs:string"/>
    <xsl:param name="md" as="node()"/>
    <xsl:param name="withPosition" as="xs:boolean"/>
    
    <xsl:variable name="node" select="$md/descendant::node()[gn:element/@ref = $nodeRef]"/>
    
    <xsl:value-of select="gn-fn-metadata:getXPath($node, $withPosition)"/>
  </xsl:function>
  


</xsl:stylesheet>
