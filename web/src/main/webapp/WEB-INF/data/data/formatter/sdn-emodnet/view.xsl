<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="#all">

	<!-- CG: 20150213
		IMPORTANT NOTE: For definition values, we use label and string from schemas iso19139.sdn-product AND iso19139
		We use emodnet-core.css and default.css
		So becareful if any of these files are modified
	-->


	<!-- Load labels. -->
	<xsl:variable name="schemaStrings"
                select="/root/schemas/iso19139.sdn-product/strings"/>
	<xsl:variable name="schemaLabels"
                select="/root/schemas/iso19139.sdn-product/labels"/>
	<xsl:variable name="schemaLabels19139"
                select="/root/schemas/iso19139/labels"/>
	<xsl:variable name="schemaStrings19139"
                select="/root/schemas/iso19139/strings"/>

	<!-- A set of existing templates copied from other XSLTs - end -->
	<!-- ########################################################### -->
	<xsl:template mode="iso19139" match="geonet:info"/>


	<!-- Root element matching. -->
	<xsl:template match="/" priority="5">
		<html>
			<!-- Set some vars. -->
			<xsl:variable name="title"
                    select="/root/gmd:MD_Metadata/gmd:identificationInfo/*/
											gmd:citation/*/gmd:title/gco:CharacterString"/>
			<xsl:variable name="identifier"
                    select="/root/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString"/>

			<head>
				<title>
					<xsl:value-of select="$title"/>
				</title>
			</head>

			<body>

				<link rel="stylesheet" type="text/css"
              href="{root/url}../../apps/sextant/css/schema/reset.css"/>
				<link rel="stylesheet" type="text/css"
              href="{root/url}../../apps/sextant/css/schema/default.css"/>
				<link rel="stylesheet" type="text/css"
              href="{root/url}../../apps/sextant/css/schema/emodnet.css"/>

				<div class="tpl-emodnet">
					<!--<div class="ui-layout-content">-->
					<div class="ui-layout-content mdshow-tabpanel">
						<a id="md-xml-btn"
               class="file-xml"
               title="Export XML"
               target="_blank"
               href="{/root/url}xml.metadata.get?uuid={$identifier}">&#160;</a>

						<table class="print_table" border="0" cellpadding="0"
							cellspacing="0">
							<tbody>

								<!-- **************************************** IDENTIFICATION **************************************** -->
									<tr valign="top">
										<td class="print_ttl">
											<xsl:value-of select="$schemaStrings/productIdentification/text()"/>
										</td>
										<td class="print_data">

										</td>
									</tr>
									<xsl:apply-templates select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title">
										<xsl:with-param name="DefinitionValue" select="$schemaStrings/conformity_title/text()"/>
									</xsl:apply-templates>
									<xsl:apply-templates select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:alternateTitle">
										<xsl:with-param name="DefinitionValue" select="$schemaLabels/element[@name='gmd:alternateTitle']/label/text()"/> 
									</xsl:apply-templates>
									<xsl:if test="//gmd:graphicOverview">
										<tr valign="top">
											<td class="print_desc"/>
											<td class="print_data">
												<img class="result-photo">
													<xsl:attribute name="src">
														<xsl:value-of select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview[position()=1]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString" />
													</xsl:attribute>
												</img>
											</td>
										</tr>
									</xsl:if>
							
									<xsl:apply-templates select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract">
										<xsl:with-param name="DefinitionValue" select="$schemaLabels19139/element[@name='gmd:abstract' and @id='25.0']/label/text()"/> 
									</xsl:apply-templates>

								<!-- *** DESCRIPTIVE KEYWORDS *** -->
								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="$schemaStrings/descriptiveKeywords/text()"/>
									</td>
									<td class="print_data">

									</td>
								</tr>

								<!-- Feature type -->
								<xsl:if test="//gmd:featureTypes/gco:LocalName != 'gridSeries'">
									<xsl:apply-templates select="//gmd:featureTypes[1]">
										<xsl:with-param name="DefinitionValue" select="$schemaStrings/featureType/text()"/> 
									</xsl:apply-templates>
									<xsl:for-each select="//gmd:featureTypes[position()>1]">
										<xsl:apply-templates select=".">
											<xsl:with-param name="DefinitionValue"/> 
										</xsl:apply-templates>
									</xsl:for-each>
								</xsl:if>

								<!-- Sea areas (C19)-->
								<xsl:apply-templates select="//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString/(text()='SeaVoX salt and fresh water body gazetteer' or text()='external.reference-geographical-area.NVS.C19')]/gmd:keyword[1]">
									<xsl:with-param name="DefinitionValue" select="$schemaStrings/seaAreas/text()"/> 
								</xsl:apply-templates>
								<xsl:for-each select="//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString/(text()='SeaVoX salt and fresh water body gazetteer' or text()='external.reference-geographical-area.NVS.C19')]/gmd:keyword[position()>1]">
									<xsl:apply-templates select=".">
										<xsl:with-param name="DefinitionValue"/> 
									</xsl:apply-templates>
								</xsl:for-each>


								<!-- Ocean Discovery Parameters -->
								<xsl:apply-templates select="//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString/(text()='Parameter Discovery Vocabulary (P02)'or text()='external.parameter.NVS.P02')]/gmd:keyword[1]">
									<xsl:with-param name="DefinitionValue" select="$schemaStrings/oceanDiscoveryParameters/text()"/> 
								</xsl:apply-templates>
								<xsl:for-each select="//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString/(text()='Parameter Discovery Vocabulary (P02)'or text()='external.parameter.NVS.P02')]/gmd:keyword[position()>1]">
									<xsl:apply-templates select=".">
										<xsl:with-param name="DefinitionValue"/> 
									</xsl:apply-templates>
								</xsl:for-each>


								<!--Ocean chemistry variable  -->
								<xsl:apply-templates select="//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString/(text()='EMODNET chemistry lot aggregated parameter names'or text()='external.parameter.NVS.P35')]/gmd:keyword[1]">
									<xsl:with-param name="DefinitionValue" select="$schemaStrings/oceanChemistryVariable/text()"/> 
								</xsl:apply-templates>
								<xsl:for-each select="//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString/(text()='EMODNET chemistry lot aggregated parameter names'or text()='external.parameter.NVS.P35')]/gmd:keyword[position()>1]">
									<xsl:apply-templates select=".">
										<xsl:with-param name="DefinitionValue"/> 
									</xsl:apply-templates>
								</xsl:for-each>

								<!-- Usagage Licence -->
								<xsl:apply-templates select="//gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation">
									<xsl:with-param name="DefinitionValue" select="$schemaStrings/usageLicense/text()"/> 
								</xsl:apply-templates>		

								<!-- **************************************** spatioTemporalExtent **************************************** -->

								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="$schemaStrings/spatioTemporalExtent/text()"/>
									</td>
									<td class="print_data">
									</td>
								</tr>

								<!-- ********************** Geographical extent ********************** -->
								<tr valign="top">
									<td class="print_ttl_h1">
										<xsl:value-of select="$schemaStrings/geographicalExtent/text()" />
									</td>
									<td class="print_data"/>
								</tr>

								<xsl:apply-templates select="//gmd:EX_GeographicBoundingBox[
                                            gmd:northBoundLatitude/gco:Decimal != '' and
                                            gmd:southBoundLatitude/gco:Decimal != '' and
                                            gmd:eastBoundLongitude/gco:Decimal != '' and
                                            gmd:westBoundLongitude/gco:Decimal != '']"/>


								<xsl:apply-templates select="//gmd:spatialResolution/gmd:MD_Resolution/gmd:distance">
									<xsl:with-param name="DefinitionValue" select="$schemaLabels/element[@name='gmd:distance']/label/text()"/> 
								</xsl:apply-templates>

								<xsl:apply-templates select="//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code">
									<xsl:with-param name="DefinitionValue" select="$schemaLabels/element[@name='gmd:code' and @id='207.0']/label/text()"/> 
								</xsl:apply-templates>

								<!-- ********************** Vertical extent ********************** -->
								<xsl:if test="//gmd:verticalElement/gmd:EX_VerticalExtent/gmd:minimumValue/gco:Real/text() !='' or
											  //gmd:verticalElement/gmd:EX_VerticalExtent/gmd:maximumValue/gco:Real/text() !='' or
											  //gmd:axisDimensionProperties/gmd:MD_Dimension/gmd:dimensionSize/gco:Integer != ''">
									<tr valign="top">
										<td class="print_ttl_h1">
											<xsl:value-of select="$schemaStrings/verticalExtent/text()" />
										</td>
										<td class="print_data"/>
									</tr>
									<xsl:apply-templates select="//gmd:verticalElement/gmd:EX_VerticalExtent/gmd:minimumValue">
										<xsl:with-param name="DefinitionValue" select="$schemaStrings19139/minDepth/text()"/> 

									</xsl:apply-templates>
									<xsl:apply-templates select="//gmd:verticalElement/gmd:EX_VerticalExtent/gmd:maximumValue">
										<xsl:with-param name="DefinitionValue" select="$schemaStrings19139/maxDepth/text()"/> 
									</xsl:apply-templates>

									<xsl:apply-templates select="//gmd:axisDimensionProperties/gmd:MD_Dimension/gmd:dimensionSize">
										<xsl:with-param name="DefinitionValue" select="$schemaStrings/nbVerticalLevels/text()"/> 
									</xsl:apply-templates>
								</xsl:if>
								<!-- ********************** Temporal extent ********************** -->
								<xsl:if test="//gml:TimePeriod/gml:beginPosition != '' or //gml:TimePeriod/gml:endPosition != '' or //gmd:axisDimensionProperties/gmd:MD_Dimension/gmd:resolution/gco:Measure != ''">
									<tr valign="top">
										<td class="print_ttl_h1">
											<xsl:value-of select="$schemaStrings/temporalExtent/text()" />
										</td>
										<td class="print_data"/>
									</tr>

									<xsl:apply-templates select="//gml:TimePeriod"/>

									<xsl:apply-templates select="//gmd:axisDimensionProperties/gmd:MD_Dimension/gmd:resolution">
										<xsl:with-param name="DefinitionValue" select="$schemaStrings/temporalResolution/text()"/> 
									</xsl:apply-templates>
								</xsl:if>

								<!-- **************************************** Access the data **************************************** -->	
								<xsl:if test="//gmd:distributor/gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:protocol/gco:CharacterString/text()='WWW:DOWNLOAD-1.0-link--download' or gmd:protocol/gco:CharacterString/text()='OGC:WMS:getCapabilities']">
									<tr valign="top">
										<td class="print_ttl">
											<xsl:value-of select="$schemaStrings/url/text()"/>	
										</td>
										<td class="print_data">
										</td>
									</tr>


									<!-- File download -->
									<xsl:apply-templates mode="onlineResource" select="//gmd:MD_Distribution/gmd:distributor[gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString/text()='WWW:DOWNLOAD-1.0-link--download'][1]/gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource">
										<xsl:with-param name="DefinitionValue" select="$schemaLabels/element[@name='gmd:protocol' and @id='398.0']/helper/option[@value='WWW:DOWNLOAD-1.0-link--download']/text()"/> 
									</xsl:apply-templates>
									<xsl:apply-templates mode="onlineResource" select="//gmd:MD_Distribution/gmd:distributor[gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString/text()='WWW:DOWNLOAD-1.0-link--download'][position()>1]/gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource">
										<xsl:with-param name="DefinitionValue"/> 
									</xsl:apply-templates>

									<tr valign="top">
										<td class="print_ttl">

										</td>
										<td class="print_data">
										</td>
									</tr>

									<!-- WMS -->
									<xsl:apply-templates mode="onlineResource" select="//gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine[gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString/text()='OGC:WMS'][1]/gmd:CI_OnlineResource">
										<xsl:with-param name="DefinitionValue" select="$schemaLabels/element[@name='gmd:protocol' and @id='398.0']/helper/option[@value='OGC:WMS']/text()"/> 
									</xsl:apply-templates>
									<xsl:apply-templates mode="onlineResource" select="//gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine[gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString/text()='OGC:WMS'][position()> 1]/gmd:CI_OnlineResource">
										<xsl:with-param name="DefinitionValue"/>
									</xsl:apply-templates>

								</xsl:if>

								<!-- **************************************** Contact **************************************** -->
								<xsl:if test="//gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='custodian' or //gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='custodian'">
									<tr valign="top">
										<td class="print_ttl">
											<xsl:value-of select="$schemaLabels19139/element[@name='gmd:contact' and @id='8.0' and @context='gmd:MD_Metadata']/label/text()"/>	
										</td>
										<td class="print_data">
										</td>
									</tr>

									<xsl:for-each select="//gmd:pointOfContact">
										<xsl:apply-templates select=".">
										</xsl:apply-templates>
									</xsl:for-each>
								</xsl:if>

								<!-- **************************************** Metadata **************************************** -->
								<tr valign="top">
									<td class="print_ttl">
										<xsl:value-of select="$schemaStrings/metadata/text()" />
									</td>
									<td class="print_data"/>
								</tr>
								<xsl:apply-templates select="/root/gmd:MD_Metadata/gmd:fileIdentifier">
									<xsl:with-param name="DefinitionValue" select="$schemaLabels/element[@name='gmd:fileIdentifier']/label/text()"/> 
								</xsl:apply-templates>

								<xsl:apply-templates select="//gmd:distributor/gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:protocol/gco:CharacterString/text()='WWW:LINK-1.0-http--metadata-URL']/gmd:linkage">
									<xsl:with-param name="DefinitionValue" select="$schemaStrings/DOIURL/text()"/> 
								</xsl:apply-templates>

								<xsl:apply-templates select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date">
									<xsl:with-param name="DefinitionValue" select="$schemaLabels/element[@name='gmd:date']/label/text()"/> 
								</xsl:apply-templates>

								<xsl:apply-templates select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:editionDate">
									<xsl:with-param name="DefinitionValue" select="$schemaLabels/element[@name='gmd:editionDate']/label/text()"/> 
								</xsl:apply-templates>

								<xsl:apply-templates select="/root/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:edition">
									<xsl:with-param name="DefinitionValue" select="$schemaLabels/element[@name='gmd:edition']/label/text()"/> 
								</xsl:apply-templates>




							</tbody>
						</table>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>



	<!-- ******************************** Functions / template macth ********************************** -->
	<xsl:template match="gmd:*[not(self::gmd:pointOfContact or self::gmd:EX_GeographicBoundingBox)]">
		<xsl:param name="DefinitionValue" />

		<xsl:if test="gco:CharacterString!='' or gmd:URL!='' or gco:Integer!='' or gco:Date!='' or gco:Real!='' or gco:LocalName!='' or gmx:Anchor!=''">
			<tr valign="top">
				<td class="print_desc">
					<xsl:value-of select="$DefinitionValue" />
				</td>
				<td class="print_data">
					<xsl:value-of select="*" />
				</td>
			</tr>
		</xsl:if>

		<xsl:if test="gco:Distance!='' or gco:Measure!=''">
			<tr valign="top">
				<td class="print_desc">
					<xsl:value-of select="$DefinitionValue" />
				</td>
				<td class="print_data">
					<xsl:value-of select="concat(*,' ',*/@uom)" />
				</td>
			</tr>
		</xsl:if>
	</xsl:template>

	<xsl:template match="gml:TimePeriod">

		<xsl:if test="//gml:TimePeriod/gml:beginPosition != ''">
			<tr valign="top">
				<td class="print_desc">
					<xsl:value-of select="$schemaStrings/startDate/text()" />
				</td>
				<td class="print_data">
					<xsl:value-of select="//gml:TimePeriod/gml:beginPosition/text()" />
				</td>
			</tr>
		</xsl:if>

		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$schemaStrings/endDate/text()" />
			</td>
			<xsl:choose>
				<xsl:when test="//gml:TimePeriod/gml:endPosition/@indeterminatePosition='unknown'">
					<td class="print_data">
						<xsl:value-of select="//gml:TimePeriod/gml:endPosition/@indeterminatePosition" />
					</td>
				</xsl:when>
				<xsl:otherwise>
					<td class="print_data">
						<xsl:value-of select="//gml:TimePeriod/gml:endPosition/text()" />
					</td>
				</xsl:otherwise>
			</xsl:choose>
		</tr>
	</xsl:template>

	<xsl:template mode="onlineResource" match='gmd:CI_OnlineResource'>
		<xsl:param name="DefinitionValue" />
		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$DefinitionValue" />
			</td>
			<xsl:choose>
				<xsl:when test="gmd:protocol/gco:CharacterString/text()='OGC:WMS'">
					<xsl:variable name="WMS_URL">
						<xsl:choose>
							<!-- SDN viewer -->
							<xsl:when
								test="//gmd:MD_Metadata/gmd:metadataStandardName/gco:CharacterString/text()='ISO 19115:2003/19139, SeadataNet product profile'">
								<xsl:value-of
									select="concat('http://oceanbrowser.net/web-vis/?server=',gmd:linkage/gmd:URL/text(),'&amp;layers=',gmd:name/gco:CharacterString/text())" />
							</xsl:when>
							<!-- emodnet-chimie -->
							<xsl:when
								test="//gmd:MD_Metadata/gmd:metadataStandardName/gco:CharacterString/text()='ISO 19115:2003/19139 - EMODNET - CHEMISTRY'">
								<xsl:value-of
									select="concat('http://oceanbrowser.net/emodnet/?server=',gmd:linkage/gmd:URL/text(),'&amp;layers=',gmd:name/gco:CharacterString/text())" />
							</xsl:when>
							<!-- default -->
							<xsl:otherwise>
								<xsl:value-of
									select="concat('http://oceanbrowser.net/emodnet/?server=',gmd:linkage/gmd:URL/text(),'&amp;layers=',gmd:name/gco:CharacterString/text())" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<td class="print_data">
						<xsl:element name="a">
							<xsl:attribute name="href">
								<xsl:value-of select="$WMS_URL" />
							</xsl:attribute>
							<xsl:attribute name="target">
								<xsl:text>_blank</xsl:text>
							</xsl:attribute>
							<xsl:value-of select="gmd:name/gco:CharacterString/text()" />
						</xsl:element>
					</td>
				</xsl:when>
				<xsl:otherwise>
					<td class="print_data">
						<xsl:element name="a">
							<xsl:attribute name="href">
								<xsl:value-of select="gmd:linkage/gmd:URL/text()" />
							</xsl:attribute>
							<xsl:attribute name="target">
								<xsl:text>_blank</xsl:text>
							</xsl:attribute>
							<xsl:value-of select="gmd:name/gco:CharacterString/text()" />
						</xsl:element>
					</td>
				</xsl:otherwise>
			</xsl:choose> 
		</tr>
	</xsl:template>

	<xsl:template match="gmd:EX_GeographicBoundingBox[
                                            gmd:northBoundLatitude/gco:Decimal != '' and
                                            gmd:southBoundLatitude/gco:Decimal != '' and
                                            gmd:eastBoundLongitude/gco:Decimal != '' and
                                            gmd:westBoundLongitude/gco:Decimal != '']">

		<xsl:variable name="box"
                  select="concat('POLYGON((',
                  gmd:eastBoundLongitude/gco:Decimal, ' ',
                  gmd:southBoundLatitude/gco:Decimal, ',',
                  gmd:eastBoundLongitude/gco:Decimal, ' ',
                  gmd:northBoundLatitude/gco:Decimal, ',',
                  gmd:westBoundLongitude/gco:Decimal, ' ',
                  gmd:northBoundLatitude/gco:Decimal, ',',
                  gmd:westBoundLongitude/gco:Decimal, ' ',
                  gmd:southBoundLatitude/gco:Decimal, ',',
                  gmd:eastBoundLongitude/gco:Decimal, ' ',
                  gmd:southBoundLatitude/gco:Decimal, '))')"/>
		<xsl:variable name="numberFormat" select="'0.00'"/>

		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$schemaLabels19139/element[@name='gmd:EX_GeographicBoundingBox' and @id='343.0']/label/text()"/>	
			</td>
			<td class="print_data">

				<table>
					<tr>
						<td/>
						<td class="print_bounding">
							<xsl:value-of select="format-number(gmd:northBoundLatitude/gco:Decimal, $numberFormat)"/>
						</td>
						<td/>
					</tr>
					<tr>
						<td class="print_bounding">
							<xsl:value-of select="format-number(gmd:westBoundLongitude/gco:Decimal, $numberFormat)"/>
						</td>
						<td class="print_bounding">
							<img class="result-photo"
                 src="{root/url}/geonetwork/srv/fre/region.getmap.png?mapsrs=EPSG:3857&amp;width=250&amp;background=osm&amp;geomsrs=EPSG:4326&amp;geom={$box}"/>
						</td>	
						<td class="print_bounding">
							<xsl:value-of select="format-number(gmd:eastBoundLongitude/gco:Decimal, $numberFormat)"/>
						</td>									
					</tr>
					<tr>
						<td/>
						<td class="print_bounding">
							<xsl:value-of select="format-number(gmd:southBoundLatitude/gco:Decimal, $numberFormat)"/>
						</td>
						<td/>
					</tr>
				</table>
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="gmd:pointOfContact">
		<xsl:if test="gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='custodian'">
			<xsl:call-template name="getContact"/>
		</xsl:if>
		<xsl:if test="gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='originator'">
			<xsl:call-template name="getContact"/>
		</xsl:if>

	</xsl:template>

	<xsl:template name="getContact">
		<xsl:if test="gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='custodian'">
			<tr valign="top">
				<td class="print_ttl_h1">
					<xsl:value-of select="$schemaStrings/custodian/text()" />
				</td>
				<td class="print_data"/>
			</tr>
		</xsl:if>
		<xsl:if test="gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='originator'">
			<tr valign="top">
				<td class="print_ttl_h1">
					<xsl:value-of select="$schemaStrings/originator/text()" />
				</td>
				<td class="print_data"/>
			</tr>
		</xsl:if>
		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$schemaStrings/organisationName/text()" />
			</td>
			<td class="print_data">
				<xsl:value-of select="gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString" />
			</td>
		</tr>

		<xsl:if test="gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString!=''">
			<tr valign="top">
				<td class="print_desc">
					<xsl:value-of select="$schemaStrings/electronicMailAddress/text()" />
				</td>
				<td class="print_data">
					<xsl:value-of select="gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString" />
				</td>
			</tr>
		</xsl:if>

		<tr valign="top">
			<td class="print_desc">
				<xsl:value-of select="$schemaLabels19139/element[@name='src']/label/text()"/>	
			</td>
			<td class="print_data">

				<xsl:element name="a">
					<xsl:attribute name="href">
						<xsl:value-of select="gmd:CI_ResponsibleParty/@uuid" />
					</xsl:attribute>
					<xsl:attribute name="target">
						<xsl:text>_blank</xsl:text>
					</xsl:attribute>
					<xsl:value-of select="gmd:CI_ResponsibleParty/@uuid" />
				</xsl:element>

			</td>
		</tr>
	</xsl:template>

</xsl:stylesheet>
