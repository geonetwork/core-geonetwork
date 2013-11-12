<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gn-fn-dublin-core="http://geonetwork-opensource.org/xsl/functions/profiles/dublin-core" >
  
  
  
  <xsl:function name="gn-fn-dublin-core:getFieldType" as="xs:string">
    <xsl:param name="name" as="xs:string"/>
    
    <xsl:value-of select="if ($name = 'dc:description') 
      then 'textarea' 
      else if ($name = 'time') 
      then 'time'
      else if ($name = 'dc:date' or $name = 'dct:modified') 
      then 'date' 
      else 'text'"/>
  </xsl:function>
  
  
  <xsl:function name="gn-fn-dublin-core:getFieldWidget" as="xs:string">
    <xsl:param name="name" as="xs:string"/>
    
    <xsl:value-of select="if ($name = 'dc:coverage') 
      then 'bbox' 
      else 'text'"/>
  </xsl:function>
</xsl:stylesheet>
