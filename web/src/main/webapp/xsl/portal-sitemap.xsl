<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:geonet="http://www.fao.org/geonetwork">

	<xsl:template match="/root">
		<xsl:variable name="format" select="response/format"/>

        <urlset
         xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
         xmlns:geo="http://www.google.com/geo/schemas/sitemap/1.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">
          <xsl:for-each select="response/record">
            <xsl:variable name="uuid" select="uuid"/>
            <xsl:variable name="schemaid" select="schemaid"/>
            <xsl:variable name="changedate" select="changedate"/>
									
			<xsl:value-of select="$format" />

            <url>
				<loc>
				<xsl:choose>
					<xsl:when test="$format='xml'">
						<xsl:variable name="metadataUrlValue">
							<xsl:call-template name="metadataXmlDocUrl">
								<xsl:with-param name="schemaid" select="$schemaid"/>
								<xsl:with-param name="uuid" select="$uuid"/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:value-of select="/root/gui/env/server/protocol"/>://<xsl:value-of select="/root/gui/env/server/host"/>:<xsl:value-of select="/root/gui/env/server/port"/><xsl:value-of select="/root/gui/locService"/>/<xsl:value-of select="$metadataUrlValue"/>
					</xsl:when>
					
					<xsl:otherwise>
						<xsl:value-of select="/root/gui/env/server/protocol"/>://<xsl:value-of select="/root/gui/env/server/host"/>:<xsl:value-of select="/root/gui/env/server/port"/><xsl:value-of select="/root/gui/url"/>/?uuid=<xsl:value-of select="$uuid"/>
					</xsl:otherwise>
				</xsl:choose>
				</loc>
				<lastmod><xsl:value-of select="$changedate"/></lastmod>
                <geo:geo>
                    <geo:format><xsl:value-of select="$schemaid"/></geo:format>
                </geo:geo>
            </url>
          </xsl:for-each>
        </urlset>
    </xsl:template>

	
	<xsl:template name="metadataXmlDocUrl">
		<xsl:param name="schemaid" />
		<xsl:param name="uuid" />
		
		<xsl:choose>
			<xsl:when test="$schemaid='dublin-core'">dc.xml?uuid=<xsl:value-of select="$uuid"/></xsl:when>

			<xsl:when test="$schemaid='fgdc-std'">fgdc.xml?uuid=<xsl:value-of select="$uuid"/></xsl:when>
			
			<xsl:when test="$schemaid='iso19115'">iso19115to19139.xml?uuid=<xsl:value-of select="$uuid"/></xsl:when>
										
			<xsl:otherwise>iso19139.xml?uuid=<xsl:value-of select="$uuid"/></xsl:otherwise>
		</xsl:choose>			
	</xsl:template>
</xsl:stylesheet>
