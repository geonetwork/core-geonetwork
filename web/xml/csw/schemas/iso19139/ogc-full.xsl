<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:csw="http://www.opengis.net/cat/csw"
										xmlns:dc ="http://purl.org/dc/elements/1.1/"
										xmlns:dct="http://purl.org/dc/terms/"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:gmd="http://www.isotc211.org/2005/gmd" 
										xmlns:ows="http://www.opengis.net/ows">

	<!-- ============================================================================= -->

	<xsl:template match="gmd:MD_Metadata">
		<csw:Record>

			<!-- DataIdentification - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification">

				<xsl:for-each select="gmd:citation/gmd:CI_Citation">
					<xsl:for-each select="gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
						<dc:identifier><xsl:value-of select="."/></dc:identifier>
					</xsl:for-each>
	
					<xsl:for-each select="gmd:title/gco:CharacterString">
						<dc:title><xsl:value-of select="."/></dc:title>
					</xsl:for-each>

					<xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date">
						<dct:modified><xsl:value-of select="."/></dct:modified>
					</xsl:for-each>

					<xsl:for-each select="gmd:citedResponsibleParty/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='originator']/gmd:organisationName/gco:CharacterString">
						<dc:creator><xsl:value-of select="."/></dc:creator>
					</xsl:for-each>

					<xsl:for-each select="gmd:citedResponsibleParty/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='publisher']/gmd:organisationName/gco:CharacterString">
						<dc:publisher><xsl:value-of select="."/></dc:publisher>
					</xsl:for-each>

					<xsl:for-each select="gmd:citedResponsibleParty/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='author']/gmd:organisationName/gco:CharacterString">
						<dc:contributor><xsl:value-of select="."/></dc:contributor>
					</xsl:for-each>
				</xsl:for-each>

				<!-- subject -->

				<xsl:for-each select="gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString">
					<dc:subject><xsl:value-of select="."/></dc:subject>
				</xsl:for-each>

				<!-- abstract -->

				<xsl:for-each select="gmd:abstract/gco:CharacterString">
					<dct:abstract><xsl:value-of select="."/></dct:abstract>
				</xsl:for-each>

				<!-- rights -->

				<xsl:for-each select="gmd:resourceConstraints/gmd:MD_LegalConstraints">
					<xsl:for-each select="*/gmd:MD_RestrictionCode/@codeListValue">
						<dc:rights><xsl:value-of select="."/></dc:rights>
					</xsl:for-each>

					<xsl:for-each select="otherConstraints/gco:CharacterString">
						<dc:rights><xsl:value-of select="."/></dc:rights>
					</xsl:for-each>
				</xsl:for-each>

				<!-- language -->

				<xsl:for-each select="gmd:language/gco:CharacterString">
					<dc:language><xsl:value-of select="."/></dc:language>
				</xsl:for-each>

				<!-- bounding box -->

				<xsl:for-each select="gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
					<xsl:variable name="rsi"  select="/gmd:MD_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier"/>
					<xsl:variable name="auth" select="$rsi/gmd:codeSpace/gco:CharacterString"/>
					<xsl:variable name="id"   select="$rsi/gmd:code/gco:CharacterString"/>
	
					<ows:BoundingBox crs="{$auth}::{$id}">
						<ows:LowerCorner>
							<xsl:value-of select="concat(gmd:eastBoundLongitude/gco:Decimal, ' ', gmd:southBoundLatitude/gco:Decimal)"/>
						</ows:LowerCorner>
		
						<ows:UpperCorner>
							<xsl:value-of select="concat(gmd:westBoundLongitude/gco:Decimal, ' ', gmd:northBoundLatitude/gco:Decimal)"/>
						</ows:UpperCorner>
					</ows:BoundingBox>
				</xsl:for-each>

			</xsl:for-each>

			<!-- Type - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue">
				<dc:type><xsl:value-of select="."/></dc:type>
			</xsl:for-each>

			<!-- Distribution - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution">
				<xsl:for-each select="gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString">
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
