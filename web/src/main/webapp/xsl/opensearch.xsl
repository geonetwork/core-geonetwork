<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/" xmlns:geo="http://a9.com/-/opensearch/extensions/geo/1.0/">
		   <ShortName><xsl:value-of select="//site/name"/> (GeoNetwork)</ShortName>
		   <LongName><xsl:value-of select="//site/organization"/> | GeoNetwork opensource</LongName>
		   <Description><xsl:value-of select="/root/gui/strings/opensearch"/></Description>
		   <Tags>GeoNetwork Metadata ISO19115 ISO19139 DC FGDC</Tags>
		   <Contact><xsl:value-of select="//feedback/email"/></Contact>
		   <Url type="application/rss+xml">
			<xsl:attribute name="template">
			<xsl:value-of select="concat('http://',//server/host,':',//server/port,/root/gui/locService,'/rss.search?')"/>
			<xsl:text>any={searchTerms}&amp;hitsPerPage={count?}&amp;bbox={geo:box?}&amp;geometry={geo:geometry?}&amp;name={geo:locationString?}</xsl:text>
			</xsl:attribute>
		   </Url>
		   <Url type="text/html">
			<xsl:attribute name="template">
			<xsl:value-of select="concat(//server/protocol,'://',//server/host,':',//server/port,/root/gui/locService,'/main.search?')"/>
			<xsl:text>any={searchTerms}&amp;hitsPerPage={count?}&amp;bbox={geo:box?}&amp;geometry={geo:geometry?}&amp;name={geo:locationString?}</xsl:text>
			</xsl:attribute>
		   </Url>
		   <Url type="application/x-suggestions+json">
		   	<xsl:attribute name="template">
				<xsl:value-of select="concat(//server/protocol,'://',//server/host,':',//server/port,/root/gui/locService,'/main.search.suggest?')"/>
				<xsl:text>q={searchTerms}</xsl:text>
			</xsl:attribute>
		   </Url>
		   <Image height="16" width="16" type="image/x-icon">
			<xsl:value-of select="concat('http://',//server/host,':',//server/port)"/>
			<xsl:value-of select="/root/gui/url"/>/images/logos/favicon.gif</Image>
		</OpenSearchDescription>
	</xsl:template>
</xsl:stylesheet>
