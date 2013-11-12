<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:gn-fn-dublin-core="http://geonetwork-opensource.org/xsl/functions/profiles/dublin-core">



  <!-- Get field type based on editor configuration.
  Search by element name or the child element name (the one
  containing the value).
  
  The child element take priority if defined.
  -->
  <xsl:function name="gn-fn-dublin-core:getFieldType" as="xs:string">
    <xsl:param name="name" as="xs:string"/>

    <xsl:value-of select="gn-fn-metadata:getFieldType($dublin-coreEditorConfiguration, $name, '')"/>
  </xsl:function>


  <xsl:function name="gn-fn-dublin-core:getFieldWidget" as="xs:string">
    <xsl:param name="name" as="xs:string"/>

    <xsl:value-of select="if ($name = 'dc:coverage') 
      then 'bbox' 
      else 'text'"/>
  </xsl:function>
</xsl:stylesheet>
