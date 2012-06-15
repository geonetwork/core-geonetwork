<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	>
    <xsl:import  href="translate-widget.xsl"/>
    <xsl:import  href="utils.xsl"/>
	<xsl:output method="xml"/>
	
    
	<!-- Return a list of user to be use
		for search and select user actions
	-->
	<xsl:template match="/">
		<ul>
			<xsl:for-each select="/root/response/record">
				<li xlink:href="local://xml.format.get?id={id}">
  			  <displayText><xsl:value-of select="normalize-space(name)"/><xsl:if test="version != ''"><xsl:text> </xsl:text>[<xsl:value-of select="version"/>]</xsl:if></displayText>
  				<valid><xsl:value-of select="normalize-space(translate(validated,$LOWER,$UPPER)) != 'N'"/></valid>
  				<href>local://xml.format.get?id=<xsl:value-of select="id"/></href>
				</li>
			</xsl:for-each>
		</ul>
	</xsl:template>
</xsl:stylesheet>
