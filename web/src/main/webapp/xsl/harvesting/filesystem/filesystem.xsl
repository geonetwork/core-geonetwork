<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-Filesystem">
		<div id="filesystem.editPanel">
			<xsl:call-template name="site-Filesystem"/>
			<div class="dots"/>
			<xsl:call-template name="options-Filesystem"/>
			<div class="dots"/>
			<xsl:call-template name="content-Filesystem"/>
			<div class="dots"/>
			<xsl:call-template name="privileges">
				<xsl:with-param name="type" select="'filesystem'"/>
			</xsl:call-template>
			<div class="dots"/>
			<xsl:call-template name="categories-Filesystem"/>
			<p/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site-Filesystem">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="filesystem.name" class="content" type="text" value="" size="200"/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/directoryname"/></td>
				<td class="padded"><input id="filesystem.directoryname" class="content" type="text" value="" size="300"/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/recurse"/></td>
				<td class="padded"><input id="filesystem.recurse" type="checkbox" checked="on"/></td>
			</tr>	
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/nodelete"/></td>
				<td class="padded"><input id="filesystem.nodelete" type="checkbox" checked="on"/></td>
			</tr>			
			<tr>
				<td class="padded" valign="bottom"><xsl:value-of select="/root/gui/harvesting/icon"/></td>
				<td class="padded">
					<select id="filesystem.icon" class="content" name="icon" size="1"/>
					&#xA0;
					<img id="filesystem.icon.image" src="" alt="" />
				</td>
			</tr>			
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="content-Filesystem">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="filesystem.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="filesystem.validate" type="checkbox" value=""/></td>
			</tr>
		</table>
	</xsl:template>


	<!-- ============================================================================================= -->

	<xsl:template name="options-Filesystem">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>
		<xsl:call-template name="schedule-widget">
			<xsl:with-param name="type">filesystem</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="categories-Filesystem">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/categories"/></h1>
		
		<select id="filesystem.categories" class="content" size="8" multiple="on"/>
	</xsl:template>

    <!-- ============================================================================================= -->

</xsl:stylesheet>
