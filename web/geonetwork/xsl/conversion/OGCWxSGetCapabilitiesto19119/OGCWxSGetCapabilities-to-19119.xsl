<?xml version="1.0" encoding="UTF-8"?>
<!--  
Mapping between : 
- WMS 1.0.0
- WMS 1.1.1
- WCS 1.0.0
- WFS 1.0.0
- WFS 1.1.0
... to ISO19119. 
 -->
<xsl:stylesheet version="2.0" xmlns    ="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:gts="http://www.isotc211.org/2005/gts"
										xmlns:gml="http://www.opengis.net/gml"
										xmlns:srv="http://www.isotc211.org/2005/srv"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:xlink="http://www.w3.org/1999/xlink"
										xmlns:date="http://exslt.org/dates-and-times"
										xmlns:wfs="http://www.opengis.net/wfs"
										xmlns:ows="http://www.opengis.net/ows"
										xmlns:wcs="http://www.opengis.net/wcs"
										extension-element-prefixes="date wcs ows wfs">

	<!-- ============================================================================= -->
	
	<xsl:param name="lang">eng</xsl:param>
	<xsl:param name="topic"></xsl:param>
	
	<!-- ============================================================================= -->
	
	<xsl:include href="resp-party.xsl"/>
	<xsl:include href="ref-system.xsl"/>
	<xsl:include href="identification.xsl"/>
	
	<!-- ============================================================================= -->

	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
	
	<!-- ============================================================================= -->

	<xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="WMT_MS_Capabilities|wfs:WFS_Capabilities|wcs:WCS_Capabilities">
	
		<xsl:variable name="ows">
			<xsl:choose>
				<xsl:when test="name(.)='wfs:WFS_Capabilities'">true</xsl:when>
				<xsl:otherwise>false</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		
		<MD_Metadata>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<fileIdentifier>
				<gco:CharacterString>
					<xsl:choose>
						<xsl:when test="$ows='true'">
							<xsl:value-of select="//ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
						</xsl:when>
						<xsl:when test="name(.)='WFS_Capabilities'">
							<xsl:value-of select="//wfs:GetCapabilities/wfs:DCPType/wfs:HTTP/wfs:Get/@onlineResource"/>
						</xsl:when>
						<xsl:when test="name(.)='WMT_MS_Capabilities'">
							<xsl:value-of select="//GetCapabilities//OnlineResource[1]/@xlink:href"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="//wcs:GetCapabilities//wcs:OnlineResource[1]/@xlink:href"/>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:text>:</xsl:text>
					<xsl:value-of select='@version'/>
				</gco:CharacterString>
			</fileIdentifier>
		
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<language>
				<gco:CharacterString><xsl:value-of select="$lang"/></gco:CharacterString>
				<!-- English is default. Not available in GetCapabilities. 
				Selected by user from GUI -->
			</language>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<characterSet>
				<MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode" codeListValue="utf8" />
			</characterSet>

			<!-- parentIdentifier : service have no parent -->
			<!-- mdHrLv -->
            <hierarchyLevel>
                <MD_ScopeCode
                    codeList="./resources/codeList.xml#MD_ScopeCode"
                    codeListValue="service" />
            </hierarchyLevel>
			
            <!-- mdHrLvName -->

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="Service/ContactInformation|wfs:Service/wfs:ContactInformation">
				<contact>
					<CI_ResponsibleParty>
						<xsl:apply-templates select="." mode="RespParty"/>
					</CI_ResponsibleParty>
				</contact>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<xsl:variable name="df">yyyy-MM-dd'T'HH:mm:ss</xsl:variable>
			<dateStamp>
				<gco:DateTime><xsl:value-of select="date:format-date(date:date-time(),$df)"/></gco:DateTime>
			</dateStamp>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<metadataStandardName>
				<gco:CharacterString>ISO 19119/2005</gco:CharacterString>
			</metadataStandardName>

			<metadataStandardVersion>
				<gco:CharacterString>1.0</gco:CharacterString>
			</metadataStandardVersion>

			<!-- spatRepInfo-->
			<!-- TODO - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="refSysInfo">
				<referenceSystemInfo>
					<MD_ReferenceSystem>
						<xsl:apply-templates select="." mode="RefSystemTypes"/>
					</MD_ReferenceSystem>
				</referenceSystemInfo>
			</xsl:for-each>

			<!--mdExtInfo-->
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<identificationInfo>
				<srv:SV_ServiceIdentification>
					<xsl:apply-templates select="." mode="SrvDataIdentification">
						<xsl:with-param name="topic"><xsl:value-of select="$topic"/></xsl:with-param>
						<xsl:with-param name="ows"><xsl:value-of select="$ows"/></xsl:with-param>
					</xsl:apply-templates>
				</srv:SV_ServiceIdentification>
			</identificationInfo>
		
			<!--contInfo-->
			<!--distInfo -->
			 <distributionInfo>
                <MD_Distribution>
                    <transferOptions>
                        <MD_DigitalTransferOptions>
                            <onLine>
                                <CI_OnlineResource>
                                    <linkage>
                                        <URL>
                                        <xsl:choose>
                                            <xsl:when test="$ows='true'">
                                                <xsl:value-of select="//ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
                                            </xsl:when>
                                            <xsl:when test="name(.)='WFS_Capabilities'">
                                                <xsl:value-of select="//wfs:GetCapabilities/wfs:DCPType/wfs:HTTP/wfs:Get/@onlineResource"/>
                                            </xsl:when>
                                            <xsl:when test="name(.)='WMT_MS_Capabilities'">
                                                <xsl:value-of select="//GetCapabilities//OnlineResource[1]/@xlink:href"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="//wcs:GetCapabilities//wcs:OnlineResource[1]/@xlink:href"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        </URL>
                                    </linkage>
                                    <protocol>
                                        <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                                    </protocol>
                                    <description>
                                        <gco:CharacterString>
                                            <xsl:choose>
                                                <xsl:when test="$ows='true'">
                                                    <xsl:value-of select="//ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
                                                </xsl:when>
                                                <xsl:when test="name(.)='WFS_Capabilities'">
                                                    <xsl:value-of select="//wfs:GetCapabilities/wfs:DCPType/wfs:HTTP/wfs:Get/@onlineResource"/>
                                                </xsl:when>
                                                <xsl:when test="name(.)='WMT_MS_Capabilities'">
                                                    <xsl:value-of select="//GetCapabilities//OnlineResource[1]/@xlink:href"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="//wcs:GetCapabilities//wcs:OnlineResource[1]/@xlink:href"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </gco:CharacterString>
                                    </description>
                                 </CI_OnlineResource>    
                             </onLine>
                        </MD_DigitalTransferOptions>
                   </transferOptions>
               </MD_Distribution>
            </distributionInfo> 
			<!--dqInfo-->
			<dataQualityInfo>
				<DQ_DataQuality>
					<scope>
						<DQ_Scope>
							<level>
								<MD_ScopeCode codeListValue="service"
									codeList="./resources/codeList.xml#MD_ScopeCode" />
							</level>
						</DQ_Scope>
					</scope>
					<lineage>
						<LI_Lineage>
							<statement>
								<gco:CharacterString />
							</statement>
						</LI_Lineage>
					</lineage>
				</DQ_DataQuality>
			</dataQualityInfo>
			<!--mdConst -->
			<!--mdMaint-->

		</MD_Metadata>
	</xsl:template>
	
	<!-- ============================================================================= -->

</xsl:stylesheet>
