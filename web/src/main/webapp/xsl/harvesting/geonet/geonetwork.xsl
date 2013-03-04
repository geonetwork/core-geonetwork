<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-GN">
		<div id="gn.editPanel">
			<xsl:call-template name="site-GN"/>
			<div class="dots"/>
			<xsl:call-template name="search-GN"/>
			<div class="dots"/>
			<xsl:call-template name="options-GN"/>
			<div class="dots"/>
			<xsl:call-template name="content-GN"/>
			<div class="dots"/>
			<xsl:call-template name="privileges-GN"/>
			<div class="dots"/>
			<xsl:call-template name="categories-GN"/>
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
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/url"/></td>
				<td class="padded"><input id="gn.host" class="content" type="text" value="" size="30"/></td>
			</tr>

		  <tr>
		    <td class="padded"><xsl:value-of select="/root/gui/harvesting/createRemoteCategory"/></td>
		    <td class="padded"><input id="gn.createRemoteCategory" type="checkbox"/></td>
		  </tr>
		  
		  <tr>
		    <td class="padded"><xsl:value-of select="/root/gui/harvesting/mefFormatFull"/></td>
		    <td class="padded"><input id="gn.mefFormatFull" type="checkbox"/></td>
		  </tr>
		  
		    <tr>
		        <td class="padded"><xsl:value-of select="/root/gui/harvesting/xslfilter"/></td>
		        <td class="padded"><input id="gn.xslfilter" class="content" type="text" value="" size="30"/></td>
		    </tr>
			
			<xsl:call-template name="useAccount">
				<xsl:with-param name="type" select="'gn'"/>
			</xsl:call-template>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="search-GN">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/search"/></h1>

		<div id="gn.searches"/>
		
		<div style="margin:4px;">
			<button id="gn.addSearch" class="content" onclick="harvesting.geonet.addSearchRow()">
				<xsl:value-of select="/root/gui/harvesting/add"/>
			</button>
			&#160;
			<button id="gn.retrieveSources" class="content" onclick="harvesting.geonet.retrieveSources()">
				<xsl:value-of select="/root/gui/harvesting/retrieveSources"/>
			</button>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="options-GN">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>
		<xsl:call-template name="schedule-widget">
			<xsl:with-param name="type">gn</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="content-GN">
	<div>
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
			<tr style="display:none;"> <!-- UNUSED -->
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="gn.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="gn.validate" type="checkbox" value=""/></td>
			</tr>
		</table>
	</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="privileges-GN">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/privileges"/></h1>
			
		<table id="gn.groups">
			<tr class="policyGroup">
				<th style="width:150px;"><b><xsl:value-of select="/root/gui/harvesting/remoteGroup"/></b></th>
				<th><b><xsl:value-of select="/root/gui/harvesting/copyPolicy"/></b></th>
			</tr>			
		</table>
				
		<div style="margin:4px;">
			<button  id="gn.retrieveGroups" class="content" onclick="harvesting.geonet.retrieveGroups()">
				<xsl:value-of select="/root/gui/harvesting/retrieveGroups"/>
			</button>
		</div>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="categories-GN">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/categories"/></h1>
		
		<select id="gn.categories" class="content" size="8" multiple="multiple"/>
	</xsl:template>

    <!-- ============================================================================================= -->

</xsl:stylesheet>
