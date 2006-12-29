<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="main.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/admin"/>
			<xsl:with-param name="content">
				<table width="100%">
				
					<!-- metadata services -->
					<xsl:variable name="mdServices">
						<xsl:if test="/root/gui/services/service/@name='metadata.add.form'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/metadata.create.form"><xsl:value-of select="/root/gui/strings/newMetadata"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/newMdDes"/></td>
							</tr>
						</xsl:if>
						
						<xsl:if test="/root/gui/services/service/@name='metadata.xmlinsert.form'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/metadata.xmlinsert.form">XML Metadata Insert</a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/xmlInsert"/></td>
							</tr>
						</xsl:if>
						
						<xsl:if test="/root/gui/services/service/@name='metadata.batchimport.form'">
							<tr>
								<td><a href="{/root/gui/locService}/metadata.batchimport.form">Batch Import</a></td>
								<td>
									<xsl:value-of select="/root/gui/strings/batchImport"/>
								</td>
							</tr>
						</xsl:if>
						
						<xsl:if test="/root/gui/services/service/@name='metadata.searchunused.form'">
							<tr>
								<td><a href="{/root/gui/locService}/metadata.searchunused.form">Search for Unused</a></td>
								<td>
									<xsl:value-of select="/root/gui/strings/searchUnused"/>
								</td>
							</tr>
						</xsl:if>
					</xsl:variable>
					<xsl:if test="$mdServices">
						<tr>
							<td colspan="2"><b><xsl:value-of select="/root/gui/strings/metadata"/></b></td>
						</tr>
						<xsl:copy-of select="$mdServices"/>
						<tr><td class="spacer"/></tr>
					</xsl:if>
					
					<!-- personal info services -->
					<xsl:variable name="persInfoServices">
						<xsl:if test="/root/gui/services/service/@name='user.pwupdate'">
							<tr>
								<td><a href="{/root/gui/locService}/user.pwedit?id={/root/gui/session/userId}"><xsl:value-of select="/root/gui/strings/userPw"/></a></td>
								<td><xsl:value-of select="/root/gui/strings/userPwDes"/></td>
							</tr>
						</xsl:if>
						<xsl:if test="/root/gui/services/service/@name='user.infoupdate'">
							<tr>
								<td><a href="{/root/gui/locService}/user.infoedit?id={/root/gui/session/userId}"><xsl:value-of select="/root/gui/strings/userInfo"/></a></td>
								<td><xsl:value-of select="/root/gui/strings/userInfoDes"/></td>
							</tr>
						</xsl:if>
					</xsl:variable>
					<xsl:if test="$persInfoServices">
						<tr>
							<td colspan="2"><b><xsl:value-of select="/root/gui/strings/persInfo"/></b></td>
						</tr>
						<xsl:copy-of select="$persInfoServices"/>
						<tr><td class="spacer"/></tr>
					</xsl:if>
					
					<!-- administration services -->
					<xsl:variable name="adminServices">
						<xsl:if test="/root/gui/services/service/@name='user.update'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/user.list"><xsl:value-of select="/root/gui/strings/userManagement"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/userManDes"/></td>
							</tr>
						</xsl:if>
						
						<xsl:if test="/root/gui/services/service/@name='group.update'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/group.list"><xsl:value-of select="/root/gui/strings/groupManagement"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/groupManDes"/></td>
							</tr>
						</xsl:if>
						
						<xsl:if test="/root/gui/services/service/@name='category.update'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/category.list"><xsl:value-of select="/root/gui/strings/categoryManagement"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/categoryManDes"/></td>
							</tr>
						</xsl:if>

						<xsl:if test="/root/gui/services/service/@name='xml.harvesting.update'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/harvesting"><xsl:value-of select="/root/gui/strings/harvestingManagement"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/harvestingManDes"/></td>
							</tr>
						</xsl:if>

						<xsl:if test="/root/gui/services/service/@name='config'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/config"><xsl:value-of select="/root/gui/strings/systemConfig"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/systemConfigDes"/></td>
							</tr>
						</xsl:if>
					</xsl:variable>
					<xsl:if test="$adminServices">
						<tr>
							<td colspan="2"><b><xsl:value-of select="/root/gui/strings/admin"/></b></td>
						</tr>
						<xsl:copy-of select="$adminServices"/>
						<tr><td class="spacer"/></tr>
					</xsl:if>
					
				</table>
				<p/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
</xsl:stylesheet>
