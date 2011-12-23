<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:gml="http://www.opengis.net/gml" xmlns:fra="http://www.cnig.gouv.fr/2005/fra"
  xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gts="http://www.isotc211.org/2005/gts"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:date="http://exslt.org/dates-and-times" xmlns:exslt="http://exslt.org/common"
  xmlns:saxon="http://saxon.sf.net/"
  extension-element-prefixes="saxon"
  >
  <xsl:import href="metadata.xsl"/>
  
  <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>

  <xsl:include href="utils.xsl"/>
  <xsl:include href="metadata-fop.xsl"/>
  <xsl:include href="metadata-fop-utils.xsl"/>

  <xsl:variable name="server" select="/root/gui/env/server"/>
  <!--<xsl:variable name="server"
    select="concat('http://', /root/gui/env/server/host, ':', /root/gui/env/server/port)"/>-->
  <xsl:variable name="siteURL" select="substring-before(/root/gui/siteURL, '/srv')"/>

 
  <xsl:template mode="schema" match="*">
    <xsl:choose>
      <xsl:when test="starts-with(//geonet:info/schema, 'iso19139')">
        <xsl:value-of select="'iso19139'"/>
      </xsl:when>
      <xsl:when test="string(//geonet:info/schema)!=''">
        <xsl:value-of select="//geonet:info/schema"/>
      </xsl:when>
      <xsl:when test="local-name(.)='MD_Metadata' or local-name(..)='MD_Metadata'"
        >iso19139</xsl:when>
      <xsl:otherwise>UNKNOWN</xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template mode="elementEPFop" match="*|@*">
    <xsl:param name="schema">
      <xsl:apply-templates mode="schema" select="."/>
    </xsl:param>
    <xsl:apply-templates mode="elementFop" select=".">
      <xsl:with-param name="schema" select="$schema"/>
    </xsl:apply-templates>
  </xsl:template>


  <!-- =============================================
    Start FOP layout
  -->
  <xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <xsl:call-template name="fop-master"/>

      <fo:page-sequence master-reference="simpleA4" initial-page-number="1">
        <xsl:call-template name="fop-footer"/>

        <fo:flow flow-name="xsl-region-body">
          <!-- Banner level -->
          <xsl:call-template name="banner"/>

          <fo:block font-size="10pt">
            <xsl:call-template name="contentFop"/>
          </fo:block>

          <fo:block id="terminator"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>

  </xsl:template>

  <!--
    page content
  -->
  <xsl:template name="contentFop">
    <xsl:param name="schema">
      <xsl:apply-templates mode="schema" select="."/>
    </xsl:param>
    <xsl:for-each select="/root/*[name(.)!='gui' and name(.)!='request']">
      <!-- Brief metadata -->
      <xsl:variable name="md">
        <xsl:apply-templates mode="brief" select="."/>
      </xsl:variable>
      <xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
      <xsl:variable name="source" select="geonet:info/source"/>
      <xsl:variable name="id" select="geonet:info/id"/>
      
      
      <fo:table width="100%" table-layout="fixed">
        <fo:table-column column-width="1.8cm"/>
        <fo:table-column column-width="12.2cm"/>
        <fo:table-column column-width="6cm"/>
        <fo:table-body>

          <fo:table-row>
            <fo:table-cell padding-left="4pt" padding-right="4pt" padding-top="4pt" margin-top="4pt">
              <fo:block>
                <fo:external-graphic content-width="35pt">
                  <xsl:attribute name="src">url('<xsl:value-of
                    select="concat('http://', /root/gui/env/server/host, ':', /root/gui/env/server/port , /root/gui/url, '/images/logos/', $source, '.gif')"/>')"</xsl:attribute>
                </fo:external-graphic>
                
              </fo:block>
            </fo:table-cell>
            <fo:table-cell display-align="center">
              <fo:block font-weight="{$title-weight}" font-size="{$title-size}" color="{$title-color}"
                padding-top="4pt" padding-bottom="4pt" padding-left="4pt" padding-right="4pt">
                <xsl:value-of select="$metadata/title"/>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block text-align="right">
                
                <xsl:call-template name="metadata-thumbnail-block">
                  <xsl:with-param name="server" select="$server"/>
                  <xsl:with-param name="metadata" select="$metadata"/>
                </xsl:call-template>
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
          
          <xsl:call-template name="metadata-resources">
            <xsl:with-param name="title" select="false()"/>
            <xsl:with-param name="gui" select="/root/gui"/>
            <xsl:with-param name="server" select="/root/gui/env/server"/>
            <xsl:with-param name="metadata" select="$metadata"/>
          </xsl:call-template>
        </fo:table-body>
      </fo:table>

      <fo:table width="100%" table-layout="fixed">
        <fo:table-column column-width="5cm"/>
        <fo:table-column column-width="15cm"/>
        <fo:table-body>
          
          <xsl:variable name="schemaTemplate" select="concat('metadata-fop-',$schema)"/>
          <saxon:call-template name="{$schemaTemplate}"> 
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:fallback>
              <xsl:message>Fall back as no saxon:call-template exists</xsl:message>
            </xsl:fallback>
          </saxon:call-template>
          
        </fo:table-body>
      </fo:table>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
