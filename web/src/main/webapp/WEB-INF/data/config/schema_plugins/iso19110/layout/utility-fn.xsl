<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:gn-fn-iso19110="http://geonetwork-opensource.org/xsl/functions/profiles/iso19110"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  exclude-result-prefixes="#all">


  <!-- Get field type based on editor configuration.
  Search by element name or the child element name (the one
  containing the value). ISO19139 defined types are returned
  if not found.
  
  The child element take priority if defined.
  -->
  <xsl:function name="gn-fn-iso19110:getFieldType" as="xs:string">
    <!-- The container element -->
    <xsl:param name="name" as="xs:string"/>
    <!-- The element containing the value eg. gco:Date -->
    <xsl:param name="childName" as="xs:string?"/>

    <xsl:variable name="iso19110type"
      select="gn-fn-metadata:getFieldType($editorConfig, $name, $childName)"/>

    <xsl:choose>
      <xsl:when test="$iso19110type = $defaultFieldType">
        <xsl:value-of
          select="gn-fn-metadata:getFieldType($iso19139EditorConfig, $name, $childName)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$iso19110type"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
