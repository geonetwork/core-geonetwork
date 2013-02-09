<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-Z3950">
		<div id="z3950.editPanel">
			<xsl:call-template name="site-Z3950"/>
			<div class="dots"/>
			<xsl:call-template name="options-Z3950"/>
			<div class="dots"/>
			<xsl:call-template name="content-Z3950"/>
			<div class="dots"/>
			<xsl:call-template name="privileges">
				<xsl:with-param name="type" select="'z3950'"/>
			</xsl:call-template>
			<div class="dots"/>
			<xsl:call-template name="categories-Z3950"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site-Z3950">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>
	
		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="z3950.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<!-- Z3950 servers -->

			<tr>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/z3950repositories"/></th>
				<td class="padded">
					<select class="content" id="z3950.repositories" size="8" multiple="true">
					</select>
				</td>
			</tr>

			<!-- Z3950 query -->

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/z3950query"/></td>
				<td class="padded"><textarea id="z3950.query" class="content" cols="50" rows="5"/></td>
			</tr>			

			<!-- icon for harvested record -->

			<tr>
				<td class="padded" valign="bottom"><xsl:value-of select="/root/gui/harvesting/icon"/></td>
				<td class="padded">
					<select id="z3950.icon" class="content" name="icon" size="1"/>
					&#xA0;
					<img id="z3950.icon.image" src="" alt="" />
				</td>
			</tr>
			
			<!-- turning off account on Z3950.50 search  -->


			<xsl:call-template name="useAccount">
				<xsl:with-param name="type" select="'z3950'"/>
			</xsl:call-template>
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="options-Z3950">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>
		<xsl:call-template name="schedule-widget">
			<xsl:with-param name="type">z3950</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="content-Z3950">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="z3950.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="z3950.validate" type="checkbox" value=""/></td>
			</tr>
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="categories-Z3950">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/categories"/></h1>
		
		<select id="z3950.categories" class="content" size="8" multiple="on"/>
	</xsl:template>
	
</xsl:stylesheet>
