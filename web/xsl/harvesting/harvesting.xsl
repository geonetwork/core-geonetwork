<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:include href="../main.xsl"/>
	<xsl:include href="buttons.xsl"/>

	<!-- ============================================================================================= -->

	<xsl:template mode="script" match="/">
		<script type="text/javascript" src="{/root/gui/url}/scripts/prototype.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/sarissa.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/geonetwork-ajax.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/xsl-transformer.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/validator.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/harvest-view.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/harvesting.js"/>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === page content -->
	<!-- ============================================================================================= -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/harvestingManagement"/>

			<xsl:with-param name="content">
				<div id="listPanel"><xsl:call-template name="listPanel"/></div>
				<div id="addPanel"> <xsl:call-template name="addPanel"/> </div>
				<div id="editPanel"><xsl:call-template name="editPanel"/></div>
			</xsl:with-param>

			<xsl:with-param name="buttons">
				<div id="listButtons"><xsl:call-template name="listButtons"/></div>
				<div id="addButtons"> <xsl:call-template name="addButtons"/> </div>
				<div id="editButtons"><xsl:call-template name="editButtons"/></div>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	<!-- === listPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="listPanel">
		<table id="table">
			<tr>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/select"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/type"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/status"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/errors"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/every"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/lastRun"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/operation"/></th>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === addPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="addPanel">
		<table>
			<tr>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/type"/></th>
				<td class="padded">
					<select id="add.type" class="content" name="type" size="1">
						<option value="geonetwork"><xsl:value-of select="/root/gui/harvesting/typeGN"/></option>
						<option value="webFolder"><xsl:value-of select="/root/gui/harvesting/typeWF"/></option>
					</select>
				</td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel">
		<input id="edit.id"   type="hidden" value=""/>
		<input id="edit.type" type="hidden" value=""/>
		
		<div id="editPanelGN">
			<xsl:call-template name="site"/>
			<xsl:call-template name="search"/>
			<xsl:call-template name="optionsGN"/>
		</div>
		<div id="editPanelWAF">
			<xsl:call-template name="host"/>
			<xsl:call-template name="optionsWAF"/>
			<xsl:call-template name="privileges"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === editPanel (geonetwork node) -->
	<!-- ============================================================================================= -->

	<xsl:template name="site">
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

	<xsl:template name="search">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/search"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/siteName"/></td>
				<td class="padded"><select id="gn.siteId" class="content" size="1"/></td>					
				<td class="padded">
					<button class="content" onclick="harvesting.addSearch()">
						<xsl:value-of select="/root/gui/harvesting/add"/>
					</button>
					&#160;
					<button class="content" onclick="harvesting.retrieveSites()">
						<xsl:value-of select="/root/gui/harvesting/retrieve"/>
					</button>
				</td>					
			</tr>
		</table>
		
		<div id="gn.searches"/>

	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="optionsGN">
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
	<!-- === editPanel (web folder node) -->
	<!-- ============================================================================================= -->

	<xsl:template name="host">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>
	
		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="waf.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/url"/></td>
				<td class="padded"><input id="waf.url" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/useAccount"/></td>
				<td class="padded"><input id="waf.useAccount" type="checkbox" checked="on"/></td>
			</tr>

			<tr>
				<td/>
				<td>
					<table id="waf.account">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/username"/></td>
							<td class="padded"><input id="waf.username" class="content" type="text" value="" size="20"/></td>
						</tr>
		
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/password"/></td>
							<td class="padded"><input id="waf.password" class="content" type="password" value="" size="20"/></td>
						</tr>
					</table>
				</td>
			</tr>			
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="optionsWAF">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/every"/></td>
				<td class="padded">
					<input id="waf.every.days"  class="content" type="text" size="2"/> :
					<input id="waf.every.hours" class="content" type="text" size="2"/> :
					<input id="waf.every.mins"  class="content" type="text" size="2"/>
					&#160;
					<xsl:value-of select="/root/gui/harvesting/everySpec"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/oneRun"/></td>
				<td class="padded"><input id="waf.oneRunOnly" type="checkbox" value=""/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="waf.validate" type="checkbox" value=""/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/structure"/></td>
				<td class="padded"><input id="waf.structure" type="checkbox" value=""/></td>
			</tr>

		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="privileges">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/privileges"/></h1>
		
		<table>
			<tr>
				<td class="padded" valign="top"><xsl:value-of select="/root/gui/harvesting/groups"/></td>
				<td class="padded"><select id="waf.groups" class="content" size="8" multiple="on"/></td>					
				<td class="padded" valign="top">
					<div align="center">
						<button class="content" onclick="harvesting.addGroup()">
							<xsl:value-of select="/root/gui/harvesting/add"/>
						</button>
						<p/>
						<button class="content" onclick="harvesting.refreshGroups()">
							<xsl:value-of select="/root/gui/harvesting/refresh"/>
						</button>
					</div>
				</td>					
			</tr>
		</table>
		
		<table id="waf.privileges">
			<tr>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/group"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/oper/op[@id='0']"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/oper/op[@id='1']"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/oper/op[@id='3']"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/oper/op[@id='5']"/></th>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/oper/op[@id='6']"/></th>
				<th/>
			</tr>
		</table>
		
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
</xsl:stylesheet>
