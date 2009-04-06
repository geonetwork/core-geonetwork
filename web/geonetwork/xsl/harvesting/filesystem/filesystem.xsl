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
			<xsl:call-template name="privileges-Filesystem"/>
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

	<xsl:template name="options-Filesystem">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/every"/></td>
				<td class="padded">
					<input id="filesystem.every.days"  class="content" type="text" size="2"/> :
					<input id="filesystem.every.hours" class="content" type="text" size="2"/> :
					<input id="filesystem.every.mins"  class="content" type="text" size="2"/>
					&#160;
					<xsl:value-of select="/root/gui/harvesting/everySpec"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/oneRun"/></td>
				<td class="padded"><input id="filesystem.oneRunOnly" type="checkbox" value=""/></td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="privileges-Filesystem">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/privileges"/></h1>
		
		<table>
			<tr>
				<td class="padded" valign="top"><xsl:value-of select="/root/gui/harvesting/groups"/></td>
				<td class="padded"><select id="filesystem.groups" class="content" size="8" multiple="on"/></td>					
				<td class="padded" valign="top">
					<div align="center">
						<button id="filesystem.addGroups" class="content" onclick="harvesting.filesystem.addGroupRow()">
							<xsl:value-of select="/root/gui/harvesting/add"/>
						</button>
					</div>
				</td>					
			</tr>
		</table>
		
		<table id="filesystem.privileges">
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

	<xsl:template name="categories-Filesystem">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/categories"/></h1>
		
		<select id="filesystem.categories" class="content" size="8" multiple="on"/>
	</xsl:template>
	
	<!-- ============================================================================================= -->	
	
</xsl:stylesheet>
