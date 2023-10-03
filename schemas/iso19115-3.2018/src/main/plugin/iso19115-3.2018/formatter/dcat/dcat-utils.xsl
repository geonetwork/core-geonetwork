<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:dct="http://purl.org/dc/terms/"
                exclude-result-prefixes="#all">

  <xsl:template name="create-node-with-info">
    <xsl:param name="message" as="xs:string?"/>
    <xsl:param name="node" as="node()"/>

    <xsl:comment select="$message"/>
    <xsl:copy-of select="$node"/>
  </xsl:template>


  <xsl:template name="rdf-localised">
    <xsl:param name="nodeName"
               as="xs:string"/>

    <!-- TODO lan:*-->
    <xsl:element name="{$nodeName}">
      <xsl:attribute name="xml:lang" select="''"/>
      <xsl:value-of select="gco:CharacterString/text()"/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
