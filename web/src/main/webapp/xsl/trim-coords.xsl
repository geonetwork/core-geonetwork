<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
    xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml"
    xmlns:fn="http://www.w3.org/2005/02/xpath-functions" xmlns:gts="http://www.isotc211.org/2005/gts"
    xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:geonet="http://www.fao.org/geonetwork"
    xmlns:xalan="http://xml.apache.org/xalan" xmlns:exslt="http://exslt.org/common"
    xmlns:util="xalan://org.fao.geonet.util.XslUtil"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:template match="gml:posList" priority="1">
		<xsl:copy>
  		  <xsl:apply-templates select="@*"/>
	      <xsl:value-of select="util:trimPosList(string(.))"/>
		</xsl:copy>
    </xsl:template>
    
    <xsl:template match="text()"/>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
	<xsl:template match="/">
			<xsl:apply-templates select="//gmd:EX_Extent"/>
	</xsl:template>
</xsl:stylesheet>