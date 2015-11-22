<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/root">
		<xsl:apply-templates select="descKeys/keyword"/>
	</xsl:template>
	
	<xsl:template match="keyword">
		<xsl:param name="ancestor">
			<null></null>
		</xsl:param>

		<keyword>
			<xsl:variable name="contents">
				<xsl:copy-of select="uri"/>
				<prefLabel>
					<en>
						<xsl:value-of select="value[@language='eng' or @lang='EN']/text()"/>
					</en>
					<eng>
						<xsl:value-of select="value[@language='eng' or @lang='EN']/text()"/>
					</eng>
				</prefLabel>
				<thesaurus>
					<xsl:value-of select="//request/thesaurus"/>
				</thesaurus>
			</xsl:variable>

			<xsl:copy-of select="$contents"/>

			<xsl:if test="not($ancestor/null)">
				<ancestor>
					<xsl:copy-of select="$ancestor"/>
				</ancestor>
			</xsl:if>

			<xsl:if test="count(../broader/*)>0">
				<broader>
					<xsl:apply-templates select="../broader/keyword">
						<xsl:with-param name="ancestor" select="$contents"/>
					</xsl:apply-templates>
				</broader>
			</xsl:if>
			<xsl:if test="count(../narrower/*)>0">
				<narrower>
					<xsl:apply-templates select="../narrower/keyword">
						<xsl:with-param name="ancestor" select="$contents"/>
					</xsl:apply-templates>
				</narrower>
			</xsl:if>
			<xsl:if test="count(../related/*)>0">
				<related>
					<xsl:apply-templates select="../related/keyword">
						<xsl:with-param name="ancestor" select="$contents"/>
					</xsl:apply-templates>
				</related>
			</xsl:if>
		</keyword>
	</xsl:template>
	
</xsl:stylesheet>
