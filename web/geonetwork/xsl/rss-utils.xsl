<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:xalan= "http://xml.apache.org/xalan"
	xmlns:media="http://search.yahoo.com/mrss/"
	xmlns:georss="http://www.georss.org/georss"
	xmlns:gml="http://www.opengis.net/gml"
	exclude-result-prefixes="geonet xalan">

	<xsl:include href="metadata.xsl"/>
	<xsl:include href="utils.xsl"/>

	<!-- Template that generates an item for every metadata record -->
	<xsl:template match="*" mode="item">
		<xsl:param name="siteURL" />
		<item>
			<xsl:variable name="md">
				<xsl:apply-templates mode="brief" select="."/>
			</xsl:variable>
			<xsl:variable name="metadata" select="xalan:nodeset($md)/*[1]"/>
			<xsl:variable name="mdURL" select="normalize-space(concat($siteURL, '/metadata.show?id=', geonet:info/id))"/>
			<xsl:variable name="thumbnailLink" select="normalize-space($metadata/image[@type='thumbnail'])"/>
			<xsl:variable name="bDynamic" select="geonet:info/dynamic" />
			<xsl:variable name="bDownload" select="geonet:info/download" />
<!--			<code><xsl:copy-of select="$metadata"/></code> -->
			
			<title><xsl:value-of select="$metadata/title"/></title>
			<link><xsl:value-of select="$mdURL"/></link>
			
			<xsl:apply-templates mode="link" select="$metadata/link">
				<xsl:with-param name="north" select="$metadata/geoBox/northBL" />
				<xsl:with-param name="south" select="$metadata/geoBox/southBL" />
				<xsl:with-param name="west" select="$metadata/geoBox/westBL" />
				<xsl:with-param name="east" select="$metadata/geoBox/eastBL" />
				<xsl:with-param name="bDynamic" select="$bDynamic" />
				<xsl:with-param name="bDownload" select="$bDownload" />
			</xsl:apply-templates>
			
			<category>Geographic metadata catalog</category>
			
			<description>
				<xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
				<xsl:if test="string($thumbnailLink)!=''">
					<p>
						<a href="{$mdURL}"><img src="{$thumbnailLink}" align="left" alt="" border="0" width="100"/></a>
					</p>
				</xsl:if>
				<p>
					<xsl:value-of select="$metadata/abstract"/>
					<br />
					<xsl:if test="not(contains($mdURL,'localhost')) and not(contains($mdURL,'127.0.0.1'))">
						<a href="http://del.icio.us/post?url={$mdURL}&amp;title={$metadata/title}">
							<img src="{/root/gui/url}/images/delicious.gif" 
								alt="Bookmark on Delicious" title="Bookmark on Delicious" 
								style="border: 0px solid;padding:2px;"/>
						</a> 
						<a href="http://digg.com/submit?url={$mdURL}&amp;title={$metadata/title}">
							<img src="{/root/gui/url}/images/digg.gif" 
								alt="Bookmark on Digg" title="Bookmark on Digg" 
								style="border: 0px solid;padding:2px;"/>
						</a> 
						<a href="http://www.facebook.com/sharer.php?u={$mdURL}">
							<img src="{/root/gui/url}/images/facebook.gif" 
								alt="Bookmark on Facebook" title="Bookmark on Facebook" 
								style="border: 0px solid;padding:2px;"/>
						</a> 
						<a href="http://www.stumbleupon.com/submit?url={$mdURL}&amp;title={$metadata/title}">
							<img src="{/root/gui/url}/images/stumbleupon.gif" 
								alt="Bookmark on StumbleUpon" title="Bookmark on StumbleUpon" 
								style="border: 0px solid;padding:2px;"/>
						</a> 
					</xsl:if>
					<xsl:if test="$bDynamic">
						<xsl:apply-templates select="$metadata/link[contains(@type,'vnd.google-earth.km')][1]" mode="GoogleEarthWMS" >
							<xsl:with-param name="url" select="/root/gui/url" />
							<xsl:with-param name="viewInGE" select="/root/gui/strings/viewInGE" />
						</xsl:apply-templates>
					</xsl:if>
					
				</p>
				<br clear="all"/>
				<xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
			</description>
			<xsl:if test="string($thumbnailLink)!=''">
				<media:content url="{$thumbnailLink}" type="image/gif" width="100"/>
			</xsl:if>
			<media:text><xsl:value-of select="$metadata/abstract"/></media:text>
			
			<xsl:apply-templates select="$metadata/geoBox" mode="geobox">
				<xsl:with-param name="rssFormat" select="/root/request/georss" />
			</xsl:apply-templates>
		</item>
	</xsl:template>
	
	<!-- Template to generate the georss bounding box -->
	<xsl:template match="geoBox" mode="geobox">
		<xsl:param name="rssFormat" />
		<xsl:if test="southBL!='' and westBL!='' and northBL!='' and eastBL!=''">
			<xsl:choose>
				<xsl:when test="string($rssFormat)='simple'">
					<xsl:comment>Bounding box in georss simple format (http://georss.org)</xsl:comment>
					<georss:box>
						<xsl:value-of select="southBL"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="westBL"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="northBL"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="eastBL"/>
					</georss:box>
				</xsl:when>
				<xsl:when test="string($rssFormat)='simplepoint'">
					<xsl:comment>Bounding box in georss simplepoint format (http://georss.org)</xsl:comment>
					<georss:point>
						<xsl:value-of select="((northBL)+(southBL))*.5"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="((westBL)+(eastBL))*.5"/>
					</georss:point>
				</xsl:when>
				<xsl:otherwise>
					<xsl:comment>Bounding box in georss GML format (http://georss.org)</xsl:comment>
					<georss:where>
						<gml:Envelope>
							<gml:lowerCorner>
								<xsl:value-of select="southBL"/>
								<xsl:text> </xsl:text>
								<xsl:value-of select="westBL"/>
							</gml:lowerCorner>
							<gml:upperCorner>
								<xsl:value-of select="northBL"/>
								<xsl:text> </xsl:text>
								<xsl:value-of select="eastBL"/>
							</gml:upperCorner>
						</gml:Envelope>
					</georss:where>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>			
	</xsl:template>
	
	<!-- Template to generate the links in the RSS XML document -->
	<xsl:template match="link" mode="link">
		<xsl:param name="north" />
		<xsl:param name="south" />
		<xsl:param name="west" />
		<xsl:param name="east" />
		<xsl:param name="bDynamic" />
		<xsl:param name="bDownload" />
		
		<xsl:variable name="nameL" select="@name" />
		
		<xsl:if test="string(@href)!=''">
			<xsl:choose>
				<xsl:when test="@type='application/vnd.ogc.wms_xml' and $bDynamic">
					<xsl:choose>
						<xsl:when test="number($west) and number($south) and number($east) 
							and number($north) and string(normalize-space($nameL))!='' and not(contains(@href,'?'))">
							<!-- The following link is a web map service with variable parameters encoded following 
								so-called URI Templates (http://bitworking.org/projects/URI-Templates/) 
								also used in OpenSearch -->
							<xsl:variable name="xyRatio" select="string(number($north - $south) div number($east - $west))" />
							<link href="{concat(@href,'?SERVICE=wms$amp;VERSION=1.1.1&amp;REQUEST=GetMap&amp;BBOX={geo:box='
								,$west,',',$south,',',$east,',',$north
								,'}&amp;LAYERS={ogc:layer=',$nameL
								,'}&amp;SRS=EPSG:4326&amp;WIDTH=200&amp;HEIGHT='
								,string(round(200 * number($xyRatio)))
								,'&amp;FORMAT=image/png'
								,'&amp;TRANSPARENT=TRUE&amp;STYLES=default')}"
								type="{@type}" rel="alternate" title="{@title}"/>
						</xsl:when>
						<xsl:when test="string(normalize-space($nameL))!='' and not(contains(@href,'?'))">
							<!-- The following link is a GetCapabilities request to an OGC Web Map Server 
								(http://opengeospatial.org) -->
							<link href="{concat(@href,'?SERVICE=wms&amp;VERSION=1.1.1&amp;REQUEST=GetCapabilities')}"
								type="{@type}" rel="alternate" title="{@title}"/>
						</xsl:when>
						<xsl:when test="contains(@href,'?')">
							<link href="{@href}" type="{@type}" rel="alternate" title="{@title}"/>
						</xsl:when>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="@type='application/vnd.google-earth.kml+xml' and $bDynamic">
					<!-- The following is a link to a KML document that will open in a.o. Google Earth
						(http://earth.google.com) -->
					<link href="{@href}" type="{@type}" rel="alternate" title="{@title}"/>
				</xsl:when>
				<xsl:otherwise>
					<link href="{@href}" type="{@type}" rel="alternate" title="{@title}"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	
	<!-- Create a Google Earth icon with a link to the KML service -->
	<xsl:template match="*" mode="GoogleEarthWMS">
		<xsl:param name="url" />
		<xsl:param name="viewInGE" />
			Open with: 
			<a href="{@href}" title="{@title}">
				<img src="{$url}/images/google_earth_link_s.png" alt="{$viewInGE}" title="{$viewInGE}" 
				style="border: 0px solid;"/>
			</a>
	</xsl:template>
	
</xsl:stylesheet>
