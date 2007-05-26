<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-GN">
		<div id="gn.editPanel">
			<xsl:call-template name="site-GN"/>
			<xsl:call-template name="search-GN"/>
			<xsl:call-template name="options-GN"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site-GN">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="gn.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/host"/></td>
				<td class="padded"><input id="gn.host" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/port"/></td>
				<td class="padded"><input id="gn.port" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/servlet"/></td>
				<td class="padded"><input id="gn.servlet" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/useAccount"/></td>
				<td class="padded"><input id="gn.useAccount" type="checkbox" checked="on"/></td>
			</tr>

			<tr>
				<td/>
				<td>
					<table id="gn.account">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/username"/></td>
							<td class="padded"><input id="gn.username" class="content" type="text" value="" size="20"/></td>
						</tr>
		
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/password"/></td>
							<td class="padded"><input id="gn.password" class="content" type="password" value="" size="20"/></td>
						</tr>
					</table>
				</td>
			</tr>			
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="search-GN">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/search"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/siteName"/></td>
				<td class="padded"><select id="gn.siteId" class="content" size="1"/></td>					
				<td class="padded">
					<button class="content" onclick="harvesting.geonet.addSearchRow()">
						<xsl:value-of select="/root/gui/harvesting/add"/>
					</button>
					&#160;
					<button class="content" onclick="harvesting.geonet.retrieveSites()">
						<xsl:value-of select="/root/gui/harvesting/retrieve"/>
					</button>
				</td>					
			</tr>
		</table>
		
		<div id="gn.searches"/>

	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="options-GN">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/every"/></td>
				<td class="padded">
					<input id="gn.every.days"  class="content" type="text" size="2"/> :
					<input id="gn.every.hours" class="content" type="text" size="2"/> :
					<input id="gn.every.mins"  class="content" type="text" size="2"/>
					&#160;
					<xsl:value-of select="/root/gui/harvesting/everySpec"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/createGroups"/></td>
				<td class="padded"><input id="gn.createGroups" type="checkbox" value=""/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/createCateg"/></td>
				<td class="padded"><input id="gn.createCateg" type="checkbox" value=""/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/oneRun"/></td>
				<td class="padded"><input id="gn.oneRunOnly" type="checkbox" value=""/></td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->
	
</xsl:stylesheet>
