<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
	xmlns:dc ="http://purl.org/dc/elements/1.1/"
	xmlns:dct="http://purl.org/dc/terms/"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:geonet="http://www.fao.org/geonetwork"
	exclude-result-prefixes="gmd srv gco">
	
	<xsl:param name="displayInfo"/>
	<xsl:param name="lang"/>
	
	<xsl:include href="../metadata-utils.xsl"/>
	
	<!-- ============================================================================= -->
	
	<xsl:template match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
		
		<xsl:variable name="info" select="geonet:info"/>
		<xsl:variable name="langId">
			<xsl:call-template name="getLangId">
				<xsl:with-param name="langGui" select="$lang"/>
				<xsl:with-param name="md" select="."/>
			</xsl:call-template>
		</xsl:variable>
		
		<csw:SummaryRecord>
			
			<xsl:for-each select="gmd:fileIdentifier">
				<dc:identifier><xsl:value-of select="gco:CharacterString"/></dc:identifier>
			</xsl:for-each>
			
			<!-- DataIdentification -->
			
			<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification|
				gmd:identificationInfo/*[@gco:isoType='gmd:MD_DataIdentification']|
				gmd:identificationInfo/srv:SV_ServiceIdentification|
				gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']">
				
				<xsl:for-each select="gmd:citation/gmd:CI_Citation/gmd:title">
					<dc:title>
						<xsl:apply-templates mode="localised" select=".">
							<xsl:with-param name="langId" select="$langId"/>
						</xsl:apply-templates>
					</dc:title>
				</xsl:for-each>
				
				<!-- Type -->
				<xsl:for-each select="../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue">
					<dc:type><xsl:value-of select="."/></dc:type>
				</xsl:for-each>
				
				
				<xsl:for-each select="gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[not(@gco:nilReason)]">
					<dc:subject>
						<xsl:apply-templates mode="localised" select=".">
							<xsl:with-param name="langId" select="$langId"/>
						</xsl:apply-templates>
					</dc:subject>
				</xsl:for-each>
				<xsl:for-each select="gmd:topicCategory/gmd:MD_TopicCategoryCode">
					<dc:subject><xsl:value-of select="."/></dc:subject><!-- TODO : translate ? -->
				</xsl:for-each>
				
				<!-- Distribution -->
				
				<xsl:for-each select="../../gmd:distributionInfo/gmd:MD_Distribution">
					<xsl:for-each select="gmd:distributionFormat/gmd:MD_Format/gmd:name">
						<dc:format>
							<xsl:apply-templates mode="localised" select=".">
								<xsl:with-param name="langId" select="$langId"/>
							</xsl:apply-templates>
						</dc:format>
					</xsl:for-each>
				</xsl:for-each>
				
				<!-- Parent Identifier -->
				
				<xsl:for-each select="../../gmd:parentIdentifier/gco:CharacterString">
					<dc:relation><xsl:value-of select="."/></dc:relation>
				</xsl:for-each>
				
				<xsl:for-each select="gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date">
					<dct:modified><xsl:value-of select="."/></dct:modified>
				</xsl:for-each>
				
				<xsl:for-each select="gmd:abstract">
					<dct:abstract>
						<xsl:apply-templates mode="localised" select=".">
							<xsl:with-param name="langId" select="$langId"/>
						</xsl:apply-templates>
					</dct:abstract>
				</xsl:for-each>
				
			</xsl:for-each>
			
			<!-- Lineage 
			
			<xsl:for-each select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString">
				<dc:source><xsl:value-of select="."/></dc:source>
				</xsl:for-each>-->
			
			
			<!-- GeoNetwork elements added when resultType is equal to results_with_summary -->
			<xsl:if test="$displayInfo = 'true'">
				<xsl:copy-of select="$info"/>
			</xsl:if>
			
		</csw:SummaryRecord>
	</xsl:template>
	
	<!-- ============================================================================= -->
	
	<xsl:template match="*">
		<xsl:apply-templates select="*"/>
	</xsl:template>
	
	<!-- ============================================================================= -->
	
</xsl:stylesheet>