<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/">
		   <ShortName>GeoNetwork opensource | <xsl:value-of select="//site/organization"/> | <xsl:value-of select="//site/name"/></ShortName>
		   <Description><xsl:value-of select="/root/gui/strings/opensearch"/></Description>
		   <Tags>GeoNetwork Metadata ISO19115 ISO19139 DC FGDC</Tags>
		   <Contact><xsl:value-of select="//feedback/email"/></Contact>
		   <Url type="text/html">
			<xsl:attribute name="template">
			<xsl:value-of select="concat('http://',//server/host,':',//server/port,/root/gui/locService,'/rss.search?')"/>
			<xsl:text>any={searchTerms}&amp;</xsl:text>
			</xsl:attribute>
		    </Url>
		    <Image height="16" width="16" type="image/vnd.microsoft.icon">
			<xsl:value-of select="concat('http://',//server/host,':',//server/port)"/>
			<xsl:value-of select="/root/gui/url"/>/favicon.ico</Image>
		</OpenSearchDescription>
	</xsl:template>
</xsl:stylesheet>
