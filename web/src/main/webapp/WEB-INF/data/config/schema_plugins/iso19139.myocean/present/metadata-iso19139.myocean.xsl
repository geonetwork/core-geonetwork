<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:fra="http://www.cnig.gouv.fr/2005/fra"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:geonet="http://www.fao.org/geonetwork" xmlns:exslt="http://exslt.org/common"
	exclude-result-prefixes="gmd gco gml gts srv xlink exslt geonet">

	<xsl:import href="metadata-iso19139.myocean-fop.xsl"/>

  <xsl:template name="iso19139.myoceanBrief">
    <metadata>
			<xsl:choose>
		    <xsl:when test="geonet:info/isTemplate='s'">
		      <xsl:apply-templates mode="iso19139-subtemplate" select="."/>
		      <xsl:copy-of select="geonet:info" copy-namespaces="no"/>
		    </xsl:when>
		    <xsl:otherwise>
	
			<!-- call iso19139 brief -->
			<xsl:call-template name="iso19139-brief"/>
		    </xsl:otherwise>
		  </xsl:choose>    
    </metadata>
  </xsl:template>


  <xsl:template name="iso19139.myoceanCompleteTab">
    <xsl:param name="tabLink"/>
    <xsl:param name="schema"/>
    
    <xsl:call-template name="iso19139CompleteTab">
      <xsl:with-param name="tabLink" select="$tabLink"/>
      <xsl:with-param name="schema" select="$schema"/>
    </xsl:call-template>
  </xsl:template>

	<!-- main template - the way into processing iso19139.myocean -->
	<xsl:template name="metadata-iso19139.myocean">
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="embedded"/>

		<xsl:apply-templates mode="iso19139" select="." >
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="embedded" select="$embedded" />
		</xsl:apply-templates>
	</xsl:template>
	
	<!-- =================================================================== -->
	<!-- === Javascript used by functions in this presentation XSLT          -->
	<!-- =================================================================== -->
	<xsl:template name="iso19139.myocean-javascript"/>

</xsl:stylesheet>
