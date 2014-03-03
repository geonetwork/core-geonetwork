<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to update metadata adding a reference to a source record.
-->
<xsl:stylesheet version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork">

	<!-- Source metadata record UUID -->
	<xsl:param name="sourceUuid"/>
	
	<!-- Do a copy of every nodes and attributes -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="gmd:LI_Lineage|*[contains(@gco:isoType, 'LI_Lineage')]" priority="2">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:copy-of select="gmd:statement|gmd:processStep|gmd:source"/>

			<!-- Only one parent identifier allowed
			- overwriting existing one. -->
			<gmd:source uuidref="{$sourceUuid}">
				<gmd:LI_Source/>
			</gmd:source>
		</xsl:copy>
	</xsl:template>

	<!-- TODO : support when record does not contain lineage (should not happen for a ISO compliant record -->

	<!-- Remove geonet:* elements. -->
	<xsl:template match="geonet:*" priority="2"/>
</xsl:stylesheet>
