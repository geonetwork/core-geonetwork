<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:saxon="http://saxon.sf.net/" xmlns:gmx="http://www.isotc211.org/2005/gmx"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:xlink="http://www.w3.org/1999/xlink" extension-element-prefixes="saxon">

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
  
  <xsl:include href="../../common/base-variables.xsl"/>
  <xsl:include href="../../common/profiles-loader-thesaurus-transformation.xsl"/>
	<!-- Default template to use (ISO19139 keyword by default). -->
	<xsl:variable name="defaultTpl" select="'to-iso19139-keyword'"/>

  <xsl:variable name="serviceUrl" select="$fullURLForService"/>
	
	<xsl:template match="/">
		<xsl:variable name="tpl"
			select="if (/root/request/transformation and /root/request/transformation != '') 
			then /root/request/transformation else $defaultTpl"/>
		
		<xsl:variable name="keywords" select="/root/*[name() != 'gui' and name() != 'request']/keyword"/>
		
		<xsl:choose>
			<xsl:when test="$keywords">
				<xsl:for-each-group select="$keywords"
					group-by="thesaurus">
					<saxon:call-template name="{$tpl}"/>
				</xsl:for-each-group>
			</xsl:when>
			<xsl:otherwise>
				<saxon:call-template name="{$tpl}"/>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
</xsl:stylesheet>
