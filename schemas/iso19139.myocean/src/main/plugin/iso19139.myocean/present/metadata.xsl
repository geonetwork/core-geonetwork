<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:fra="http://www.cnig.gouv.fr/2005/fra"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:geonet="http://www.fao.org/geonetwork" xmlns:exslt="http://exslt.org/common"
	exclude-result-prefixes="gmd gco gml gts srv xlink exslt geonet">

	<xsl:import href="metadata-fop.xsl"/>
	<xsl:include href="metadata-markup.xsl"/>
	
	<!-- CSV export mode -->
	<xsl:template mode="csv" match="gmd:MD_Metadata">
		
		<xsl:variable name="isProduct" select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='series'"/>
		
		<xsl:choose>
			<xsl:when test="geonet:info/schema = 'iso19139.myocean' or geonet:info/schema = 'iso19139.myocean.short'">
				<metadata>
					
					<Type><xsl:value-of select="if ($isProduct) then 'PRODUCT' else 'DATASET'"/></Type>

					<!--<Identifier><xsl:value-of select="gmd:fileIdentifier"/></Identifier>
					-->
					<ProductionCentre><xsl:value-of select="if ($isProduct) then gmd:identificationInfo/gmd:MD_DataIdentification/
						gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='custodian']/
						gmd:organisationName/gco:CharacterString else ''"/></ProductionCentre>
					
					<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords
						[gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='reference-geographical-area']/gmd:MD_Keywords/gmd:keyword">
						<GeographicalReferenceArea><xsl:value-of select="./*/text()"/></GeographicalReferenceArea>
					</xsl:for-each>
					
					
					<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords
						[gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='parameter']/gmd:MD_Keywords/gmd:keyword">
						<Parameters><xsl:value-of select="*/text()"/></Parameters>
					</xsl:for-each>
						
					
					<xsl:choose>
						<xsl:when test="$isProduct">
							<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords
								[gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='temporal-scale']/gmd:MD_Keywords/gmd:keyword">
								<TemporalScale><xsl:value-of select="./*/text()"/></TemporalScale>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<TemporalScale></TemporalScale>
						</xsl:otherwise>
					</xsl:choose>
					
					<Title><xsl:value-of select="if ($isProduct) then gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/
						gmd:CI_Citation/gmd:title/gco:CharacterString else ''"/></Title>
					
					<Description><xsl:value-of select="if ($isProduct) then '' else gmd:identificationInfo/
						gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString"/></Description>
					
					
					
					<xsl:choose>
						<xsl:when test="$isProduct">
							<DisseminationUnit></DisseminationUnit>
						</xsl:when>
						<xsl:otherwise>
							<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/
								gmd:pointOfContact/gmd:CI_ResponsibleParty/
								gmd:organisationName/gco:CharacterString">
								<DisseminationUnit><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/
									gmd:pointOfContact/gmd:CI_ResponsibleParty/
									gmd:contactInfo/gmd:CI_Contact/gmd:address/
									gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"/></DisseminationUnit>
							</xsl:for-each>
						</xsl:otherwise>
					</xsl:choose>
					
					
					<CustomerName><xsl:value-of select="if ($isProduct) then gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/
						gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString else ''"/></CustomerName>
					
					<ProductionUnit><xsl:value-of select="if ($isProduct) then gmd:identificationInfo/gmd:MD_DataIdentification/
						gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='originator']/
						gmd:organisationName/gco:CharacterString else ''"/></ProductionUnit>
					
					<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/
						gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
						<GeographicalCoverage><xsl:value-of select="gmd:westBoundLongitude"/>-<xsl:value-of select="gmd:eastBoundLongitude"/><xsl:text> </xsl:text><xsl:value-of select="gmd:southBoundLatitude"/>-<xsl:value-of select="gmd:northBoundLatitude"/></GeographicalCoverage>
					</xsl:for-each>
					
					<HorizontalResolution><xsl:if test="$isProduct"><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/
						gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance"/><xsl:text> </xsl:text><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/
							gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance/@uom"/></xsl:if></HorizontalResolution>
					
					
					<xsl:variable name="vnl_flag">vertical level number: </xsl:variable>
					<NumberOfVerticalLevels><xsl:if test="$isProduct"><xsl:value-of select="gmd:contentInfo/gmd:MD_CoverageDescription/gmd:dimension/
						gmd:MD_RangeDimension/gmd:descriptor[not(contains(., 'temporal'))]/substring-after(gco:CharacterString, $vnl_flag)"/></xsl:if></NumberOfVerticalLevels>
					
					<xsl:variable name="tr_flag">temporal resolution: </xsl:variable>
					<TemporalResolution><xsl:if test="$isProduct"><xsl:value-of select="gmd:contentInfo/gmd:MD_CoverageDescription/gmd:dimension/
						gmd:MD_RangeDimension/gmd:descriptor[contains(., 'temporal')]/substring-after(gco:CharacterString, $tr_flag)"/></xsl:if></TemporalResolution>
					
					
					<xsl:choose>
						<xsl:when test="$isProduct">
							<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords
								[gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='processing-level']/gmd:MD_Keywords">
								<ProcessingLevel><xsl:value-of select="gmd:keyword/*/text()"/></ProcessingLevel>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<ProcessingLevel></ProcessingLevel>
						</xsl:otherwise>
					</xsl:choose>
					
					<ProcessingInformationTargetDeliveryTime><xsl:value-of select="if ($isProduct) then gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceMaintenance/
						gmd:MD_MaintenanceInformation/gmd:maintenanceNote else ''"/></ProcessingInformationTargetDeliveryTime>
					
					<ProcessingInformationUpdateFrequency><xsl:value-of select="if ($isProduct) then gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceMaintenance/
						gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode/@codeListValue else ''"/></ProcessingInformationUpdateFrequency>
					
					<ProcessingInformationUpdatedPeriodStart><xsl:value-of select="if ($isProduct) then substring-before(gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceMaintenance/
						gmd:MD_MaintenanceInformation/gmd:updateScopeDescription/gmd:MD_ScopeDescription/gmd:other/gco:CharacterString, '#') else ''"/></ProcessingInformationUpdatedPeriodStart>
					
					<ProcessingInformationUpdatedPeriodEnd><xsl:value-of select="if ($isProduct) then substring-after(gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceMaintenance/
						gmd:MD_MaintenanceInformation/gmd:updateScopeDescription/gmd:MD_ScopeDescription/gmd:other/gco:CharacterString, '#') else ''"/></ProcessingInformationUpdatedPeriodEnd>
					
					<TemporalExtentStart><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/
						gmd:EX_Extent/gmd:temporalElement/
						gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition"/></TemporalExtentStart>
					
					<TemporalExtentEnd><xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/
							gmd:EX_Extent/gmd:temporalElement/
							gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition"/></TemporalExtentEnd>
					
					<NumberOfDatasetsRelated><xsl:value-of select="if ($isProduct) then count(.//gmd:onLine[@uuidref!='']) else ''"/></NumberOfDatasetsRelated>
					
					
					<xsl:choose>
						<xsl:when test="$isProduct">
							<xsl:for-each select=".//gmd:onLine[@uuidref!='']">
								<ListOfRelatedDataset><xsl:value-of select="@uuidref"/></ListOfRelatedDataset>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<ListOfRelatedDataset></ListOfRelatedDataset>
						</xsl:otherwise>
					</xsl:choose>
					
					<Protocol-MYO-SUB><xsl:value-of select="if ($isProduct) then count(.//gmd:onLine[gmd:CI_OnlineResource/
						gmd:protocol/gco:CharacterString = 'MYO:MOTU-SUB']) > 0 else ''"/></Protocol-MYO-SUB>
					<Protocol-MYO-DGF><xsl:value-of select="if ($isProduct) then count(.//gmd:onLine[gmd:CI_OnlineResource/
						gmd:protocol/gco:CharacterString = 'MYO:MOTU-DGF']) > 0 else ''"/></Protocol-MYO-DGF>
					<Protocol-WWW-FTP><xsl:value-of select="if ($isProduct) then count(.//gmd:onLine[gmd:CI_OnlineResource/
						gmd:protocol/gco:CharacterString = 'WWW:FTP']) > 0 else ''"/></Protocol-WWW-FTP>
					<Protocol-OGC-WMS><xsl:value-of select="if ($isProduct) then count(.//gmd:onLine[gmd:CI_OnlineResource/
						gmd:protocol/gco:CharacterString = 'OGC:WMS']) > 0 else ''"/></Protocol-OGC-WMS>
					
					<xsl:choose>
						<xsl:when test="$isProduct">
							<URL-MYO-MOTU-SUB></URL-MYO-MOTU-SUB>
							<URL-MYO-DGF></URL-MYO-DGF>
							<URL-WWW-FTP></URL-WWW-FTP>
							<URL-OGC-WMS></URL-OGC-WMS>
						</xsl:when>
						<xsl:otherwise>
							<xsl:for-each select=".//gmd:onLine/gmd:CI_OnlineResource[
								gmd:protocol/gco:CharacterString = 'MYO:MOTU-SUB']">
								<URL-MYO-MOTU-SUB><xsl:value-of select="gmd:linkage/gmd:URL"/></URL-MYO-MOTU-SUB>
							</xsl:for-each>
							<xsl:for-each select=".//gmd:onLine/gmd:CI_OnlineResource[
								gmd:protocol/gco:CharacterString = 'MYO:MOTU-DGF']">
								<URL-MYO-DGF><xsl:value-of select="gmd:linkage/gmd:URL"/></URL-MYO-DGF>
							</xsl:for-each>
							<xsl:for-each select=".//gmd:onLine/gmd:CI_OnlineResource[
								gmd:protocol/gco:CharacterString = 'WWW:FTP']">
								<URL-WWW-FTP><xsl:value-of select="gmd:linkage/gmd:URL"/></URL-WWW-FTP>
							</xsl:for-each>
							<xsl:for-each select=".//gmd:onLine/gmd:CI_OnlineResource[
								gmd:protocol/gco:CharacterString = 'OGC:WMS']">
								<URL-OGC-WMS><xsl:value-of select="gmd:linkage/gmd:URL"/></URL-OGC-WMS>
							</xsl:for-each>
						</xsl:otherwise>
					</xsl:choose>
					
					<!-- ProductOrigins column -->
					<xsl:choose>
						<xsl:when test="$isProduct">
							<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords
							/gmd:MD_Keywords/gmd:keyword/gmx:Anchor[starts-with(@xlink:href,'http://purl.org/myocean/ontology/vocabulary/discipline')]">
								<ProductOrigins><xsl:value-of select="./text()"/></ProductOrigins>
							</xsl:for-each>
						</xsl:when>
					</xsl:choose>
					
					<ProductUpdateDate><xsl:value-of select="if ($isProduct) then gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/
						gmd:CI_Citation/gmd:editionDate/gco:Date else ''"/></ProductUpdateDate>
					<!-- FeatureType column -->
					<xsl:for-each select="gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureTypes/gco:LocalName">
						<FeatureType><xsl:value-of select="./text()"/></FeatureType>
					</xsl:for-each>
					
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
