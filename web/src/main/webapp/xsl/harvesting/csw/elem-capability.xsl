<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate a table that represents a search on the remote node -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/search">
		<search id="{@id}">
			<xsl:apply-templates select="." mode="data"/>
		</search>
	</xsl:template>

	<!-- ============================================================================================= -->
		
	<xsl:template match="*" mode="data">
		<xsl:for-each select="/root/search/*">
		
		<capability>
			<xsl:attribute name="name">
				<xsl:value-of select="local-name()" />
			</xsl:attribute>
		
				<xsl:value-of select="concat('csw.',local-name())" />
		
		</capability>
		</xsl:for-each>
		
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
