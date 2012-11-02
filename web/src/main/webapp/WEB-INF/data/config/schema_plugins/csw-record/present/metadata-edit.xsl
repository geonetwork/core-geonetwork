<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dc = "http://purl.org/dc/elements/1.1/"
	xmlns:dct = "http://purl.org/dc/terms/"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:ows="http://www.opengis.net/ows"
	xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
	xmlns:geonet="http://www.fao.org/geonetwork">

  <xsl:include href="metadata-csw-record-view.xsl"/>

	<!-- main template - the way into processing csw-record which is 
	     published as dublic-core -->
	<xsl:template name="csw-record">
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="embedded"/>
		
    <xsl:apply-templates mode="dublin-core" select="." >
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="embedded" select="$embedded" />
    </xsl:apply-templates>
  </xsl:template>

	<!-- CompleteTab template - csw-record just calls completeTab from 
	     metadata-utils.xsl -->
	<xsl:template name="csw-recordCompleteTab">
		<xsl:param name="tabLink"/>
	  
	  <xsl:call-template name="mainTab">
	    <xsl:with-param name="title" select="/root/gui/strings/completeTab"/>
	    <xsl:with-param name="default">metadata</xsl:with-param>
	    <xsl:with-param name="menu">
	      <item label="metadata">metadata</item>
	    </xsl:with-param>
	  </xsl:call-template>
	</xsl:template>

	<!-- Brief template - csw-record just calls Brief from 
	     dublin-core -->
	<xsl:template name="csw-recordBrief">
		<xsl:call-template name="dublin-coreBrief"/>
	</xsl:template>

</xsl:stylesheet>
