<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns    ="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="AppSchInfo">

		<name>
			<CI_Citation>
				<xsl:apply-templates select="asName" mode="Citation"/>
			</CI_Citation>
		</name>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<schemaLanguage>
			<gco:CharacterString><xsl:value-of select="asSchLang"/></gco:CharacterString>
		</schemaLanguage>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<constraintLanguage>
			<gco:CharacterString><xsl:value-of select="asCstLang"/></gco:CharacterString>
		</constraintLanguage>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="asAscii">
			<schemaAscii>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</schemaAscii>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="asGraFile">
			<graphicsFile>
				<gco:Binary><xsl:value-of select="."/></gco:Binary>
			</graphicsFile>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="asSwDevFile">
			<softwareDevelopmentFile>
				<gco:Binary><xsl:value-of select="."/></gco:Binary>
			</softwareDevelopmentFile>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="asSwDevFiFt">
			<softwareDevelopmentFileFormat>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</softwareDevelopmentFileFormat>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
