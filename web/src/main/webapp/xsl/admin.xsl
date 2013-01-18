<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:java="java:org.fao.geonet.util.XslUtil"
	exclude-result-prefixes="#all">

	<xsl:include href="main.xsl"/>

  <!-- Use the link parameter to display a custom hyperlink instead of 
  a default GeoNetwork Jeeves service URL. -->
	<xsl:template name="addrow">
		<xsl:param name="service"/>
		<xsl:param name="link"/>
		<xsl:param name="args" select="''"/>
		<xsl:param name="displayLink" select="true()"/>
		<xsl:param name="title"/>
		<xsl:param name="desc"/>
		<xsl:param name="icon"/>
		<xsl:param name="content"/>

		<xsl:variable name="modalArg">
			<xsl:choose>
				<xsl:when test="/root/request/modal">
					<xsl:text>&amp;modal</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:if test="java:isAccessibleService($service)">
			<xsl:variable name="url">
				<xsl:choose>
					<xsl:when test="normalize-space($link)!=''">
						<xsl:value-of select="$link"/>
					</xsl:when>
					<xsl:when test="normalize-space($args)='' and normalize-space($modalArg)=''">
						<xsl:value-of select="concat(/root/gui/locService,'/',$service)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of
							select="concat(/root/gui/locService,'/',$service,'?',$args,$modalArg)"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>

			<tr><td class="configOption">
				<xsl:if test="normalize-space($icon)">
					<img src="../../images/{$icon}" alt="{$desc}" class="configOption"/>
				</xsl:if>
				</td>
				<td class="padded">
					<xsl:choose>
						<xsl:when test="not($displayLink)">
							<xsl:value-of select="$title"/>
						</xsl:when>
						<xsl:when test="/root/request/modal">
							<a onclick="popAdminWindow('{$url}');" href="javascript:void(0);">
								<xsl:value-of select="$title"/>
							</a>
						</xsl:when>
						<xsl:otherwise>
							<a href="{$url}">
								<xsl:value-of select="$title"/>
							</a>
						</xsl:otherwise>
					</xsl:choose>
				</td>
				<td class="padded">
					<xsl:value-of select="$desc"/>
					<xsl:if test="normalize-space($content)">
						<xsl:copy-of select="$content"/>
					</xsl:if>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>


	<xsl:template name="addTitle">
		<xsl:param name="icon"/>
		<xsl:param name="title"/>
		<xsl:param name="content"/>

		<xsl:if test="normalize-space($content)">
			<tr>
				<td colspan="3" class="configTitle"><img src="../../images/{$icon}" class="configTitle"/>&#160;<b><xsl:value-of
							select="$title"/></b></td>
			</tr>
			<xsl:copy-of select="$content"/>
			<tr>
				<td class="spacer"/>
			</tr>
		</xsl:if>
	</xsl:template>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/admin"/>
			<xsl:with-param name="content">

				<table width="100%" class="text-aligned-left">

					<!-- metadata services -->
					<xsl:variable name="mdServices">
						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'metadata.create.form'"/>
							<xsl:with-param name="link">
								<!-- When client application is the widget redirect to that app 
								FIXME : hl parameter is only available for GUI widget experimental client.
								-->
								<xsl:if test="/root/gui/config/client/@widget='true'"><xsl:value-of select="concat(/root/gui/config/client/@url, '?hl=', /root/gui/language, /root/gui/config/client/@createParameter)"/></xsl:if>
							</xsl:with-param>
							<xsl:with-param name="title" select="/root/gui/strings/newMetadata"/>
							<xsl:with-param name="desc" select="/root/gui/strings/newMdDes"/>
							<xsl:with-param name="icon">page_add.png</xsl:with-param>
						</xsl:call-template>

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'metadata.searchunused.form'"/>
							<xsl:with-param name="title"
								select="/root/gui/strings/searchUnusedTitle"/>
							<xsl:with-param name="desc" select="/root/gui/strings/searchUnused"/>
						</xsl:call-template>

						<xsl:choose>
						  <xsl:when test="/root/gui/config/client/@widget='true' and /root/gui/config/client/@stateId!=''">
								
								<xsl:call-template name="addrow">
									<xsl:with-param name="service" select="'metadata.create.form'"/>
									<xsl:with-param name="displayLink" select="false()"/>
									<xsl:with-param name="title" select="/root/gui/strings/quickSearch"/>
									<xsl:with-param name="content">
										<ul>
											<li>
											  <a href="{concat(/root/gui/config/client/@url, '?hl=', /root/gui/language, '&amp;s_search&amp;', /root/gui/config/client/@stateId, '_E__owner=', /root/gui/session/userId)}">
													<xsl:value-of select="/root/gui/strings/mymetadata"/>
												</a>
											</li>
											<li>
											  <a href="{concat(/root/gui/config/client/@url, '?hl=', /root/gui/language, '&amp;s_search&amp;', /root/gui/config/client/@stateId, '_E_siteId=', /root/gui/env/site/siteId)}">
													<xsl:value-of select="/root/gui/strings/catalogueRecords"/>
												</a>
											</li>
											<li>
											  <a href="{concat(/root/gui/config/client/@url, '?hl=', /root/gui/language, '&amp;s_search&amp;', /root/gui/config/client/@stateId, '_E__isHarvested=y')}">
													<xsl:value-of select="/root/gui/strings/harvestedRecords"/>
												</a>
											</li>
											<li>
											  <a href="{concat(/root/gui/config/client/@url, '?hl=', /root/gui/language, '&amp;s_search&amp;', /root/gui/config/client/@stateId, '_E_template=y')}">
													<xsl:value-of select="/root/gui/strings/catalogueTemplates"/>
												</a>
											</li>
										</ul>
										
										
									</xsl:with-param>
								</xsl:call-template>
								
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="addrow">
									<xsl:with-param name="service" select="'main.search'"/>
									<xsl:with-param name="args" select="'hitsPerPage=10&amp;editable=true'"/>
									
									<xsl:with-param name="title"
										select="/root/gui/strings/mymetadata"/>
									<xsl:with-param name="desc" select="/root/gui/strings/mymetadata"/>
								</xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>
						

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'transfer.ownership'"/>
							<xsl:with-param name="title"
								select="/root/gui/strings/transferOwnership"/>
							<xsl:with-param name="desc"
								select="/root/gui/strings/transferOwnershipDes"/>
						</xsl:call-template>
						
						<tr>
							<td class="spacer"/>
						</tr>
						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'metadata.schema.add.form'"/>
							<xsl:with-param name="title" select="/root/gui/strings/addSchema"/>
							<xsl:with-param name="desc" select="/root/gui/strings/addSchemaDes"/>
							<xsl:with-param name="icon">folder_add.png</xsl:with-param>
						</xsl:call-template>

						<xsl:if test="count(/root/gui/schemalist/name[@plugin='true'])>0">
							<xsl:call-template name="addrow">
								<xsl:with-param name="service" select="'metadata.schema.update.form'"/>
								<xsl:with-param name="title" select="/root/gui/strings/updateSchema"/>
								<xsl:with-param name="desc" select="/root/gui/strings/updateSchemaDes"/>
							</xsl:call-template>

							<xsl:call-template name="addrow">
								<xsl:with-param name="service" select="'metadata.schema.delete.form'"/>
								<xsl:with-param name="title" select="/root/gui/strings/deleteSchema"/>
								<xsl:with-param name="desc" select="/root/gui/strings/deleteSchemaDes"/>
							</xsl:call-template>
						</xsl:if>
					</xsl:variable>

					<!-- Template administration -->
					<xsl:variable name="mdTemplate">
							
							

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'metadata.templates.list'"/>
							<xsl:with-param name="title"
								select="/root/gui/strings/metadata-template-order"/>
							<xsl:with-param name="desc"
								select="/root/gui/strings/metadata-template-order-desc"/>
						</xsl:call-template>
					</xsl:variable>

					<xsl:call-template name="addTitle">
						<xsl:with-param name="icon">xml.png</xsl:with-param>
						<xsl:with-param name="title"
							select="concat(/root/gui/strings/metadata, '&#160;&amp;&#160;', /root/gui/strings/template)"/>
						<xsl:with-param name="content">
							<xsl:copy-of select="$mdServices"/>
							<tr>
								<td class="spacer"/>
							</tr>
							<xsl:copy-of select="$mdTemplate"/>
							<tr>
								<td class="spacer"/>
							</tr>
							<xsl:call-template name="addrow">
							  <xsl:with-param name="service" select="'metadata.templates.add.default'"/>
							  <xsl:with-param name="displayLink" select="false()"/>
							  <xsl:with-param name="title" select="/root/gui/strings/metadata-templates-samples-add"/>
								<xsl:with-param name="icon">add.png</xsl:with-param>
								<xsl:with-param name="content">
									<table>
										<tr>
											<td width="30%">
												<xsl:value-of
													select="/root/gui/strings/selectTemplate"
												/> : <br/>
												<select class="content"
													id="metadata.schemas.select" size="8"
													multiple="true">
													<xsl:for-each select="/root/gui/schemalist/name">
														<xsl:sort select="."/>
														<option value="{string(.)}">
															<xsl:value-of select="string(.)"/>
														</option>
													</xsl:for-each>
												</select>
											</td>
											<td style="align:center;width:20%;vertical-align:bottom;">
											  <div id="addTemplatesSamplesButtons">
  												<button class="content"
  												  onclick="addTemplate('{/root/gui/strings/metadata-schema-select}', '{/root/gui/strings/metadata-template-add-success}');"
  													id="tplBtn">
  													<xsl:value-of
  														select="/root/gui/strings/metadata-template-add-default"
  													/>
  												</button>
  												<button class="content"
  													onclick="addSampleData('{/root/gui/strings/metadata-schema-select}', '{/root/gui/strings/metadata-samples-add-failed}', '{/root/gui/strings/metadata-samples-add-success}');"
  													id="tplSamples">
  													<xsl:value-of
  														select="/root/gui/strings/metadata-samples-add"/>
  												</button>
											  </div>
											  <img src="{/root/gui/url}/images/loading.gif"
											    id="waitLoadingTemplatesSamples" style="display:none;"/>
											</td>
										</tr>
									</table>
								</xsl:with-param>
							</xsl:call-template>

						</xsl:with-param>
					</xsl:call-template>

					<xsl:variable name="io">

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'metadata.xmlinsert.form'"/>
							<xsl:with-param name="title" select="/root/gui/strings/xmlInsertTitle"/>
							<xsl:with-param name="desc" select="/root/gui/strings/xmlInsert"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'metadata.batchimport.form'"/>
							<xsl:with-param name="title" select="/root/gui/strings/batchImportTitle"/>
							<xsl:with-param name="desc" select="/root/gui/strings/batchImport"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'harvesting'"/>
							<xsl:with-param name="title"
								select="/root/gui/strings/harvestingManagement"/>
							<xsl:with-param name="desc" select="/root/gui/strings/harvestingManDes"
							/>
							<xsl:with-param name="icon">connect.png</xsl:with-param>	
						</xsl:call-template>

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'notifications.list'"/>
							<xsl:with-param name="title" select="/root/gui/strings/notifications"/>
							<xsl:with-param name="desc" select="/root/gui/strings/notificationsDes"
							/>
						  <xsl:with-param name="icon">bell.png</xsl:with-param>
						</xsl:call-template>
					</xsl:variable>

					<xsl:call-template name="addTitle">
						<xsl:with-param name="icon">connect.png</xsl:with-param>
						<xsl:with-param name="title" select="/root/gui/strings/io"/>
						<xsl:with-param name="content" select="$io"/>
					</xsl:call-template>

					<xsl:variable name="catalogueConfiguration">

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'config'"/>
							<xsl:with-param name="title" select="/root/gui/strings/systemConfig"/>
							<xsl:with-param name="desc" select="/root/gui/strings/systemConfigDes"/>
							<xsl:with-param name="icon">exec.png</xsl:with-param>	
						</xsl:call-template>

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'csw.config.get'"/>
							<xsl:with-param name="title" select="/root/gui/strings/cswServer"/>
							<xsl:with-param name="desc" select="/root/gui/strings/cswServerDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'config.info'"/>
							<xsl:with-param name="title" select="/root/gui/strings/systemInfo"/>
							<xsl:with-param name="desc" select="/root/gui/strings/systemInfoDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'logo'"/>
							<xsl:with-param name="title" select="/root/gui/strings/logo"/>
							<xsl:with-param name="desc" select="/root/gui/strings/logoDes"/>
							<xsl:with-param name="icon">color_swatch.png</xsl:with-param>
						</xsl:call-template>

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'metadata.formatter.admin'"/>
							<xsl:with-param name="title" select="/root/gui/strings/formatter.admin"/>
							<xsl:with-param name="desc" select="/root/gui/strings/formatter.admin.des"/>
						</xsl:call-template>

						<xsl:if test="string(/root/gui/env/searchStats/enable)='true'">
							<xsl:call-template name="addrow">
								<xsl:with-param name="service" select="'stat.main'"/>
								<xsl:with-param name="title" select="/root/gui/strings/searchStatistics"/>
								<xsl:with-param name="desc"
									select="/root/gui/strings/searchStatisticsDes"/>
								<xsl:with-param name="icon">chart_bar.png</xsl:with-param>
							</xsl:call-template>
						</xsl:if>

					</xsl:variable>


					<xsl:call-template name="addTitle">
						<xsl:with-param name="icon">exec.png</xsl:with-param>
						<xsl:with-param name="title"
							select="/root/gui/strings/catalogueConfiguration"/>
						<xsl:with-param name="content" select="$catalogueConfiguration"/>
					</xsl:call-template>


					<!-- user and group services -->
					<xsl:variable name="persInfoServices">
						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'user.pwedit'"/>
							<xsl:with-param name="args"
								select="concat('id=',/root/gui/session/userId)"/>
							<xsl:with-param name="title" select="/root/gui/strings/userPw"/>
							<xsl:with-param name="desc" select="/root/gui/strings/userPwDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'user.infoedit'"/>
							<xsl:with-param name="args"
								select="concat('id=',/root/gui/session/userId)"/>
							<xsl:with-param name="title" select="/root/gui/strings/userInfo"/>
							<xsl:with-param name="desc" select="/root/gui/strings/userInfoDes"/>
						</xsl:call-template>

						<tr>
							<td class="spacer"/>
						</tr>

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'user.list'"/>
							<xsl:with-param name="title" select="/root/gui/strings/userManagement"/>
							<xsl:with-param name="desc" select="/root/gui/strings/userManagementDes"
							/>
							<xsl:with-param name="icon">user.png</xsl:with-param>
						</xsl:call-template>

						<xsl:if test="java:isAccessibleService('group.update')">
						<xsl:call-template name="addrow">
						  <xsl:with-param name="service" select="'group.list'"/>
							<xsl:with-param name="title" select="/root/gui/strings/groupManagement"/>
							<xsl:with-param name="desc" select="/root/gui/strings/groupManDes"/>
							<xsl:with-param name="icon">group.png</xsl:with-param>
						</xsl:call-template>
						</xsl:if>
					</xsl:variable>

					<xsl:call-template name="addTitle">
						<xsl:with-param name="icon">group.png</xsl:with-param>
						<xsl:with-param name="title" select="/root/gui/strings/usersAndGroups"/>
						<xsl:with-param name="content" select="$persInfoServices"/>
					</xsl:call-template>


					<xsl:variable name="classification">

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'category.list'"/>
							<xsl:with-param name="title"
								select="/root/gui/strings/categoryManagement"/>
							<xsl:with-param name="desc" select="/root/gui/strings/categoryManDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'thesaurus.admin'"/>
							<xsl:with-param name="title"
								select="/root/gui/strings/thesaurus/management"/>
							<xsl:with-param name="desc" select="/root/gui/strings/thesaurus/manDes"
							/>
						</xsl:call-template>

						<!-- Only add the subtemplate if the client is widget based -->
						<xsl:if test="/root/gui/config/client/@widget">
							<tr>
								<td class="spacer"/>
							</tr>

							<xsl:call-template name="addrow">
								<xsl:with-param name="service" select="'subtemplate.admin'"/>
								<xsl:with-param name="title" select="/root/gui/strings/subtemplate.admin"/>
								<xsl:with-param name="desc" select="/root/gui/strings/subtemplate.admin.desc"/>
							</xsl:call-template>
						</xsl:if>
					</xsl:variable>

					<xsl:call-template name="addTitle">
						<xsl:with-param name="icon">book_addresses.png</xsl:with-param>
						<xsl:with-param name="title" select="/root/gui/strings/classification"/>
						<xsl:with-param name="content" select="$classification"/>
					</xsl:call-template>

					<xsl:variable name="indexConfiguration">
						<xsl:if
							test="java:isAccessibleService('metadata.admin.index.rebuild') and java:isAccessibleService('metadata.admin.index.optimize')">
							<xsl:call-template name="admin-index"/>
						</xsl:if>
					</xsl:variable>

					<xsl:call-template name="addTitle">
						<xsl:with-param name="icon">find.png</xsl:with-param>
						<xsl:with-param name="title" select="/root/gui/strings/indexConfiguration"/>
						<xsl:with-param name="content" select="$indexConfiguration"/>
					</xsl:call-template>

					<!-- samples and tests services 
					<xsl:variable name="adminServices">
						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'test.csw'"/>
							<xsl:with-param name="title" select="/root/gui/strings/cswTest"/>
							<xsl:with-param name="desc" select="/root/gui/strings/cswTestDesc"/>
						</xsl:call-template>
					</xsl:variable>

					<xsl:call-template name="addTitle">
						<xsl:with-param name="icon">folder_page.png</xsl:with-param>
						<xsl:with-param name="title" select="/root/gui/strings/samplesAndTests"/>
						<xsl:with-param name="content" select="$adminServices"/>
					</xsl:call-template>
					</xsl:variable>
					-->

					<xsl:variable name="i18n">
						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'localization'"/>
							<xsl:with-param name="title" select="/root/gui/strings/localiz"/>
							<xsl:with-param name="desc" select="/root/gui/strings/localizDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
							<xsl:with-param name="service" select="'test.i18n'"/>
							<xsl:with-param name="title" select="/root/gui/strings/i18n"/>
							<xsl:with-param name="desc" select="/root/gui/strings/i18nDesc"/>
						</xsl:call-template>
					</xsl:variable>

					<xsl:call-template name="addTitle">
						<xsl:with-param name="icon">comment.png</xsl:with-param>
						<xsl:with-param name="title" select="/root/gui/strings/localiz"/>
						<xsl:with-param name="content" select="$i18n"/>
					</xsl:call-template>

				</table>
				<p/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>



	<!-- ================================================================================= -->

	<xsl:template name="admin-index">
		<tr>
			<td/>
			<td class="padded">
				<xsl:value-of select="/root/gui/strings/metadata.admin.index.desc"/>
			</td>
			<td>
				<button class="content"
					onclick="idxOperation('metadata.admin.index.rebuild?reset=yes','waitIdx', this.name, true)"
					id="btIdx" name="btIdx">
					<xsl:value-of select="/root/gui/strings/rebuild"/>
				</button>
				<img src="{/root/gui/url}/images/loading.gif" id="waitIdx" style="display:none;"/>
			</td>
		</tr>
		<tr>
			<td/>
			<td class="padded">
				<xsl:value-of select="/root/gui/strings/metadata.admin.index.optimize.desc"/>
			</td>
			<td>
				<button class="content"
					onclick="idxOperation('metadata.admin.index.optimize', 'waitIdxOpt', this.name, true)"
					id="btOptIdx" name="btOptIdx">
					<xsl:value-of select="/root/gui/strings/optimize"/>
				</button>
				<img src="{/root/gui/url}/images/loading.gif" id="waitIdxOpt" style="display:none;"
				/>
			</td>
		</tr>
		<tr>
			<td/>
			<td class="padded">
				<xsl:value-of select="/root/gui/strings/lucene.config.reload"/>
			</td>
			<td>
				<button class="content"
					onclick="idxOperation('lucene.config.reload', 'waitIdxReload', this.name, false)"
					id="btReloadIdx" name="btReloadIdx">
					<xsl:value-of select="/root/gui/strings/reload"/>
				</button>
				<img src="{/root/gui/url}/images/loading.gif" id="waitIdxReload"
					style="display:none;"/>
			</td>
		</tr>
		<xsl:if test="string(/root/gui/env/xlinkResolver/enable)='true'">
			<tr>
				<td/>
				<td class="padded">
					<xsl:value-of select="/root/gui/strings/metadata.admin.index.rebuildxlinks.desc"
					/>
				</td>
				<td>
					<button class="content"
						onclick="idxOperation('metadata.admin.index.rebuildxlinks', 'waitIdxXLnks', this.name)"
						id="btIdxXLnks" name="btIdxXLnks">
						<xsl:value-of select="/root/gui/strings/rebuildxlinks"/>
					</button>
					<img src="{/root/gui/url}/images/loading.gif" id="waitIdxXLnks"
						style="display:none;"/>
				</td>
			</tr>
		</xsl:if>

	</xsl:template>

</xsl:stylesheet>
