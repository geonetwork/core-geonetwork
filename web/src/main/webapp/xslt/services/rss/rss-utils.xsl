<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:gn="http://www.fao.org/geonetwork"
	xmlns:exslt="http://exslt.org/common"
	xmlns:media="http://search.yahoo.com/mrss/"
	xmlns:georss="http://www.georss.org/georss"
	xmlns:gml="http://www.opengis.net/gml"
	exclude-result-prefixes="gn exslt">

  <xsl:include href="../../common/profiles-loader-tpl-brief.xsl"/>

	<!-- Template that generates an item for every metadata record -->
	<xsl:template match="*" mode="item">
	   <item>
			<xsl:variable name="md">
				<xsl:apply-templates mode="brief" select="."/>
			</xsl:variable>
		  
		  
			<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
			<xsl:variable name="mdURL" select="normalize-space(concat($baseURL, '?uuid=', gn:info/uuid))"/>
			<xsl:variable name="thumbnailLink" select="normalize-space($metadata/image[@type='thumbnail'])"/>
			<xsl:variable name="bDynamic" select="gn:info/dynamic" />
			<xsl:variable name="bDownload" select="gn:info/download" />
			<title><xsl:value-of select="$metadata/title"/></title>
			<link><xsl:value-of select="$mdURL"/></link>
			
			<xsl:if test="not(/root/request/mdlinkonly)">
				<xsl:apply-templates mode="link" select="$metadata/link">
					<xsl:with-param name="north" select="$metadata/geoBox/northBL" />
					<xsl:with-param name="south" select="$metadata/geoBox/southBL" />
					<xsl:with-param name="west" select="$metadata/geoBox/westBL" />
					<xsl:with-param name="east" select="$metadata/geoBox/eastBL" />
					<xsl:with-param name="bDynamic" select="$bDynamic" />
					<xsl:with-param name="bDownload" select="$bDownload" />
				</xsl:apply-templates>
			</xsl:if>
			
			<category>Geographic metadata catalog</category>
			
			<description>
				<xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
				
				<p>
					<xsl:if test="string($thumbnailLink)!=''">
						<a href="{$mdURL}"><img src="{$thumbnailLink}" align="left" alt="" border="0" width="100"/></a>
					</xsl:if>
					<xsl:value-of select="$metadata/abstract"/>
					<br />
					<xsl:if test="$bDynamic">
						<xsl:apply-templates select="$metadata/link[contains(@type,'vnd.google-earth.km')][1]" mode="GoogleEarthWMS" >
							<xsl:with-param name="url" select="$baseURL" />
							<xsl:with-param name="viewInGE" select="/root/gui/i18n/viewInGE" />
						</xsl:apply-templates>
					</xsl:if>
					
				</p>
				<br clear="all"/>
				<xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
			</description>
			<xsl:variable name="date" select="gn:info/changeDate"/>
			<xsl:variable name="day" select="substring($date,9,2)" />
			<xsl:variable name="monthnumber" select="substring($date,6,2)" />
			<xsl:variable name="year" select="substring($date,1,4)" />
			<xsl:variable name="time" select="substring($date,12)" />

			<xsl:variable name="month">
			<xsl:choose>
			<xsl:when test="$monthnumber='01'">
			<xsl:value-of select="'Jan'" />
			</xsl:when>
			<xsl:when test="$monthnumber='02'">
			<xsl:value-of select="'Feb'" />
			</xsl:when>
			<xsl:when test="$monthnumber='03'">
			<xsl:value-of select="'Mar'" />
			</xsl:when>
			<xsl:when test="$monthnumber='04'">
			<xsl:value-of select="'Apr'" />
			</xsl:when>
			<xsl:when test="$monthnumber='05'">
			<xsl:value-of select="'May'" />
			</xsl:when>
			<xsl:when test="$monthnumber='06'">
			<xsl:value-of select="'Jun'" />
			</xsl:when>
			<xsl:when test="$monthnumber='07'">
			<xsl:value-of select="'Jul'" />
			</xsl:when>
			<xsl:when test="$monthnumber='08'">
			<xsl:value-of select="'Aug'" />
			</xsl:when>
			<xsl:when test="$monthnumber='09'">
			<xsl:value-of select="'Sep'" />
			</xsl:when>
			<xsl:when test="$monthnumber='10'">
			<xsl:value-of select="'Oct'" />
			</xsl:when>
			<xsl:when test="$monthnumber='11'">
			<xsl:value-of select="'Nov'" />
			</xsl:when>
			<xsl:otherwise>
			<xsl:value-of select="'Dec'" />
			</xsl:otherwise>
			</xsl:choose>
			</xsl:variable>

			<pubDate><xsl:value-of select="concat($day,' ',$month,' ',$year,' ',$time,' EST')"/></pubDate> 
			<guid><xsl:value-of select="$mdURL"/></guid>
			<guid><xsl:value-of select="$mdURL"/></guid>
			<xsl:if test="string($thumbnailLink)!='' and starts-with($thumbnailLink, 'http')">
				<media:content url="{$thumbnailLink}" type="image/gif" width="100"/>
			</xsl:if>
			
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
					<xsl:comment>Bounding box in georss simplepoint format (default) (http://georss.org)</xsl:comment>
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
		
		<xsl:variable name="nameL" select="normalize-space(@name)" />
		
		<xsl:if test="string(@href)!=''">
			<xsl:choose>
				<xsl:when test="@type='application/vnd.ogc.wms_xml' and $bDynamic">
					<xsl:choose>
						<xsl:when test="number($west) and number($south) and number($east) 
							and number($north) and string($nameL)!='' and not(contains(@href,'?'))">
							<!-- The following link is a web map service. 
								There's a hint providing the possible layers available in the service -->
							<xsl:variable name="xyRatio" select="string(number($north - $south) div number($east - $west))" />
							<!-- This is a full GetMap request resulting in a PNG image of 200px wide-->
							<link href="{@href}" type="{@type}" rel="alternate" title="{@title}" gn:layers="{$nameL}" />
							<link href="{concat(@href,'?SERVICE=wms$amp;VERSION=1.1.1&amp;REQUEST=GetMap&amp;BBOX=',
								concat($west,',',$south,',',$east,',',$north),
								'&amp;LAYERS=',$nameL,
								'&amp;SRS=EPSG:4326&amp;WIDTH=200&amp;HEIGHT='
								,string(round(200 * number($xyRatio)))
								,'&amp;FORMAT=image/png'
								,'&amp;TRANSPARENT=TRUE&amp;STYLES=default')}"
								type="image/png" rel="alternate" title="{@title}"
							/>
						</xsl:when>
						<xsl:when test="string($nameL)!='' and not(contains(@href,'?'))">
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
			<a href="{@href}" title="{@title}">
				<img src="{$url}/images/google_earth_link_s.png" alt="{$viewInGE}" title="{$viewInGE}"/>
			</a>
	</xsl:template>
	
</xsl:stylesheet>
