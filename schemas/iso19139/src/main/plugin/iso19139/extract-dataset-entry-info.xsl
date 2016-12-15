<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
				xmlns:gco="http://www.isotc211.org/2005/gco">
	<xsl:variable name="protocol">WWW:DOWNLOAD-1.0-HTTP--DOWNLOAD</xsl:variable>
	<xsl:variable name="applicationProfile">INSPIRE-Download-Atom</xsl:variable>
    <xsl:template match="gmd:MD_Metadata">
        <gmd:MD_Metadata>
			<xsl:if test="count(gmd:distributionInfo/*/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[upper-case(gmd:protocol/gco:CharacterString) = $protocol and gmd:applicationProfile/gco:CharacterString=$applicationProfile]/gmd:description)>0">
				<xsl:copy-of select="gmd:fileIdentifier"/>                
				<xsl:copy-of select="gmd:language"/>                
				<xsl:copy-of select="gmd:identificationInfo/gmd:MD_DataIdentification"/>                
				<xsl:copy-of select="gmd:distributionInfo/*/gmd:transferOptions/gmd:MD_DigitalTransferOptions"/>                
				<xsl:copy-of select="gmd:dateStamp/gco:DateTime"/>
				<xsl:copy-of select="gmd:locale"/>
				<xsl:copy-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[1]/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox"/>
			</xsl:if>
        </gmd:MD_Metadata>
    </xsl:template>
</xsl:stylesheet>