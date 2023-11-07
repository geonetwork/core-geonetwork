<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gcoold="http://www.isotc211.org/2005/gco"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                exclude-result-prefixes="#all">

  <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" scope="stylesheet">
    <xd:desc>
      <xd:p><xd:b>Created on:</xd:b>December 5, 2014
      </xd:p>
      <xd:p>These templates transform ISO 19139 DateTime XML content into ISO
        19115-3 DateTime.
        They are designed to be imported as a template library
      </xd:p>
      <xd:p>Version December 5, 2014</xd:p>
      <xd:p><xd:b>Author:</xd:b>thabermann@hdfgroup.org
      </xd:p>
    </xd:desc>
  </xd:doc>

  <xsl:template name="writeDateTime">
    <xsl:for-each select="descendant::gcoold:*">
      <xsl:element name="{concat('gco:',local-name(.))}">
        <xsl:apply-templates select="@*"
                             mode="from19139to19115-3.2018"/>
        <xsl:value-of select="."/>
      </xsl:element>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
