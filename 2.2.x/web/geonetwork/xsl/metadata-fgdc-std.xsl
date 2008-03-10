<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<!--
	default: in simple mode just a flat list
	-->
	<xsl:template mode="fgdc-std" match="*|@*">
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
	<xsl:template mode="fgdc-std" match="idinfo|citeinfo|timeperd|status|bounding|keywords|metainfo|metc">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	keywords
	-->
	<xsl:template mode="fgdc-std" match="theme|place|stratum|temporal">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
	
		<xsl:choose>
		<xsl:when test="$edit=false()">
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
		</xsl:when>
		<xsl:otherwise>
		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		</xsl:otherwise>
		</xsl:choose>
	
	</xsl:template>

	<!--
	online link
	-->
	<xsl:template mode="fgdc-std" match="onlink">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text">
				<a href="{.}"><xsl:value-of select="."/></a>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	abstract
	-->
	<xsl:template mode="fgdc-std" match="abstract">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text">
				<xsl:call-template name="getElementText">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="rows"   select="10"/>
				</xsl:call-template>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	placeholder
	<xsl:template mode="fgdc-std" match="TAG">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		BODY
	</xsl:template>
	-->
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- fgdc-std brief formatting -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<xsl:template name="fgdc-stdBrief">
		<metadata>
			<xsl:copy-of select="idinfo/citation/citeinfo/title"/>
			<xsl:copy-of select="idinfo/descript/abstract"/>

			<xsl:for-each select="idinfo/keywords/theme/themekey[text()]">
				<keyword><xsl:value-of select="."/></keyword>
			</xsl:for-each>
			<xsl:for-each select="idinfo/keywords/place/placekey[text()]">
				<keyword><xsl:value-of select="."/></keyword>
			</xsl:for-each>
			<xsl:for-each select="idinfo/keywords/stratum/stratkey[text()]">
				<keyword><xsl:value-of select="."/></keyword>
			</xsl:for-each>
			<xsl:for-each select="idinfo/keywords/temporal/tempkey[text()]">
				<keyword><xsl:value-of select="."/></keyword>
			</xsl:for-each>
			<xsl:for-each select="idinfo/citation/citeinfo/onlink[text()]">
				<link type="url"><xsl:value-of select="."/></link>
			</xsl:for-each>
			<!-- FIXME
			<image>IMAGE</image>
			-->
			
			<xsl:if test="idinfo/spdom/bounding">
				<geoBox>
					<westBL><xsl:value-of select="idinfo/spdom/bounding/westbc"/></westBL>
					<eastBL><xsl:value-of select="idinfo/spdom/bounding/eastbc"/></eastBL>
					<southBL><xsl:value-of select="idinfo/spdom/bounding/southbc"/></southBL>
					<northBL><xsl:value-of select="idinfo/spdom/bounding/northbc"/></northBL>
				</geoBox>
			</xsl:if>
		
			<xsl:copy-of select="geonet:info"/>
		</metadata>
	</xsl:template>
				
</xsl:stylesheet>
