<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
						xmlns:gco="http://www.isotc211.org/2005/gco"
						xmlns:gmd="http://www.isotc211.org/2005/gmd"
						xmlns:srv="http://www.isotc211.org/2005/srv"
						xmlns:che="http://www.geocat.ch/2008/che"
						exclude-result-prefixes="gco gmd srv">

	<xsl:template match="che:CHE_MD_Metadata">
		<serviceInfo>
			
			<!-- Get Service URL from GetCapabilities Operation, if null from distribution information -->
			<xsl:variable name="url">
				<xsl:value-of select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata[srv:operationName/gco:CharacterString='GetCapabilities']/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL|
					gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/srv:containsOperations/srv:SV_OperationMetadata[srv:operationName/gco:CharacterString='GetCapabilities']/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>					
			</xsl:variable>
			<xsl:variable name="protocol">
				<xsl:value-of select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata[srv:operationName/gco:CharacterString='GetCapabilities']/srv:connectPoint/gmd:CI_OnlineResource/gmd:protocol|
					gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']//srv:containsOperations/srv:SV_OperationMetadata[srv:operationName/gco:CharacterString='GetCapabilities']/srv:connectPoint/gmd:CI_OnlineResource/gmd:protocol"/>
			</xsl:variable>

			<url>
			<xsl:choose>
				<xsl:when test="$url=''">
					<xsl:value-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$url"/>
				</xsl:otherwise>
			</xsl:choose>
			</url>
			
			<!-- Get service protocol -->
			<protocol>
				<xsl:value-of select="$protocol"/>
			</protocol>
			
			<!-- Get service descritpion -->
			<desc>
				<xsl:value-of select="normalize-space(gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString|
					gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString)"/>
			</desc>
		</serviceInfo>
	</xsl:template>
</xsl:stylesheet>
