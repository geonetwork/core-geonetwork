<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns    ="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="MdExInfo">

		<xsl:for-each select="extOnRes">
			<extensionOnLineResource>
				<CI_OnlineResource>
					<xsl:apply-templates select="." mode="OnLineRes"/>
				</CI_OnlineResource>
			</extensionOnLineResource>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="extEleInfo">
			<extendedElementInformation>
				<MD_ExtendedElementInformation>
					<xsl:apply-templates select="." mode="ExtEleInfo"/>
				</MD_ExtendedElementInformation>
			</extendedElementInformation>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="ExtEleInfo">

		<name>
			<gco:CharacterString><xsl:value-of select="extEleName"/></gco:CharacterString>
		</name>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="extShortName">
			<shortName>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</shortName>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="extDomcode">
			<domainCode>
				<gco:Integer><xsl:value-of select="."/></gco:Integer>
			</domainCode>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<definition>
			<gco:CharacterString><xsl:value-of select="extEleDef"/></gco:CharacterString>
		</definition>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="extEleOb">
			<obligation>
				<MD_ObligationCode codeList="./resources/codeList.xml#MD_ObligationCode" codeListValue="{ObCd/@value}" />
			</obligation>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="extEleCond">
			<condition>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</condition>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<dataType>
			<MD_DatatypeCode codeList="./resources/codeList.xml#MD_DatatypeCode" codeListValue="{eleDatatype/DatatypeCd/@value}" />
		</dataType>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="extEleMxOc">
			<maximumOccurrence>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</maximumOccurrence>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="extEleDomVal">
			<domainValue>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</domainValue>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="extEleParEnt">
			<parentEntity>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</parentEntity>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<rule>
			<gco:CharacterString><xsl:value-of select="extEleRule"/></gco:CharacterString>
		</rule>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="extEleRat">
			<rationale>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</rationale>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="extEleSrc">
			<source>
				<CI_ResponsibleParty>
					<xsl:apply-templates select="." mode="RespParty"/>
				</CI_ResponsibleParty>
			</source>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
