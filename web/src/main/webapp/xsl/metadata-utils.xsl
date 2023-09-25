<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="3.0"
                exclude-result-prefixes="#all">

  <xsl:include href="blanks/metadata-schema01/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema02/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema03/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema04/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema05/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema06/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema07/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema08/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema09/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema10/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema11/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema12/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema13/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema14/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema15/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema16/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema17/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema18/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema19/present/metadata.xsl"/>
  <xsl:include href="blanks/metadata-schema20/present/metadata.xsl"/>

  <xsl:template mode="schema" match="*">
    <xsl:choose>
      <xsl:when test="string(geonet:info/schema)!=''">
        <xsl:value-of select="geonet:info/schema"/>
      </xsl:when>
      <xsl:otherwise>UNKNOWN</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- summary: copy it -->
  <xsl:template match="summary" mode="brief">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- brief -->
  <xsl:template match="*" mode="brief">
    <xsl:param name="schema">
      <xsl:apply-templates mode="schema" select="."/>
    </xsl:param>

    <xsl:variable name="fn"
                  as="function(*)?"
                  select="fn:function-lookup(xs:QName('gn-fn-metadata:' || $schema || 'Brief'), 1)"/>

    <xsl:if test="exists($fn)">
      <xsl:copy-of select="$fn($metadata)"/>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
