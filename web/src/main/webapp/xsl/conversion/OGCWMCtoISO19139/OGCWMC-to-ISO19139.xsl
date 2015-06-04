<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
				xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:geonet="http://www.fao.org/geonetwork" 
				xmlns:wmc="http://www.opengis.net/context"
				xmlns:wmc11="http://www.opengeospatial.net/context"
				xmlns:gts="http://www.isotc211.org/2005/gts"
				xmlns:gco="http://www.isotc211.org/2005/gco"
				xmlns:gml="http://www.opengis.net/gml"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:xlink="http://www.w3.org/1999/xlink"
				xmlns:java="java:org.fao.geonet.util.XslUtil"
				xmlns:saxon="http://saxon.sf.net/">

				
	<!-- ============================================================================= -->				

	<xsl:param name="lang">eng</xsl:param>
	<xsl:param name="topic"></xsl:param>
    <xsl:param name="viewer_url"></xsl:param>
    <xsl:param name="wmc_url"></xsl:param>
    
    <!-- These are provided by the ImportWmc.java jeeves service -->
    <xsl:param name="currentuser_name"></xsl:param>
    <xsl:param name="currentuser_phone"></xsl:param>
    <xsl:param name="currentuser_mail"></xsl:param>
    <xsl:param name="currentuser_org"></xsl:param>
    
    
	
	<xsl:include href="./resp-party.xsl"/>
	<xsl:include href="./identification.xsl"/>
	
	<!-- ============================================================================= -->
	
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
	
	<!-- ============================================================================= -->

	<xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>
	
	<!-- ============================================================================= -->	
	
	<xsl:template match="wmc:ViewContext|wmc11:ViewContext">
		<gmd:MD_Metadata>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<!--  <fileIdentifier>
				<gco:CharacterString><xsl:value-of select="/wmc:ViewContext/@id"/></gco:CharacterString>
			</fileIdentifier>
			 -->
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<gmd:language>
				<gco:CharacterString><xsl:value-of select="$lang"/></gco:CharacterString>
				<!-- English is default. Not available in Web Map Context. Selected by user from GUI -->
			</gmd:language>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<gmd:characterSet>
				<gmd:MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode" codeListValue="utf8" />
			</gmd:characterSet>

			<!-- parentIdentifier : Web Map Context has no parent -->
			<!-- mdHrLv -->
			<!-- mdHrLvName -->

			<!-- hierarchy level -->
			<gmd:hierarchyLevel>
				<gmd:MD_ScopeCode codeListValue="mapDigital"
					codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_ScopeCode" />
			</gmd:hierarchyLevel>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="/wmc:ViewContext/wmc:General/wmc:ContactInformation|/wmc11:ViewContext/wmc11:General/wmc11:ContactInformation">
				<gmd:contact>
					<gmd:CI_ResponsibleParty>
						<xsl:apply-templates select="." mode="RespParty"/>
					</gmd:CI_ResponsibleParty>
				</gmd:contact>
			</xsl:for-each>

			<gmd:contact>
				<gmd:CI_ResponsibleParty>
					<gmd:individualName>
						<gco:CharacterString>
							<xsl:value-of select="$currentuser_name" />
						</gco:CharacterString>
					</gmd:individualName>
					<gmd:organisationName>
						<gco:CharacterString>
							<xsl:value-of select="$currentuser_org" />
						</gco:CharacterString>
					</gmd:organisationName>
					<gmd:contactInfo>
						<gmd:CI_Contact>
							<gmd:phone>
								<gmd:CI_Telephone>
									<gmd:voice>
										<gco:CharacterString>
											<xsl:value-of select="$currentuser_phone" />
										</gco:CharacterString>
									</gmd:voice>
								</gmd:CI_Telephone>
							</gmd:phone>
							<gmd:address>
								<gmd:CI_Address>
									<gmd:electronicMailAddress>
										<gco:CharacterString>
											<xsl:value-of select="$currentuser_mail" />
										</gco:CharacterString>
									</gmd:electronicMailAddress>
								</gmd:CI_Address>
							</gmd:address>
						</gmd:CI_Contact>
					</gmd:contactInfo>
					<gmd:role>
						<gmd:CI_RoleCode
							codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_RoleCode"
							codeListValue="author" />
					</gmd:role>
				</gmd:CI_ResponsibleParty>
			</gmd:contact>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>
			<gmd:dateStamp>
				<gco:DateTime><xsl:value-of select="format-dateTime(current-dateTime(),$df)"/></gco:DateTime>
			</gmd:dateStamp>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<gmd:metadataStandardName>
				<gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>
			</gmd:metadataStandardName>

			<gmd:metadataStandardVersion>
				<gco:CharacterString>1.0</gco:CharacterString>
			</gmd:metadataStandardVersion>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<gmd:referenceSystemInfo>
				<gmd:MD_ReferenceSystem>
					<gmd:referenceSystemIdentifier>
						<gmd:RS_Identifier>
							<gmd:code>
								<gco:CharacterString><xsl:value-of select="/wmc:ViewContext/wmc:General/wmc:BoundingBox/@SRS
									|/wmc11:ViewContext/wmc11:General/wmc11:BoundingBox/@SRS"/></gco:CharacterString>
							</gmd:code>
						</gmd:RS_Identifier>
					</gmd:referenceSystemIdentifier>
				</gmd:MD_ReferenceSystem>
			</gmd:referenceSystemInfo>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<gmd:identificationInfo>
				<gmd:MD_DataIdentification>
					<xsl:apply-templates select="." mode="DataIdentification">
						<xsl:with-param name="topic"><xsl:value-of select="$topic"/></xsl:with-param>
                        <xsl:with-param name="lang"><xsl:value-of select="$lang"/></xsl:with-param>						
					</xsl:apply-templates>
					<!--  extracts the extent (if not 4326, need to reproject) -->
					<gmd:extent>
						<gmd:EX_Extent>
							<gmd:geographicElement>
							       <xsl:variable name="minx" select="string(/wmc:ViewContext/wmc:General/wmc:BoundingBox/@minx)" />
							       <xsl:variable name="miny" select="string(/wmc:ViewContext/wmc:General/wmc:BoundingBox/@miny)" />
							       <xsl:variable name="maxx" select="string(/wmc:ViewContext/wmc:General/wmc:BoundingBox/@maxx)" />
							       <xsl:variable name="maxy" select="string(/wmc:ViewContext/wmc:General/wmc:BoundingBox/@maxy)" />
							       <xsl:variable name="fromEpsg" select="string(/wmc:ViewContext/wmc:General/wmc:BoundingBox/@SRS)" />
							       <xsl:variable name="reprojected" select="java:reprojectCoords($minx,$miny,$maxx,$maxy,$fromEpsg)" />
							       <xsl:copy-of select="saxon:parse($reprojected)" />
							</gmd:geographicElement>
						</gmd:EX_Extent>
					</gmd:extent>
				</gmd:MD_DataIdentification>
			</gmd:identificationInfo>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<gmd:distributionInfo>
				<gmd:MD_Distribution>
					<gmd:transferOptions>
						<gmd:MD_DigitalTransferOptions>
							<xsl:for-each select="/wmc:ViewContext/wmc:LayerList/wmc:Layer">
								<gmd:onLine>
									<!-- iterates over the layers -->
									<xsl:variable name="wmsUrl"
										select="./wmc:Server/wmc:OnlineResource/@xlink:href" />
									<xsl:variable name="wmsName" select="./wmc:Name/text()" />
									<xsl:variable name="wmsTitle" select="./wmc:Title/text()" />
									<xsl:variable name="wmsVersion" select="./wmc:Server/@version" />
									<gmd:CI_OnlineResource>
										<gmd:linkage>
											<gmd:URL>
												<xsl:value-of select="$wmsUrl" />
											</gmd:URL>
										</gmd:linkage>
										<gmd:protocol>
											<gco:CharacterString>
												<xsl:value-of select="concat('OGC:WMS-', $wmsVersion, '-http-get-map')" />
											</gco:CharacterString>
										</gmd:protocol>
										<gmd:name>
											<gco:CharacterString>
												<xsl:value-of select="$wmsName" />
											</gco:CharacterString>
										</gmd:name>
										<gmd:description>
											<gco:CharacterString>
												<xsl:value-of select="$wmsTitle" />
											</gco:CharacterString>
										</gmd:description>
									</gmd:CI_OnlineResource>
								</gmd:onLine>
							</xsl:for-each>
                            <gmd:onLine>
								<gmd:CI_OnlineResource>
									<gmd:linkage>
									  <gmd:URL><xsl:value-of select="$wmc_url" /></gmd:URL>
									</gmd:linkage>
									<gmd:protocol>
										<!-- FIXME : use standardized label for WMS protocol -->
										<gco:CharacterString>OGC:WMC</gco:CharacterString>
									</gmd:protocol>
									<gmd:name>
										<gco:CharacterString><xsl:value-of select="/wmc:ViewContext/wmc:General/wmc:Title
											|/wmc11:ViewContext/wmc11:General/wmc11:Title"/></gco:CharacterString>
									</gmd:name>
									<gmd:description>
										<gco:CharacterString><xsl:value-of select="/wmc:ViewContext/wmc:General/wmc:Title
											|/wmc11:ViewContext/wmc11:General/wmc11:Title"/></gco:CharacterString>
									</gmd:description>
								</gmd:CI_OnlineResource>
							</gmd:onLine>
							<gmd:onLine>
                                <gmd:CI_OnlineResource>
                                    <gmd:linkage>
                                      <gmd:URL><xsl:value-of select="$viewer_url" /></gmd:URL>
                                    </gmd:linkage>
                                    <gmd:protocol>
                                        <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                                    </gmd:protocol>
                                    <gmd:name>
                                        <gco:CharacterString><xsl:value-of select="/wmc:ViewContext/wmc:General/wmc:Title
                                            |/wmc11:ViewContext/wmc11:General/wmc11:Title"/></gco:CharacterString>
                                    </gmd:name>
                                    <gmd:description>
                                        <gco:CharacterString><xsl:value-of select="/wmc:ViewContext/wmc:General/wmc:Title
                                            |/wmc11:ViewContext/wmc11:General/wmc11:Title"/></gco:CharacterString>
                                    </gmd:description>
                                </gmd:CI_OnlineResource>
                            </gmd:onLine>
						</gmd:MD_DigitalTransferOptions>
					</gmd:transferOptions>
				</gmd:MD_Distribution>
			</gmd:distributionInfo>
				 <gmd:dataQualityInfo>
					<gmd:DQ_DataQuality>
						<gmd:scope>
							<gmd:DQ_Scope>
								<gmd:level>
									<gmd:MD_ScopeCode codeListValue="mapDigital"
										codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_ScopeCode" />
								</gmd:level>
							</gmd:DQ_Scope>
						</gmd:scope>
						<gmd:lineage>
			                <gmd:LI_Lineage>
                                <xsl:for-each select="/wmc:ViewContext/wmc:LayerList/wmc:Layer">
                                    <xsl:variable name="sourceNode" select="java:generateLineageSource(string(./wmc:MetadataURL/wmc:OnlineResource/@xlink:href))" />
			                        <xsl:copy-of select="saxon:parse($sourceNode)" />
			                   </xsl:for-each>
			                </gmd:LI_Lineage>
			            </gmd:lineage>
			        </gmd:DQ_DataQuality>
			    </gmd:dataQualityInfo>
		</gmd:MD_Metadata>
	</xsl:template>
</xsl:stylesheet>
