<?xml version="1.0" encoding="UTF-8"?>

<!--
	Converts XML into a nice readable format.
	Tested with Saxon 6.5.3.
	As a test, this stylesheet should not change when run on itself.
	But note that there are no guarantees about attribute order within an
	element (see http://www.w3.org/TR/xpath#dt-document-order), or about
	which characters are escaped (see
	http://www.w3.org/TR/xslt#disable-output-escaping).
	I did not test processing instructions, CDATA sections, or
	namespaces.
	
	Hew Wolff
	Senior Engineer
	Art & Logic, Inc.
	www.artlogic.com
-->
	
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<!-- Take control of the whitespace. -->
	
	<xsl:output method="xml" indent="no" encoding="UTF-8"/>
	<xsl:strip-space elements="*"/>
	<xsl:preserve-space elements="xsl:text"/>

	<!-- Copy comments, and elements recursively. -->
	
	<xsl:template match="*|comment()">
		<xsl:param name="depth">0</xsl:param>
		
		<!--
			Set off from the element above if one of the two has children.
			Also, set off a comment from an element.
			And set off from the XML declaration if necessary.
		-->
		
		<xsl:variable name="isFirstNode" select="count(../..) = 0 and position() = 1"/>
		<xsl:variable name="previous" select="preceding-sibling::node()[1]"/>
		<xsl:variable name="adjacentComplexElement" select="count($previous/*) &gt; 0 or count(*) &gt; 0"/>
		<xsl:variable name="adjacentDifferentType" select="not(($previous/self::comment() and self::comment()) or ($previous/self::* and self::*))"/>
		
		<xsl:if test="$isFirstNode or ($previous and ($adjacentComplexElement or $adjacentDifferentType))">
			<xsl:text>&#xA;</xsl:text>
		</xsl:if>
		
		<!-- Start a new line. -->
		
		<xsl:text>&#xA;</xsl:text>
		
		<xsl:call-template name="indent">
			<xsl:with-param name="depth" select="$depth"/>
		</xsl:call-template>
		
		<xsl:copy>
			<xsl:if test="self::*">
				<xsl:copy-of select="@*"/>
				
				<xsl:apply-templates>
					<xsl:with-param name="depth" select="$depth + 1"/>
				</xsl:apply-templates>
				
				<xsl:if test="count(*) &gt; 0">
					<xsl:text>&#xA;</xsl:text>
					
					<xsl:call-template name="indent">
						<xsl:with-param name="depth" select="$depth"/>
					</xsl:call-template>
				</xsl:if>
			</xsl:if>
		</xsl:copy>
		
		<xsl:variable name="isLastNode" select="count(../..) = 0 and position() = last()"/>
		
		<xsl:if test="$isLastNode">
			<xsl:text>&#xA;</xsl:text>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="indent">
		<xsl:param name="depth"/>
		
		<xsl:if test="$depth &gt; 0">
			<xsl:text>   </xsl:text>
			
			<xsl:call-template name="indent">
				<xsl:with-param name="depth" select="$depth - 1"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<!-- Escape newlines within text nodes, for readability. -->
	
	<xsl:template match="text()">
		<xsl:call-template name="escapeNewlines">
			<xsl:with-param name="text">
				<xsl:value-of select="."/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="escapeNewlines">
		<xsl:param name="text"/>
		
		<xsl:if test="string-length($text) &gt; 0">
			<xsl:choose>
				<xsl:when test="substring($text, 1, 1) = '&#xA;'">
					<xsl:text disable-output-escaping="yes">&amp;#xA;</xsl:text>
				</xsl:when>
				
				<xsl:otherwise>
					<xsl:value-of select="substring($text, 1, 1)"/>
				</xsl:otherwise>
			</xsl:choose>
			
			<xsl:call-template name="escapeNewlines">
				<xsl:with-param name="text" select="substring($text, 2)"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>	

