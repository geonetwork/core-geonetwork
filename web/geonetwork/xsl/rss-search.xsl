<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:xalan= "http://xml.apache.org/xalan"
	xmlns:media="http://search.yahoo.com/mrss/"
	xmlns:georss="http://www.georss.org/georss"
	xmlns:gml="http://www.opengis.net/gml"
	exclude-result-prefixes="geonet xalan">

	<xsl:output method="xml"/>

	<xsl:strip-space elements="*"/>

	<xsl:include href="metadata.xsl"/>
	<xsl:include href="utils.xsl"/>

	<xsl:variable name="siteURL" select="/root/gui/siteURL"/>
	
	<xsl:template match="/root">
		
		<rss version="2.0" xmlns:media="http://search.yahoo.com/mrss/" xmlns:georss="http://www.georss.org/georss" xmlns:gml="http://www.opengis.net/gml">
			<channel>
			
				<title><xsl:value-of select="gui/strings/header_meta/meta[@name='DC.title']/@content"/></title>
				<link><xsl:value-of select="$siteURL"/>/main.home</link>
				<description><xsl:value-of select="gui/strings/header_meta/meta[@name='DC.description']/@content"/></description>
				<language><xsl:value-of select="gui/language"/></language>
				<copyright><xsl:value-of select="gui/strings/copyright2"/></copyright>
				<category>Geographic metadata catalog</category>
				<generator>GeoNetwork Open Source</generator>
				<ttl>30</ttl> <!-- FIXME -->
	
				<xsl:apply-templates mode="item" select="//rssItems/*"/>
				
			</channel>
		</rss>
	</xsl:template>

	<xsl:template match="*" mode="item">
		<item>
			<xsl:variable name="md">
				<xsl:apply-templates mode="brief" select="."/>
			</xsl:variable>
			<xsl:variable name="metadata" select="xalan:nodeset($md)/*[1]"/>
			
			<xsl:variable name="mdURL" select="concat($siteURL, '/metadata.show?id=', geonet:info/id)"/>
			<title><xsl:value-of select="$metadata/title"/></title>
			<link><xsl:value-of select="$mdURL"/></link>
			<category>Geographic metadata catalog</category>
			
			<xsl:choose>
				<xsl:when test="$metadata/image[@type='thumbnail']">
					<xsl:variable name="link" select="$metadata/image[@type='thumbnail']"/>
					<xsl:variable name="thumbnailURL" select="concat(substring($siteURL, 1, string-length($siteURL) - 18), $link)"/>
					<description>
						<xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
						<p>
							<a href="{$mdURL}"><img src="{$thumbnailURL}" align="left" alt="" border="0" width="100"/></a>
						</p>
						<p>
							<xsl:value-of select="$metadata/abstract"/>
						</p>
						<br clear="all"/>
						<xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
					</description>
					<media:content url="{$thumbnailURL}" type="image/gif" width="100"/>
					<media:text><xsl:value-of select="$metadata/abstract"/></media:text>
				</xsl:when>
				<xsl:otherwise>
					<description><xsl:value-of select="$metadata/abstract"/></description>
				</xsl:otherwise>
			</xsl:choose>
			
			<xsl:if test="$metadata/geoBox/southBL!='' and $metadata/geoBox/westBL!='' and $metadata/geoBox/northBL!='' and $metadata/geoBox/eastBL!=''">
			<xsl:choose>

				<xsl:when test="string(/root/request/georss)='simple'">
					<georss:box>
						<xsl:value-of select="$metadata/geoBox/southBL"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="$metadata/geoBox/westBL"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="$metadata/geoBox/northBL"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="$metadata/geoBox/eastBL"/>
					</georss:box>
				</xsl:when>

			 	<xsl:when test="string(/root/request/georss)='simplepoint'">
					<georss:point>
						<xsl:value-of select="(($metadata/geoBox/northBL)+($metadata/geoBox/southBL))*.5"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="(($metadata/geoBox/westBL)+($metadata/geoBox/eastBL))*.5"/>
					</georss:point>
				</xsl:when>

				<xsl:otherwise>
					<georss:where>
						<gml:Envelope>
							<gml:lowerCorner><xsl:value-of select="$metadata/geoBox/southBL"/><xsl:text> </xsl:text><xsl:value-of select="$metadata/geoBox/westBL"/></gml:lowerCorner>
							<gml:upperCorner><xsl:value-of select="$metadata/geoBox/northBL"/><xsl:text> </xsl:text><xsl:value-of select="$metadata/geoBox/eastBL"/></gml:upperCorner>
						</gml:Envelope>
					</georss:where>
				</xsl:otherwise>
			</xsl:choose>
			</xsl:if>			

			
		</item>
	</xsl:template>
	
</xsl:stylesheet>
