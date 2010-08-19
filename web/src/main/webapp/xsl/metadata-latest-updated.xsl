<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:exslt="http://exslt.org/common">

	<xsl:include href="metadata.xsl"/>
	<xsl:include href="utils.xsl"/>
	
	<!--
	latest updates
	-->
	<xsl:template match="/">
		<xsl:if test="/root/gui/latestUpdated/*">
			<div class="geosearchfields">
				<h1 align="left">
					<!--xsl:value-of select="/root/gui/strings/recentAdditions"/--> &#160;&#160;&#160; 
					<a href="{/root/gui/locService}/rss.latest?georss=simplepoint" target="_blank">
						<img style="cursor:hand;cursor:pointer" src="{/root/gui/url}/images/georss.png"
							alt="GeoRSS-GML" title="{/root/gui/strings/georss}" align="top"/>
					</a>
				</h1>
				
				<xsl:for-each select="/root/gui/latestUpdated/*">
					<xsl:variable name="md">
						<xsl:apply-templates mode="brief" select="."/>
					</xsl:variable>
					<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
					<div class="arrow" onClick="gn_showSingleMetadataUUID('{geonet:info/uuid}');" 
						style="cursor:hand;cursor:pointer">
						<xsl:value-of select="$metadata/title"/>
						<br/>
					</div>
				</xsl:for-each>
				
			</div>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>
