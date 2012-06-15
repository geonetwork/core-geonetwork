<?xml version="1.0" encoding="UTF-8"?>
<!-- Utility templates for iso19139.che profil. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:che="http://www.geocat.ch/2008/che" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	version="1.0">


	<!-- Template use to return a localised URL eg. <gmd:linkage xsi:type="che:PT_FreeURL_PropertyType"> 
		<gmd:URL>http://myUrlInDefaultMetadataLanguage</gmd:URL> <che:PT_FreeURL> 
		<che:URLGroup> <che:LocalisedURL locale="#DE">http://myUrlInDe</che:LocalisedURL> 
		</che:URLGroup> </che:PT_FreeURL> </gmd:linkage> -->
	<xsl:template name="localisedUrl" mode="localisedUrl"
		match="gmd:linkage">
		<xsl:param name="langId" />

		<xsl:choose>
			<xsl:when
				test="che:PT_FreeURL/che:URLGroup/che:LocalisedURL[@locale=$langId]">
				<xsl:value-of
					select="che:PT_FreeURL/che:URLGroup/che:LocalisedURL[@locale=$langId]" />
			</xsl:when>
			<xsl:when test="che:LocalisedURL">
				<xsl:value-of select="che:LocalisedURL" />
			</xsl:when>
			<xsl:when test="not(gmd:URL)">
				<!-- If no default URL, try to use the first textGroup available -->
				<xsl:value-of
					select="che:PT_FreeURL/che:URLGroup[position()=1]/che:LocalisedURL[@locale=$langId]" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="gmd:URL" />
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
</xsl:stylesheet>
