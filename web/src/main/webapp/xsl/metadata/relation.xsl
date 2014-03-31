<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  Create a simple XML tree for relation description.
  <relations>
    <relation type="related|services|children">
      + super-brief representation.
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
  xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:geonet="http://www.fao.org/geonetwork" 
  xmlns:util="java:org.fao.geonet.util.XslUtil"
  xmlns:exslt="http://exslt.org/common"
  exclude-result-prefixes="geonet exslt">

  <xsl:include href="common.xsl"/>
  <xsl:include href="../schema-xsl-relations-loader.xsl"/>

  <xsl:template match="/">
    <relations>
      <xsl:apply-templates mode="relation" select="/root/relations/*"/>
    </relations>
  </xsl:template>

  <xsl:template mode="relation" match="related|services|datasets|children|parent|sources|fcats|hasfeaturecat|siblings|associated|source|hassource">
    <xsl:apply-templates mode="relation" select="response/*">
      <xsl:with-param name="type" select="name(.)"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="relation" match="sibling">
    <xsl:apply-templates mode="relation" select="*">
      <xsl:with-param name="type" select="'sibling'"/>
      <xsl:with-param name="subType" select="@initiative"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Bypass summary elements -->
  <xsl:template mode="relation" match="summary" priority="99"/>


  <!-- In Lucene only mode, metadata are retrieved from 
  the index and pass as a simple XML with one level element.
  Make a simple copy here. -->
  <xsl:template mode="superBrief" match="metadata">
    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="relation" match="*">
    <xsl:param name="type"/>
    <xsl:param name="subType" select="''"/>

    <!-- Fast output doesn't produce a full metadata record -->
    <xsl:variable name="md">
      <xsl:apply-templates mode="superBrief" select="."/>
    </xsl:variable>
    <xsl:variable name="metadata" select="exslt:node-set($md)"/>

    <relation type="{$type}">
			<xsl:if test="normalize-space($subType)!=''">
				<xsl:attribute name="subType">
					<xsl:value-of select="$subType"/>		
				</xsl:attribute>
			</xsl:if>
      <xsl:copy-of select="$metadata"/>
    </relation>
  </xsl:template>
  
  <!-- Add the default title as title. This may happen
  when title is retrieve from index and the record is
  not available in current language. eg. iso19110 records
  are only indexed with no language info. -->
  <xsl:template mode="superBrief" match="metadata[not(title)]">
    <xsl:copy>
      <xsl:copy-of select="*"/>
      <title><xsl:value-of select="defaultTitle"/></title>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
