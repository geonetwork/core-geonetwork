<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-WD">
		<div id="wd.editPanel">
			<xsl:call-template name="host-WD"/>
			<div class="dots"/>
			<xsl:call-template name="options-WD"/>
			<div class="dots"/>
			<xsl:call-template name="content-WD"/>
			<div class="dots"/>
			<xsl:call-template name="privileges-WD"/>
			<div class="dots"/>
			<xsl:call-template name="categories-WD"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="host-WD">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>
	
		<table border="0">

			<tr>
				<td class="padded" valign="bottom">Subtype</td>
				<td class="padded">
					<select id="wd.subtype" class="content" name="subytpe" size="1">
						<option  value="waf">Web Access Folder(WAF)</option>
						<option  value="webdav">Web DAV Server(WebDAV)</option> 				   
					</select>
				</td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="wd.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/url"/></td>
				<td class="padded"><input id="wd.url" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded" valign="bottom"><xsl:value-of select="/root/gui/harvesting/icon"/></td>
				<td class="padded">
					<select id="wd.icon" class="content" name="icon" size="1"/>
					&#xA0;
					<img id="wd.icon.image" src="" alt="" />
				</td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/useAccount"/></td>
				<td class="padded"><input id="wd.useAccount" type="checkbox" checked="on"/></td>
			</tr>

			<tr>
				<td/>
				<td>
					<table id="wd.account">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/username"/></td>
							<td class="padded"><input id="wd.username" class="content" type="text" value="" size="20"/></td>
						</tr>
		
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/password"/></td>
							<td class="padded"><input id="wd.password" class="content" type="password" value="" size="20"/></td>
						</tr>
					</table>
				</td>
			</tr>			
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="options-WD">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>

		<table border="0">
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/every"/></td>
				<td class="padded">
					<input id="wd.every.days"  class="content" type="text" size="2"/> :
					<input id="wd.every.hours" class="content" type="text" size="2"/> :
					<input id="wd.every.mins"  class="content" type="text" size="2"/>
					&#160;
					<xsl:value-of select="/root/gui/harvesting/everySpec"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/oneRun"/></td>
				<td class="padded"><input id="wd.oneRunOnly" type="checkbox" value=""/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="wd.validate" type="checkbox" value=""/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/recurse"/></td>
				<td class="padded"><input id="wd.recurse" type="checkbox" value=""/></td>
			</tr>

		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="content-WD">
	<div style="display:none;"> <!-- UNUSED -->
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="wd.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="wd.validate" type="checkbox" value=""/></td>
			</tr>
		</table>
	</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="privileges-WD">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/privileges"/></h1>
		
		<table>
			<tr>
				<td class="padded" valign="top"><xsl:value-of select="/root/gui/harvesting/groups"/></td>
				<td class="padded"><select id="wd.groups" class="content" size="8" multiple="on"/></td>					
				<td class="padded" valign="top">
					<div align="center">
						<button class="content" onclick="harvesting.webdav.addGroupRow()">
							<xsl:value-of select="/root/gui/harvesting/add"/>
						</button>
					</div>
				</td>					
			</tr>
		</table>
		
		<table id="wd.privileges">
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

	<xsl:template name="categories-WD">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/categories"/></h1>
		
		<select id="wd.categories" class="content" size="8" multiple="on"/>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
</xsl:stylesheet>
