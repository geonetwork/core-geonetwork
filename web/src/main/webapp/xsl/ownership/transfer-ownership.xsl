<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:include href="../main.xsl"/>

	<!-- ============================================================================================= -->

	<xsl:template mode="script" match="/">
		<script type="text/javascript" src="{/root/gui/url}/static/kernel.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/core/gui/gui.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/ownership/transfer-ownership.js"/>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === page content -->
	<!-- ============================================================================================= -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/transferOwnership"/>

			<xsl:with-param name="content">
				<xsl:call-template name="sourceUser"/>
				<xsl:call-template name="groupList"/>
			</xsl:with-param>

			<xsl:with-param name="buttons">
				<xsl:call-template name="buttons"/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	<!-- === Source user -->
	<!-- ============================================================================================= -->

	<xsl:template name="sourceUser">
		<div align="left">
			<span><xsl:value-of select="/root/gui/ownership/sourceUser"/> </span>

			<select id="source.user" class="content" name="type" size="1">
				<option name=""/>
			</select>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === Group list -->
	<!-- ============================================================================================= -->

	<xsl:template name="groupList">
		<div id="groups" style="margin-top:4px;">		
			<div width="100%" class="dots"/>
		
			<table id="group.list" class="text-aligned-left">
				<tr>
					<th class="padded"><b><xsl:value-of select="/root/gui/ownership/sourceGroup"/></b></th>
					<th class="padded"><b><xsl:value-of select="/root/gui/ownership/targetGroup"/></b></th>
					<th class="padded"><b><xsl:value-of select="/root/gui/ownership/targetUser"/></b></th>
					<th class="padded"><b><xsl:value-of select="/root/gui/ownership/operation"/></b></th>
				</tr>
			</table>			
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === Buttons -->
	<!-- ============================================================================================= -->

	<xsl:template name="buttons">
	</xsl:template>

	<!-- ============================================================================================= -->
	
</xsl:stylesheet>
