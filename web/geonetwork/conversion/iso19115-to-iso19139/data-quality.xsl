<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns    ="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="DataQuality">

		<scope>
			<DQ_Scope>
				<xsl:apply-templates select="dqScope" mode="DQScope"/>
			</DQ_Scope>
		</scope>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="dqReport">
			<report>
				<xsl:apply-templates select="." mode="DQElementTypes"/>
			</report>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="dataLineage">
			<lineage>
				<LI_Lineage>
					<xsl:apply-templates select="." mode="Lineage"/>
				</LI_Lineage>
			</lineage>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === Data quality scope === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="DQScope">

		<level>
			<MD_ScopeCode codeList="./resources/codeList.xml#MD_ScopeCode" codeListValue="{scpLvl/ScopeCd/@value}" />
		</level>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="scpExt">
			<extent>
				<EX_Extent>
					<xsl:apply-templates select="." mode="Extent"/>
				</EX_Extent>
			</extent>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="scpLvlDesc">
			<levelDescription>
				<MD_ScopeDescription>
					<xsl:apply-templates select="." mode="ScpDesc"/>
				</MD_ScopeDescription>
			</levelDescription>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="ScpDesc">

		<xsl:for-each select="attribIntSet">
			<attributeInstances><xsl:value-of select="."/></attributeInstances>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="attribSet">
			<attributes><xsl:value-of select="."/></attributes>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="datasetSet">
			<dataset><xsl:value-of select="."/></dataset>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="featIntSet">
			<featureInstances><xsl:value-of select="."/></featureInstances>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="featSet">
			<features><xsl:value-of select="."/></features>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="other">
			<other><xsl:value-of select="."/></other>
		</xsl:for-each>
	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === DQElementTypes === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="DQElementTypes">

		<xsl:for-each select="DQAbsExtPosAcc">
			<DQ_AbsoluteExternalPositionalAccuracy>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_AbsoluteExternalPositionalAccuracy>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQAccTimeMeas">
			<DQ_AccuracyOfATimeMeasurement>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_AccuracyOfATimeMeasurement>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQCompComm">
			<DQ_CompletenessCommission>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_CompletenessCommission>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQCompOm">
			<DQ_CompletenessOmission>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_CompletenessOmission>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQConcConsis">
			<DQ_ConceptualConsistency>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_ConceptualConsistency>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQDomConsis">
			<DQ_DomainConsistency>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_DomainConsistency>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQFormConsis">
			<DQ_FormatConsistency>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_FormatConsistency>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQGridDataPosAcc">
			<DQ_GriddedDataPositionalAccuracy>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_GriddedDataPositionalAccuracy>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQNonQuanAttAcc">
			<DQ_NonQuantitativeAttributeAccuracy>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_NonQuantitativeAttributeAccuracy>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQQuanAttAcc">
			<DQ_QuantitativeAttributeAccuracy>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_QuantitativeAttributeAccuracy>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQRelIntPosAcc">
			<DQ_RelativeInternalPositionalAccuracy>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_RelativeInternalPositionalAccuracy>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQTempConsis">
			<DQ_TemporalConsistency>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_TemporalConsistency>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQTempValid">
			<DQ_TemporalValidity>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_TemporalValidity>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQThemClassCor">
			<DQ_ThematicClassificationCorrectness>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_ThematicClassificationCorrectness>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="DQTopConsis">
			<DQ_TopologicalConsistency>
				<xsl:apply-templates select="." mode="DQElement"/>
			</DQ_TopologicalConsistency>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="DQElement">

		<xsl:for-each select="measName">
			<nameOfMeasure>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</nameOfMeasure>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="measId">
			<measureIdentification>
				<xsl:apply-templates select="." mode="MdIdentTypes"/>
			</measureIdentification>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="measureDescription">
			<measureDescription>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</measureDescription>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="evalMethType">
			<evaluationMethodType>
				<DQ_EvaluationMethodTypeCode codeList="./resources/codeList.xml#DQ_EvaluationMethodTypeCode" codeListValue="{EvalMethTypeCd/@value}" />
			</evaluationMethodType>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="evalMethDesc">
			<evaluationMethodDescription>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</evaluationMethodDescription>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="evaluationProcedure">
			<evaluationProcedure>
				<CI_Citation>
					<xsl:apply-templates select="." mode="Citation"/>
				</CI_Citation>
			</evaluationProcedure>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="measDateTm">
			<dateTime>
				<gco:DateTime><xsl:value-of select="."/></gco:DateTime>
			</dateTime>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="measResult">
			<result>
				<xsl:apply-templates select="." mode="ResultTypes"/>
			</result>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="MdIdentTypes">

		<xsl:for-each select="MdIdent">
			<MD_Identifier>
				<xsl:apply-templates select="." mode="MdIdent"/>
			</MD_Identifier>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="RS_Identifier">
			<RS_Identifier>
				<xsl:apply-templates select="." mode="MdIdent"/>
			</RS_Identifier>
		</xsl:for-each>
		
	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="ResultTypes">

		<xsl:for-each select="ConResult">
			<DQ_ConformanceResult>
				<xsl:apply-templates select="." mode="ConResult"/>
			</DQ_ConformanceResult>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="QuanResult">
			<DQ_QuantitativeResult>
				<xsl:apply-templates select="." mode="QuanResult"/>
			</DQ_QuantitativeResult>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="ConResult">

		<specification>
			<CI_Citation>
				<xsl:apply-templates select="conSpec" mode="Citation"/>
			</CI_Citation>
		</specification>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<explanation>
			<gco:CharacterString><xsl:value-of select="conExpl"/></gco:CharacterString>
		</explanation>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<pass>
			<gco:Boolean><xsl:value-of select="conPass"/></gco:Boolean>
		</pass>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="QuanResult">

		<xsl:for-each select="quanValType">
			<valueType>
				<gco:RecordType><xsl:value-of select="."/></gco:RecordType>
			</valueType>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="quanValUnit">
			<valueUnit>
				<gco:Measure>
					<xsl:apply-templates select="." mode="Measure"/>
				</gco:Measure>
			</valueUnit>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="errStat">
			<errorStatistic>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</errorStatistic>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="quanValue">
			<value>
				<gco:Record><xsl:value-of select="."/></gco:Record>
			</value>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === Lineage === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Lineage">

		<xsl:for-each select="statement">
			<statement>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</statement>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="prcStep">
			<processStep>
				<LI_ProcessStep>
					<xsl:apply-templates select="." mode="PrcessStep"/>
				</LI_ProcessStep>
			</processStep>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="dataSource">
			<source>
				<LI_Source>
					<xsl:apply-templates select="." mode="Source"/>
				</LI_Source>
			</source>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="PrcessStep">

		<description>
			<gco:CharacterString><xsl:value-of select="stepDesc"/></gco:CharacterString>
		</description>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="stepRat">
			<rationale>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</rationale>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="stepDateTm">
			<dateTime>
				<gco:DateTime><xsl:value-of select="."/></gco:DateTime>
			</dateTime>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="stepProc">
			<processor>
				<CI_ResponsibleParty>
					<xsl:apply-templates select="." mode="RespParty"/>
				</CI_ResponsibleParty>
			</processor>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="stepSrc">
			<source>
				<LI_Source>
					<xsl:apply-templates select="." mode="Source"/>
				</LI_Source>
			</source>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Source">

		<xsl:for-each select="srcDesc">
			<description>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</description>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="srcScale">
			<scaleDenominator>
				<MD_RepresentativeFraction>
					<denominator>
						<gco:Integer><xsl:value-of select="rfDenom"/></gco:Integer>
					</denominator>
				</MD_RepresentativeFraction>
			</scaleDenominator>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="srcRefSys">
			<sourceReferenceSystem>
				<MD_ReferenceSystem>
					<xsl:apply-templates select="." mode="RefSystemTypes"/>
				</MD_ReferenceSystem>
			</sourceReferenceSystem>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="srcCitatn">
			<sourceCitation>
				<CI_Citation>
					<xsl:apply-templates select="." mode="Citation"/>
				</CI_Citation>
			</sourceCitation>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="srcExt">
			<sourceExtent>
				<EX_Extent>
					<xsl:apply-templates select="." mode="Extent"/>
				</EX_Extent>
			</sourceExtent>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="srcStep">
			<sourceStep>
				<LI_ProcessStep>
					<xsl:apply-templates select="." mode="PrcessStep"/>
				</LI_ProcessStep>
			</sourceStep>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
