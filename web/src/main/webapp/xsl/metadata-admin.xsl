<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:fo="http://www.w3.org/1999/XSL/Format" 
	xmlns:util="xalan://org.fao.geonet.util.XslUtil"
    xmlns:exsl="http://exslt.org/common"
    extension-element-prefixes="exsl">
	
	<xsl:include href="modal.xsl"/>

	<xsl:variable name="profile"  select="/root/gui/session/profile"/>

	<!-- ================================================================================= -->
	<!-- page content -->
	<!-- ================================================================================= -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/privileges"/>
			<xsl:with-param name="content">
			
				<xsl:variable name="lang" select="/root/gui/language"/>
				<xsl:variable name="groupOwner" select="/root/response/groupOwner"/>
				<xsl:variable name="isNotReviewer" select="not(/root/response/groups/group[id=$groupOwner and userProfile='Reviewer'])"/>
				<xsl:variable name="disabled" select="(/root/response/owner='false')"/>
				<xsl:variable name="path" select="string(/root/response/luceneIndexPath)"/>
				<xsl:variable name="id" select="string(/root/response/id)"/>
				<xsl:variable name="schema" select="/root/response/schema"/>

				<xsl:variable name="valid">
					<xsl:choose>
						<xsl:when test="contains(/root/gui/reqService,'metadata.batch')">y</xsl:when>
                        <!-- only apply restriction to iso19139 metadata records that are not templates -->
                        <xsl:when test="/root/response/isMetadata = 'false'">y</xsl:when>
                        <xsl:when test="/root/response/validation/record[status='2']">n</xsl:when>
						<xsl:otherwise><xsl:text>y</xsl:text></xsl:otherwise>
					</xsl:choose>                               
				</xsl:variable>
				<div id="privileges">
					<input name="metadataid" id="metadataid" type="hidden" value="{$id}"/>
					<table>
					<tr><td width="80%" class="padded">
					<table>
						<tr>
							<th class="padded"><xsl:value-of select="/root/gui/strings/groups"/></th>
							<!-- loop on all operations leaving editing and notify to last -->
							<xsl:for-each select="/root/response/operations/record">
								<xsl:if test="id!='2' and id!='3'">
									<th class="padded-center"><xsl:value-of select="label/child::*[name() = $lang]"/></th>
								</xsl:if>
							</xsl:for-each>
							<xsl:for-each select="/root/response/operations/record">
								<xsl:if test="id='2' or id='3'">
									<th class="padded-center"><xsl:value-of select="label/child::*[name() = $lang]"/></th>
								</xsl:if>
							</xsl:for-each>
							<th width="70"/>
							<th/>
						</tr>
			
						<!-- 'Internet', 'Intranet' and GUEST groups 
							Disabled if user is not an administrator
							or if user is not a reviewer of the metadata group.
						-->
						<xsl:variable name="isTemplate" select="/root/response/isMetadata = 'false'"/>
						<xsl:variable name="isAdministrator" select="$profile = 'Administrator'"/>
						<xsl:variable name="isReviewer" select="not($isNotReviewer)"/>
						<xsl:variable name="isValid" select="$valid='y'"/>
						
						<!-- Metadata can only be published by administrator or reviewer 
						if it is valid or (sub)template -->
						<xsl:variable name="cannotPublic" select="
									not(($isAdministrator or $isReviewer) and
									($isTemplate or $isValid))"/>
						
						<xsl:apply-templates select="/root/response/groups/group[id='1']" mode="group">
							<xsl:with-param name="lang" select="$lang"/>
							<xsl:with-param name="disabled" select="$cannotPublic"/>
						</xsl:apply-templates>

						<xsl:apply-templates select="/root/response/groups/group[id='0']" mode="group">
							<xsl:with-param name="lang" select="$lang"/>
							<xsl:with-param name="disabled" select="$cannotPublic"/>
						</xsl:apply-templates>

						<xsl:apply-templates select="/root/response/groups/group[id='-1']" mode="group">
							<xsl:with-param name="lang" select="$lang"/>
							<xsl:with-param name="disabled" select="$cannotPublic"/>
						</xsl:apply-templates>

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
							<xsl:sort select="name"/>
							
							<xsl:variable name="userGroup" select="@userGroup"/>
							<!-- Display group if it's one of the user groups 
							or not a user group but the usergrouponly catalog setting is false
							-->
							<xsl:if test="(/root/gui/env/metadataprivs/usergrouponly='false' and $userGroup!='true') or $userGroup='true'">
								<xsl:if test="id!='0' and id!='1' and id!='-1'">
									<xsl:variable name="groupId" select="id"/>
									<tr id="row.{id}">
										<td class="padded">
											<span>
												<xsl:if test="$disabled">
													<xsl:attribute name="style">color: #A0A0A0;</xsl:attribute>
												</xsl:if>
												<xsl:choose>
													<xsl:when test="label/child::*[name() = $lang]">
														<xsl:value-of select="label/child::*[name() = $lang]"/>
													</xsl:when>
													<xsl:otherwise><xsl:value-of select="name"/></xsl:otherwise>
												</xsl:choose>
												<xsl:if test="/root/gui/env/metadataprivs/usergrouponly!='true' and $userGroup='true'"><xsl:text> *</xsl:text></xsl:if>
											</span>
										</td>
										
								<!-- loop on all operations leaving editing and notify to last -->
										<xsl:for-each select="oper">
											<xsl:if test="id!='2' and id!='3'">
												<td class="padded" align="center" width="80">
													<input type="checkbox" id="_{$groupId}_{id}" name="_{$groupId}_{id}">
														<xsl:if test="on">
															<xsl:attribute name="checked"/>
														</xsl:if>
														<xsl:if test="$disabled">
															<xsl:attribute name="disabled"/>
														</xsl:if>
													</input>
												</td>
											</xsl:if>
										</xsl:for-each>
										<xsl:for-each select="oper">
											<xsl:if test="id='2' or id='3'">
												<td class="padded" align="center" width="80">
													<xsl:if test="$userGroup='true'">
														<input type="checkbox" id="_{$groupId}_{id}" name="_{$groupId}_{id}">
															<xsl:if test="on">
																<xsl:attribute name="checked"/>
															</xsl:if>
															<xsl:if test="$disabled">
																<xsl:attribute name="disabled"/>
															</xsl:if>
														</input>
													</xsl:if>
												</td>
											</xsl:if>
										</xsl:for-each>
	
										<!-- 'set all' button -->
	
										<td>
											<button class="content" onclick="setAll('row.{id}'); return false;">
												<xsl:if test="$disabled">
													<xsl:attribute name="disabled"/>
													<xsl:attribute name="style">color: #A0A0A0;</xsl:attribute>
												</xsl:if>
												<xsl:value-of select="/root/gui/strings/setAll"/>
											</button>
										</td>
	
										<!-- 'clear all' button -->
	
										<td>
											<button class="content" onclick="clearAll('row.{id}'); return false;">
												<xsl:if test="$disabled">
													<xsl:attribute name="disabled"/>
													<xsl:attribute name="style">color: #A0A0A0;</xsl:attribute>
												</xsl:if>
												<xsl:value-of select="/root/gui/strings/clearAll"/>
											</button>
										</td>
									</tr>
								</xsl:if>
							</xsl:if>
						</xsl:for-each>
						<xsl:if test="not($disabled)">
							<tr width="100%">
								<td align="center" colspan="8">
									<xsl:choose>
										<xsl:when test="contains(/root/gui/reqService,'metadata.batch')">
											<button class="content" onclick="checkBoxModalUpdate('privileges','metadata.batch.update.privileges',true,'{concat(/root/gui/strings/results,' ',/root/gui/strings/batchUpdatePrivilegesTitle)}')"><xsl:value-of select="/root/gui/strings/submit"/></button>
										</xsl:when>
										<xsl:otherwise>
											<button class="content" onclick="checkBoxModalUpdate('privileges','metadata.admin')"><xsl:value-of select="/root/gui/strings/submit"/></button>
										</xsl:otherwise>
									</xsl:choose>
								</td>
							</tr>
						</xsl:if>
					</table>
					<xsl:if test="/root/gui/env/metadataprivs/usergrouponly!='true'">
					* <xsl:value-of select="/root/gui/strings/usergroups"/>
					</xsl:if>
					</td>
					<xsl:if test="not(contains(/root/gui/reqService,'metadata.batch'))">
					<td class="padded-center">
					<h1><xsl:value-of select="/root/gui/strings/displayValidationReport"/></h1>
				
					<table>						
						<xsl:for-each select="/root/response/validation/record">
							<xsl:variable name="valid" select="normalize-space(status)"/>
							<xsl:variable name="name"><xsl:value-of select="normalize-space(valtype)"/></xsl:variable>	
							<tr class="{$name}">
								<td style="width:60px">
									<xsl:choose>
										<xsl:when test="$valid='1'"><!-- valid -->
											<img class="icon16" src="../../images/button_ok.png" alt="valid" title="valid"/>
										</xsl:when>
										<xsl:when test="$valid='0'"><!-- error -->
											<img class="icon18" src="../../images/validationError.png" alt="{/root/gui/strings/publishOnlyIfAdminOrValid}" title="{/root/gui/strings/publishOnlyIfAdminOrValid}"/>
										</xsl:when>
										<xsl:otherwise><!-- warning -->
											<img class="icon18" src="../../images/validationWarning.png" alt="{/root/gui/strings/publishOnlyIfAdminOrValid}" title="{/root/gui/strings/publishOnlyIfAdminOrValid}"/>
										</xsl:otherwise>
									</xsl:choose>
								</td>
								<td class="padded">	
									<xsl:variable name="name_"><xsl:value-of
										select="/root/gui/strings/validmd/*[name()=$name]"/></xsl:variable>
									<xsl:choose>
										<xsl:when test="$name_ != ''"><xsl:value-of select="$name_"/></xsl:when>
										<xsl:otherwise><xsl:value-of select="$name"/></xsl:otherwise>
									</xsl:choose>
								</td>
							</tr>
						</xsl:for-each>
					</table>
					</td>
					</xsl:if>
					</tr></table>
				</div>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<!-- ================================================================================= -->

	<xsl:template match="*" mode="group">
		<xsl:param name="lang"/>
		<xsl:param name="disabled" select="($profile != 'Administrator') and ($profile != 'Reviewer') and (/root/response/owner='false')"/>

		<xsl:variable name="groupId"  select="id"/>
		
		<tr id="row.{id}">
			<td class="padded">
				<span>
					<xsl:if test="$disabled">
						<xsl:attribute name="style">color: #A0A0A0;</xsl:attribute>
					</xsl:if>
					<xsl:value-of select="label/child::*[name() = $lang]"/>
				</span>
			</td>
			
			<!-- loop on all operations,  edit, notify and admin privileges are hidden-->
			<xsl:for-each select="oper">
				<xsl:if test="id!='2' and id!='3'">
					<td class="padded" align="center" width="80">
						<input type="checkbox" name="_{$groupId}_{id}" id="_{$groupId}_{id}">
							<xsl:if test="$disabled">
								<xsl:attribute name="disabled"/>
							</xsl:if>
							<xsl:if test="on">
								<xsl:attribute name="checked"/>
							</xsl:if>
						</input>
					</td>
				</xsl:if>
			</xsl:for-each>

			<!-- fill empty slots -->
			<xsl:for-each select="oper">
				<xsl:if test="id='2' or id='3'">
					<td/>
				</xsl:if>
			</xsl:for-each>

			<!-- 'set all' button -->

			<td>
				<button class="content" onclick="setAll('row.{id}'); return false;">
					<xsl:if test="$disabled">
						<xsl:attribute name="disabled"/>
						<xsl:attribute name="style">color: #A0A0A0;</xsl:attribute>
					</xsl:if>
					<xsl:value-of select="/root/gui/strings/setAll"/>
				</button>
			</td>

			<!-- 'clear all' button -->

			<td>
				<button class="content" onclick="clearAll('row.{id}'); return false;">
					<xsl:if test="$disabled">
						<xsl:attribute name="disabled"/>
						<xsl:attribute name="style">color: #A0A0A0;</xsl:attribute>
					</xsl:if>
					<xsl:value-of select="/root/gui/strings/clearAll"/>
				</button>
			</td>
		</tr>
	</xsl:template>

	<!-- ================================================================================= -->

</xsl:stylesheet>
