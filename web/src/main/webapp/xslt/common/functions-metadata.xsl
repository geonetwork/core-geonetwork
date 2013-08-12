<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata">
  <!-- Provides XSL function related to metadata management like
  retrieving labels, helper, -->


  <!-- 
    Return the label of an element looking in <schema>/loc/<lang>/labels.xml
  -->
  <xsl:function name="gn-fn-metadata:getLabel" as="xs:string">
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="labels" as="node()"/>

    <!-- TODO : Add xpath support -->
    <xsl:value-of select="string($labels/element[@name=$name and not(@context)]/label)"/>
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
  <xsl:template match="@*|node()[namespace-uri()!='http://www.fao.org/geonetwork']" mode="geonet-cleaner">
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
    
    <xsl:value-of select="if ($isAttribute) 
      then concat($xpath, $xpathSeparator, '@', $elementName) 
      else concat($xpath, $xpathSeparator, $elementName, '[', $node/position(), ']')"/>
  </xsl:function>
  


</xsl:stylesheet>
