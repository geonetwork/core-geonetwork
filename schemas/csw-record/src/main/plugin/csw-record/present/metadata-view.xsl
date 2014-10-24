<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:dc = "http://purl.org/dc/elements/1.1/"
  xmlns:dct = "http://purl.org/dc/terms/">

  <xsl:template name="metadata-csw-recordview-simple" match="metadata-csw-recordview-simple">

    <xsl:call-template name="md-content">
      <xsl:with-param name="title" select="//dc:title"/>
      <xsl:with-param name="exportButton"/>
      <xsl:with-param name="abstract"/>
      <xsl:with-param name="logo">
        <img src="../../images/logos/{//geonet:info/source}.gif" alt="logo"/>
      </xsl:with-param>
      <xsl:with-param name="relatedResources">
        <xsl:call-template name="dublin-core-relatedResources"/>
      </xsl:with-param>
      <xsl:with-param name="tabs">
        <xsl:call-template name="complexElementSimpleGui">
          <xsl:with-param name="title"
            select="/root/gui/schemas/iso19139/strings/understandResource"/>
          <xsl:with-param name="content">
            <xsl:apply-templates mode="dublin-core" select=".">
              <xsl:with-param name="schema" select="'dublin-core'"/>
              <xsl:with-param name="edit" select="false()"/>
            </xsl:apply-templates>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
