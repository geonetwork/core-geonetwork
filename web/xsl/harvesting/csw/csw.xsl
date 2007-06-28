<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-CSW">
		<div id="csw.editPanel">
			<xsl:call-template name="site-CSW"/>
			<div class="dots"/>
			<xsl:call-template name="search-CSW"/>
			<div class="dots"/>
			<xsl:call-template name="options-CSW"/>
			<div class="dots"/>
			<xsl:call-template name="privileges-CSW"/>
			<div class="dots"/>
			<xsl:call-template name="categories-CSW"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>
	
		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="csw.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/capabUrl"/></td>
				<td class="padded"><input id="csw.capabUrl" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded" valign="bottom"><xsl:value-of select="/root/gui/harvesting/icon"/></td>
				<td class="padded">
					<select id="csw.icon" class="content" name="icon" size="1"/>
					&#xA0;
					<img id="csw.icon.image" src="" alt="" />
				</td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/useAccount"/></td>
				<td class="padded"><input id="csw.useAccount" type="checkbox" checked="on"/></td>
			</tr>

			<tr>
				<td/>
				<td>
					<table id="csw.account">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/username"/></td>
							<td class="padded"><input id="csw.username" class="content" type="text" value="" size="20"/></td>
						</tr>
		
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/password"/></td>
							<td class="padded"><input id="csw.password" class="content" type="password" value="" size="20"/></td>
						</tr>
					</table>
				</td>
			</tr>			
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="search-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/search"/></h1>
		
		<div id="csw.searches"/>
		
		<button class="content" onclick="harvesting.csw.addSearchRow()">
			<xsl:value-of select="/root/gui/harvesting/add"/>
		</button>
	</xsl:template>

	<!-- ============================================================================================= -->
	
	<xsl:template name="options-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/every"/></td>
				<td class="padded">
					<input id="csw.every.days"  class="content" type="text" size="2"/> :
					<input id="csw.every.hours" class="content" type="text" size="2"/> :
					<input id="csw.every.mins"  class="content" type="text" size="2"/>
					&#160;
					<xsl:value-of select="/root/gui/harvesting/everySpec"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/oneRun"/></td>
				<td class="padded"><input id="csw.oneRunOnly" type="checkbox" value=""/></td>
			</tr>			
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="privileges-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/privileges"/></h1>
		
		<table>
			<tr>
				<td class="padded" valign="top"><xsl:value-of select="/root/gui/harvesting/groups"/></td>
				<td class="padded"><select id="csw.groups" class="content" size="8" multiple="on"/></td>					
				<td class="padded" valign="top">
					<div align="center">
						<button class="content" onclick="harvesting.csw.addGroupRow()">
							<xsl:value-of select="/root/gui/harvesting/add"/>
						</button>
					</div>
				</td>					
			</tr>
		</table>
		
		<table id="csw.privileges">
			<tr>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/group"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/oper/op[@id='0']"/></th>
				<!--th class="padded"><xsl:value-of select="/root/gui/harvesting/oper/op[@id='1']"/></th-->
				<!--th class="padded"><xsl:value-of select="/root/gui/harvesting/oper/op[@id='3']"/></th-->
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/oper/op[@id='5']"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/oper/op[@id='6']"/></th>
				<th/>
			</tr>
		</table>
		
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="categories-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/categories"/></h1>
		
		<select id="csw.categories" class="content" size="8" multiple="on"/>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
</xsl:stylesheet>
