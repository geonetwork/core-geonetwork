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
		<script type="text/javascript" src="{/root/gui/url}/scripts/config/config-view.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/config/config-model.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/config/config.js"/>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === page content -->
	<!-- ============================================================================================= -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/systemConfig"/>

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
		<xsl:call-template name="proxy"/>
		<xsl:call-template name="feedback"/>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site">		
		<h1 align="left"><xsl:value-of select="/root/gui/config/site"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/name"/></td>
				<td class="padded"><input id="site.name" class="content" type="text" value="" size="30"/></td>
			</tr>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/organ"/></td>
				<td class="padded"><input id="site.organ" class="content" type="text" value="" size="30"/></td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="server">
		<h1 align="left"><xsl:value-of select="/root/gui/config/server"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/host"/></td>
				<td class="padded"><input id="server.host" class="content" type="text" value="" size="30"/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/port"/></td>
				<td class="padded"><input id="server.port" class="content" type="text" value="" size="30"/></td>
			</tr>
		</table>		
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="intranet">
		<h1 align="left"><xsl:value-of select="/root/gui/config/intranet"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/network"/></td>
				<td class="padded"><input id="intranet.network" class="content" type="text" value="" size="30"/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/netmask"/></td>
				<td class="padded"><input id="intranet.netmask" class="content" type="text" value="" size="30"/></td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="z3950">
		<h1 align="left"><xsl:value-of select="/root/gui/config/z3950"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/enable"/></td>
				<td class="padded"><input id="z3950.enable" class="content" type="checkbox"/></td>
			</tr>

			<tr>
				<td/>
				<td>
					<table id="z3950.subpanel">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/config/port"/></td>
							<td class="padded"><input id="z3950.port" class="content" type="text" value="" size="20"/></td>
						</tr>
					</table>
				</td>
			</tr>			
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="feedback">
		<h1 align="left"><xsl:value-of select="/root/gui/config/feedback"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/email"/></td>
				<td class="padded"><input id="feedback.email" class="content" type="text" value=""/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/smtpHost"/></td>
				<td class="padded"><input id="feedback.mail.host" class="content" type="text" value=""/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/smtpPort"/></td>
				<td class="padded"><input id="feedback.mail.port" class="content" type="text" value=""/></td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="proxy">
		<h1 align="left"><xsl:value-of select="/root/gui/config/proxy"/></h1>

		<table>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/use"/></td>
				<td class="padded"><input id="proxy.use" class="content" type="checkbox" value=""/></td>
			</tr>
			<tr>
				<td/>
				<td>
					<table id="proxy.subpanel">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/config/host"/></td>
							<td class="padded"><input id="proxy.host" class="content" type="text" value="" size="20"/></td>
						</tr>
		
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/config/port"/></td>
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
		<button class="content" onclick="config.save()">
			<xsl:value-of select="/root/gui/config/save"/>
		</button>
		&#160;
		<button class="content" onclick="config.refresh()">
			<xsl:value-of select="/root/gui/config/refresh"/>
		</button>
	</xsl:template>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
