<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:sdn-product="http://seadatanet.org/geonetwork" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes="#all">


	<xsl:template match="/root">
		<xsl:apply-templates select="gmd:MD_Metadata"/>
	</xsl:template>

	<xsl:function name="sdn-product:buildIdentifier" as="xs:string">
		<xsl:param name="point-of-contact-email"/>
		<xsl:param name="file-identifier"/>		
		<xsl:value-of select="concat(substring-after($point-of-contact-email, '@'), '/', $file-identifier)"/>
	</xsl:function>



	<xsl:template match="gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/gmd:MD_Identifier/gmd:code" priority="99">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<gco:CharacterString>
				<xsl:value-of select="sdn-product:buildIdentifier(/root/gmd:MD_Metadata/
					gmd:identificationInfo/gmd:MD_DataIdentification/
					gmd:pointOfContact[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='originator'
					and position() = 1]/
					gmd:CI_ResponsibleParty/
					gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/
 					gmd:electronicMailAddress/gco:CharacterString/text() , 
 					/root/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString/text())"/>
			</gco:CharacterString>
		</xsl:copy>
	</xsl:template>



	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>