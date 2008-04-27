<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
										xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" 
										xmlns:dc ="http://purl.org/dc/elements/1.1/" 
										xmlns:dct="http://purl.org/dc/terms/" 
										xmlns:ows="http://www.opengis.net/ows">

	<!-- ============================================================================= -->

	<xsl:template match="Metadata">
		<csw:Record>

			<xsl:for-each select="mdFileID">
				<dc:identifier><xsl:value-of select="."/></dc:identifier>
			</xsl:for-each>

			<!-- DataIdentification - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="dataIdInfo">

				<xsl:for-each select="idCitation">
					<xsl:for-each select="resTitle">
						<dc:title><xsl:value-of select="."/></dc:title>
					</xsl:for-each>

					<xsl:for-each select="resRefDate[refDateType/DateTypCd/@value='revision']/refDate">
						<dct:modified><xsl:value-of select="."/></dct:modified>
					</xsl:for-each>

					<xsl:for-each select="citRespParty[role/RoleCd/@value='originator']/rpOrgName">
						<dc:creator><xsl:value-of select="."/></dc:creator>
					</xsl:for-each>

					<xsl:for-each select="citRespParty[role/RoleCd/@value='publisher']/rpOrgName">
						<dc:publisher><xsl:value-of select="."/></dc:publisher>
					</xsl:for-each>

					<xsl:for-each select="citRespParty[role/RoleCd/@value='author']/rpOrgName">
						<dc:contributor><xsl:value-of select="."/></dc:contributor>
					</xsl:for-each>
				</xsl:for-each>

				<!-- subject -->

				<xsl:for-each select="descKeys/keyword">
					<dc:subject><xsl:value-of select="."/></dc:subject>
				</xsl:for-each>

				<!-- abstract -->

				<xsl:for-each select="idAbs">
					<dct:abstract><xsl:value-of select="."/></dct:abstract>
				</xsl:for-each>

				<!-- rights -->

				<!-- language -->

				<xsl:for-each select="mdLang">
					<dc:language><xsl:value-of select="."/></dc:language>
				</xsl:for-each>

				<!-- bounding box -->

				<xsl:for-each select="dataExt/geoEle">
					<xsl:variable name="rsi" select="/Metadata/refSysInfo/refSysId"/>
					<xsl:variable name="auth" select="$rsi/identCodeSpace"/>
					<xsl:variable name="id" select="$rsi/identCode"/>

					<ows:BoundingBox crs="{$auth}::{$id}">
						<ows:LowerCorner>
							<xsl:value-of select="concat(eastBL, ' ', southBL)"/>
						</ows:LowerCorner>

						<ows:UpperCorner>
							<xsl:value-of select="concat(westBL, ' ', northBL)"/>
						</ows:UpperCorner>
					</ows:BoundingBox>
				</xsl:for-each>

			</xsl:for-each>

			<!-- Type - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="mdHrLv/ScopeCd/@value">
				<dc:type><xsl:value-of select="."/></dc:type>
			</xsl:for-each>

			<!-- Distribution - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="distInfo">
				<xsl:for-each select="distributor/distorFormat/formatName">
					<dc:format><xsl:value-of select="."/></dc:format>
				</xsl:for-each>
			</xsl:for-each>

		</csw:Record>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*">
		<xsl:apply-templates select="*"/>
	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
