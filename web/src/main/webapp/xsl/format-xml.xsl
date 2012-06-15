<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" 
	xmlns:gco="http://www.isotc211.org/2005/gco">

	<xsl:output method="xml"/>

	<!-- Return an iso19139 representation of a contact 
	stored in the metadata catalogue. 
		
		TODO : return profil specific records.
	-->
	<xsl:template match="/">
		<xsl:for-each select="root/response/record">
				<gmd:MD_Format>
					<gmd:name>
						<xsl:if test="name=''">
							<xsl:attribute name="gco:nilReason">missing</xsl:attribute>
						</xsl:if>
						<gco:CharacterString><xsl:value-of select="name"/></gco:CharacterString> 
					</gmd:name> 
					<gmd:version>
						<xsl:if test="version=''">
							<xsl:attribute name="gco:nilReason">missing</xsl:attribute>
						</xsl:if>
						<gco:CharacterString><xsl:value-of select="version"/></gco:CharacterString> 
					</gmd:version> 
				</gmd:MD_Format>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
