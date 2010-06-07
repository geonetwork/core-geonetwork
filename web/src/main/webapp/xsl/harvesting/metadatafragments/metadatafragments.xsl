<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-metadatafragments">
		<div id="metadatafragments.editPanel">
			<xsl:call-template name="site-metadatafragments"/>
			<div class="dots"/>
			<xsl:call-template name="options-metadatafragments"/>
			<div class="dots"/>
			<xsl:call-template name="content-metadatafragments"/>
			<div class="dots"/>
			<xsl:call-template name="privileges-metadatafragments"/>
			<div class="dots"/>
			<xsl:call-template name="categories-metadatafragments"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site-metadatafragments">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>
	
		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="metadatafragments.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/capabUrl"/></td>
				<td class="padded">
					<input id="metadatafragments.url" class="content" type="text" value="http://" size="30"/>
				</td>
			</tr>
			<!-- TODO : Do we have to set up an account for WxS secured ? -->
			<tr style="display:none;">
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/useAccount"/></td>
				<td class="padded"><input id="metadatafragments.useAccount" type="checkbox"/></td>
			</tr>

			<tr style="display:none;">
				<td/>
				<td>
					<table id="metadatafragments.account">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/username"/></td>
							<td class="padded"><input id="metadatafragments.username" class="content" type="text" value="" size="20"/></td>
						</tr>
		
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/password"/></td>
							<td class="padded"><input id="metadatafragments.password" class="content" type="password" value="" size="20"/></td>
						</tr>
					</table>
				</td>
			</tr>			

			<!-- language -->
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/wxslang"/></td>
				<td class="padded">
					<select id="metadatafragments.lang">
					<!--  TODO loop on languages -->
						<option value="eng">eng</option>
						<option value="fra">fra</option>
					</select>
				</td>
			</tr>

			<!-- query -->
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/metadatafragmentsQuery"/></td>
				<td class="padded">
					<textarea id="metadatafragments.query" class="content" rows="20" cols="80"/>
				</td>
			</tr>
			
			<!-- optional stylesheet to apply to wfs output -->

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/metadataFragmentsStylesheet"/></td>
				<td class="padded">
					<select class="content" id="metadatafragments.stylesheet" size="1"/>
				</td>
			</tr>

			<!-- template to match fragments into -->
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/template"/></td>
				<td class="padded">
					<select class="content" id="metadatafragments.templateId" size="1">
						<xsl:for-each select="/root/gui/templates/record">
							<option value="{id}">
								<xsl:value-of select="name"/>
							</option>
						</xsl:for-each>
					</select>
				</td>
			</tr>

			<!-- categories of records build from template -->
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/recordCategory"/></td>
				<td class="padded">
					<select id="metadatafragments.recordsCategory" class="content"/>
				</td>
			</tr>
		</table>
	</xsl:template>
		

	<!-- ============================================================================================= -->
	
	<xsl:template name="options-metadatafragments">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/every"/></td>
				<td class="padded">
					<input id="metadatafragments.every.days"  class="content" type="text" size="2"/> :
					<input id="metadatafragments.every.hours" class="content" type="text" size="2"/> :
					<input id="metadatafragments.every.mins"  class="content" type="text" size="2"/>
					&#160;
					<xsl:value-of select="/root/gui/harvesting/everySpec"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/oneRun"/></td>
				<td class="padded"><input id="metadatafragments.oneRunOnly" type="checkbox" value=""/></td>
			</tr>			
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="content-metadatafragments">
	<div style="display:none;"> <!-- UNUSED -->
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="metadatafragments.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="metadatafragments.validate" type="checkbox" value=""/></td>
			</tr>
		</table>
	</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="privileges-metadatafragments">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/privileges"/></h1>
		
		<table>
			<tr>
				<td class="padded" valign="top"><xsl:value-of select="/root/gui/harvesting/groups"/></td>
				<td class="padded"><select id="metadatafragments.groups" class="content" size="8" multiple="on"/></td>					
				<td class="padded" valign="top">
					<div align="center">
						<button id="metadatafragments.addGroups" class="content" onclick="harvesting.metadatafragments.addGroupRow()">
							<xsl:value-of select="/root/gui/harvesting/add"/>
						</button>
					</div>
				</td>					
			</tr>
		</table>
		
		<table id="metadatafragments.privileges">
			<tr>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/group"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='0']"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='5']"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='6']"/></b></th>
				<th/>
			</tr>
		</table>
		
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="categories-metadatafragments">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/fragmentCategory"/></h1>
		
		<select id="metadatafragments.categories" class="content"/>
	</xsl:template>
	
	<!-- ============================================================================================= -->
		
</xsl:stylesheet>
