<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mcp="http://schemas.aodn.org.au/mcp-2.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:geonet="http://www.fao.org/geonet"
                exclude-result-prefixes="mcp geonet">

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />

  <xsl:template match="/">
    <xsl:apply-templates select="mcp:MD_Metadata"/>
  </xsl:template>

  <xsl:template match="mcp:MD_Metadata" priority="400">
    <xsl:element name="{@gco:isoType}">
      <xsl:namespace name="gmd" select="'http://www.isotc211.org/2005/gmd'"/>
      <xsl:namespace name="gco" select="'http://www.isotc211.org/2005/gco'"/>
      <xsl:namespace name="gmx" select="'http://www.isotc211.org/2005/gmx'"/>
      <xsl:namespace name="srv" select="'http://www.isotc211.org/2005/srv'"/>
      <xsl:namespace name="gml" select="'http://www.opengis.net/gml'"/>
      <xsl:namespace name="xlink" select="'http://www.w3.org/1999/xlink'"/>
      <xsl:copy-of select="@*[name()!='xsi:schemaLocation' and name()!='gco:isoType']"/>
      <xsl:attribute name="xsi:schemaLocation">http://www.isotc211.org/2005/gmd http://www.isotc211.org/2005/gmd/gmd.xsd http://www.isotc211.org/2005/srv http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd</xsl:attribute>
      <xsl:apply-templates select="child::*"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="gmd:metadataStandardName">
    <xsl:copy>
      <gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:metadataStandardVersion">
    <xsl:copy>
      <gco:CharacterString>1.0</gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[@gco:isoType and name()!='mcp:MD_Metadata']" priority="100">
    <xsl:variable name="elemName" select="@gco:isoType"/>

    <xsl:element name="{$elemName}">
      <xsl:apply-templates select="@*[name()!='gco:isoType']"/>
      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*[namespace-uri()='http://schemas.aodn.org.au/mcp-2.0']"/>

</xsl:stylesheet>
