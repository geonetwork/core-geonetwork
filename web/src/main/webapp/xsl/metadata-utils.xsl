<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:exslt="http://exslt.org/common"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                version="1.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="exslt saxon geonet java">

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

    <xsl:variable name="briefSchemaCallBack" select="concat($schema,'Brief')"/>
    <saxon:call-template name="{$briefSchemaCallBack}"/>
  </xsl:template>

</xsl:stylesheet>
