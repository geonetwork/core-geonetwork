<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:include href="../main.xsl"/>
	<xsl:include href="buttons.xsl"/>
	<xsl:include href="geonet/geonetwork.xsl"/>
	<xsl:include href="geonet20/geonetwork.xsl"/>
	<xsl:include href="webdav/webdav.xsl"/>
	<xsl:include href="csw/csw.xsl"/>
	<xsl:include href="ogcwxs/ogcwxs.xsl"/>
	<xsl:include href="z3950/z3950.xsl"/>
	<xsl:include href="z3950Config/z3950Config.xsl"/>
	<xsl:include href="oaipmh/oaipmh.xsl"/>
	<xsl:include href="arcsde/arcsde.xsl"/>
	<xsl:include href="thredds/thredds.xsl"/>
	<xsl:include href="wfsfeatures/wfsfeatures.xsl"/>
	<xsl:include href="filesystem/filesystem.xsl"/>

	<!-- ============================================================================================= -->

	<xsl:template mode="script" match="/">
        <link rel="stylesheet" type="text/css" href="../../scripts/ext/resources/css/ext-all.css"/>
        <script type="text/javascript" src="{/root/gui/url}/scripts/ext/adapter/ext/ext-base.js"/>
        <script type="text/javascript" src="{/root/gui/url}/scripts/ext/ext-all.js"></script>
		<script type="text/javascript" src="{/root/gui/url}/scripts/core/kernel/kernel.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/editor/metadata-editor.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/core/gui/gui.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/harvesting/harvesting.js"/>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === page content -->
	<!-- ============================================================================================= -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/harvestingManagement"/>

			<xsl:with-param name="content">
				<div id="listPanel" style="display:none;"><xsl:call-template name="listPanel"/></div>
				<div id="addPanel"  style="display:none;"><xsl:call-template name="addPanel"/> </div>
				<div id="editPanel" style="display:none;"><xsl:call-template name="editPanel"/></div>
			</xsl:with-param>

			<xsl:with-param name="buttons">
				<div id="listButtons" style="display:none;"><xsl:call-template name="listButtons"/></div>
				<div id="addButtons"  style="display:none;"><xsl:call-template name="addButtons"/> </div>
				<div id="editButtons" style="display:none;"><xsl:call-template name="editButtons"/></div>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	<!-- === listPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="listPanel">
	  <div style="max-height:600px;overflow:auto;">
		<table id="table">
			<tr>
				<th class="padded" style="width:40px;"><b><xsl:value-of select="/root/gui/harvesting/select"/></b></th>
				<th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/harvesting/name"/></b></th>
				<th class="padded" style="width:60px;"><b><xsl:value-of select="/root/gui/harvesting/type"/></b></th>
				<th class="padded" style="width:40px;" align="center"><b><xsl:value-of select="/root/gui/harvesting/status"/></b></th>
				<th class="padded" style="width:40px;" align="center"><b><xsl:value-of select="/root/gui/harvesting/errors"/></b></th>
				<th class="padded" style="width:60px;"><b><xsl:value-of select="/root/gui/harvesting/at"/></b></th>
				<th class="padded" style="width:60px;"><b><xsl:value-of select="/root/gui/harvesting/every"/></b></th>
				<th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/harvesting/lastRun"/></b></th>
				<th class="padded" style="width:60px;"><b><xsl:value-of select="/root/gui/harvesting/operation"/></b></th>
			</tr>
		</table>
	  </div>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === addPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="addPanel">
		<table>
			<tr>
				<th class="padded"><xsl:value-of select="/root/gui/harvesting/type"/></th>
				<td class="padded">
					<select id="add.type" class="content" name="type" size="1"/>
				</td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel">
    <table class="text-aligned-left">
    <tr>
        <td>
		<xsl:call-template name="editPanel-GN"/>
		<xsl:call-template name="editPanel-WD"/>
		<xsl:call-template name="editPanel-GN20"/>
		<xsl:call-template name="editPanel-CSW"/>
		<xsl:call-template name="editPanel-OGCWXS"/>
		<xsl:call-template name="editPanel-thredds"/>
		<xsl:call-template name="editPanel-wfsfeatures"/>
    <xsl:call-template name="editPanel-Z3950"/>
    <xsl:call-template name="editPanel-Z3950Config"/>
		<xsl:call-template name="editPanel-OAI"/>
		<xsl:call-template name="editPanel-Arcsde"/>
		<xsl:call-template name="editPanel-Filesystem"/>
        </td>
    </tr>
    </table>
    </xsl:template>

	<xsl:template name="privileges">
		<xsl:param name="type"/>
		<xsl:param name="jsId" required="no" select="$type"/>
		
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/privileges"/></h1>
		
		<table>
			<tr>
				<td class="padded" valign="top"><xsl:value-of select="/root/gui/harvesting/groups"/></td>
				<td class="padded"><select id="{$type}.groups" class="content" size="8" multiple="on"/></td>
				<td class="padded" valign="top">
					<div align="center">
						<button id="{$type}.addGroups" class="content" onclick="harvesting.{$jsId}.addGroupRow()">
							<xsl:value-of select="/root/gui/harvesting/add"/>
						</button>
					</div>
				</td>
			</tr>
		</table>
		
		<table id="{$type}.privileges">
			<tr>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/group"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='0']"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='5']"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='6']"/></b></th>
				<th/>
			</tr>
		</table>
		
	</xsl:template>

	<!-- Create elements to define username and password field. -->
	<xsl:template name="useAccount">
		<xsl:param name="type"/>
		
		<tr>
			<td class="padded"><label for="{$type}.useAccount"><xsl:value-of select="/root/gui/harvesting/useAccount"/></label></td>
			<td class="padded"><input id="{$type}.useAccount" type="checkbox" checked="on"/></td>
		</tr>
		<tr>
			<td/>
			<td>
				<table id="{$type}.account">
					<tr>
						<td class="padded"><label for="{$type}.username"><xsl:value-of select="/root/gui/harvesting/username"/></label></td>
						<td class="padded"><input id="{$type}.username" class="content" type="text" value="" size="20"/></td>
					</tr>
					
					<tr>
						<td class="padded"><label for="{$type}.password"><xsl:value-of select="/root/gui/harvesting/password"/></label></td>
						<td class="padded"><input id="{$type}.password" class="content" type="password" value="" size="20"/></td>
					</tr>
				</table>
			</td>
		</tr>
		
	</xsl:template>
	<!-- ============================================================================================= -->
	
</xsl:stylesheet>
