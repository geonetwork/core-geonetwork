<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-OAI">
		<div id="harvesterBusy" style="position:absolute;
		                               left:45%;top:45%;display:none;">
			<img src="{/root/gui/url}/images/spinner.gif" alt="busy"/>
		</div>
		<div id="oai.editPanel">
			<xsl:call-template name="site-OAI"/>
			<div class="dots"/>
			<xsl:call-template name="search-OAI"/>
			<div class="dots"/>
			<xsl:call-template name="options-OAI"/>
			<div class="dots"/>
			<xsl:call-template name="content-OAI"/>
			<div class="dots"/>
			<xsl:call-template name="privileges">
				<xsl:with-param name="type" select="'oai'"/>
				<xsl:with-param name="jsId" select="'oaipmh'"/>
			</xsl:call-template>
			<div class="dots"/>
			<xsl:call-template name="categories-OAI"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site-OAI">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>
	
		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="oai.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/url"/></td>
				<td class="padded"><input id="oai.url" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded" valign="bottom"><xsl:value-of select="/root/gui/harvesting/icon"/></td>
				<td class="padded">
					<select id="oai.icon" class="content" name="icon" size="1"/>
					&#xA0;
					<img id="oai.icon.image" src="" alt="" />
				</td>
			</tr>
			
			
			<xsl:call-template name="useAccount">
				<xsl:with-param name="type" select="'oai'"/>
			</xsl:call-template>
			
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="search-OAI">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/search"/></h1>

		<div id="oai.searches"/>
		
		<div>
			<button id="oai.addSearch" class="content" onclick="harvesting.oaipmh.addSearchRow()">
				<xsl:value-of select="/root/gui/harvesting/add"/>
			</button>
			&#xA0;
			<button id="oai.retrInfo" class="content" onclick="harvesting.oaipmh.retrieveInfo()">
				<xsl:value-of select="/root/gui/harvesting/retrieveSetsPrefixes"/>
			</button>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->
	
	<xsl:template name="options-OAI">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>
		<xsl:call-template name="schedule-widget">
			<xsl:with-param name="type">oai</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="content-OAI">
	<div style="display:none;"> <!-- UNUSED -->
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="oai.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="oai.validate" type="checkbox" value=""/></td>
			</tr>
		</table>
	</div>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="categories-OAI">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/categories"/></h1>
		
		<select id="oai.categories" class="content" size="8" multiple="on"/>
	</xsl:template>

    <!-- ============================================================================================= -->

</xsl:stylesheet>
