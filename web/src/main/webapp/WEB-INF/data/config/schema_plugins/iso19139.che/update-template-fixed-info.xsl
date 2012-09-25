<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
						xmlns:gml="http://www.opengis.net/gml"
						xmlns:srv="http://www.isotc211.org/2005/srv"
						xmlns:java="java:org.fao.geonet.util.XslUtil"
						xmlns:gco="http://www.isotc211.org/2005/gco"
						xmlns:gmd="http://www.isotc211.org/2005/gmd"
						xmlns:che="http://www.geocat.ch/2008/che"
						exclude-result-prefixes="java">

	<!-- ================================================================= -->
	
	<xsl:template match="/root">
		 <xsl:apply-templates select="che:CHE_MD_Metadata"/>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="che:CHE_MD_Metadata">
		<che:CHE_MD_Metadata xmlns:gml="http://www.opengis.net/gml"
			xmlns:srv="http://www.isotc211.org/2005/srv"
			xmlns:gco="http://www.isotc211.org/2005/gco"
			xmlns:gmd="http://www.isotc211.org/2005/gmd"
			xmlns:che="http://www.geocat.ch/2008/che">
			<xsl:apply-templates select="@*"/>
			<xsl:if test="not(gmd:fileIdentifier)">
				<gmd:fileIdentifier>
					<gco:CharacterString><xsl:value-of select="/root/env/uuid"/></gco:CharacterString>
				</gmd:fileIdentifier>
			</xsl:if>
			<xsl:apply-templates select="node()"/>
		</che:CHE_MD_Metadata>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="gmd:fileIdentifier" priority="10">
		<xsl:copy>
			<gco:CharacterString><xsl:value-of select="/root/env/uuid"/></gco:CharacterString>
		</xsl:copy>
	</xsl:template>
	
	<!-- ================================================================= -->
	<!-- Set local identifier to the first 2 letters of iso code. Locale ids
	are used for multilingual charcterString -->
	<xsl:template match="gmd:PT_Locale">
		<xsl:variable name="id" select="upper-case(java:twoCharLangCode(gmd:languageCode/gmd:LanguageCode/@codeListValue))"/>
		<xsl:variable name="charset">
			<xsl:choose>
				<xsl:when test="normalize-space(gmd:characterEncoding/gmd:MD_CharacterSetCode/@codeListValue) != ''">
					<xsl:copy-of select="gmd:characterEncoding"/>
				</xsl:when>
				<xsl:otherwise>
					<gmd:characterEncoding>
						<gmd:MD_CharacterSetCode codeListValue="utf8" codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_CharacterSetCode">UTF8</gmd:MD_CharacterSetCode>
					</gmd:characterEncoding>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="langCode">
			<xsl:choose>
				<xsl:when test="normalize-space(gmd:languageCode/gmd:LanguageCode/@codeList) != ''">
					<xsl:copy-of select="gmd:languageCode"/>
				</xsl:when>
				<xsl:otherwise>
				  <gmd:languageCode>
				    <gmd:LanguageCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#LanguageCode" codeListValue="{gmd:languageCode/gmd:LanguageCode/@codeListValue}">
				    	<xsl:value-of select="gmd:languageCode/gmd:LanguageCode"/>
				    </gmd:LanguageCode>
  				</gmd:languageCode>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<gmd:PT_Locale>
			<xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
			<xsl:copy-of select="$langCode"/>
			<xsl:copy-of select="$charset"/>
		</gmd:PT_Locale>
	</xsl:template>

	<!-- ================================================================= -->
	<!-- Do not allow to expand operatesOn sub-elements 
		and constrain users to use uuidref attribute to link
		service metadata to datasets. This will avoid to have
		error on XSD validation. -->
	<xsl:template match="srv:operatesOn">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- ================================================================= -->
	<!-- Add gmd:id attribute to all gml elements which required one. -->
	<xsl:template match="gml:MultiSurface[not(@gml:id)]|gml:Polygon[not(@gml:id)]">
		<xsl:copy>
			<xsl:attribute name="gml:id">
				<xsl:value-of select="generate-id(.)"/>
			</xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>			
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@gml:id">
		<xsl:choose>
			<xsl:when test="normalize-space(.)=''">
				<xsl:attribute name="gml:id">
					<xsl:value-of select="generate-id(.)"/>
				</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ================================================================= -->
	<!-- Fix srsName attribute and generate epsg:4326 entry by default -->

	<xsl:template match="@srsName">
		<xsl:choose>
			<xsl:when test="normalize-space(.)=''">
				<xsl:attribute name="srsName">
					<xsl:text>urn:x-ogc:def:crs:EPSG:6.6:4326</xsl:text>
				</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- ================================================================= -->
	
	<xsl:template match="*[gco:CharacterString]">
		<xsl:copy>
			<xsl:copy-of select="@*[not(name()='gco:nilReason')]"/>
			<xsl:if test="normalize-space(gco:CharacterString)=''">
				<xsl:attribute name="gco:nilReason">
					<xsl:value-of select="'missing'"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="gco:CharacterString"/>
			<xsl:copy-of select="gmd:PT_FreeText"/>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	<!-- codelists: set @codeList path -->
	<!-- ================================================================= -->
	
	<xsl:template match="gmd:*[@codeListValue]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="codeList">
				<xsl:value-of select="concat('http://www.isotc211.org/2005/resources/codeList.xml#',local-name(.))"/>
			</xsl:attribute>
		</xsl:copy>
	</xsl:template>

	<!-- can't find the location of the 19119 codelists - so we make one up -->

	<xsl:template match="srv:*[@codeListValue]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="codeList">
				<xsl:value-of select="concat('http://www.isotc211.org/2005/iso19119/resources/Codelist/gmxCodelists.xml#',local-name(.))"/>
			</xsl:attribute>
		</xsl:copy>
	</xsl:template>
	
		<xsl:template match="che:*[@codeListValue]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="codeList">
			  <xsl:value-of select="concat('#',local-name(.))"/>
			</xsl:attribute>
		</xsl:copy>
	</xsl:template>
	

	<!-- ================================================================= -->
	<!-- online resources: download -->
	<!-- ================================================================= -->

	<xsl:template match="gmd:linkage[starts-with(following-sibling::gmd:protocol/gco:CharacterString,'WWW:DOWNLOAD-') and contains(following-sibling::gmd:protocol/gco:CharacterString,'http--download') and following-sibling::gmd:name]">
		<gmd:linkage>
			<gmd:URL>
				<xsl:value-of select="concat(/root/env/siteURL,'/resources.get?id=',/root/env/id,'&amp;fname=',following-sibling::gmd:name/gco:CharacterString,'&amp;access=private')"/>
			</gmd:URL>
		</gmd:linkage>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="@*|node()">
		 <xsl:copy>
			  <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->

</xsl:stylesheet>
