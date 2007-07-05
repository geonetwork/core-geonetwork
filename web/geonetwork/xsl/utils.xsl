<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:variable name="apos">&#x27;</xsl:variable>

	<xsl:variable name="maxAbstract" select="200"/>
	
	<!-- default: just copy -->
	<xsl:template match="@*|node()" mode="copy">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="copy"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template mode="escapeXMLEntities" match="text()">
	
		<xsl:variable name="expr" select="."/>
		
		<xsl:variable name="e1">
			<xsl:call-template name="replaceString">
				<xsl:with-param name="expr"        select="$expr"/>
				<xsl:with-param name="pattern"     select="'&amp;'"/>
				<xsl:with-param name="replacement" select="'&amp;amp;'"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="e2">
			<xsl:call-template name="replaceString">
				<xsl:with-param name="expr"        select="$e1"/>
				<xsl:with-param name="pattern"     select="'&lt;'"/>
				<xsl:with-param name="replacement" select="'&amp;lt;'"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="e3">
			<xsl:call-template name="replaceString">
				<xsl:with-param name="expr"        select="$e2"/>
				<xsl:with-param name="pattern"     select="'&gt;'"/>
				<xsl:with-param name="replacement" select="'&amp;gt;'"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="e4">
			<xsl:call-template name="replaceString">
				<xsl:with-param name="expr"        select="$e3"/>
				<xsl:with-param name="pattern"     select='"&apos;"'/>
				<xsl:with-param name="replacement" select="'&amp;apos;'"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:call-template name="replaceString">
			<xsl:with-param name="expr"        select="$e4"/>
			<xsl:with-param name="pattern"     select="'&quot;'"/>
			<xsl:with-param name="replacement" select="'&amp;quot;'"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="replaceString">
		<xsl:param name="expr"/>
		<xsl:param name="pattern"/>
		<xsl:param name="replacement"/>
		
		<xsl:variable name="first" select="substring-before($expr,$pattern)"/>
		<xsl:choose>
			<xsl:when test="$first">
				<xsl:value-of select="$first"/>
				<xsl:value-of select="$replacement"/>
				<xsl:call-template name="replaceString">
					<xsl:with-param name="expr"        select="substring-after($expr,$pattern)"/>
					<xsl:with-param name="pattern"     select="$pattern"/>
					<xsl:with-param name="replacement" select="$replacement"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$expr"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
