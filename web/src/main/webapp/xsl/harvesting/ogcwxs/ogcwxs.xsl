<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:include href="../schedule-widget.xsl"/>
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-OGCWXS">
		<div id="ogcwxs.editPanel">
			<xsl:call-template name="site-OGCWXS"/>
			<div class="dots"/>
			<xsl:call-template name="options-OGCWXS"/>
			<div class="dots"/>
			<xsl:call-template name="content-OGCWXS"/>
			<div class="dots"/>
			<xsl:call-template name="privileges">
				<xsl:with-param name="type" select="'ogcwxs'"/>
			</xsl:call-template>
			<div class="dots"/>
			<xsl:call-template name="categories-OGCWXS"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site-OGCWXS">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>
	
		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="ogcwxs.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/wxstype"/></td>
				<td class="padded">
					<select id="ogcwxs.ogctype">
					<xsl:for-each select="/root/gui/harvesting/wxstypes/type">
						<option>
							<xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute>
							<xsl:value-of select="."/>
						</option>
					</xsl:for-each>
					</select>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/capabUrl"/></td>
				<td class="padded">
					<input id="ogcwxs.capabUrl" class="content" type="text" value="http://" size="30"/>
				</td>
			</tr>
			
			<xsl:call-template name="useAccount">
				<xsl:with-param name="type" select="'ogcwxs'"/>
			</xsl:call-template>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/wxslang"/></td>
				<td class="padded">
					<select id="ogcwxs.lang">
					<!--  TODO loop on languages -->
						<option value="eng">eng</option>
						<option value="fre">fre</option>
					</select>
				</td>
			</tr>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/strings/theme"/></td>
				<td class="padded">
					<select id="ogcwxs.topic">
					<!--  TODO loop on topic -->
						<option value=""></option>
						<option value="farming">farming</option>
						<option value="biota">biota</option>
						<option value="boundaries">boundaries</option>
						<option value="climatologyMeteorologyAtmosphere">climatologyMeteorologyAtmosphere</option>
						<option value="economy">economy</option>
						<option value="elevation">elevation</option>
						<option value="environment">environment</option>
						<option value="geoscientificInformation">geoscientificInformation</option>
						<option value="health">health</option>
						<option value="imageryBaseMapsEarthCover">imageryBaseMapsEarthCover</option>
						<option value="intelligenceMilitary">intelligenceMilitary</option>
						<option value="location">location</option>
						<option value="inlandWaters">inlandWaters</option>
						<option value="oceans">oceans</option>
						<option value="planningCadastre">planningCadastre</option>
						<option value="society">society</option>
						<option value="structure">structure</option>
						<option value="transportation">transportation</option>
						<option value="utilitiesCommunication">utilitiesCommunication</option>						
					</select>
				</td>
			</tr>


			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/wxsImp"/></td>
				<td class="padded">
				<xsl:value-of select="/root/gui/harvesting/wxs119Only"/><br/>
				<input type="checkbox" id="ogcwxs.useLayer" name="ogcwxs.useLayer" value=""/><label for="ogcwxs.useLayer"><xsl:value-of select="/root/gui/harvesting/wxs139"/></label><br/>
				<input type="checkbox" id="ogcwxs.useLayerMd" name="ogcwxs.useLayerMd" value=""/><label for="ogcwxs.useLayerMd"><xsl:value-of select="/root/gui/harvesting/wxs139Layer"/></label><br/>
				<input type="checkbox" id="ogcwxs.createThumbnails" name="ogcwxs.createThumbnails" value=""/><label for="ogcwxs.createThumbnails"><xsl:value-of select="/root/gui/harvesting/createThumbnails"/></label>
				</td>
			</tr>


			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/wxsOutputSchema"/></td>
				<td class="padded">
					<select id="ogcwxs.outputSchema"/>
				</td>
			</tr>

			<tr>
				<td class="padded" valign="bottom"><xsl:value-of select="/root/gui/harvesting/icon"/></td>
				<td class="padded">
					<select id="ogcwxs.icon" class="content" name="icon" size="1"/>
					&#xA0;
					<img id="ogcwxs.icon.image" src="" alt="" />
				</td>
			</tr>
			

		</table>
	</xsl:template>
		

	<!-- ============================================================================================= -->
	
	<xsl:template name="options-OGCWXS">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>

		<xsl:call-template name="schedule-widget">
			<xsl:with-param name="type">ogcwxs</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="content-OGCWXS">
	<div style="display:none;"> <!-- UNUSED -->
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="ogcwxs.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="ogcwxs.validate" type="checkbox" value=""/></td>
			</tr>
		</table>
	</div>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="categories-OGCWXS">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/serviceCategory"/></h1>
		
		<select id="ogcwxs.categories" class="content"/>

		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/datasetCategory"/></h1>
		
		<select id="ogcwxs.datasetCategory" class="content"/>

	</xsl:template>

    <!-- ============================================================================================= -->

</xsl:stylesheet>
