<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:fra="http://www.cnig.gouv.fr/2005/fra"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:geonet="http://www.fao.org/geonetwork" xmlns:exslt="http://exslt.org/common"
	exclude-result-prefixes="gmd gco gml gts srv xlink exslt geonet">

	<xsl:import href="metadata-fop.xsl"/>
	
	<!-- CSV export mode -->
	<xsl:template mode="csv" match="gmd:MD_Metadata">
		<xsl:choose>
			<xsl:when test="geonet:info/schema = 'iso19139.myocean'">
				<metadata>
					<ProductID><xsl:value-of select="gmd:fileIdentifier"/></ProductID>

					<ProductionCentre><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/
						gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='custodian']/
						gmd:organisationName/gco:CharacterString"/></ProductionCentre>

					<WorkPackage>Not available</WorkPackage>

					<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords
						[gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='reference-geographical-area']/gmd:MD_Keywords">
						<GeographicalReferenceArea><xsl:value-of select="gmd:keyword/*/text()"/></GeographicalReferenceArea>
					</xsl:for-each>

					<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords
						[gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='parameter']/gmd:MD_Keywords">
						<Parameters><xsl:value-of select="gmd:keyword/*/text()"/></Parameters>
					</xsl:for-each>
					
					<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords
						[gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='temporal-scale']/gmd:MD_Keywords">
						<TemporalScale><xsl:value-of select="gmd:keyword/*/text()"/></TemporalScale>
					</xsl:for-each>
					
					<Title><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/
						gmd:CI_Citation/gmd:title/gco:CharacterString"/></Title>
					
					<CustomerName><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/
						gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString"/></CustomerName>
					
					<ProductionUnit><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/
						gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='originator']/
						gmd:organisationName/gco:CharacterString"/></ProductionUnit>
					
					<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/
						gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
						<GeographicalCoverage><xsl:value-of select="gmd:westBoundLongitude"/>,<xsl:value-of select="gmd:southBoundLatitude"/><xsl:text> </xsl:text><xsl:value-of select="gmd:eastBoundLongitude"/>,<xsl:value-of select="gmd:northBoundLatitude"/></GeographicalCoverage>
					</xsl:for-each>
					
					<HorizontalResolution><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/
						gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance"/><xsl:text> </xsl:text><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/
							gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance/@uom"/></HorizontalResolution>
					
					
					<xsl:variable name="vnl_flag">vertical level number: </xsl:variable>
					<NumberOfVerticalLevels><xsl:value-of select="gmd:contentInfo/gmd:MD_CoverageDescription/gmd:dimension/
						gmd:MD_RangeDimension/gmd:descriptor[not(contains(., 'temporal'))]/substring-after(gco:CharacterString, $vnl_flag)"/></NumberOfVerticalLevels>
					
					<xsl:variable name="tr_flag">temporal resolution: </xsl:variable>
					<TemporalResolution><xsl:value-of select="gmd:contentInfo/gmd:MD_CoverageDescription/gmd:dimension/
						gmd:MD_RangeDimension/gmd:descriptor[contains(., 'temporal')]/substring-after(gco:CharacterString, $tr_flag)"/></TemporalResolution>
					
					<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords
						[gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='processing-level']/gmd:MD_Keywords">
						<ProcessingLevel><xsl:value-of select="gmd:keyword/*/text()"/></ProcessingLevel>
					</xsl:for-each>
					
					<Variables>Not available</Variables>
					
					<ScientificAccurary>Not available</ScientificAccurary>
					
					<ProcessingInformationTargetDeliveryTime><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceMaintenance/
						gmd:MD_MaintenanceInformation/gmd:maintenanceNote"/></ProcessingInformationTargetDeliveryTime>
					
					<ProcessingInformationUpdateFrequency><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceMaintenance/
						gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode/@codeListValue"/></ProcessingInformationUpdateFrequency>
					
					<ProcessingInformationUpdatedPeriodStart><xsl:value-of select="substring-before(gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceMaintenance/
						gmd:MD_MaintenanceInformation/gmd:updateScopeDescription/gmd:MD_ScopeDescription/gmd:other/gco:CharacterString, '#')"/></ProcessingInformationUpdatedPeriodStart>
					
					<ProcessingInformationUpdatedPeriodEnd><xsl:value-of select="substring-after(gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceMaintenance/
						gmd:MD_MaintenanceInformation/gmd:updateScopeDescription/gmd:MD_ScopeDescription/gmd:other/gco:CharacterString, '#')"/></ProcessingInformationUpdatedPeriodEnd>
					
					<TemporalExtent><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/
						gmd:EX_Extent/gmd:temporalElement/
						gmd:EX_TemporalExtent/gmd:extent//*[gml:beginPosition]"/><xsl:text>-</xsl:text><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/
							gmd:EX_Extent/gmd:temporalElement/
							gmd:EX_TemporalExtent/gmd:extent//*[gml:endPosition]"/></TemporalExtent>
					
					<NumberOfDatasetsRelated><xsl:value-of select="count(//gmd:onLine[@uuidref!=''])"/></NumberOfDatasetsRelated>
					
					<ProtocolMYOSUB><xsl:value-of select="count(//gmd:onLine[gmd:CI_OnlineResource/
						gmd:protocol/gco:CharacterString = 'MYO:MOTU-SUB'])"/></ProtocolMYOSUB>
					<ProtocolMYODGF><xsl:value-of select="count(//gmd:onLine[gmd:CI_OnlineResource/
						gmd:protocol/gco:CharacterString = 'MYO:MOTU-DGF'])"/></ProtocolMYODGF>
					<ProtocolWWWFTP><xsl:value-of select="count(//gmd:onLine[gmd:CI_OnlineResource/
						gmd:protocol/gco:CharacterString = 'WWW:FTP'])"/></ProtocolWWWFTP>
					<ProtocolOGCWMS><xsl:value-of select="count(//gmd:onLine[gmd:CI_OnlineResource/
						gmd:protocol/gco:CharacterString = 'OGC:WMS'])"/></ProtocolOGCWMS>
					
					<MetadataCreationTime><xsl:value-of select="geonet:info/createDate"/></MetadataCreationTime>
					
					<MetadataLastUpdateTime><xsl:value-of select="geonet:info/changeDate"/></MetadataLastUpdateTime>
					
					<xsl:copy-of select="geonet:info"/>
				</metadata>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
  <xsl:template name="iso19139.myoceanBrief">
    <metadata>
			<xsl:choose>
		    <xsl:when test="geonet:info/isTemplate='s'">
		      <xsl:apply-templates mode="iso19139-subtemplate" select="."/>
		      <xsl:copy-of select="geonet:info" copy-namespaces="no"/>
		    </xsl:when>
		    <xsl:otherwise>
	
			<!-- call iso19139 brief -->
			<xsl:call-template name="iso19139-brief"/>
		    </xsl:otherwise>
		  </xsl:choose>    
    </metadata>
  </xsl:template>


  <xsl:template name="iso19139.myoceanCompleteTab">
    <xsl:param name="tabLink"/>
    <xsl:param name="schema"/>
    
    <xsl:call-template name="iso19139CompleteTab">
      <xsl:with-param name="tabLink" select="$tabLink"/>
      <xsl:with-param name="schema" select="$schema"/>
    </xsl:call-template>
  </xsl:template>

	<!-- main template - the way into processing iso19139.myocean -->
	<xsl:template name="metadata-iso19139.myocean">
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="embedded"/>

		<xsl:apply-templates mode="iso19139" select="." >
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="embedded" select="$embedded" />
		</xsl:apply-templates>
	</xsl:template>
	
	<!-- =================================================================== -->
	<!-- === Javascript used by functions in this presentation XSLT          -->
	<!-- =================================================================== -->
	<xsl:template name="iso19139.myocean-javascript"/>

</xsl:stylesheet>
