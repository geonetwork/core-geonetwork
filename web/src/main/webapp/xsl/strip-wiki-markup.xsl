<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="header.xsl"/>
	<xsl:include href="banner.xsl"/>
	<xsl:include href="utils.xsl"/>
	<xsl:include href="metadata.xsl"/>
	
	<xsl:param name="markupType">Default</xsl:param>
	<xsl:param name="outputType"/>
	<xsl:param name="wysiwygEnabled"/>

	<xsl:template priority="100" match="@*|node()">
	
	    <xsl:variable name="allowMarkup">
			<xsl:if test="node()">
				<xsl:apply-templates mode="permitMarkup" select="."/>
			</xsl:if>
		</xsl:variable>
		
		<xsl:choose>
			<xsl:when test="$allowMarkup = 'true'">
				<xsl:copy>
					<xsl:apply-templates select="@*"/>
					<xsl:apply-templates mode="stripWikiMarkup">
						<xsl:with-param name="node" select="."/>
					</xsl:apply-templates>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="@*"/>
					<xsl:apply-templates/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template mode="stripWikiMarkup" match="@*|node()">
		<xsl:param name="node"/>
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates mode="stripWikiMarkup">
				<xsl:with-param name="node" select="$node"/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template priority="100" mode="stripWikiMarkup" match="node()[count(./*) = 0]">
		<xsl:param name="node"/>
		<xsl:variable name="markedUp">
			<xsl:call-template name="processText">
				<xsl:with-param name="node" select="$node"/>
				<xsl:with-param name="text" select="."/>
				<xsl:with-param name="markupType" select="$markupType"/>
				<xsl:with-param name="wysiwygEnabled" select="$wysiwygEnabled"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:copy>
			<xsl:value-of select="$markedUp"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>