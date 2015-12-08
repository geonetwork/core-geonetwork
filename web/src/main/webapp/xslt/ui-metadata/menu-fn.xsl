<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <!-- Evaluate XPath expression to
                    see if view should be displayed
                    according to the metadata record or
                    the session information. -->
  <xsl:function name="gn-fn-metadata:check-viewtab-visibility" as="xs:boolean">
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="metadata"/>
    <xsl:param name="serviceInfo"/>
    <xsl:param name="displayIfRecord" as="xs:string?"/>
    <xsl:param name="displayIfServiceInfo" as="xs:string?"/>

    <xsl:variable name="isInRecord" as="xs:boolean">
      <xsl:choose>
        <xsl:when test="$displayIfRecord">
          <saxon:call-template name="{concat('evaluate-', $schema, '-boolean')}">
            <xsl:with-param name="base" select="$metadata"/>
            <xsl:with-param name="in" select="concat('/../', $displayIfRecord)"/>
          </saxon:call-template>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="false()"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="isInServiceInfo" as="xs:boolean">
      <xsl:choose>
        <xsl:when test="$displayIfServiceInfo">
          <saxon:call-template name="{concat('evaluate-', $schema, '-boolean')}">
            <xsl:with-param name="base" select="$serviceInfo"/>
            <xsl:with-param name="in" select="concat('/', $displayIfServiceInfo)"/>
          </saxon:call-template>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="false()"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$displayIfServiceInfo and $displayIfRecord">
        <xsl:value-of select="$isInServiceInfo and $isInRecord"/>
      </xsl:when>
      <xsl:when test="$displayIfServiceInfo">
        <xsl:value-of select="$isInServiceInfo"/>
      </xsl:when>
      <xsl:when test="$displayIfRecord">
        <xsl:value-of select="$isInRecord"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="true()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
</xsl:stylesheet>