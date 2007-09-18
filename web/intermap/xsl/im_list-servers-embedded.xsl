<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		
	<xsl:template match="/">
		<div>
			<h1 id = "im_serverList_title" class="padded"><xsl:value-of select="/root/gui/strings/mapServer" /></h1>
						
			<ul id = "im_serverList_list">
				<xsl:apply-templates select="/root/response/mapServers/server" />
				<li><xsl:value-of select="/root/gui/strings/otherWMS" />
					<input type="text" size="40" id="im_wmsservername" class="content"></input>				
				</li>
			</ul>

			<xsl:variable name="action">im_mapServerURL($('im_wmsservername').value);</xsl:variable>			
			<button onclick="{$action}"><xsl:value-of select="/root/gui/strings/connect" /></button>

		</div>
	</xsl:template>
	
	<!-- Servers  -->
	<xsl:template match="/root/response/mapServers/server">
		<xsl:variable name="action">im_mapServerSelected(<xsl:value-of select="@id" />,"<xsl:value-of select="@name" />");</xsl:variable>
		<li id="im_mapserver_{@id}" onclick="{$action}"><a><xsl:value-of select="@name" /></a></li>
	</xsl:template>

</xsl:stylesheet>
