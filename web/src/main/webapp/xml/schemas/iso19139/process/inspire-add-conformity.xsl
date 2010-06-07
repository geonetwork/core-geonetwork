<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd"
	exclude-result-prefixes="gmd">

	<!-- ================================================================= -->
	<!-- Add a dataQuality section to set INSPIRE conformance result     
		 Set the report date to metadata date stamp					       -->
	<!-- ================================================================= -->
	<xsl:template match="/gmd:MD_Metadata|/*[@gco:isoType='gmd:MD_Metadata']">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:copy-of
				select="gmd:fileIdentifier|
				gmd:language|
				gmd:characterSet|
				gmd:parentIdentifier|
				gmd:hierarchyLevel|
				gmd:hierarchyLevelName|
				gmd:contact|
				gmd:dateStamp|
				gmd:metadataStandardName|
				gmd:metadataStandardVersion|
				gmd:dataSetURI|
				gmd:locale|
				gmd:spatialRepresentationInfo|
				gmd:referenceSystemInfo|
				gmd:metadataExtensionInfo|
				gmd:identificationInfo|
				gmd:contentInfo|
				gmd:distributionInfo|
				gmd:dataQualityInfo"/>
			
			<gmd:dataQualityInfo>
				<gmd:DQ_DataQuality>
					<gmd:scope>
						<gmd:DQ_Scope>
							<gmd:level>
								<gmd:MD_ScopeCode codeListValue="dataset"
									codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode"/>
							</gmd:level>
						</gmd:DQ_Scope>
					</gmd:scope>
					<gmd:report>
						<gmd:DQ_DomainConsistency>
							<gmd:result>
								<gmd:DQ_ConformanceResult>
									<gmd:specification>
										<gmd:CI_Citation>
											<gmd:title>
												<gco:CharacterString>INSPIRE</gco:CharacterString>
											</gmd:title>
											<gmd:date>
												<gmd:CI_Date>
													<gmd:date>
														<gco:Date><xsl:value-of select="substring-before(gmd:dateStamp/gco:DateTime, 'T')"/></gco:Date>
													</gmd:date>
													<gmd:dateType>
														<gmd:CI_DateTypeCode
															codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode"
															codeListValue="publication"/>
													</gmd:dateType>
												</gmd:CI_Date>
											</gmd:date>
										</gmd:CI_Citation>
									</gmd:specification>
									<gmd:explanation>
										<gco:CharacterString>INSPIRE related dataset</gco:CharacterString>
									</gmd:explanation>
									<gmd:pass>
										<gco:Boolean>1</gco:Boolean>
									</gmd:pass>
								</gmd:DQ_ConformanceResult>
							</gmd:result>
						</gmd:DQ_DomainConsistency>
					</gmd:report><gmd:report>
						<gmd:DQ_DomainConsistency>
							<gmd:result>
								<gmd:DQ_ConformanceResult>
									<gmd:specification>
										<gmd:CI_Citation>
											<gmd:title>
												<gco:CharacterString>INSPIRE Implementing rules</gco:CharacterString>
											</gmd:title>
											<gmd:date>
												<gmd:CI_Date>
													<gmd:date>
														<gco:Date><xsl:value-of select="substring-before(gmd:dateStamp/gco:DateTime, 'T')"/></gco:Date>
													</gmd:date>
													<gmd:dateType>
													<gmd:CI_DateTypeCode
													codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode"
													codeListValue="publication"/>
													</gmd:dateType>
												</gmd:CI_Date>
											</gmd:date>
										</gmd:CI_Citation>
									</gmd:specification>
									<gmd:explanation>
										<gco:CharacterString>-- more information on the test --</gco:CharacterString>
									</gmd:explanation>
									<gmd:pass>
										<gco:Boolean>1</gco:Boolean>
									</gmd:pass>
								</gmd:DQ_ConformanceResult>
							</gmd:result>
						</gmd:DQ_DomainConsistency>
					</gmd:report>
				</gmd:DQ_DataQuality>
			</gmd:dataQualityInfo>
			
			<xsl:copy-of select="gmd:portrayalCatalogueInfo|
				gmd:metadataConstraints|
				gmd:applicationSchemaInfo|
				gmd:metadataMaintenance|
				gmd:series|
				gmd:describes|
				gmd:propertyType|
				gmd:featureType|
				gmd:featureAttribute"/>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->

</xsl:stylesheet>
