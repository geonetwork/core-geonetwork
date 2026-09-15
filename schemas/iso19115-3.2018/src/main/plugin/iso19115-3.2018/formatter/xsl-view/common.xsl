<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0" extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">


  <xsl:function name="gn-fn-metadata:getXPath" as="xs:string">
    <xsl:param name="node" as="node()"/>

    <xsl:value-of select="gn-fn-metadata:getXPath($node, false())"/>
  </xsl:function>

  <xsl:function name="gn-fn-metadata:positionOfType" as="xs:string">
    <xsl:param name="node" as="node()"/>
    <xsl:variable name="nodePosition" select="$node/position()"/>
    <xsl:variable name="allPrecedingSiblings"
                  select="$node/preceding-sibling::*[name() = name($node)]"/>
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
</xsl:stylesheet>
