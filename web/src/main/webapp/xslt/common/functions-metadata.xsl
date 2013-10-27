<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata">
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

  <xsl:function name="gn-fn-metadata:getLabel" as="node()">
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="labels" as="node()"/>
    <xsl:param name="parent" as="xs:string"/>
    <xsl:param name="parentIsoType" as="xs:string"/>
    <xsl:param name="xpath" as="xs:string"/>

    <!-- TODO : add fallback schema -->
    
    <!-- Name with context in current schema -->
    <xsl:variable name="schemaLabelWithContext"
      select="$labels/element[@name=$name and (@context=$xpath or @context=$parent or @context=$parentIsoType)]"/>
    
    <!-- Name in current schema -->
    <xsl:variable name="schemaLabel"
      select="$labels/element[@name=$name and not(@context)]"/>
    
    <xsl:choose>
      <xsl:when
        test="$schemaLabelWithContext"><xsl:copy-of select="$schemaLabelWithContext"/>
      </xsl:when>
      <xsl:when
        test="$schemaLabel"><xsl:copy-of select="$schemaLabel"/>
      </xsl:when>
      <xsl:otherwise>
        <element><label><xsl:value-of select="$name"/></label></element>
        <xsl:message>gn-fn-metadata:getLabel | missing translation in schema <xsl:value-of select="$schema"/> for <xsl:value-of select="$name"/>.</xsl:message>
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

    <xsl:variable name="values" select="$codelists/codelist[@name=$name]"/>
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
  -->
  <xsl:function name="gn-fn-metadata:getHelper" as="xs:string">
    <xsl:text/>
  </xsl:function>



  <!-- Copy all elements and attributes excluding GeoNetwork elements. 
    This could be useful to get the source XML when working on a metadocument.
  -->
  <xsl:template match="@*|node()[namespace-uri()!='http://www.fao.org/geonetwork']"
    mode="geonet-cleaner">
    <xsl:copy>
      <xsl:copy-of select="@*[namespace-uri()!='http://www.fao.org/geonetwork']"/>
      <xsl:apply-templates select="node()" mode="geonet-cleaner"/>
    </xsl:copy>
  </xsl:template>



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
