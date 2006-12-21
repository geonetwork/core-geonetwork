<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:include href="../main.xsl"/>

	<!-- ============================================================================================= -->

	<xsl:template mode="script" match="/">
		<script type="text/javascript" src="{/root/gui/url}/scripts/prototype.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/sarissa.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/geonetwork-ajax.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/validator.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/options/options.js"/>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === page content -->
	<!-- ============================================================================================= -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/options"/>

			<xsl:with-param name="content">
				<xsl:call-template name="panel"/>
			</xsl:with-param>

			<xsl:with-param name="buttons">
				<xsl:call-template name="buttons"/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	<!-- === Panel -->
	<!-- ============================================================================================= -->

	<xsl:template name="panel">
		<xsl:call-template name="site"/>
		<xsl:call-template name="server"/>
		<xsl:call-template name="intranet"/>
		<xsl:call-template name="z3950"/>
		<xsl:call-template name="feedback"/>
		<xsl:call-template name="proxy"/>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site">		
		<h1 align="left"><xsl:value-of select="/root/gui/options/site"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/options/name"/></td>
				<td class="padded"><input id="site.name" class="content" type="text" value="" size="30"/></td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="server">
		<h1 align="left"><xsl:value-of select="/root/gui/options/server"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/options/host"/></td>
				<td class="padded"><input id="server.host" class="content" type="text" value="" size="30"/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/options/port"/></td>
				<td class="padded"><input id="server.port" class="content" type="text" value="" size="30"/></td>
			</tr>
		</table>		
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="intranet">
		<h1 align="left"><xsl:value-of select="/root/gui/options/intranet"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/options/network"/></td>
				<td class="padded"><input id="intranet.network" class="content" type="text" value="" size="30"/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/options/netmask"/></td>
				<td class="padded"><input id="intranet.netmask" class="content" type="text" value="" size="30"/></td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="z3950">
		<h1 align="left"><xsl:value-of select="/root/gui/options/z3950"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/options/enable"/></td>
				<td class="padded"><input id="z3950.enable" class="content" type="checkbox"/></td>
			</tr>

			<tr>
				<td/>
				<td>
					<table id="z3950.subpanel">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/options/port"/></td>
							<td class="padded"><input id="z3950.port" class="content" type="text" value="" size="20"/></td>
						</tr>
					</table>
				</td>
			</tr>			
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="feedback">
		<h1 align="left"><xsl:value-of select="/root/gui/options/feedback"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/options/email"/></td>
				<td class="padded"><input id="feedback.email" class="content" type="text" value=""/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/options/smtpHost"/></td>
				<td class="padded"><input id="feedback.smtpHost" class="content" type="text" value=""/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/options/smtpPort"/></td>
				<td class="padded"><input id="feedback.smtpPort" class="content" type="text" value=""/></td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="proxy">
		<h1 align="left"><xsl:value-of select="/root/gui/options/proxy"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/options/enable"/></td>
				<td class="padded"><input id="proxy.enable" class="content" type="checkbox" value=""/></td>
			</tr>
			<tr>
				<td/>
				<td>
					<table id="proxy.subpanel">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/options/host"/></td>
							<td class="padded"><input id="proxy.host" class="content" type="text" value="" size="20"/></td>
						</tr>
		
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/options/port"/></td>
							<td class="padded"><input id="proxy.port" class="content" type="text" value="" size="20"/></td>
						</tr>
					</table>
				</td>
			</tr>			
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === Buttons -->
	<!-- ============================================================================================= -->

	<xsl:template name="buttons">
		<button class="content" onclick="load('{/root/gui/locService}/admin')">
			<xsl:value-of select="/root/gui/strings/back"/>
		</button>
		&#160;
		<button class="content" onclick="options.save()">
			<xsl:value-of select="/root/gui/options/save"/>
		</button>
		&#160;
		<button class="content" onclick="options.refresh()">
			<xsl:value-of select="/root/gui/options/refresh"/>
		</button>
	</xsl:template>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
