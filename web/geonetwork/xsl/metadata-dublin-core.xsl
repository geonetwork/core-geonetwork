<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dc = "http://purl.org/dc/elements/1.1/"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<!--
	default: in simple mode just a flat list
	-->
	<xsl:template mode="dublin-core" match="*|@*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="element" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$currTab='simple'"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	these elements should be boxed
	-->
	<xsl:template mode="dublin-core" match="simpledc">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	keywords
	<xsl:template mode="dublin-core" match="dc:subject">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text">
				<xsl:for-each select="themekey|placekey|stratkey|tempkey">
					<xsl:if test="position() &gt; 1">,	</xsl:if>
					<xsl:value-of select="."/>
				</xsl:for-each>
				<xsl:if test="themekt|placekt|stratkt|tempkt">
					<xsl:text> (</xsl:text>
					<xsl:value-of select="themekt|placekt|stratkt|tempkt"/>
					<xsl:text>)</xsl:text>
				</xsl:if>
			</xsl:with-param>
		</xsl:apply-templates>
	
	</xsl:template>
	-->

	<!--
	online link
	-->
	<xsl:template mode="dublin-core" match="dc:identifier">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text"><xsl:value-of select="."/></xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	placeholder
	<xsl:template mode="dublin-core" match="TAG">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		BODY
	</xsl:template>
	-->
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- dublin-core brief formatting -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<xsl:template name="dublin-coreBrief">
		<metadata>
			<xsl:if test="dc:title">
				<title><xsl:value-of select="dc:title"/></title>
			</xsl:if>
			<xsl:if test="dc:description">
				<abstract><xsl:value-of select="dc:description"/></abstract>
			</xsl:if>

			<xsl:for-each select="dc:subject[text()]">
				<keyword><xsl:value-of select="."/></keyword>
			</xsl:for-each>
			<xsl:for-each select="dc:identifier[text()]">
				<link type="url"><xsl:value-of select="."/></link>
			</xsl:for-each>
			<!-- FIXME
			<image>IMAGE</image>
			-->
			
			<xsl:variable name="coverage" select="dc:coverage"/>
			<xsl:variable name="n" select="substring-after($coverage,'North ')"/>
			<xsl:variable name="north" select="substring-before($n,',')"/>
			<xsl:variable name="s" select="substring-after($coverage,'South ')"/>
			<xsl:variable name="south" select="substring-before($s,',')"/>
			<xsl:variable name="e" select="substring-after($coverage,'East ')"/>
			<xsl:variable name="east" select="substring-before($e,',')"/>
			<xsl:variable name="w" select="substring-after($coverage,'West ')"/>
			<xsl:variable name="west" select="substring-before($w,'. ')"/>
			<xsl:variable name="p" select="substring-after($coverage,'(')"/>
			<xsl:variable name="place" select="substring-before($p,')')"/>
			<xsl:if test="$n!=''">
				<geoBox>
					<westBL><xsl:value-of select="$west"/></westBL>
					<eastBL><xsl:value-of select="$east"/></eastBL>
					<southBL><xsl:value-of select="$south"/></southBL>
					<northBL><xsl:value-of select="$north"/></northBL>
				</geoBox>
			</xsl:if>
		
			<xsl:copy-of select="geonet:info"/>
		</metadata>
	</xsl:template>
				
</xsl:stylesheet>
