<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns    ="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Distribution">

		<xsl:for-each select="distributor">
			<distributor>
				<MD_Distributor>
					<xsl:apply-templates select="." mode="Distributor"/>
				</MD_Distributor>
			</distributor>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="distTranOps">
			<transferOptions>
				<MD_DigitalTransferOptions>
					<xsl:apply-templates select="." mode="DigTranOps"/>
				</MD_DigitalTransferOptions>
			</transferOptions>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === Distributor === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Distributor">

		<distributorContact>
			<CI_ResponsibleParty>
				<xsl:apply-templates select="distorCont" mode="RespParty"/>
			</CI_ResponsibleParty>
		</distributorContact>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="distorOrdPrc">
			<distributionOrderProcess>
				<MD_StandardOrderProcess>
					<xsl:apply-templates select="." mode="StanOrdProc"/>
				</MD_StandardOrderProcess>
			</distributionOrderProcess>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="distorFormat">
			<distributorFormat>
				<MD_Format>
					<xsl:apply-templates select="." mode="Format"/>
				</MD_Format>
			</distributorFormat>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="distorTran">
			<distributorTransferOptions>
				<MD_DigitalTransferOptions>
					<xsl:apply-templates select="." mode="DigTranOps"/>
				</MD_DigitalTransferOptions>
			</distributorTransferOptions>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="StanOrdProc">

		<xsl:for-each select="resFees">
			<fees>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</fees>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="planAvDtTm">
			<plannedAvailableDateTime>
				<gco:DateTime><xsl:value-of select="."/></gco:DateTime>
			</plannedAvailableDateTime>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="ordInstr">
			<orderingInstructions>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</orderingInstructions>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="ordTurn">
			<turnaround>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</turnaround>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === DigTranOps === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="DigTranOps">

		<xsl:for-each select="unitsODist">
			<unitsOfDistribution>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</unitsOfDistribution>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="transSize">
			<transferSize>
				<gco:Real><xsl:value-of select="."/></gco:Real>
			</transferSize>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="onLineSrc">
			<onLine>
				<CI_OnlineResource>
					<xsl:apply-templates select="." mode="OnLineRes"/>
				</CI_OnlineResource>
			</onLine>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="onLineMed">
			<offLine>
				<MD_Medium>
					<xsl:apply-templates select="." mode="Medium"/>
				</MD_Medium>
			</offLine>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Medium">

		<xsl:for-each select="medName">
			<name>
				<MD_MediumNameCode codeList="./resources/codeList.xml#MD_MediumNameCode">
					<xsl:attribute name="codeListValue">
						<xsl:choose>
							<xsl:when test="MedNameCd/@value = 'online'">onLine</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="MedNameCd/@value"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:attribute>
				</MD_MediumNameCode>
			</name>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="medDensity">
			<density>
				<gco:Real><xsl:value-of select="."/></gco:Real>
			</density>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="medDenUnits">
			<densityUnits>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</densityUnits>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="medVol">
			<volumes>
				<gco:Integer><xsl:value-of select="."/></gco:Integer>
			</volumes>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="medFormat">
			<mediumFormat>
				<MD_MediumFormatCode codeList="./resources/codeList.xml#MD_MediumFormatCode" codeListValue="{MedFormCd/@value}"/>
			</mediumFormat>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="medNote">
			<mediumNote>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</mediumNote>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
