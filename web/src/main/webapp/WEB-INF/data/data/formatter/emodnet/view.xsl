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
					href="{/root/url}../../apps/sextant/css/schema/reset.css" />
				<link rel="stylesheet" type="text/css"
					href="{/root/url}../../apps/sextant/css/schema/emodnet.css" />
				<div class="tpl-emodnet">
					<div class="ui-layout-content">
						<table class="print_table" border="0" cellpadding="0"
							cellspacing="0">
							<tbody>

								<!-- **************************************** IDENTIFICATION **************************************** -->
								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="/root/loc/strings/identification" />
									</td>
									<td class="print_data">
									</td>
								</tr>

								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title|
											/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code" />

								<!--
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName[1]" />
-->
							</tbody>
						</table>

						<table class="print_table2" border="0" cellpadding="0" cellspacing="0">
							<tbody>
								<tr>
								<td/>
									<td class="print_bounding">
										<xsl:value-of select="//gmd:northBoundLatitude/gco:Decimal" />
									</td>
									<td/>
								</tr>
								<tr>
									<td class="print_bounding">
										<xsl:value-of select="//gmd:westBoundLongitude/gco:Decimal" />
									</td>
									<td class="print_bounding">
										<img class="result-photo">
											<xsl:attribute name="src">
												<xsl:value-of select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview[position()=1]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString" />
											</xsl:attribute>
										</img>
									</td>	
									<td class="print_bounding">
										<xsl:value-of select="//gmd:eastBoundLongitude/gco:Decimal" />
									</td>									
								</tr>
								<tr>
																<td/>
									<td class="print_bounding">
										<xsl:value-of select="//gmd:southBoundLatitude/gco:Decimal" />
									</td>
									<td/>
								</tr>

							</tbody>
						</table>

						<table class="print_table" border="0" cellpadding="0"
							cellspacing="0">
							<tbody>
								<!-- **************************************** ABSTRACT **************************************** -->
								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of
											select="/root/schemas/iso19139.emodnet.chemistry/strings/abstractTitle" />
									</td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract" />




								<!-- **************************************** WHAT **************************************** -->
								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="/root/loc/strings/what" />
									</td>
									<td class="print_data">
									</td>
								</tr>

								<tr valign="top">
									<td class="print_desc">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/ParamsMeasuredTitle" />
									</td>
									<td class="print_data">
										<xsl:value-of select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[../gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='discipline' and ../gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/' ]" />
									</td>
								</tr>


								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialRepresentationType"/>

								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:pointInPixel | /root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator"/>

								<xsl:if test="/root/gmd:MD_Metadata//gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='column']/gmd:dimensionSize or 
								/root/gmd:MD_Metadata//gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='row']/gmd:dimensionSize">
									<tr valign="top">
										<td class="print_ttl_h1">
											<xsl:value-of
												select="/root/schemas/iso19139.emodnet.chemistry/strings/DimensionsTitle" />
										</td>
										<td class="print_data"/>
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



								<!-- **************************************** DETAILED DESCRIPTION **************************************** -->
								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of
											select="/root/schemas/iso19139.emodnet.chemistry/strings/detailedDescTitle" />
									</td>
									<td class="print_data">
									</td>
								</tr>

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/loc/strings/processedDSDescr" />
									</td>
									<td class="print_data"/>
								</tr>

								<tr valign="top">
									<td class="print_desc">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/MeasuringInstrsTitle" />
									</td>
									<td class="print_data">
										<xsl:value-of select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[../gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme' and ../gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']" />
									</td>
								</tr>
								<!-- Only for Emodnet hydro -->
								<xsl:if test="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='stratum'  and following-sibling::gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']">
									<tr valign="top">
										<td class="print_desc">
											<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/PositioningInstrsTitle" />
										</td>
										<td class="print_data">
											<xsl:value-of select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='stratum'  and following-sibling::gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']" />
										</td>
									</tr>
								</xsl:if>


								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description" />



								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/loc/strings/dataProcDescr" />
									</td>
									<td class="print_data"/>
								</tr>
<!--
								<xsl:if test="/root//gmd:MD_ScopeDescription/gmd:attributes">
									<xsl:call-template name="writeAttribute">
										<xsl:with-param name="element" select="/root//gmd:MD_ScopeDescription" />
										<xsl:with-param name="attr" select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope[gmd:level/gmd:MD_ScopeCode/@codeListValue='propertyType']/gmd:levelDescription/gmd:MD_ScopeDescription/gmd:attributes/@uuidref" />
									</xsl:call-template>
								</xsl:if>
-->
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:dataQualityInfo[position()>1]/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement" />

								<xsl:if test="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope[gmd:level/gmd:MD_ScopeCode/@codeListValue='software']/gmd:extent/gmd:EX_Extent/gmd:description/gco:CharacterString!=''">
									<tr valign="top">
										<td class="print_desc">
											<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/processingSoftwareTitle" />
										</td>
										<td class="print_data">
											<xsl:value-of select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope[gmd:level/gmd:MD_ScopeCode/@codeListValue='software']/gmd:extent/gmd:EX_Extent/gmd:description" />
										</td>
									</tr>
								</xsl:if>

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/loc/strings/qualityAccur" />
									</td>
									<td class="print_data"/>
								</tr>

								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_NonQuantitativeAttributeAccuracy/gmd:measureDescription" />

								<xsl:if test="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:measureDescription/gco:CharacterString!=''
								or /root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record!='' 
								or /root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:evaluationMethodDescription/gco:CharacterString!=''
								">
									<tr valign="top">
										<td class="print_ttl_h2">
											<xsl:value-of select="/root/loc/strings/accuracyH" />
										</td>
										<td class="print_data"/>
									</tr>
									<!-- la définit (titre) de ces valeurs ont été placés dans le fichier string.xml (WEB-INF/data/data/user_xsl/emodnet/loc) ce qui diffère un peu du fonctionnement global -->
									<xsl:if test="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:measureDescription/gco:CharacterString!=''">
										<tr valign="top">
											<td class="print_desc">
												<xsl:value-of select="/root/loc/strings/measureDescription" />
											</td>
											<td class="print_data">
												<xsl:value-of select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:measureDescription" />
											</td>
										</tr>
									</xsl:if>
									<xsl:if test="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record!='' ">
										<tr valign="top">
											<td class="print_desc">
												<xsl:value-of select="/root/loc/strings/quantitativeValue" />
											</td>
											<td class="print_data">
												<xsl:value-of select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:result/gmd:DQ_QuantitativeResult/gmd:value" />
											</td>
										</tr>
									</xsl:if>
									<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:evaluationMethodDescription" />

								</xsl:if>


								<xsl:if test="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:measureDescription/gco:CharacterString!=''
											or /root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:evaluationMethodDescription/gco:CharacterString!=''">

									<tr valign="top">
										<td class="print_ttl_h2">
											<xsl:value-of select="/root/loc/strings/accuracyV" />
										</td>
										<td class="print_data"/>
									</tr>

									<xsl:if test="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:measureDescription/gco:CharacterString!=''">
										<tr valign="top">
											<td class="print_desc">
												<xsl:value-of select="/root/loc/strings/measureDescription" />
											</td>
											<td class="print_data">
												<xsl:value-of select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:measureDescription" />
											</td>
										</tr>
									</xsl:if>
									<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:evaluationMethodDescription" />

								</xsl:if>		


								<xsl:if test="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString!=''">
									<tr valign="top">
										<td class="print_ttl_h1">
											<xsl:value-of select="/root/loc/strings/suitability" />
										</td>
										<td class="print_data"/>
									</tr>

									<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation" />
								</xsl:if>

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/loc/strings/intellProperty" />
									</td>
									<td class="print_data"/>
								</tr>

								<!--CG : 10/04/2014 - Consider several cases about useLimitation tag -->

								<xsl:if test="/root/gmd:MD_Metadata//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation/gco:CharacterString">
									<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation" />
								</xsl:if>

								<xsl:if test="/root/gmd:MD_Metadata//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation/gmx:Anchor">
									<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation"/>
								</xsl:if>


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

								<!-- **************************************** WHERE **************************************** -->
								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="/root/loc/strings/where" />
									</td>
									<td class="print_data">
									</td>
								</tr>

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/spatialResTitle" />
									</td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance" />

<!--
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:EX_GeographicBoundingBox" />
-->
								<tr valign="top">
									<td class="print_desc">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/geoAreaNameTitle" />
									</td>
									<td class="print_data">
										<xsl:value-of select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier[not(gmd:RS_Identifier/gmd:codeSpace)]/gmd:MD_Identifier" />
									</td>
								</tr>

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/depthTitle" />
									</td>
									<td class="print_data">
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:minimumValue|
										/root/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:maximumValue" />

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/loc/strings/CRS" />
									</td>
									<td class="print_data"/>
								</tr>


								<tr valign="top">
									<td class="print_desc">
										<xsl:value-of select="/root/loc/strings/geoCRS" />
									</td>
									<td class="print_data">
										<xsl:value-of select="/root/gmd:MD_Metadata//gmd:referenceSystemInfo//gmd:RS_Identifier/gmd:code" />
									</td>
								</tr>
								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:version" />


								<xsl:if test="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier[gmd:RS_Identifier/gmd:codeSpace]/gmd:RS_Identifier/gmd:code">
									<tr valign="top">
										<td class="print_ttl_h1">
											<xsl:value-of select="/root/loc/strings/projCRS" />
										</td>
										<td class="print_data"/>
									</tr>

									<tr valign="top">
										<td class="print_desc">
											<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/geodeticSystemAndProjectionTitle" />
										</td>
										<td class="print_data">
											<xsl:value-of select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier[gmd:RS_Identifier/gmd:codeSpace]/gmd:RS_Identifier/gmd:code" />
										</td>
									</tr>

									<xsl:apply-templates mode="iso19139"
										select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:description[../gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:RS_Identifier/gmd:codeSpace]" />

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

								<!-- **************************************** WHHEN **************************************** -->
								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="/root/loc/strings/when" />
									</td>
									<td class="print_data">
									</td>
								</tr>

								<xsl:if test="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition!=''">
									<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition"/>
								</xsl:if>
								<xsl:if test="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition!=''">
									<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition"/>
								</xsl:if>


								<xsl:if test="/root/gmd:MD_Metadata/gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='time']/gmd:resolution/gco:Measure!=''">
									<tr valign="top">
										<td class="print_ttl_h1">
											<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/tempResolutionTitle" />
										</td>
										<td class="print_data"/>
									</tr>

									<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='time']/gmd:resolution/gco:Measure"/>
								</xsl:if>

								<!-- **************************************** WHO **************************************** -->
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
									<td class="print_data"/>
								</tr>

								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='originator']/gmd:organisationName|
											 /root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='originator']/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress" />

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/dataHoldingCenterTitle" />
									</td>
									<td class="print_data"/>
								</tr>

								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='custodian']/gmd:organisationName|
									/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='custodian']/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress" />


								<!-- **************************************** WHERE TO FIND **************************************** -->
								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="/root/loc/strings/whereToFind" />
									</td>
									<td class="print_data">
									</td>
								</tr>


								<tr valign="top">
									<td class="print_desc">
										<xsl:value-of select="/root/loc/strings/datadistribCenter" />
									</td>
									<td class="print_data">
										<xsl:value-of select="/root/gmd:MD_Metadata/gmd:distributionInfo//gmd:distributorContact//gmd:organisationName" />
									</td>
								</tr>

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/distriFormatTitle" />
									</td>
									<td class="print_data"/>
								</tr>

								<xsl:apply-templates mode="iso19139"
									select="root/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format|
									root/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:transferSize" />

								<xsl:if test="root/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage">
									<tr valign="top">
										<td class="print_ttl_h1">
											<xsl:value-of select="/root/loc/strings/accessing" />
										</td>
										<td class="print_data"/>
									</tr>
								</xsl:if>							
								<xsl:apply-templates mode="iso19139"
									select="root/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage" />

								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="/root/loc/strings/collatingCenter" />
									</td>
									<td class="print_data"/>
								</tr>

								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata//gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName|
											 /root/gmd:MD_Metadata//gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress" />
							
															<!-- ******************************************* METADATA INFO ************************************* -->
								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="/root/loc/strings/mdinfo" />
									</td>
									<td class="print_data">
									</td>
								</tr>


								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:fileIdentifier | /root/gmd:MD_Metadata/gmd:hierarchyLevelName/gmx:Anchor" />

								<tr valign="top">
									<td class="print_desc">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/projectName" />
									</td>
									<td class="print_data">
										<xsl:value-of select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[../gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme' and ../gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://www.seadatanet.org/urnurl/']" />
									</td>
								</tr>

								<tr valign="top">
									<td class="print_desc">
										<xsl:value-of select="/root/schemas/iso19139.emodnet.chemistry/strings/DisseminationLevelTitle" />
									</td>
									<td class="print_data">
										<xsl:value-of select="/root/gmd:MD_Metadata/gmd:metadataConstraints//gmd:useLimitation" />
									</td>
								</tr>

								<!--CG : 10/04/2014 - Consider several cases about language tag -->
								<xsl:if test="/root/gmd:MD_Metadata/gmd:language/gco:CharacterString">
									<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:language" />
								</xsl:if>

								<xsl:if test="/root/gmd:MD_Metadata/gmd:language/gmd:LanguageCode">
									<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:language/gmd:LanguageCode" />
								</xsl:if>


								<xsl:apply-templates mode="iso19139"
									select="/root/gmd:MD_Metadata/gmd:dateStamp" />	
							
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
		match="gmd:transferSize|gml:beginPosition|gml:endPosition|gmd:maximumValue|gmd:minimumValue|gmd:MD_Identifier|gmd:pointInPixel|
		gmd:DQ_QuantitativeResult/gmd:value|gmd:CI_DateTypeCode/gmd:date|gml:VerticalDatum/gml:identifier|gmd:version">
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


	<!-- CG : 10/04/2014 - Template to fix Anchor names -->
	<xsl:template mode="iso19139"
		match="gmx:Anchor">
		<xsl:variable name="name" select="name(../.)" />
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
		match="gmd:spatialRepresentationType|gmd:accessConstraints|gmd:useConstraints">
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

	<!-- CG : 10/04/2014 - Template to take into account the LanguageCode tag. For Myocean, SeaDatanet and some sextant products -->
	<xsl:template mode="iso19139"
		match="gmd:LanguageCode">
		<xsl:variable name="name" select="name(../.)" />
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
				<xsl:value-of select="./@codeListValue" />
			</td>
		</tr>
	</xsl:template>

	<xsl:template mode="iso19139"
		match="gco:Distance|gco:Measure">
		<xsl:variable name="name" select="name(.)" />
		<xsl:variable name="name2" select="name(./@uom)" />
		<xsl:variable name="title">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="title2">
			<xsl:call-template name="getTitle">
				<xsl:with-param name="name" select="$name2" />
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
		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$title2" />
			</td>
			<td class="print_data">
				<xsl:value-of select="@uom" />
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
				<xsl:value-of select="concat($title,' (WGS 84)')" />
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


	<!-- Get title from the profil if exist, if not default to iso. -->
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
