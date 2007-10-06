<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<!-- ============================================================================================ -->
	
	<xsl:output indent="yes"/>
	
	<!-- ============================================================================================ -->
	
	<xsl:template match="Metadata">
		<oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
						xmlns:dc   ="http://purl.org/dc/elements/1.1/"
						xmlns:xsi  ="http://www.w3.org/2001/XMLSchema-instance"
						xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">

			<xsl:for-each select="mdFileID">
				<dc:identifier><xsl:value-of select="."/></dc:identifier>
			</xsl:for-each>

			<dc:date><xsl:value-of select="/root/env/changeDate"/></dc:date>
			
			<!-- DataIdentification - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="dataIdInfo">

				<xsl:for-each select="idCitation">
					<xsl:for-each select="resTitle">
						<dc:title><xsl:value-of select="."/></dc:title>
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

				<!-- description -->

				<xsl:for-each select="idAbs">
					<dc:description><xsl:value-of select="."/></dc:description>
				</xsl:for-each>

				<!-- rights -->

				<!-- language -->

				<xsl:for-each select="mdLang">
					<dc:language><xsl:value-of select="."/></dc:language>
				</xsl:for-each>

				<!-- bounding box -->

				<xsl:for-each select="dataExt/geoEle">
					<dc:coverage>
						<xsl:value-of select="concat('North ', northBL, ', ')"/>
						<xsl:value-of select="concat('South ', southBL, ', ')"/>
						<xsl:value-of select="concat('East ' , eastBL,  ', ')"/>
						<xsl:value-of select="concat('West ' , westBL,  '.')"/>
					</dc:coverage>
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
		</oai_dc:dc>
	</xsl:template>
	
	<!-- ============================================================================================ -->
	
	<xsl:template match="*">
		<xsl:apply-templates select="*"/>
	</xsl:template>
	
	<!-- ============================================================================================ -->
	
</xsl:stylesheet>
