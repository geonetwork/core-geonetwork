<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
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

    <!-- TODO : add fallback schema -->

    <!-- Name with context in current schema -->
    <xsl:variable name="schemaLabelWithContext"
      select="$labels/element[@name=$name and (@context=$xpath or @context=$parent or @context=$parentIsoType)]"/>

    <!-- Name in current schema -->
    <xsl:variable name="schemaLabel" select="$labels/element[@name=$name and not(@context)]"/>

    <xsl:choose>
      <xsl:when test="$schemaLabelWithContext">
        <xsl:copy-of select="$schemaLabelWithContext" copy-namespaces="no"/>
      </xsl:when>
      <xsl:when test="$schemaLabel">
        <xsl:copy-of select="$schemaLabel" copy-namespaces="no"/>
      </xsl:when>
      <xsl:otherwise>
        <element>
          <label>
            <xsl:value-of select="$name"/>
          </label>
        </element>
        <xsl:message>gn-fn-metadata:getLabel | missing translation in schema <xsl:value-of
            select="$schema"/> for <xsl:value-of select="$name"/>.</xsl:message>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:function>


  <!-- Return the list of values for a codelist or <null/>.
  
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

    <xsl:variable name="values" select="$codelists/codelist[@name=$name]"
      exclude-result-prefixes="#all"/>
    <xsl:choose>
      <xsl:when test="$values">
        <xsl:copy-of select="$values"/>
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
    <xsl:param name="helper" as="node()?"/>
    <xsl:param name="node" as="node()"/>
    
    
    <xsl:choose>
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





  <!-- 
    Return the xpath of a node.
  -->
  <xsl:function name="gn-fn-metadata:getXPath" as="xs:string">
    <xsl:param name="node" as="node()"/>

    <!-- Avoid root element. -->
    <xsl:variable name="untilIndex" select="1"/>
    <xsl:variable name="xpathSeparator">/</xsl:variable>
    <xsl:variable name="elementName" select="name($node)"/>
    <xsl:variable name="isAttribute" select="$node/../attribute::*[name() = $elementName]"/>
    <xsl:variable name="ancestors" select="$node/ancestor::*"/>

    <xsl:variable name="xpath">
      <xsl:for-each select="$ancestors[position() != $untilIndex]">
        <xsl:value-of select="concat($xpathSeparator, name(.), '[', position(), ']')"/>
      </xsl:for-each>
    </xsl:variable>

    <xsl:value-of
      select="if ($isAttribute) 
      then concat($xpath, $xpathSeparator, '@', $elementName) 
      else concat($xpath, $xpathSeparator, $elementName, '[', $node/position(), ']')"
    />
  </xsl:function>



</xsl:stylesheet>
