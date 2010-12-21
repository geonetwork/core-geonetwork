<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet 	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
						xmlns:geonet="http://www.fao.org/geonetwork" 
						xmlns:exslt= "http://exslt.org/common"
						xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
						exclude-result-prefixes="xsl exslt geonet">

	<xsl:output method="text" version="1.0" encoding="UTF-8" indent="no"/>
	
	<xsl:variable name="sep" select="';'"/>

	<!-- A template to add a new line \n with no extra space. -->	
	<xsl:template name="newLine">
<xsl:text>
</xsl:text>		
	</xsl:template>


	<xsl:template name="content" match="/">

		<!-- Display results
				* header first (once)
				* content then.
		-->
		<xsl:for-each select="root/gui/statCSV/record">
			<xsl:choose>
				<xsl:when test="position()=1">
					<xsl:call-template name="csvHeader" >
						<xsl:with-param name="arow" select="."/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="csvLine">
						<xsl:with-param name="arow" select="."/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	<!-- ============================================================== -->
	<!-- Create header using first row corresponding to current schema -->
	<xsl:template name="csvHeader">
		<xsl:param name="arow"/>
		<xsl:for-each select="$arow/*">
			<xsl:value-of select="name(.)"/>
			<xsl:if test="position() != last()">
				<xsl:value-of select="$sep"/>
			</xsl:if>
		</xsl:for-each>
		<xsl:call-template name="newLine"/>
	</xsl:template>
	
	<!-- ============================================================== -->
	<!-- Dump line -->
	<xsl:template name="csvLine">
		<xsl:param name="arow"/>

		<xsl:for-each select="$arow/*">
			<xsl:value-of select="."/>
			<xsl:if test="position() != last()">
				<xsl:value-of select="$sep"/>
			</xsl:if>
		</xsl:for-each>
		
		<xsl:call-template name="newLine"/>
	</xsl:template>

</xsl:stylesheet>
