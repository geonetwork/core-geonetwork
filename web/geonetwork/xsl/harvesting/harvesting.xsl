<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:include href="../main.xsl"/>
	<xsl:include href="buttons.xsl"/>
	<xsl:include href="geonet/geonetwork.xsl"/>
	<xsl:include href="geonet20/geonetwork.xsl"/>
	<xsl:include href="webdav/webdav.xsl"/>
	<xsl:include href="csw/csw.xsl"/>
	<xsl:include href="ogcwxs/ogcwxs.xsl"/>
	<xsl:include href="z3950/z3950.xsl"/>
	<xsl:include href="oaipmh/oaipmh.xsl"/>

	<!-- ============================================================================================= -->

	<xsl:template mode="script" match="/">
		<script type="text/javascript" src="{/root/gui/url}/scripts/core/kernel/kernel.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/core/gui/gui.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/harvesting/harvesting.js"/>
		<link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/calendar/calendar-blue2.css" />
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
		<table id="table">
			<tr>
				<th class="padded" style="width:40px;"><b><xsl:value-of select="/root/gui/harvesting/select"/></b></th>
				<th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/harvesting/name"/></b></th>
				<th class="padded" style="width:60px;"><b><xsl:value-of select="/root/gui/harvesting/type"/></b></th>
				<th class="padded" style="width:40px;" align="center"><b><xsl:value-of select="/root/gui/harvesting/status"/></b></th>
				<th class="padded" style="width:40px;" align="center"><b><xsl:value-of select="/root/gui/harvesting/errors"/></b></th>
				<th class="padded" style="width:60px;"><b><xsl:value-of select="/root/gui/harvesting/every"/></b></th>
				<th class="padded" style="width:80px;"><b><xsl:value-of select="/root/gui/harvesting/lastRun"/></b></th>
				<th class="padded" style="width:60px;"><b><xsl:value-of select="/root/gui/harvesting/operation"/></b></th>
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
					<select id="add.type" class="content" name="type" size="1"/>
				</td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel">
		<xsl:call-template name="editPanel-GN"/>
		<xsl:call-template name="editPanel-WD"/>
		<xsl:call-template name="editPanel-GN20"/>
		<xsl:call-template name="editPanel-CSW"/>
		<xsl:call-template name="editPanel-OGCWXS"/>
        <xsl:call-template name="editPanel-Z39"/>
		<xsl:call-template name="editPanel-OAI"/>
	</xsl:template>

	<!-- ============================================================================================= -->
	
</xsl:stylesheet>
