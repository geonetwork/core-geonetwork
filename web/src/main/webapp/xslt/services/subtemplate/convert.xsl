<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
  
  <xsl:include href="../../common/base-variables.xsl"/>
  <xsl:include href="../../common/profiles-loader-subtemplate-transformation.xsl"/>
  <!-- Default template to use (ISO19139 keyword by default). -->

  <xsl:variable name="serviceUrl" select="$fullURLForService"/>
  
  <xsl:template match="/">
    <xsl:variable name="subtemplate" select="/root/*[name() != 'gui' and name() != 'request']"/>
    
    <xsl:choose>
      <xsl:when test="/root/request/transformation != ''">
        <xsl:for-each select="$subtemplate">
          <saxon:call-template name="{/root/request/transformation}"/>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="$subtemplate"/>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
</xsl:stylesheet>
