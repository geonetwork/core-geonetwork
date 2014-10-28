<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ================================================================= -->
	
	<xsl:template match="/root">
		 <xsl:apply-templates select="metadata"/>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="metadata">
		<xsl:copy>
			<xsl:apply-templates select="idinfo"/>
			<xsl:apply-templates select="dataqual"/>
			<xsl:apply-templates select="spdoinfo"/>
			<xsl:apply-templates select="spref"/>
			<xsl:apply-templates select="eainfo"/>
			<xsl:apply-templates select="distinfo"/>
			<xsl:apply-templates select="metainfo"/>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="idinfo">
		<xsl:copy>
			<xsl:apply-templates select="citation"/>
			<xsl:apply-templates select="descript"/>
			<xsl:apply-templates select="timeperd"/>
			<xsl:apply-templates select="status"/>
			<xsl:apply-templates select="spdom"/>
			<xsl:apply-templates select="keywords"/>
			<xsl:apply-templates select="accconst"/>
			<xsl:apply-templates select="useconst"/>
			<xsl:apply-templates select="ptcontac"/>
			<xsl:apply-templates select="browse[browset != /root/env/type]"/>
			<xsl:call-template name="fill"/>
			<xsl:apply-templates select="datacred"/>
			<xsl:apply-templates select="secinfo"/>
			<xsl:apply-templates select="native"/>
			<xsl:apply-templates select="crossref"/>
		</xsl:copy>
	</xsl:template>
		
	<!-- ================================================================= -->
		
	<xsl:template name="fill">
		<browse>
			<browsen><xsl:value-of select="/root/env/file"/></browsen>
			<browsed><xsl:value-of select="/root/env/ext"/></browsed>
			<browset><xsl:value-of select="/root/env/type"/></browset>
		</browse>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="@*|node()">
		 <xsl:copy>
			  <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
</xsl:stylesheet>
