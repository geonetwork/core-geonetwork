<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:include href="../main.xsl"/>

	<!-- ============================================================================================= -->

	<xsl:variable name="style" select="'margin-left:50px;'"/>
	<xsl:variable name="width" select="'70px'"/>
	
	<!-- ============================================================================================= -->
	
	<xsl:template mode="script" match="/">
		<script type="text/javascript" src="{/root/gui/url}/scripts/core/kernel/kernel.js"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/core/gui/gui.js"/>
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
		<xsl:call-template name="removedMetadata"/>
		<xsl:call-template name="ldap"/>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site">		
		<h1 align="left"><xsl:value-of select="/root/gui/config/site"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/name"/></td>
					<td class="padded"><input id="site.name" class="content" type="text" value="" size="30"/></td>
				</tr>
				<tr>
					<td class="padded"><xsl:value-of select="/root/gui/config/organ"/></td>
					<td class="padded"><input id="site.organ" class="content" type="text" value="" size="30"/></td>
				</tr>
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="server">
		<h1 align="left"><xsl:value-of select="/root/gui/config/server"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/host"/></td>
					<td class="padded"><input id="server.host" class="content" type="text" value="" size="30"/></td>
				</tr>
				
				<tr>
					<td class="padded"><xsl:value-of select="/root/gui/config/port"/></td>
					<td class="padded"><input id="server.port" class="content" type="text" value="" size="30"/></td>
				</tr>
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="intranet">
		<h1 align="left"><xsl:value-of select="/root/gui/config/intranet"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/network"/></td>
					<td class="padded"><input id="intranet.network" class="content" type="text" value="" size="30"/></td>
				</tr>
				
				<tr>
					<td class="padded"><xsl:value-of select="/root/gui/config/netmask"/></td>
					<td class="padded"><input id="intranet.netmask" class="content" type="text" value="" size="30"/></td>
				</tr>
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="z3950">
		<h1 align="left"><xsl:value-of select="/root/gui/config/z3950"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/enable"/></td>
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
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="proxy">
		<h1 align="left"><xsl:value-of select="/root/gui/config/proxy"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/use"/></td>
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

							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/username"/></td>
								<td class="padded"><input id="proxy.username" class="content" type="text" value="" size="20"/></td>
							</tr>

							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/password"/></td>
								<td class="padded"><input id="proxy.password" class="content" type="password" value="" size="20"/></td>
							</tr>
						</table>
					</td>
				</tr>			
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="feedback">
		<h1 align="left"><xsl:value-of select="/root/gui/config/feedback"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/email"/></td>
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
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="removedMetadata">
		<h1 align="left"><xsl:value-of select="/root/gui/config/removedMetadata"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded" width="{$width}"><xsl:value-of select="/root/gui/config/dir"/></td>
					<td class="padded"><input id="removedMd.dir" class="content" type="text" value=""/></td>
				</tr>			
			</table>
		</div>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	<!-- === LDAP panels === -->
	<!-- ============================================================================================= -->
	
	<xsl:template name="ldap">
		<h1 align="left"><xsl:value-of select="/root/gui/config/ldap"/></h1>

		<div align="left" style="{$style}">
			<table>
				<tr>
					<td class="padded"><xsl:value-of select="/root/gui/config/use"/></td>
					<td class="padded"><input id="ldap.use" class="content" type="checkbox" value=""/></td>
				</tr>
				<tr>
					<td/>
					<td>
						<table id="ldap.subpanel">
							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/host"/></td>
								<td class="padded"><input id="ldap.host" class="content" type="text" value="" size="20"/></td>
							</tr>
			
							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/port"/></td>
								<td class="padded"><input id="ldap.port" class="content" type="text" value="" size="20"/></td>
							</tr>
							
							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/defProfile"/></td>
								<td class="padded"><xsl:call-template name="ldapDefProfile"/></td>
							</tr>
							
							<!-- distinguished names -->
							
							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/distNames"/></td>
								<td/>
							</tr>
							<tr>
								<td/>
								<td class="padded"><xsl:call-template name="ldapDistNames"/></td>
							</tr>
							
							<!-- user's attributes -->
							
							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/config/userAttribs"/></td>
								<td/>
							</tr>
							<tr>
								<td/>
								<td class="padded"><xsl:call-template name="ldapUserAttribs"/></td>
							</tr>
						</table>
					</td>
				</tr>			
			</table>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->
	
	<xsl:template name="ldapDefProfile">
		<select class="content" size="1" name="profile" id="ldap.defProfile">
			<!--option value="Administrator">
				<xsl:value-of select="/root/gui/strings/Administrator"/>
			</option-->
		
			<!--option value="UserAdmin">
				<xsl:value-of select="/root/gui/strings/UserAdmin"/>
			</option-->
		
			<option value="Reviewer">
				<xsl:value-of select="/root/gui/strings/Reviewer"/>
			</option>
		
			<option value="Editor">
				<xsl:value-of select="/root/gui/strings/Editor"/>
			</option>
			
			<option value="RegisteredUser">
				<xsl:value-of select="/root/gui/strings/RegisteredUser"/>
			</option>
		</select>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="ldapDistNames">
		<table>
			<tr>
				<td class="padded" width="60px"><xsl:value-of select="/root/gui/config/baseDN"/></td>
				<td class="padded"><input id="ldap.baseDN" class="content" type="text" value="" size="20"/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/usersDN"/></td>
				<td class="padded"><input id="ldap.usersDN" class="content" type="text" value="" size="20"/></td>
			</tr>
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="ldapUserAttribs">
		<table>
			<tr>
				<td class="padded" width="60px"><xsl:value-of select="/root/gui/config/name"/></td>
				<td class="padded"><input id="ldap.nameAttr" class="content" type="text" value="" size="20"/></td>
			</tr>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/config/profile"/></td>
				<td class="padded"><input id="ldap.profileAttr" class="content" type="text" value="" size="20"/></td>
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
