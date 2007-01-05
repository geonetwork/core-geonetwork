<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
						xmlns:gco="http://www.isotc211.org/2005/gco"
						xmlns:gmd="http://www.isotc211.org/2005/gmd" exclude-result-prefixes="gmd">

	<!-- ================================================================= -->
	
	<xsl:template match="/root">
		 <xsl:apply-templates select="gmd:MD_Metadata"/>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="gmd:MD_Metadata">
		 <xsl:copy>
		 		<xsl:if test="not(gmd:fileIdentifier)">
		 			<gmd:fileIdentifier>
						<gco:CharacterString><xsl:value-of select="/root/env/uuid"/></gco:CharacterString>
					</gmd:fileIdentifier>
				</xsl:if>
			  <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="gmd:fileIdentifier">
		<xsl:copy>
			<gco:CharacterString><xsl:value-of select="/root/env/uuid"/></gco:CharacterString>
		</xsl:copy>
	</xsl:template>
	
	<!-- ================================================================= -->
	
	<xsl:template match="gmd:dateStamp">
		<xsl:copy>
			<gco:DateTime><xsl:value-of select="/root/env/currDate"/></gco:DateTime>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="gmd:MD_Metadata/gmd:characterSet">
		<xsl:copy>
			<gmd:MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode" codeListValue="utf8" />
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="gmd:metadataStandardName">
		<xsl:copy>
			<gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="gmd:metadataStandardVersion">
		<xsl:copy>
			<gco:CharacterString>1.0</gco:CharacterString>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	<!-- codelists: set @codeList path -->
	<!-- ================================================================= -->
	
	<xsl:template match="gmd:*[@codeListValue]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="codeList">
				<xsl:value-of select="concat('./resources/codeList.xml#',local-name(.))"/>
			</xsl:attribute>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	<!-- online resources: download -->
	<!-- ================================================================= -->

	<xsl:template match="gmd:linkage[starts-with(following-sibling::gmd:protocol/gco:CharacterString,'WWW:DOWNLOAD-') and contains(following-sibling::gmd:protocol/gco:CharacterString,'http--download') and following-sibling::gmd:name]">

		<xsl:choose>
			<xsl:when test="string(/root/env/siteID)=string(/root/env/source)">
				<gmd:linkage>
					<gmd:URL>
						<xsl:value-of select="concat(/root/env/siteURL,'/resources.get?id=',/root/env/id,'&amp;fname=',following-sibling::gmd:name/gco:CharacterString,'&amp;access=private')"/>
					</gmd:URL>
				</gmd:linkage>
			</xsl:when>

			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="@*|node()">
		 <xsl:copy>
			  <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->

</xsl:stylesheet>
