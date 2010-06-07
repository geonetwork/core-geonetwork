<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
				xmlns="http://www.isotc211.org/2005/gmd" 
				xmlns:geonet="http://www.fao.org/geonetwork" 
				xmlns:sld="http://www.opengis.net/sld"
				xmlns:gts="http://www.isotc211.org/2005/gts"
				xmlns:gco="http://www.isotc211.org/2005/gco"
				xmlns:gml="http://www.opengis.net/gml"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:xlink="http://www.w3.org/1999/xlink">

				
	<!-- ============================================================================= -->				

	<xsl:param name="lang">eng</xsl:param>
	<xsl:param name="topic"></xsl:param>

	<xsl:include href="identification.xsl"/>
	
	<!-- ============================================================================= -->
	
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
	
	<!-- ============================================================================= -->

	<xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>
	
	<!-- ============================================================================= -->	
	
	<xsl:template match="sld:StyledLayerDescriptor">
		<MD_Metadata>
					
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<!--  <fileIdentifier>
				<gco:CharacterString><xsl:value-of select="/wmc:ViewContext/@id"/></gco:CharacterString>
			</fileIdentifier>
			 -->
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<language>
				<gco:CharacterString><xsl:value-of select="$lang"/></gco:CharacterString>
				<!-- English is default. Not available in Web Map Context. Selected by user from GUI -->
			</language>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<characterSet>
				<MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode" codeListValue="utf8" />
			</characterSet>

			<!-- parentIdentifier : Web Map Context has no parent -->
			<!-- mdHrLv -->
			<!-- mdHrLvName -->

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>
			<dateStamp>
				<gco:DateTime><xsl:value-of select="format-dateTime(current-dateTime(),$df)"/></gco:DateTime>
			</dateStamp>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<metadataStandardName>
				<gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>
			</metadataStandardName>

			<metadataStandardVersion>
				<gco:CharacterString>1.0</gco:CharacterString>
			</metadataStandardVersion>
			
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<identificationInfo>
				<MD_DataIdentification>
					<xsl:apply-templates select="." mode="DataIdentification">
						<xsl:with-param name="topic"><xsl:value-of select="$topic"/></xsl:with-param>
					</xsl:apply-templates>
				</MD_DataIdentification>
			</identificationInfo>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<distributionInfo>
				<MD_Distribution>
					<transferOptions>
						<MD_DigitalTransferOptions>
							<onLine>
								<CI_OnlineResource>
									<linkage><URL/></linkage>
									<protocol>
										<!-- FIXME : use standardized label for WMS protocol -->
										<gco:CharacterString>OGC-SLD</gco:CharacterString>
									</protocol>
									<name>
										<gco:CharacterString><xsl:value-of select="sld:NamedLayer/sld:Name"/></gco:CharacterString>
									</name>
									<description>
										<gco:CharacterString><xsl:value-of select="sld:NamedLayer/sld:Name"/></gco:CharacterString>
									</description>
								</CI_OnlineResource>
							</onLine>
						</MD_DigitalTransferOptions>
					</transferOptions>
				</MD_Distribution>
			</distributionInfo>
		</MD_Metadata>
	</xsl:template>	
	
</xsl:stylesheet>
