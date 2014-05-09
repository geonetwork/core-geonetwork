<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gml="http://www.opengis.net/gml" xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:che="http://www.geocat.ch/2008/che" xmlns:xlink="http://www.w3.org/1999/xlink">


	<!-- Load labels. -->
	<xsl:variable name="label"
		select="/root/schemas/iso19139.emodnet.chemistry" />
	<xsl:template xmlns:geonet="http://www.fao.org/geonetwork"
		mode="iso19139" match="geonet:info" />
	<!-- Root element matching. -->
	<xsl:template match="/" priority="5">
		<html>
			<!-- Set some vars. -->
			<xsl:variable name="title"
				select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString" />

			<head>
				<title>
					Metadata:
					<xsl:value-of select="$title" />
				</title>
			</head>
			<body>

				<link rel="stylesheet" type="text/css"
					href="{root/url}/apps/sextant/css/schema/reset.css" />
				<link rel="stylesheet" type="text/css"
					href="{root/url}/apps/sextant/css/schema/emodnet.css" />
				<div class="tpl-emodnet">
					<div class="ui-layout-content">
						<table class="print_table" border="0" cellpadding="0"
							cellspacing="0">
							<tbody>
								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="/root/loc/strings/mdinfo" />
									</td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:fileIdentifier|root/gmd:MD_Metadata/gmd:language|
										/root/gmd:MD_Metadata/gmd:hierarchyLevelName/gmx:Anchor|
										/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme' and gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://www.seadatanet.org/urnurl/']|
										/root/gmd:MD_Metadata//gmd:resourceConstraints//gmd:useLimitation|
										/root/gmd:MD_Metadata/gmd:dateStamp
								" />

								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="/root/loc/strings/identification" />
									</td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title|
											/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code|
											/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName[1]" />

								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="/root/loc/strings/what" />
									</td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='discipline' and gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/' ]|
									/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialRepresentationType|
									/root/gmd:MD_Metadata//gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:cellGeometry|
									/root/gmd:MD_Metadata//gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:pointInPixel|
									/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator" />

								<xsl:if test="/root/gmd:MD_Metadata//gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='column']/gmd:dimensionSize or 
								/root/gmd:MD_Metadata//gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='row']/gmd:dimensionSize">
									<tr valign="top">
										<td class="print_ttl_h2">
											<xsl:value-of
												select="/root/schemas/iso19139.emodnet.chemistry/strings/DimensionsTitle" />
										</td>
										<td class="print_data"></td>
									</tr>
									<tr valign="top">
										<td class="print_desc">
											<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/nbLinesTitle" />
										</td>
										<td class="print_data">
											<xsl:value-of select="/root/gmd:MD_Metadata//gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='row']/gmd:dimensionSize" />
										</td>
									</tr>
									<tr valign="top">
										<td class="print_desc">
											<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/nbColumnsTitle" />
										</td>
										<td class="print_data">
											<xsl:value-of select="/root/gmd:MD_Metadata//gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='column']/gmd:dimensionSize" />
										</td>
									</tr>
								</xsl:if>
								
								

								<tr valign="top">
									<td class="print_ttl"><xsl:value-of
											select="/root/schemas/iso19139.emodnet.chemistry/strings/abstractTitle" /></td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract" />

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/dataSourcesTitle" />
									</td>
									<td class="print_data"></td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme' and gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']|
											/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='stratum'  and following-sibling::gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']|
											/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description
											" />

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/loc/strings/dataProcDescr" />
									</td>
									<td class="print_data"></td>
								</tr>
								
								<xsl:call-template name="writeAttribute">
									<xsl:with-param name="element" select="/root//gmd:MD_ScopeDescription" />
									<xsl:with-param name="attr" select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope[gmd:level/gmd:MD_ScopeCode/@codeListValue='propertyType']/gmd:levelDescription/gmd:MD_ScopeDescription/gmd:attributes/@uuidref" />
								</xsl:call-template>
								
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement|
											/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope[gmd:level/gmd:MD_ScopeCode/@codeListValue='software']/gmd:extent/gmd:EX_Extent/gmd:description" />

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/loc/strings/qualityAccur" />
									</td>
									<td class="print_data"></td>
								</tr>
								
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_NonQuantitativeAttributeAccuracy/gmd:measureDescription|
									/root/gmd:MD_Metadata//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation" />
								

								<tr valign="top">
									<td class="print_ttl_h2">
										<xsl:value-of select="/root/loc/strings/accuracyH" />
									</td>
									<td class="print_data"></td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:measureDescription|
											/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:result/gmd:DQ_QuantitativeResult/gmd:value|
											/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:evaluationMethodDescription" />

								<tr valign="top">
									<td class="print_ttl_h2">
										<xsl:value-of select="/root/loc/strings/accuracyV" />
									</td>
									<td class="print_data"></td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:measureDescription|
											/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:evaluationMethodDescription" />

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/loc/strings/suitability" />
									</td>
									<td class="print_data"></td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation" />

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/loc/strings/intellProperty" />
									</td>
									<td class="print_data"></td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints|
									/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useConstraints|
									/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints
									" />
								
								<tr valign="top">
									<td class="print_desc">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/creationDateTitle" />
									</td>
									<td class="print_data">
										<xsl:value-of select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date" />
									</td>
								</tr>
								<tr valign="top">
									<td class="print_desc">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/revisionDateTitle" />
									</td>
									<td class="print_data">
										<xsl:value-of select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date" />
									</td>
								</tr>
								
								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="/root/loc/strings/where" />
									</td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:EX_GeographicBoundingBox|
										/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance|
										/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier[not(gmd:RS_Identifier/gmd:codeSpace)]/gmd:MD_Identifier|
										/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:minimumValue|
										/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:maximumValue" />

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/loc/strings/CRS" />
									</td>
									<td class="print_data"></td>
								</tr>
								<tr valign="top">
									<td class="print_desc">
										<xsl:value-of select="/root/loc/strings/geoCRS" />
									</td>
									<td class="print_data">
										<xsl:value-of select="/root/gmd:MD_Metadata//gmd:referenceSystemInfo//gmd:RS_Identifier/gmd:code" />
									</td>
								</tr>
								
								<xsl:if test="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier[gmd:RS_Identifier/gmd:codeSpace]/gmd:RS_Identifier/gmd:code">
									<tr valign="top">
										<td class="print_ttl_h2">
											<xsl:value-of select="/root/loc/strings/projCRS" />
										</td>
										<td class="print_data"></td>
									</tr>
	
									<xsl:apply-templates mode="iso19139"
										select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier[gmd:RS_Identifier/gmd:codeSpace]/gmd:RS_Identifier/gmd:code|
										/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:description[../gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:RS_Identifier/gmd:codeSpace]" />
								</xsl:if>

								
								<!-- <tr valign="top">
									<td class="print_ttl_h1">Vertical Datum</td>
									<td class="print_data"></td>
								</tr>
								<tr valign="top">
									<td class="print_ttl_h2">Vertical reference of water depth</td>
									<td class="print_data"></td>
								</tr> -->
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent//gml:VerticalDatum/gml:identifier" />


								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="/root/loc/strings/when" />
									</td>
									<td class="print_data">
									</td>
								</tr>

								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:*/gml:TimeInstant|
									/root/gmd:MD_Metadata/gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='time']/gmd:resolution/gco:Measure|
									/root/gmd:MD_Metadata/gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='time']/gmd:resolution/gco:Measure/@uom" />

								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="/root/loc/strings/who" />
									</td>
									<td class="print_data">
									</td>
								</tr>
								
								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/originatorTitle" />
									</td>
									<td class="print_data"></td>
								</tr>

								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='originator']/gmd:organisationName|
											 /root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='originator']/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress" />
								
								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/dataHoldingCenterTitle" />
									</td>
									<td class="print_data"></td>
								</tr>
									
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='custodian']/gmd:organisationName|
									/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='custodian']/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress" />
								
								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="/root/loc/strings/whereToFind" />
									</td>
									<td class="print_data">
									</td>
								</tr>
								
								<xsl:apply-templates mode="iso19139"
									select="root/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage" />
								
								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/loc/strings/datadistribCenter" />
									</td>
									<td class="print_data"></td>
								</tr>

								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:distributionInfo//gmd:distributorContact//gmd:organisationName|
									root/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format|
									root/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:transferSize" />

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/loc/strings/collatingCenter" />
									</td>
									<td class="print_data"></td>
								</tr>

								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName|
											 /root/gmd:MD_Metadata//gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress" />
							</tbody>
						</table>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>

	<xsl:template mode="iso19139"
		match="gmd:MD_RepresentativeFraction/gmd:denominator" priority="3">
		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>

		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$title" />
			</td>
			<td class="print_data">
				<xsl:value-of select="concat('1 : ', gco:Integer)" />
			</td>
		</tr>
	</xsl:template>

	<!-- Write gco:DateTime under this element -->
	<xsl:template mode="iso19139" match="gmd:CI_Date/gmd:date|gmd:dateStamp">
		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>

		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$title" />
			</td>
			<td class="print_data">
				<xsl:value-of select="gco:DateTime" />
			</td>
		</tr>
	</xsl:template>

	<!-- Write text of the node under this element -->
	<xsl:template mode="iso19139"
		match="gmd:transferSize|gml:TimeInstant|gmd:maximumValue|gmd:minimumValue|gmd:MD_Identifier|gmd:pointInPixel|
		gmd:DQ_QuantitativeResult/gmd:value|gmd:CI_DateTypeCode/gmd:date|gmx:Anchor|gml:VerticalDatum/gml:identifier|
		gmd:MD_Resolution/gmd:distance">
		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>

		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$title" />
			</td>
			<td class="print_data">
				<xsl:value-of select="." />
			</td>
		</tr>
	</xsl:template>

	<xsl:template mode="iso19139"
		match="gmd:spatialRepresentationType|gmd:accessConstraints|gmd:MD_CellGeometryCode">
		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>

		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$title" />
			</td>
			<td class="print_data">
				<xsl:value-of select="*/@codeListValue" />
			</td>
		</tr>
	</xsl:template>


	<xsl:template mode="iso19139" match="gmd:EX_GeographicBoundingBox">

		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>

		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$title" />
			</td>
			<td class="print_data">
				<xsl:value-of select="gmd:westBoundLongitude/gco:Decimal" />
				/
				<xsl:value-of select="gmd:southBoundLatitude/gco:Decimal" />
				/
				<xsl:value-of select="gmd:eastBoundLongitude/gco:Decimal" />
				/
				<xsl:value-of select="gmd:northBoundLatitude/gco:Decimal" />
			</td>
		</tr>
	</xsl:template>

	<!-- Display Attribute -->
	<xsl:template name="writeAttribute">
		<xsl:param name="element" />
		<xsl:param name="attr" />
		
		<xsl:variable name="name" select="name($element)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>
		
		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$title" />
			</td>
			<td class="print_data">
				<xsl:value-of select="$attr" />
			</td>
		</tr>
	</xsl:template>
	
	<!-- Display characterString -->
	<xsl:template mode="iso19139"
		match="gmd:*[gco:CharacterString or gmd:PT_FreeText]|
        srv:*[gco:CharacterString or gmd:PT_FreeText]|
        gco:aName[gco:CharacterString]|gmd:*[gmd:URL]|gmd:*[gco:Integer]"
		priority="2">
		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:if test="gco:CharacterString!=''">
			<tr valign="top">
				<td class="print_desc">
					<xsl:value-of select="$title" />
				</td>
				<td class="print_data">
					<xsl:value-of select="gco:CharacterString" />
				</td>
			</tr>
		</xsl:if>
		<xsl:if test="gmd:URL!=''">
			<tr valign="top">
				<td class="print_desc">
					<xsl:value-of select="$title" />
				</td>
				<td class="print_data">
					<xsl:value-of select="gmd:URL" />
				</td>
			</tr>
		</xsl:if>
		<xsl:if test="gco:Integer!=''">
			<tr valign="top">
				<td class="print_desc">
					<xsl:value-of select="$title" />
				</td>
				<td class="print_data">
					<xsl:value-of select="gco:Integer" />
				</td>
			</tr>
		</xsl:if>
		<!-- Here you could display translation using PT_FreeText -->
	</xsl:template>


	<!-- Get title from che profil if exist, if not default to iso. -->
	<xsl:template name="getTitle">
		<xsl:param name="name" />
		<xsl:variable name="title"
			select="string($label/labels/element[@name=$name]/label)" />
		<xsl:choose>
			<xsl:when test="normalize-space($title)">
				<xsl:value-of select="$title" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of
					select="string(/root/schemas/iso19139/labels/element[@name=$name]/label)" />
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>


</xsl:stylesheet>