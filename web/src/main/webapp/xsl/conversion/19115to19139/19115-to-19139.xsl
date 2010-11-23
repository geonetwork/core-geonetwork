<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns="http://www.isotc211.org/2005/gmd"
				xmlns:gco="http://www.isotc211.org/2005/gco"
				xmlns:gts="http://www.isotc211.org/2005/gts"
				xmlns:gml="http://www.opengis.net/gml"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================= -->

	<xsl:include href="resp-party.xsl"/>
	<xsl:include href="spat-rep-types.xsl"/>
	<xsl:include href="citation.xsl"/>
	<xsl:include href="extension.xsl"/>
	<xsl:include href="extent.xsl"/>
	<xsl:include href="ref-system.xsl"/>
	<xsl:include href="data-quality.xsl"/>
	<xsl:include href="identification.xsl"/>
	<xsl:include href="content.xsl"/>
	<xsl:include href="distribution.xsl"/>
	<xsl:include href="app-schema.xsl"/>

	<!-- ============================================================================= -->

	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
	
	<!-- ============================================================================= -->

	<xsl:template match="/">
<!--		<DS_DataSet>
			<has> -->
				<xsl:apply-templates/>
<!--			</has>
		</DS_DataSet> -->
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="Metadata">
		<MD_Metadata xsi:schemaLocation="http://www.isotc211.org/2005/gmd http://schemas.opengis.net/iso/19139/20070417/gmd/gmd.xsd">

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="mdFileID">
				<fileIdentifier>
					<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
				</fileIdentifier>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="mdLang">
				<language>
					<LanguageCode codeList="" codeListValue="{languageCode/@value}"></LanguageCode>
				</language>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="mdChar">
				<characterSet>
					<MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode" codeListValue="{CharSetCd/@value}" />
				</characterSet>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="mdParentID">
				<parentIdentifier>
					<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
				</parentIdentifier>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="mdHrLv">
				<hierarchyLevel>
					<MD_ScopeCode codeList="./resources/codeList.xml#MD_ScopeCode" codeListValue="{ScopeCd/@value}" />
				</hierarchyLevel>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="mdHrLvName">
				<hierarchyLevelName>
					<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
				</hierarchyLevelName>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="mdContact">
				<contact>
					<CI_ResponsibleParty>
						<xsl:apply-templates select="." mode="RespParty"/>
					</CI_ResponsibleParty>
				</contact>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<dateStamp>
				<gco:DateTime><xsl:value-of select="mdDateSt"/></gco:DateTime>
			</dateStamp>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<metadataStandardName>
				<gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>
			</metadataStandardName>

			<metadataStandardVersion>
				<gco:CharacterString>1.0</gco:CharacterString>
			</metadataStandardVersion>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="spatRepInfo">
				<spatialRepresentationInfo>
					<xsl:apply-templates select="." mode="SpatRepTypes"/>
				</spatialRepresentationInfo>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="refSysInfo">
				<referenceSystemInfo>
					<MD_ReferenceSystem>
						<xsl:apply-templates select="." mode="RefSystemTypes"/>
					</MD_ReferenceSystem>
				</referenceSystemInfo>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="mdExtInfo">
				<metadataExtensionInfo>
					<MD_MetadataExtensionInformation>
						<xsl:apply-templates select="." mode="MdExInfo"/>
					</MD_MetadataExtensionInformation>
				</metadataExtensionInfo>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="dataIdInfo">
				<identificationInfo>
					<MD_DataIdentification>
						<xsl:apply-templates select="." mode="DataIdentification"/>
					</MD_DataIdentification>
				</identificationInfo>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="contInfo">
				<contentInfo>
					<xsl:apply-templates select="." mode="ContInfoTypes"/>
				</contentInfo>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="distInfo">
				<distributionInfo>
					<MD_Distribution>
						<xsl:apply-templates select="." mode="Distribution"/>
					</MD_Distribution>
				</distributionInfo>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="dqInfo">
				<dataQualityInfo>
					<DQ_DataQuality>
						<xsl:apply-templates select="." mode="DataQuality"/>
					</DQ_DataQuality>
				</dataQualityInfo>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="porCatInfo">
				<portrayalCatalogueInfo>
					<MD_PortrayalCatalogueReference>
						<portrayalCatalogueCitation>
							<CI_Citation>
								<xsl:apply-templates select="." mode="Citation"/>
							</CI_Citation>
						</portrayalCatalogueCitation>
					</MD_PortrayalCatalogueReference>
				</portrayalCatalogueInfo>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="mdConst">
				<metadataConstraints>
					<xsl:apply-templates select="." mode="ConstsTypes"/>
				</metadataConstraints>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="appSchInfo">
				<applicationSchemaInfo>
					<MD_ApplicationSchemaInformation>
						<xsl:apply-templates select="." mode="AppSchInfo"/>
					</MD_ApplicationSchemaInformation>
				</applicationSchemaInfo>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="mdMaint">
				<metadataMaintenance>
					<MD_MaintenanceInformation>
						<xsl:apply-templates select="." mode="MaintInfo"/>
					</MD_MaintenanceInformation>
				</metadataMaintenance>
			</xsl:for-each>

		</MD_Metadata>
	</xsl:template>
	
	<!-- ============================================================================= -->

</xsl:stylesheet>
