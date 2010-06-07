<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
<xsl:template name="banner">
	<table width="100%" border="0" cellpadding="0" cellspacing="0">
		<tr class="im_banner">
			<td class="im_banner">
				<img src="{/root/gui/url}/images/header-left.gif" alt="InterMap opensource" align="top" />
			</td>
			<td align="right" class="im_banner">
				<img src="{/root/gui/url}/images/header-right.gif" alt="World picture" align="top" />
			</td>
		</tr>
<!--		<tr class="banner">
			<td class="banner-menu" colspan="2">
				<table width="850" border="0" cellpadding="0" cellspacing="0" id="nav">
					<tr>
						<td class="fullextent"><a id="fullExtentButton" href="javascript:fullExtentButtonListener()">Full extent</a></td>
						<td class="aoi"><a id="oaiButton" href="javascript:setTool('aoi')">Area of interest</a></td> <!- - aoi = area of interest - ->
						<td class="zoomin"><a href="javascript:setTool('zoomin');">Zoom in</a></td>
						<td class="zoomout"><a href="javascript:setTool('zoomout');">Zoom out</a></td>
						<td class="zoomToSelected"><a href="javascript:zoomToLayer(activeLayerId)">Zoom to selected</a></td>
						<td class="pan"><a href="javascript:setTool('pan');">Pan</a></td>
						<td class="identify"><a href="javascript:setTool('identify');">Identify</a></td>
						<td><a href="javascript:print();">Print</a></td>
						<td><a href="javascript:resizeImage()">Bigger/Smaller map</a></td>
						<td><a href="{/root/gui/locService}/mapServers.listServers">Add layer</a></td>
						<td class="reset"><a href="{/root/gui/locService}/map.reset">Reset</a></td>
					</tr>
				</table>
			</td>
		</tr>
-->	</table>
</xsl:template>
	
</xsl:stylesheet>
