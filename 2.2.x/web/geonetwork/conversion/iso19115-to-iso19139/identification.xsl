<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns    ="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:gts="http://www.isotc211.org/2005/gts"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="DataIdentification">

		<citation>
			<CI_Citation>
				<xsl:apply-templates select="idCitation" mode="Citation"/>
			</CI_Citation>
		</citation>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<abstract>
			<gco:CharacterString><xsl:value-of select="idAbs"/></gco:CharacterString>
		</abstract>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="idPurp">
			<purpose>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</purpose>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="idCredit">
			<credit>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</credit>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="status">
			<status>
				<MD_ProgressCode codeList="./resources/codeList.xml#MD_ProgressCode" codeListValue="{ProgCd/@value}" />
			</status>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="idPoC">
			<pointOfContact>
				<CI_ResponsibleParty>
					<xsl:apply-templates select="." mode="RespParty"/>
				</CI_ResponsibleParty>
			</pointOfContact>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="resMaint">
			<resourceMaintenance>
				<MD_MaintenanceInformation>
					<xsl:apply-templates select="." mode="MaintInfo"/>
				</MD_MaintenanceInformation>
			</resourceMaintenance>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="graphOver">
			<graphicOverview>
				<MD_BrowseGraphic>
					<xsl:apply-templates select="." mode="BrowGraph"/>
				</MD_BrowseGraphic>
			</graphicOverview>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="dsFormat">
			<resourceFormat>
				<MD_Format>
					<xsl:apply-templates select="." mode="Format"/>
				</MD_Format>
			</resourceFormat>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="descKeys">
			<descriptiveKeywords>
				<MD_Keywords>
					<xsl:apply-templates select="." mode="Keywords"/>
				</MD_Keywords>
			</descriptiveKeywords>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="idSpecUse">
			<resourceSpecificUsage>
				<MD_Usage>
					<xsl:apply-templates select="." mode="Usage"/>
				</MD_Usage>
			</resourceSpecificUsage>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="resConst">
			<resourceConstraints>
				<xsl:apply-templates select="." mode="ConstsTypes"/>
			</resourceConstraints>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="spatRpType">
			<spatialRepresentationType>
				<MD_SpatialRepresentationTypeCode codeList="./resources/codeList.xml#MD_SpatialRepresentationTypeCode" codeListValue="{SpatRepTypCd/@value}" />
			</spatialRepresentationType>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="dataScale">
			<spatialResolution>
				<MD_Resolution>
					<xsl:apply-templates select="." mode="Resol"/>
				</MD_Resolution>
			</spatialResolution>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="dataLang">
			<language>
				<gco:CharacterString>
					<xsl:call-template name="convertLanguage">
						<xsl:with-param name="code" select="languageCode/@value"/>
					</xsl:call-template>
				</gco:CharacterString>
			</language>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="dataChar">
			<characterSet>
				<MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode" codeListValue="{CharSetCd/@value}" />
			</characterSet>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="tpCat">
			<topicCategory>
				<MD_TopicCategoryCode><xsl:value-of select="TopicCatCd/@value" /></MD_TopicCategoryCode>
			</topicCategory>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="envirDesc">
			<environmentDescription>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</environmentDescription>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="dataExt">
			<extent>
				<EX_Extent>
					<xsl:apply-templates select="." mode="Extent"/>
				</EX_Extent>
			</extent>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="geoBox">
			<extent>
				<EX_Extent>
					<geographicElement>
						<EX_GeographicBoundingBox>
							<westBoundLongitude>
								<gco:Decimal><xsl:value-of select="westBL"/></gco:Decimal>
							</westBoundLongitude>
							<eastBoundLongitude>
								<gco:Decimal><xsl:value-of select="eastBL"/></gco:Decimal>
							</eastBoundLongitude>
							<southBoundLatitude>
								<gco:Decimal><xsl:value-of select="southBL"/></gco:Decimal>
							</southBoundLatitude>
							<northBoundLatitude>
								<gco:Decimal><xsl:value-of select="northBL"/></gco:Decimal>
							</northBoundLatitude>
						</EX_GeographicBoundingBox>
					</geographicElement>
				</EX_Extent>
			</extent>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="geoDesc">
			<extent>
				<EX_Extent>
					<geographicElement>
						<EX_GeographicDescription>
							<xsl:apply-templates select="." mode="GeoDesc"/>
						</EX_GeographicDescription>
					</geographicElement>
				</EX_Extent>
			</extent>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="suppInfo">
			<supplementalInformation>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</supplementalInformation>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === MaintInfo === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="MaintInfo">

		<maintenanceAndUpdateFrequency>
			<MD_MaintenanceFrequencyCode codeList="./resources/codeList.xml#MD_MaintenanceFrequencyCode" codeListValue="{maintFreq/MaintFreqCd/@value}" />
		</maintenanceAndUpdateFrequency>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="dateNext">
			<dateOfNextUpdate>
				<gco:Date><xsl:value-of select="."/></gco:Date>
			</dateOfNextUpdate>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="usrDefFreq">
			<userDefinedMaintenanceFrequency>			
				<gts:TM_PeriodDuration>
					<xsl:apply-templates select="." mode="TM_PeriodDuration"/>
				</gts:TM_PeriodDuration>
			</userDefinedMaintenanceFrequency>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="maintScp">
			<updateScope>
				<MD_ScopeCode codeList="./resources/codeList.xml#MD_ScopeCode" codeListValue="{ScopeCd/@value}" />
			</updateScope>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="upScpDesc">
			<updateScopeDescription>
				<MD_ScopeDescription>
					<xsl:apply-templates select="." mode="ScpDesc"/>
				</MD_ScopeDescription>
			</updateScopeDescription>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="maintNote">
			<maintenanceNote>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</maintenanceNote>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="TM_PeriodDuration">
		<xsl:value-of select="years"/>-<xsl:value-of select="months"/>-<xsl:value-of select="days"/>T
		<xsl:value-of select="hours"/>:<xsl:value-of select="minutes"/>:<xsl:value-of select="seconds"/>
	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === BrowGraph === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="BrowGraph">

		<fileName>
			<gco:CharacterString><xsl:value-of select="bgFileName"/></gco:CharacterString>
		</fileName>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="bgFileDesc">
			<fileDescription>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</fileDescription>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="bgFileType">
			<fileType>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</fileType>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === Format === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Format">

		<name>
			<gco:CharacterString><xsl:value-of select="formatName"/></gco:CharacterString>
		</name>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<version>
			<gco:CharacterString><xsl:value-of select="formatVer"/></gco:CharacterString>
		</version>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="formatAmdNum">
			<amendmentNumber>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</amendmentNumber>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="formatSpec">
			<specification>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</specification>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="fileDecmTech">
			<fileDecompressionTechnique>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</fileDecompressionTechnique>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === Keywords === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Keywords">

		<xsl:for-each select="keyword">
			<keyword>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</keyword>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="keyTyp">
			<type>
				<MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode" codeListValue="{KeyTypCd/@value}" />
			</type>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="thesaName">
			<thesaurusName>
				<CI_Citation>
					<xsl:apply-templates select="idCitation" mode="Citation"/>
				</CI_Citation>
			</thesaurusName>
		</xsl:for-each>

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
