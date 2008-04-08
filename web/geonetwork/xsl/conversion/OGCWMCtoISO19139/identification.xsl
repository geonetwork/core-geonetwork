<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns ="http://www.isotc211.org/2005/gmd"
							  xmlns:wmc="http://www.opengis.net/context"
							  xmlns:gco="http://www.isotc211.org/2005/gco"
							  xmlns:gts="http://www.isotc211.org/2005/gts"
							  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
							  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
							  xmlns:xlink="http://www.w3.org/1999/xlink"
							  xmlns:math="http://exslt.org/math"
							  extension-element-prefixes="math">

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="DataIdentification">
	
		<citation>
			<CI_Citation>
				<title>
					<gco:CharacterString><xsl:value-of select="/wmc:ViewContext/wmc:General/wmc:Title"/></gco:CharacterString>
				</title>
			</CI_Citation>
		</citation>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<abstract>
			<gco:CharacterString><xsl:value-of select="/wmc:ViewContext/wmc:General/wmc:Abstract"/></gco:CharacterString>
		</abstract>

		<!--idPurp-->
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<status>
			<MD_ProgressCode codeList="./resources/codeList.xml#MD_ProgressCode" codeListValue="completed" />
		</status>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="/wmc:ViewContext/wmc:General/wmc:ContactInformation">
			<pointOfContact>
				<CI_ResponsibleParty>
					<xsl:apply-templates select="." mode="RespParty"/>
				</CI_ResponsibleParty>
			</pointOfContact>
		</xsl:for-each>

		<!-- resMaint -->
		<!-- graphOver -->
		<!-- dsFormat-->
		
		<xsl:for-each select="/wmc:ViewContext/wmc:General/wmc:KeywordList">
			<descriptiveKeywords>
				<MD_Keywords>
					<xsl:apply-templates select="." mode="Keywords"/>
				</MD_Keywords>
			</descriptiveKeywords>
		</xsl:for-each>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		
		<topicCategory>
			<MD_TopicCategoryCode><xsl:value-of select="$topic"/></MD_TopicCategoryCode>
		</topicCategory>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		
		<xsl:choose>
			<xsl:when test="/wmc:ViewContext/wmc:General/wmc:BoundingBox/@SRS='EPSG:4326'">
				<extent>
				<EX_Extent>
					<geographicElement>
						<EX_GeographicBoundingBox>
							<westBoundLongitude>
								<gco:Decimal><xsl:value-of select="/wmc:ViewContext/wmc:General/wmc:BoundingBox/@minx"/></gco:Decimal>
							</westBoundLongitude>
							<eastBoundLongitude>
								<gco:Decimal><xsl:value-of select="/wmc:ViewContext/wmc:General/wmc:BoundingBox/@maxx"/></gco:Decimal>
							</eastBoundLongitude>
							<southBoundLatitude>
								<gco:Decimal><xsl:value-of select="/wmc:ViewContext/wmc:General/wmc:BoundingBox/@miny"/></gco:Decimal>
							</southBoundLatitude>
							<northBoundLatitude>
								<gco:Decimal><xsl:value-of select="/wmc:ViewContext/wmc:General/wmc:BoundingBox/@maxy"/></gco:Decimal>
							</northBoundLatitude>
						</EX_GeographicBoundingBox>
					</geographicElement>
				</EX_Extent>
			</extent>
			</xsl:when>
			<xsl:otherwise>
				<!-- TODO support other SRS as EX_BoundingPolygon  -->
			</xsl:otherwise>
		</xsl:choose>
		
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		

	</xsl:template>



	<!-- ============================================================================= -->
	<!-- === Keywords === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Keywords">

		<xsl:for-each select="Keyword">
			<keyword>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</keyword>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<type>
			<MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode" codeListValue="theme" />
		</type>

	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
