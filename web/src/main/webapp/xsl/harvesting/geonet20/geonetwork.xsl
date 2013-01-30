<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-GN20">
		<div id="gn20.editPanel">
			<xsl:call-template name="site-GN20"/>
			<xsl:call-template name="search-GN20"/>
			<xsl:call-template name="options-GN20"/>
			<xsl:call-template name="content-GN20"/>
			<p/>
			<span style="color:red"><xsl:value-of select="/root/gui/harvesting/gn20Unsafe"/></span>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site-GN20">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="gn20.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/url"/></td>
				<td class="padded"><input id="gn20.host" class="content" type="text" value="" size="30"/></td>
			</tr>
			
			
			<xsl:call-template name="useAccount">
				<xsl:with-param name="type" select="'gn20'"/>
			</xsl:call-template>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="search-GN20">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/search"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/siteId"/></td>
				<td class="padded"><input id="gn20.siteId" class="content" type="text" size="20"/></td>
				<td class="padded">
					<button class="content" onclick="harvesting.geonet20.addSearchRow()">
						<xsl:value-of select="/root/gui/harvesting/add"/>
					</button>
				</td>					
			</tr>
		</table>
		
		<div id="gn20.searches"/>

	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="options-GN20">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>
		<xsl:call-template name="schedule-widget">
			<xsl:with-param name="type">gn20</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="content-GN20">
	<div>
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
             <!-- UNUSED -->
			<tr style="display:none;">
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="gn20.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="gn20.validate" type="checkbox" value=""/></td>
			</tr>
		</table>
	</div>
	</xsl:template>

    <!-- ============================================================================================= -->

</xsl:stylesheet>
