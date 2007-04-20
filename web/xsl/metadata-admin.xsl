<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	
	<xsl:include href="main.xsl"/>

	<xsl:template mode="script" match="/">
		<script type="text/javascript" language="JavaScript">			
			function setAll(id)
			{
				var list = $(id).getElementsByTagName('input');
			
				for (var i=0; i &lt; list.length; i++)
					list[i].checked = true;
			}

			function clearAll(id)
			{
				var list = $(id).getElementsByTagName('input');
			
				for (var i=0; i &lt; list.length; i++)
					list[i].checked = false;
			}
		</script>
	</xsl:template>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/privileges"/>
			<xsl:with-param name="content">
			
				<xsl:variable name="lang" select="/root/gui/language"/>

				<form name="update" accept-charset="UTF-8" action="{/root/gui/locService}/metadata.admin" method="post">
					<input name="id" type="hidden" value="{/root/response/id}"/>
					<table>
						<tr>
							<th class="padded"><xsl:value-of select="/root/gui/strings/groups"/></th>
							<!-- loop on all operations, add edit, notify and admin privileges to the end -->
							<xsl:for-each select="/root/response/operations/record">
								<xsl:if test="id!='2' and id!='4' and id!='3'">
									<th class="padded-center"><xsl:value-of select="label/child::*[name() = $lang]"/></th>
								</xsl:if>
							</xsl:for-each>
							<xsl:for-each select="/root/response/operations/record">
								<xsl:if test="id='2' or id='4' or id='3'">
									<th class="padded-center"><xsl:value-of select="label/child::*[name() = $lang]"/></th>
								</xsl:if>
							</xsl:for-each>
							<th width="70"/>
							<th/>
						</tr>
			
						<!-- loop on 'All' and 'Internal' groups -->
						<xsl:for-each select="/root/response/groups/group">
							<xsl:if test="id='0' or id='1'">
								<xsl:variable name="groupId" select="id"/>
								<tr id="row.{id}">
									<td class="padded"><xsl:value-of select="label/child::*[name() = $lang]"/></td>
									
									<!-- loop on all operations,  edit, notify and admin privileges are hidden-->
									<xsl:for-each select="oper">
										<xsl:if test="id!=2 and id!=4 and id!='3'">
											<td class="padded" align="center" width="80">
												<input type="checkbox" name="_{$groupId}_{id}">
													<xsl:if test="on">
														<xsl:attribute name="checked"/>
													</xsl:if>
												</input>
											</td>
										</xsl:if>
									</xsl:for-each>

									<!-- fill empty slots -->
									<xsl:for-each select="oper">
										<xsl:if test="id='2' or id='4' or id='3'">
											<td/>
										</xsl:if>
									</xsl:for-each>

									<!-- 'set all' button -->

									<td>
										<button class="content" onclick="setAll('row.{id}'); return false;">
											<xsl:value-of select="/root/gui/strings/setAll"/>
										</button>
									</td>

									<!-- 'clear all' button -->

									<td>
										<button class="content" onclick="clearAll('row.{id}'); return false;">
											<xsl:value-of select="/root/gui/strings/clearAll"/>
										</button>
									</td>
								</tr>
							</xsl:if>
						</xsl:for-each>
						<tr>
							<td class="dots"/>
							<xsl:for-each select="/root/response/operations/record">
								<td class="dots"/>
							</xsl:for-each>
							<td class="dots"/>
							<td class="dots"/>
						</tr>
			
						<!-- loop on other groups except -->
						<xsl:for-each select="/root/response/groups/group">
							<xsl:if test="id!='0' and id!='1'">
								<xsl:variable name="groupId" select="id"/>
								<tr id="row.{id}">
									<td class="padded"><xsl:value-of select="label/child::*[name() = $lang]"/></td>
									
									<!-- loop on all operations, add edit, notify and admin privileges to the end -->
									<xsl:for-each select="oper">
										<xsl:if test="id!='2' and id!='4' and id!='3'">
											<td class="padded" align="center" width="80">
												<input type="checkbox" name="_{$groupId}_{id}">
													<xsl:if test="on">
														<xsl:attribute name="checked"/>
													</xsl:if>
												</input>
											</td>
										</xsl:if>
									</xsl:for-each>
									<xsl:for-each select="oper">
										<xsl:if test="id='2' or id='4' or id='3'">
											<td class="padded" align="center" width="80">
												<input type="checkbox" name="_{$groupId}_{id}">
													<xsl:if test="on">
														<xsl:attribute name="checked"/>
													</xsl:if>
												</input>
											</td>
										</xsl:if>
									</xsl:for-each>

									<!-- 'set all' button -->

									<td>
										<button class="content" onclick="setAll('row.{id}'); return false;">
											<xsl:value-of select="/root/gui/strings/setAll"/>
										</button>
									</td>

									<!-- 'clear all' button -->

									<td>
										<button class="content" onclick="clearAll('row.{id}'); return false;">
											<xsl:value-of select="/root/gui/strings/clearAll"/>
										</button>
									</td>
								</tr>
							</xsl:if>
						</xsl:for-each>
					</table>			
				</form>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="goSubmit('update')"><xsl:value-of select="/root/gui/strings/submit"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
</xsl:stylesheet>
