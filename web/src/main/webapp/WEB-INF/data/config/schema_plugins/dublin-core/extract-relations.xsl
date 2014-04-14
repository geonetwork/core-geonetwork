<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template mode="relation" match="metadata[simpledc]" priority="99">
    <xsl:for-each select="*/descendant::*[name(.) = 'dct:references'][starts-with(., 'http') or contains(. , 'resources.get') or contains(., 'file.disclaimer')]">
      <relation type="onlinesrc">
        <id><xsl:value-of select="."/></id>
        <title>
          <xsl:value-of select="."/>
        </title>
        <url>
          <xsl:value-of select="."/>
        </url>
        <name>
          <xsl:value-of select="."/>
        </name>
        <abstract><xsl:value-of select="."/></abstract>
        <description><xsl:value-of select="."/></description>
        <xsl:choose>
          <xsl:when test="contains(. , 'resources.get') or contains(., 'file.disclaimer')">
            <protocol><xsl:value-of select="'WWW:DOWNLOAD-1.0-http--download'"/></protocol>
          </xsl:when>
          <xsl:otherwise>
            <protocol><xsl:value-of select="'WWW:LINK-1.0-http--link'"/></protocol>
          </xsl:otherwise>
        </xsl:choose>
      </relation>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>