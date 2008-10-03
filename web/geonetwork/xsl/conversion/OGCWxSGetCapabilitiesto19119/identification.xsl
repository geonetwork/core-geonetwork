<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns    ="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:gts="http://www.isotc211.org/2005/gts"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:srv="http://www.isotc211.org/2005/srv"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:xlink="http://www.w3.org/1999/xlink"
										xmlns:wfs="http://www.opengis.net/wfs"
										xmlns:ows="http://www.opengis.net/ows"
										xmlns:ows11="http://www.opengis.net/ows/1.1"
										xmlns:wcs="http://www.opengis.net/wcs"
                                        xmlns:wps="http://www.opengeospatial.net/wps"
                                        xmlns:wps1="http://www.opengis.net/wps/1.0.0"
                                        xmlns:gml="http://www.opengis.net/gml"
										xmlns:math="http://exslt.org/math"
										xmlns:xalan= "http://xml.apache.org/xalan"
										extension-element-prefixes="math xalan wcs ows wfs gml">

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="SrvDataIdentification">
		<xsl:param name="topic"/>
		<xsl:param name="ows"/>
		
		
		<xsl:variable name="s" select="Service|wfs:Service|ows:ServiceIdentification|ows11:ServiceIdentification|wcs:Service"/>
		
		<citation>
			<CI_Citation>
				<title>
					<gco:CharacterString>
						<xsl:choose>
							<xsl:when test="$ows='true'">
								<xsl:value-of select="ows:ServiceIdentification/ows:Title|
													ows11:ServiceIdentification/ows11:Title"/>
							</xsl:when>
							<xsl:when test="name(.)='WFS_Capabilities'">
								<xsl:value-of select="wfs:Service/wfs:Title"/>
							</xsl:when>
							<xsl:when test="name(.)='WMT_MS_Capabilities'">
								<xsl:value-of select="Service/Title"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="wcs:Service/wcs:label"/>
							</xsl:otherwise>
						</xsl:choose>
					</gco:CharacterString>
				</title>
			</CI_Citation>
		</citation>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<abstract>
			<gco:CharacterString>
				<xsl:choose>
					<xsl:when test="$ows='true'">
						<xsl:value-of select="ows:ServiceIdentification/ows:Abstract|
											ows11:ServiceIdentification/ows11:Abstract"/>
					</xsl:when>
					<xsl:when test="name(.)='WFS_Capabilities'">
						<xsl:value-of select="wfs:Service/wfs:Abstract"/>
					</xsl:when>
					<xsl:when test="name(.)='WMT_MS_Capabilities'">
						<xsl:value-of select="Service/Abstract"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="wcs:Service/wcs:description"/>
					</xsl:otherwise>
				</xsl:choose>
			</gco:CharacterString>
		</abstract>

		<!--idPurp-->

		<status>
			<MD_ProgressCode codeList="./resources/codeList.xml#MD_ProgressCode" codeListValue="completed" />
		</status>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="//ContactInformation|//wcs:responsibleParty">
			<pointOfContact>
				<CI_ResponsibleParty>
					<xsl:apply-templates select="." mode="RespParty"/>
				</CI_ResponsibleParty>
			</pointOfContact>
		</xsl:for-each>
		<xsl:for-each select="//ows:ServiceProvider|//ows11:ServiceProvider">
			<pointOfContact>
				<CI_ResponsibleParty>
					<xsl:apply-templates select="." mode="RespParty"/>
				</CI_ResponsibleParty>
			</pointOfContact>
		</xsl:for-each>


		<!-- resMaint -->
		<!-- graphOver -->
		<!-- dsFormat-->
		<xsl:for-each select="$s/KeywordList|$s/wfs:keywords|$s/wcs:keywords|$s/ows:Keywords|$s/ows11:Keywords">
			<descriptiveKeywords>
				<MD_Keywords>
					<xsl:apply-templates select="." mode="Keywords"/>
				</MD_Keywords>
			</descriptiveKeywords>
		</xsl:for-each>
		
		<topicCategory>
			<MD_TopicCategoryCode><xsl:value-of select="$topic"/></MD_TopicCategoryCode>
		</topicCategory>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		Extent in OGC spec are somehow differents !
		
		WCS 1.0.0
		<lonLatEnvelope srsName="WGS84(DD)">
				<gml:pos>-130.85168 20.7052</gml:pos>
				<gml:pos>-62.0054 54.1141</gml:pos>
		</lonLatEnvelope>
		
		WFS 1.1.0
		<ows:WGS84BoundingBox>
				<ows:LowerCorner>-124.731422 24.955967</ows:LowerCorner>
				<ows:UpperCorner>-66.969849 49.371735</ows:UpperCorner>
		</ows:WGS84BoundingBox>
		
		WMS 1.1.1
		<LatLonBoundingBox minx="-74.047185" miny="40.679648" maxx="-73.907005" maxy="40.882078"/>
        
        WPS 0.4.0 : none
        
        WPS 1.0.0 : none
		 -->
        <xsl:if test="name(.)!='wps:Capabilities'">
		    <srv:extent>
				<EX_Extent>
					<geographicElement>
						<EX_GeographicBoundingBox>
							<xsl:choose>
								<xsl:when test="$ows='true' or name(.)='WCS_Capabilities'">
					
									<xsl:variable name="boxes">
										<xsl:choose>
											<xsl:when test="$ows='true'">
												<xsl:for-each select="//ows:WGS84BoundingBox/ows:LowerCorner">
													<xmin><xsl:value-of	select="substring-before(., ' ')"/></xmin>
													<ymin><xsl:value-of	select="substring-after(., ' ')"/></ymin>
												</xsl:for-each>
												<xsl:for-each select="//ows:WGS84BoundingBox/ows:UpperCorner">
													<xmax><xsl:value-of	select="substring-before(., ' ')"/></xmax>
													<ymax><xsl:value-of	select="substring-after(., ' ')"/></ymax>
												</xsl:for-each>
											</xsl:when>
											<xsl:when test="name(.)='WCS_Capabilities'">
												<xsl:for-each select="//wcs:lonLatEnvelope/gml:pos[1]">
													<xmin><xsl:value-of	select="substring-before(., ' ')"/></xmin>
													<ymin><xsl:value-of	select="substring-after(., ' ')"/></ymin>
												</xsl:for-each>
												<xsl:for-each select="//wcs:lonLatEnvelope/gml:pos[2]">
													<xmax><xsl:value-of	select="substring-before(., ' ')"/></xmax>
													<ymax><xsl:value-of	select="substring-after(., ' ')"/></ymax>
												</xsl:for-each>
											</xsl:when>
										</xsl:choose>
									</xsl:variable>
											
									<westBoundLongitude>
										<gco:Decimal><xsl:copy-of select="math:min(xalan:nodeset($boxes)/*[name(.)='xmin'])"/></gco:Decimal>
									</westBoundLongitude>
									<eastBoundLongitude>
										<gco:Decimal><xsl:value-of select="math:max(xalan:nodeset($boxes)/*[name(.)='xmax'])"/></gco:Decimal>
									</eastBoundLongitude>
									<southBoundLatitude>
										<gco:Decimal><xsl:value-of select="math:min(xalan:nodeset($boxes)/*[name(.)='ymin'])"/></gco:Decimal>
									</southBoundLatitude>
									<northBoundLatitude>
										<gco:Decimal><xsl:value-of select="math:max(xalan:nodeset($boxes)/*[name(.)='ymax'])"/></gco:Decimal>
									</northBoundLatitude> 
								</xsl:when>
								<xsl:otherwise>
									<westBoundLongitude>
										<gco:Decimal><xsl:value-of select="math:min(//LatLonBoundingBox/@minx|//wfs:LatLongBoundingBox/@minx)"/></gco:Decimal>
									</westBoundLongitude>
									<eastBoundLongitude>
										<gco:Decimal><xsl:value-of select="math:max(//LatLonBoundingBox/@maxx|//wfs:LatLongBoundingBox/@maxx)"/></gco:Decimal>
									</eastBoundLongitude>
									<southBoundLatitude>
										<gco:Decimal><xsl:value-of select="math:min(//LatLonBoundingBox/@miny|//wfs:LatLongBoundingBox/@miny)"/></gco:Decimal>
									</southBoundLatitude>
									<northBoundLatitude>
										<gco:Decimal><xsl:value-of select="math:max(//LatLonBoundingBox/@maxy|//wfs:LatLongBoundingBox/@maxy)"/></gco:Decimal>
									</northBoundLatitude>
								</xsl:otherwise>
							</xsl:choose>
						</EX_GeographicBoundingBox>
					</geographicElement>
				</EX_Extent>
		    </srv:extent>
        </xsl:if>
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			
		<srv:serviceType>
			<gco:LocalName codeSpace="www.w3c.org">
			<xsl:choose>
				<xsl:when test="name(.)='WMT_MS_Capabilities'">OGC:WMS</xsl:when>
				<xsl:when test="name(.)='WCS_Capabilities'">OGC:WCS</xsl:when>
                <xsl:when test="name(.)='wps:Capabilities'">OGC:WPS</xsl:when>
				<xsl:otherwise>OGC:WFS</xsl:otherwise>
			</xsl:choose>
			</gco:LocalName>
		</srv:serviceType>
		<srv:serviceTypeVersion>
			<gco:CharacterString><xsl:value-of select='@version'/></gco:CharacterString>
		</srv:serviceTypeVersion>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		
		<srv:accessProperties>
			<MD_StandardOrderProcess>
				<fees>
					<gco:CharacterString><xsl:value-of select="$s/Fees|$s/wfs:Fees|$s/ows:Fees|$s/ows11:Fees|$s/wcs:fees"/></gco:CharacterString>
				</fees>
			</MD_StandardOrderProcess>
		</srv:accessProperties>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		
		<srv:couplingType>
			<srv:SV_CouplingType codeList="#SV_CouplingType" codeListValue="tight">
				<xsl:choose>
					<xsl:when test="name(.)='wps:Capabilities' or name(.)='wps1:Capabilities'">loosely</xsl:when>
					<xsl:otherwise>tight</xsl:otherwise>
				</xsl:choose>
			</srv:SV_CouplingType>
		</srv:couplingType>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
            Operation could be OGC standard operation described in specification
            OR a specific process in a WPS. In that case, each process are described
            as one operation.
        -->
		
		<xsl:for-each select="Capability/Request/*|
                                wfs:Capability/wfs:Request/*|
                                wcs:Capability/wcs:Request/*|
                                ows:OperationsMetadata/*|
                                ows11:OperationsMetadata/*|
                                wps:ProcessOfferings/*|
                                wps1:ProcessOfferings/*">
			<srv:containsOperations>
				<srv:SV_OperationMetadata>
					<srv:operationName>
						<gco:CharacterString>
							<xsl:choose>
								<xsl:when test="name(.)='wps:Process'">WPS Process: <xsl:value-of select="ows:Title|ows11:Title"/></xsl:when>
                                <xsl:when test="$ows='true'"><xsl:value-of select="@name"/></xsl:when>
								<xsl:otherwise><xsl:value-of select="name(.)"/></xsl:otherwise>
							</xsl:choose>
						</gco:CharacterString>
					</srv:operationName>
					<!--  CHECKME : DCPType/SOAP ? -->
					<xsl:for-each select="DCPType/HTTP/*|wfs:DCPType/wfs:HTTP/*|wcs:DCPType/wcs:HTTP/*|ows:DCP/ows:HTTP/*|ows11:DCP/ows11:HTTP/*">
						<srv:DCP>
							<srv:DCPList codeList="#DCPList">
								<xsl:variable name="dcp">
									<xsl:choose>
										<xsl:when test="name(.)='Get' or name(.)='wfs:Get' or name(.)='wcs:Get' or name(.)='ows:Get' or name(.)='ows11:Get'">HTTP-GET</xsl:when>
										<xsl:when test="name(.)='Post' or name(.)='wfs:Post' or name(.)='wcs:Post' or name(.)='ows:Post' or name(.)='ows11:Post'">HTTP-POST</xsl:when>
										<xsl:otherwise>WebServices</xsl:otherwise>
									</xsl:choose>
								</xsl:variable>
								<xsl:attribute name="codeListValue">
									<xsl:value-of select="$dcp"/>
								</xsl:attribute>
							</srv:DCPList>
						</srv:DCP>
					</xsl:for-each>
          
                    <xsl:if test="name(.)='wps:Process' or name(.)='wps11:ProcessOfferings'">
                      <srv:operationDescription>
                          <gco:CharacterString><xsl:value-of select="ows:Abstract|ows11:Title"/></gco:CharacterString> 
                      </srv:operationDescription> 
                      <srv:invocationName>
                          <gco:CharacterString><xsl:value-of select="ows:Identifier|ows11:Identifier"/></gco:CharacterString> 
                      </srv:invocationName> 
                    </xsl:if>
                    
					<srv:connectPoint>
						<xsl:for-each select="Format|ows:Parameter[@name='AcceptFormats' or @name='outputFormat']">
							<CI_OnlineResource>
								<linkage>
									<URL>
										<xsl:choose>
											<xsl:when test="$ows='true'">
												<xsl:value-of select="..//ows:Get[1]/@xlink:href"/><!-- FIXME supposed at least one Get -->
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="..//OnlineResource[1]/@xlink:href"/>
											</xsl:otherwise>
										</xsl:choose>
									</URL>
								</linkage>
								<protocol>
									<gco:CharacterString>
										<xsl:choose>
											<xsl:when test="$ows='true'">
												<xsl:value-of select="ows:Value"/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="."/>
											</xsl:otherwise>
										</xsl:choose>
									</gco:CharacterString>
								</protocol>
								<description>
                                    <gco:CharacterString>
                                          Format : <xsl:value-of select="."/>
                                    </gco:CharacterString>
                                </description>
								<function>
									<CI_OnLineFunctionCode codeList="./resources/codeList.xml#CI_OnLineFunctionCode" codeListValue="information"/>
								</function>
							</CI_OnlineResource>
						</xsl:for-each>
						<!-- Some Operations in WFS 1.0.0 have no ResultFormat no CI_OnlineResource created 
								WCS has no output format
						-->
						<xsl:for-each select="wfs:ResultFormat/*">
							<CI_OnlineResource>
								<linkage>
									<URL><xsl:value-of select="../..//wfs:Get[1]/@onlineResource"/></URL>
								</linkage>
								<protocol>
									<gco:CharacterString><xsl:value-of select="name(.)"/></gco:CharacterString>
								</protocol>
								<function>
									<CI_OnLineFunctionCode codeList="./resources/codeList.xml#CI_OnLineFunctionCode" codeListValue="information"/>
								</function>
							</CI_OnlineResource>
						</xsl:for-each>
					</srv:connectPoint>
				</srv:SV_OperationMetadata>
			</srv:containsOperations>
		</xsl:for-each>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		Done by harvester after data metadata creation
		<xsl:for-each select="//Layer[count(./*[name(.)='Layer'])=0] | FeatureType[count(./*[name(.)='FeatureType'])=0] | CoverageOfferingBrief[count(./*[name(.)='CoverageOfferingBrief'])=0]">
				<srv:operatesOn>
						<MD_DataIdentification uuidref="">
						<xsl:value-of select="Name"/>
						</MD_DataIdentification>
				</srv:operatesOn>
		</xsl:for-each>
		-->
		
	</xsl:template>


	<!-- ============================================================================= -->
	<!-- === LayerDataIdentification === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="LayerDataIdentification">
		<xsl:param name="Name"/>
		<xsl:param name="topic"/>		
		<xsl:param name="ows"/>
		
		<citation>
			<CI_Citation>
				<title>
					<gco:CharacterString>
						<xsl:choose>
							<xsl:when test="name(.)='WFS_Capabilities' or $ows='true'">
								<xsl:value-of select="//wfs:FeatureType[wfs:Name=$Name]/wfs:Title"/>
							</xsl:when>
							<xsl:when test="name(.)='WMT_MS_Capabilities'">
								<xsl:value-of select="//Layer[Name=$Name]/Title"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="//wcs:CoverageOfferingBrief[wcs:name=$Name]/wcs:label"/>
							</xsl:otherwise>
						</xsl:choose>
					</gco:CharacterString>
				</title>
			</CI_Citation>
		</citation>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<abstract>
			<gco:CharacterString>
				<xsl:choose>
					<xsl:when test="name(.)='WFS_Capabilities' or $ows='true'">
						<xsl:value-of select="//wfs:FeatureType[wfs:Name=$Name]/wfs:Abstract"/>
					</xsl:when>
					<xsl:when test="name(.)='WMT_MS_Capabilities'">
						<xsl:value-of select="//Layer[Name=$Name]/Abstract"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="//wcs:CoverageOfferingBrief[wcs:name=$Name]/wcs:description"/>
					</xsl:otherwise>
				</xsl:choose>
			</gco:CharacterString>
		</abstract>

		<!--idPurp-->

		<status>
			<MD_ProgressCode codeList="./resources/codeList.xml#MD_ProgressCode" codeListValue="completed" />
		</status>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="Service/ContactInformation">
			<pointOfContact>
				<CI_ResponsibleParty>
					<xsl:apply-templates select="." mode="RespParty"/>
				</CI_ResponsibleParty>
			</pointOfContact>
		</xsl:for-each>

		<!-- resMaint -->
		<!-- graphOver -->
		<!-- dsFormat-->
		<xsl:for-each select="//Layer[Name=$Name]/KeywordList|keywords">
			<descriptiveKeywords>
				<MD_Keywords>
					<xsl:apply-templates select="." mode="Keywords"/>
				</MD_Keywords>
			</descriptiveKeywords>
		</xsl:for-each>
		<xsl:for-each select="//wfs:FeatureType[wfs:Name=$Name]">
			<descriptiveKeywords>
				<MD_Keywords>
					<xsl:apply-templates select="." mode="Keywords"/>
				</MD_Keywords>
			</descriptiveKeywords>
		</xsl:for-each>
		<xsl:for-each select="//wfs:FeatureType[wfs:Name=$Name]/ows:Keywords">
			<descriptiveKeywords>
				<MD_Keywords>
					<xsl:apply-templates select="." mode="Keywords"/>
				</MD_Keywords>
			</descriptiveKeywords>
		</xsl:for-each>
		<xsl:for-each select="//wcs:CoverageOfferingBrief[wcs:name=$Name]/wcs:keywords">
			<descriptiveKeywords>
				<MD_Keywords>
					<xsl:apply-templates select="." mode="Keywords"/>
				</MD_Keywords>
			</descriptiveKeywords>
		</xsl:for-each>
		
		
		<xsl:choose>
		 	<xsl:when test="//wfs:FeatureType">
				<spatialRepresentationType>
					<MD_SpatialRepresentationTypeCode codeList="./resources/codeList.xml#MD_SpatialRepresentationTypeCode" codeListValue="vector" />
				</spatialRepresentationType>
			</xsl:when>
			<xsl:when test="//wcs:CoverageOfferingBrief">
				<spatialRepresentationType>
					<MD_SpatialRepresentationTypeCode codeList="./resources/codeList.xml#MD_SpatialRepresentationTypeCode" codeListValue="grid" />
				</spatialRepresentationType>
			</xsl:when>
		</xsl:choose>
		
		<!-- TODO WCS -->
		<xsl:if test="//Layer[Name=$Name]/MinScaleDenominator">
			<spatialResolution>
				<MD_Resolution>
					<equivalentScale>
						<MD_RepresentativeFraction>
							<denominator>
								<gco:Integer><xsl:value-of select="MinScaleDenominator"/></gco:Integer>
							</denominator>
						</MD_RepresentativeFraction>
					</equivalentScale>
				</MD_Resolution>
			</spatialResolution>
			<spatialResolution>
				<MD_Resolution>
					<equivalentScale>
						<MD_RepresentativeFraction>
							<denominator>
								<gco:Integer><xsl:value-of select="MaxScaleDenominator"/></gco:Integer>
							</denominator>
						</MD_RepresentativeFraction>
					</equivalentScale>
				</MD_Resolution>
			</spatialResolution>
		</xsl:if>
		
		<topicCategory>
			<MD_TopicCategoryCode><xsl:value-of select="$topic"/></MD_TopicCategoryCode>
		</topicCategory>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		<extent>
				<EX_Extent>
					<geographicElement>
						<EX_GeographicBoundingBox>
							<xsl:choose>
								<xsl:when test="$ows='true' or name(.)='WCS_Capabilities'">
									<xsl:variable name="boxes">
										<xsl:choose>
											<xsl:when test="$ows='true'">
												<xsl:for-each select="//wfs:FeatureType[wfs:Name=$Name]/ows:WGS84BoundingBox/ows:LowerCorner">
													<xmin><xsl:value-of	select="substring-before(., ' ')"/></xmin>
													<ymin><xsl:value-of	select="substring-after(., ' ')"/></ymin>
												</xsl:for-each>
												<xsl:for-each select="//wfs:FeatureType[wfs:Name=$Name]/ows:WGS84BoundingBox/ows:UpperCorner">
													<xmax><xsl:value-of	select="substring-before(., ' ')"/></xmax>
													<ymax><xsl:value-of	select="substring-after(., ' ')"/></ymax>
												</xsl:for-each>
											</xsl:when>
											<xsl:when test="name(.)='WCS_Capabilities'">
												<xsl:for-each select="//wcs:CoverageOfferingBrief[wcs:name=$Name]/wcs:lonLatEnvelope/gml:pos[1]">
													<xmin><xsl:value-of	select="substring-before(., ' ')"/></xmin>
													<ymin><xsl:value-of	select="substring-after(., ' ')"/></ymin>
												</xsl:for-each>
												<xsl:for-each select="//wcs:CoverageOfferingBrief[wcs:name=$Name]/wcs:lonLatEnvelope/gml:pos[2]">
													<xmax><xsl:value-of	select="substring-before(., ' ')"/></xmax>
													<ymax><xsl:value-of	select="substring-after(., ' ')"/></ymax>
												</xsl:for-each>
											</xsl:when>
										</xsl:choose>
									</xsl:variable>
											
									<westBoundLongitude>
										<gco:Decimal><xsl:copy-of select="xalan:nodeset($boxes)/*[name(.)='xmin']"/></gco:Decimal>
									</westBoundLongitude>
									<eastBoundLongitude>
										<gco:Decimal><xsl:value-of select="xalan:nodeset($boxes)/*[name(.)='xmax']"/></gco:Decimal>
									</eastBoundLongitude>
									<southBoundLatitude>
										<gco:Decimal><xsl:value-of select="xalan:nodeset($boxes)/*[name(.)='ymin']"/></gco:Decimal>
									</southBoundLatitude>
									<northBoundLatitude>
										<gco:Decimal><xsl:value-of select="xalan:nodeset($boxes)/*[name(.)='ymax']"/></gco:Decimal>
									</northBoundLatitude> 
								</xsl:when>
								<xsl:when test="name(.)='WFS_Capabilities'">
									<westBoundLongitude>
										<gco:Decimal><xsl:value-of select="//wfs:FeatureType[wfs:Name=$Name]/wfs:LatLongBoundingBox/@minx"/></gco:Decimal>
									</westBoundLongitude>
									<eastBoundLongitude>
										<gco:Decimal><xsl:value-of select="//wfs:FeatureType[wfs:Name=$Name]/wfs:LatLongBoundingBox/@maxx"/></gco:Decimal>
									</eastBoundLongitude>
									<southBoundLatitude>
										<gco:Decimal><xsl:value-of select="//wfs:FeatureType[wfs:Name=$Name]/wfs:LatLongBoundingBox/@miny"/></gco:Decimal>
									</southBoundLatitude>
									<northBoundLatitude>
										<gco:Decimal><xsl:value-of select="//wfs:FeatureType[wfs:Name=$Name]/wfs:LatLongBoundingBox/@maxy"/></gco:Decimal>
									</northBoundLatitude>
								</xsl:when>
								<xsl:otherwise>
									<westBoundLongitude>
										<gco:Decimal><xsl:value-of select="//Layer[Name=$Name]/LatLonBoundingBox/@minx"/></gco:Decimal>
									</westBoundLongitude>
									<eastBoundLongitude>
										<gco:Decimal><xsl:value-of select="//Layer[Name=$Name]/LatLonBoundingBox/@maxx"/></gco:Decimal>
									</eastBoundLongitude>
									<southBoundLatitude>
										<gco:Decimal><xsl:value-of select="//Layer[Name=$Name]/LatLonBoundingBox/@miny"/></gco:Decimal>
									</southBoundLatitude>
									<northBoundLatitude>
										<gco:Decimal><xsl:value-of select="//Layer[Name=$Name]/LatLonBoundingBox/@maxy"/></gco:Decimal>
									</northBoundLatitude>
								</xsl:otherwise>
							</xsl:choose>
						</EX_GeographicBoundingBox>
					</geographicElement>
				</EX_Extent>
		</extent>
			
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			TODO : could be added to the GUI ?  
		<xsl:for-each select="tpCat">
			<topicCategory>
				<MD_TopicCategoryCode codeList="./resources/codeList.xml#MD_TopicCategoryCode" codeListValue="{TopicCatCd/@value}" />
			</topicCategory>
		</xsl:for-each>

		  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		  
	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === Keywords === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Keywords">
		<!-- TODO : tokenize WFS 100 keywords list -->
		<xsl:for-each select="Keyword|ows:Keyword|ows11:Keyword|wfs:Keywords|wcs:keyword">
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
	<!-- === Usage === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Usage">

		<specificUsage>
			<gco:CharacterString><xsl:value-of select="specUsage"/></gco:CharacterString>
		</specificUsage>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="usageDate">
			<usageDateTime>
				<gco:DateTime><xsl:value-of select="."/></gco:DateTime>
			</usageDateTime>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="usrDetLim">
			<userDeterminedLimitations>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</userDeterminedLimitations>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="usrCntInfo">
			<userContactInfo>
				<CI_ResponsibleParty>
					<xsl:apply-templates select="." mode="RespParty"/>
				</CI_ResponsibleParty>
			</userContactInfo>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === ConstsTypes === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="ConstsTypes">

		<xsl:for-each select="Consts">
			<MD_Constraints>
				<xsl:apply-templates select="." mode="Consts"/>
			</MD_Constraints>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="LegConsts">
			<MD_LegalConstraints>
				<xsl:apply-templates select="." mode="LegConsts"/>
			</MD_LegalConstraints>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="SecConsts">
			<MD_SecurityConstraints>
				<xsl:apply-templates select="." mode="SecConsts"/>
			</MD_SecurityConstraints>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Consts">

		<xsl:for-each select="useLimit">
			<useLimitation>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</useLimitation>
		</xsl:for-each>
		
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="LegConsts">

		<xsl:apply-templates select="." mode="Consts"/>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="accessConsts">
			<accessConstraints>
			     <!-- TODO AccessConstraint mapping -->
				<MD_RestrictionCode codeList="./resources/codeList.xml#MD_RestrictionCode" codeListValue="{RestrictCd/@value}" />
			</accessConstraints>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="useConsts">
			<useConstraints>
				<MD_RestrictionCode codeList="./resources/codeList.xml#MD_RestrictionCode" codeListValue="{RestrictCd/@value}" />
			</useConstraints>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="othConsts">
			<otherConstraints>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</otherConstraints>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="SecConsts">

		<xsl:apply-templates select="." mode="Consts"/>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<classification>
			<MD_ClassificationCode codeList="./resources/codeList.xml#MD_ClassificationCode">
				<xsl:attribute name="codeListValue">
					<xsl:choose>
						<xsl:when test="class/ClasscationCd/@value = 'topsecret'">topSecret</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="class/ClasscationCd/@value"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
			</MD_ClassificationCode>
		</classification>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="userNote">
			<userNote>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</userNote>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="classSys">
			<classificationSystem>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</classificationSystem>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="handDesc">
			<handlingDescription>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</handlingDescription>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === Resol === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Resol">

		<xsl:for-each select="equScale">
			<equivalentScale>
				<MD_RepresentativeFraction>
					<denominator>
						<gco:Integer><xsl:value-of select="rfDenom"/></gco:Integer>
					</denominator>
				</MD_RepresentativeFraction>
			</equivalentScale>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="scaleDist">
			<distance>
				<gco:Distance>
					<xsl:apply-templates select="." mode="Measure"/>
				</gco:Distance>
			</distance>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
