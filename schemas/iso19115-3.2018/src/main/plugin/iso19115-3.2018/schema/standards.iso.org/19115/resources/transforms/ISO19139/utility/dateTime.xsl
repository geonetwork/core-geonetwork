<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gcoold="http://www.isotc211.org/2005/gco"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                exclude-result-prefixes="#all">

    <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" scope="stylesheet">
        <xd:desc>
            <xd:p><xd:b>Created on:</xd:b>December 5, 2014 </xd:p>
            <xd:p>These templates transform ISO 19139 DateTime XML content into ISO 19115-3 DateTime.
                They are designed to be imported as a template library</xd:p>
            <xd:p>Version December 5, 2014</xd:p>
            <xd:p><xd:b>Author:</xd:b>thabermann@hdfgroup.org</xd:p>
        </xd:desc>
    </xd:doc>

    <xsl:template name="writeDateTime">
     <!--
      have to account for gco:Date and gco:DateTime which are both valid descendants of gmd:date
     -->
        <xsl:apply-templates select="@*"
                             mode="from19139to19115-3"/>
        <xsl:if test="descendant::gcoold:*">
          <gco:DateTime>
              <xsl:for-each select="descendant::gcoold:Date">
                  <xsl:variable name="dateNodeString">
                      <xsl:value-of select="xs:string(.)"/>
                  </xsl:variable>
                  <xsl:choose>
                      <xsl:when test="string-length($dateNodeString)=4">
                          <!-- Assume YYYY -->
                          <xsl:value-of select="concat(substring($dateNodeString,1,4),'-01-01T00:00:00')"/>
                      </xsl:when>
                      <xsl:when test="string-length($dateNodeString)=6">
                          <!-- Assume YYYYMM -->
                          <xsl:value-of select="concat(substring($dateNodeString,1,4),'-',substring($dateNodeString,5,2),'-01T00:00:00')"/>
                      </xsl:when>
                      <xsl:when test="string-length($dateNodeString)=7">
                          <!-- Assume YYYY-MM -->
                          <xsl:value-of select="concat(substring($dateNodeString,1,4),'-',substring($dateNodeString,6,2),'-01T00:00:00')"/>
                      </xsl:when>
                      <xsl:when test="string-length($dateNodeString)=8">
                          <!-- Assume YYYY-MM-DD -->
                          <xsl:value-of select="concat(substring($dateNodeString,1,4),'-',substring($dateNodeString,5,2),'-',substring($dateNodeString,7,2),'T00:00:00')"/>
                      </xsl:when>
                      <xsl:otherwise>
                          <xsl:value-of select="concat($dateNodeString,'T00:00:00')"/>
                      </xsl:otherwise>
                  </xsl:choose>
              </xsl:for-each>
              <xsl:for-each select="descendant::gcoold:DateTime">
                  <xsl:value-of select="."/>
              </xsl:for-each>
          </gco:DateTime>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>