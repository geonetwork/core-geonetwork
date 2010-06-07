<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns    ="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Citation">

		<title>
			<gco:CharacterString><xsl:value-of select="resTitle"/></gco:CharacterString>
		</title>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="resAltTitle">
			<alternateTitle>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</alternateTitle>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="resRefDate">
			<date>
				<CI_Date>
					<xsl:apply-templates select="." mode="RefDate"/>
				</CI_Date>
			</date>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="resEd">
			<edition>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</edition>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="resEdDate">
			<editionDate>
				<gco:DateTime><xsl:value-of select="."/></gco:DateTime>
			</editionDate>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="citId">
			<identifier>
				<MD_Identifier>
					<code>
						<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
					</code>
				</MD_Identifier>
			</identifier>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="citRespParty">
			<citedResponsibleParty>
				<CI_ResponsibleParty>
					<xsl:apply-templates select="." mode="RespParty"/>
				</CI_ResponsibleParty>
			</citedResponsibleParty>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="presForm">
			<presentationForm>
				<CI_PresentationFormCode codeList="./resources/codeList.xml#CI_PresentationFormCode" codeListValue="{PresFormCd/@value}" />
			</presentationForm>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="datasetSeries">
			<series>
				<CI_Series>
					<xsl:apply-templates select="." mode="DatasetSeries"/>
				</CI_Series>
			</series>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="otherCitDet">
			<otherCitationDetails>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</otherCitationDetails>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="collTitle">
			<collectiveTitle>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</collectiveTitle>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="isbn">
			<ISBN>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</ISBN>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="issn">
			<ISSN>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</ISSN>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="RefDate">

		<date>
			<gco:DateTime><xsl:value-of select="refDate"/></gco:DateTime>
		</date>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<dateType>
			<CI_DateTypeCode codeList="./resources/codeList.xml#CI_DateTypeCode" codeListValue="{refDateType/DateTypCd/@value}" />
		</dateType>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="DatasetSeries">

		<xsl:for-each select="seriesName">
			<name>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</name>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="issId">
			<issueIdentification>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</issueIdentification>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="artPage">
			<page>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</page>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
