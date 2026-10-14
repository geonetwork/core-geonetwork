<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:schema-org-fn="http://geonetwork-opensource.org/xsl/functions/schema-org"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:function name="schema-org-fn:toJsonText" as="xs:string">
    <xsl:param name="value" as="xs:string?"/>
    <xsl:value-of select="concat('&quot;', util:escapeForJson($value), '&quot;')"/>
  </xsl:function>
</xsl:stylesheet>
