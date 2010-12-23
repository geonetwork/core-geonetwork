<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="main.xsl"/>

	<xsl:template name="addrow">
		<xsl:param name="service"/>
		<xsl:param name="args" select="''"/>
		<xsl:param name="title"/>
		<xsl:param name="desc"/>

		<xsl:variable name="modalArg">
			<xsl:choose>
				<xsl:when test="/root/request/modal">
					<xsl:text>&amp;modal</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text></xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
			
		<xsl:if test="/root/gui/services/service/@name=$service">
			<xsl:variable name="url">
				<xsl:choose>
					<xsl:when test="normalize-space($args)='' and normalize-space($modalArg)=''">
						<xsl:value-of select="concat(/root/gui/locService,'/',$service)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(/root/gui/locService,'/',$service,'?',$args,$modalArg)"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>

			<tr>
				<td class="padded">
					<xsl:choose>
						<xsl:when test="/root/request/modal">
							<a onclick="popAdminWindow('{$url}');" href="javascript:void(0);" style="text-transform:capitalize;"><xsl:value-of select="$title"/></a>
						</xsl:when>
						<xsl:otherwise>
							<a href="{$url}"><xsl:value-of select="$title"/></a>
						</xsl:otherwise>
					</xsl:choose>
				</td>
				<td class="padded"><xsl:value-of select="$desc"/></td>
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
<xsl:with-param name="title" select="/root/gui/strings/newMetadata"/>
<xsl:with-param name="desc" select="/root/gui/strings/newMdDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
<xsl:with-param name="service" select="'metadata.xmlinsert.form'"/>
<xsl:with-param name="title" select="/root/gui/strings/xmlInsertTitle"/>
<xsl:with-param name="desc" select="/root/gui/strings/xmlInsert"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
<xsl:with-param name="service" select="'{/root/gui/locService}/main.search?hitsPerPage=10&amp;editable=true'"/>
<xsl:with-param name="title" select="/root/gui/strings/mymetadata"/>
<xsl:with-param name="desc" select="/root/gui/strings/mymetadata"/>
						</xsl:call-template>

						
						<xsl:call-template name="addrow">
<xsl:with-param name="service" select="'metadata.batchimport.form'"/>
<xsl:with-param name="title" select="/root/gui/strings/batchImportTitle"/>
<xsl:with-param name="desc" select="/root/gui/strings/batchImport"/>
						</xsl:call-template>
						
						<xsl:call-template name="addrow">
<xsl:with-param name="service" select="'metadata.searchunused.form'"/>
<xsl:with-param name="title" select="/root/gui/strings/searchUnusedTitle"/>
<xsl:with-param name="desc" select="/root/gui/strings/searchUnused"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
<xsl:with-param name="service" select="'transfer.ownership'"/>
<xsl:with-param name="title" select="/root/gui/strings/transferOwnership"/>
<xsl:with-param name="desc" select="/root/gui/strings/transferOwnershipDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
<xsl:with-param name="service" select="'metadata.schema.add.form'"/>
<xsl:with-param name="title" select="/root/gui/strings/addSchema"/>
<xsl:with-param name="desc" select="/root/gui/strings/addSchemaDes"/>
						</xsl:call-template>

						<!-- Disable for now as we may need atomic ops on schema 
						     resources before we can allow update/delete

								 if we have any plugin schemas then allow update and delete
						     ops 
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
						-->
					</xsl:variable>

					<xsl:if test="normalize-space($mdServices)">
						<tr>
							<td colspan="2"><b><xsl:value-of select="/root/gui/strings/metadata"/></b></td>
						</tr>
						<xsl:copy-of select="$mdServices"/>
						<tr><td class="spacer"/></tr>
					</xsl:if>
					
					
					<!-- Template administration -->
					<xsl:variable name="mdTemplate">

						<xsl:call-template name="addrow">
<xsl:with-param name="service" select="'metadata.templates.list'"/>
<xsl:with-param name="title" 
								select="/root/gui/strings/metadata-template-order"/>
<xsl:with-param name="desc" 
								select="/root/gui/strings/metadata-template-order-desc"/>
						</xsl:call-template>

						<xsl:if test="/root/gui/services/service/@name='metadata.templates.add.default'">
							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/strings/metadata-template-add-default"/></td>
								<td class="padded">
									<table>
										<tr>
											<td align="center" width="20%">
									<xsl:value-of select="/root/gui/strings/metadata-template-add-default-desc"/> :
											</td>
											<td align="center" width="60%">
									<select class="content" id="metadata.templates.select" size="8" multiple="true">
										<xsl:for-each select="/root/gui/schemalist/name">
											<xsl:sort select="."/>
											<option value="{string(.)}">
												<xsl:value-of select="string(.)"/>
											</option>
										</xsl:for-each>
									</select>
											</td>
											<td align="center" width="20%">
									<button class="content" onclick="addTemplate('{/root/gui/strings/metadata-template-select}');" id="tplBtn">
										<xsl:value-of select="/root/gui/strings/metadata-template-add-default"/>
									</button>
									<img src="{/root/gui/url}/images/loading.gif" id="waitTpl" style="display:none;"/>									
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</xsl:if>
					</xsl:variable>

					<xsl:if test="normalize-space($mdTemplate)">
						<tr>
							<td colspan="2"><b><xsl:value-of select="/root/gui/strings/template"/></b></td>
						</tr>
						<xsl:copy-of select="$mdTemplate"/>
						<tr><td class="spacer"/></tr>
					</xsl:if>
					
					
					<!-- personal info services -->
					<xsl:variable name="persInfoServices">
						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'user.pwedit'"/>
	<xsl:with-param name="args" select="concat('id=',/root/gui/session/userId)"/>
	<xsl:with-param name="title" select="/root/gui/strings/userPw"/>
	<xsl:with-param name="desc" select="/root/gui/strings/userPwDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'user.infoedit'"/>
	<xsl:with-param name="args" select="concat('id=',/root/gui/session/userId)"/>
	<xsl:with-param name="title" select="/root/gui/strings/userInfo"/>
	<xsl:with-param name="desc" select="/root/gui/strings/userInfoDes"/>
						</xsl:call-template>
					</xsl:variable>

					<xsl:if test="normalize-space($persInfoServices)">
						<tr>
							<td colspan="2"><b><xsl:value-of select="/root/gui/strings/persInfo"/></b></td>
						</tr>
						<xsl:copy-of select="$persInfoServices"/>
						<tr><td class="spacer"/></tr>
					</xsl:if>

					<!-- administration services -->
					<xsl:variable name="adminServices">
						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'user.list'"/>
	<xsl:with-param name="title" select="/root/gui/strings/userManagement"/>
	<xsl:with-param name="desc" select="/root/gui/strings/userManagementDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'group.list'"/>
	<xsl:with-param name="title" select="/root/gui/strings/groupManagement"/>
	<xsl:with-param name="desc" select="/root/gui/strings/groupManDes"/>
						</xsl:call-template>
						
						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'category.update'"/>
	<xsl:with-param name="title" select="/root/gui/strings/categoryManagement"/>
	<xsl:with-param name="desc" select="/root/gui/strings/categoryManDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'thesaurus.admin'"/>
	<xsl:with-param name="title" select="/root/gui/strings/thesaurus/management"/>
	<xsl:with-param name="desc" select="/root/gui/strings/thesaurus/manDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'harvesting'"/>
	<xsl:with-param name="title" select="/root/gui/strings/harvestingManagement"/>
	<xsl:with-param name="desc" select="/root/gui/strings/harvestingManDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'config'"/>
	<xsl:with-param name="title" select="/root/gui/strings/systemConfig"/>
	<xsl:with-param name="desc" select="/root/gui/strings/systemConfigDes"/>
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
						</xsl:call-template>

						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'stat.main'"/>
	<xsl:with-param name="title" select="/root/gui/strings/searchStatistics"/>
	<xsl:with-param name="desc" select="/root/gui/strings/searchStatisticsDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'notifications.list'"/>
	<xsl:with-param name="title" select="/root/gui/strings/notifications"/>
	<xsl:with-param name="desc" select="/root/gui/strings/notificationsDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'localization'"/>
	<xsl:with-param name="title" select="/root/gui/strings/localiz"/>
	<xsl:with-param name="desc" select="/root/gui/strings/localizDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'index.languages.get'"/>
	<xsl:with-param name="title" select="/root/gui/strings/indexLanguages"/>
	<xsl:with-param name="desc" select="/root/gui/strings/indexLanguagesDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'csw.config.get'"/>
	<xsl:with-param name="title" select="/root/gui/strings/cswServer"/>
	<xsl:with-param name="desc" select="/root/gui/strings/cswServerDes"/>
						</xsl:call-template>

						<xsl:call-template name="addrow">
	<xsl:with-param name="service" select="'test.i18n'"/>
	<xsl:with-param name="title" select="/root/gui/strings/i18n"/>
	<xsl:with-param name="desc" select="/root/gui/strings/i18nDesc"/>
						</xsl:call-template>

						<xsl:if test="/root/gui/services/service/@name='metadata.admin.index.rebuild' and /root/gui/services/service/@name='metadata.admin.index.optimize'">            
							<xsl:call-template name="admin-index"/>
						</xsl:if>
						
					</xsl:variable>

					<xsl:if test="normalize-space($adminServices)">
						<tr>
							<td colspan="2"><b><xsl:value-of select="/root/gui/strings/admin"/></b></td>
						</tr>
						<xsl:copy-of select="$adminServices"/>
					</xsl:if>
					
					<tr>
						<td class="padded"><a href="{/root/gui/locService}/test.csw"><xsl:value-of select="/root/gui/strings/cswTest"/></a></td>
						<td class="padded"><xsl:value-of select="/root/gui/strings/cswTestDesc"/></td>
					</tr>
	
          <!-- Sample metadata -->
          <xsl:if test="/root/gui/services/service/@name='metadata.samples.add'">
            <tr><td colspan="2" class="spacer"/></tr>
            <tr>
              <td class="padded"><xsl:value-of select="/root/gui/strings/metadata-samples"/></td>
              <td class="padded">
								<table>
									<tr>
										<td width="60%" align="center">
											<select class="content" id="metadata.sampledata.select" size="8" multiple="true">
												<xsl:for-each select="/root/gui/schemalist/name">
													<xsl:sort select="."/>
													<option value="{string(.)}">
														<xsl:value-of select="string(.)"/>
													</option>
												</xsl:for-each>
											</select>
										</td>
										<td width="40%" align="center">
                      <button class="content" onclick="addSampleData('{/root/gui/strings/metadata-sampledata-select}', '{/root/gui/strings/metadata-samples-add-failed}', '{/root/gui/strings/metadata-samples-add-success}');" id="tplSamples">                                   
                        <xsl:value-of select="/root/gui/strings/metadata-samples-add"/>
                      </button>
                      <img src="{/root/gui/url}/images/loading.gif" id="waitSamples" style="display:none;"/>									
										</td>
									</tr>
								</table>
              </td>
            </tr>
          </xsl:if>
				</table>
				<p/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	
	
	<!-- ================================================================================= -->
	
	<xsl:template name="admin-index">
		
		<tr>
			<td class="padded"><xsl:value-of select="/root/gui/strings/metadata.admin.index.desc"/>
			</td>
			<td>
				<button class="content" onclick="idxOperation('metadata.admin.index.rebuild','waitIdx', this.name, true)" id="btIdx" name="btIdx"><xsl:value-of select="/root/gui/strings/rebuild"/></button>
				<img src="{/root/gui/url}/images/loading.gif" id="waitIdx" style="display:none;"/>
			</td>
		</tr>
		<tr>
      <td class="padded"><xsl:value-of select="/root/gui/strings/metadata.admin.index.optimize.desc"/></td>
      <td>
        <button class="content" onclick="idxOperation('metadata.admin.index.optimize', 'waitIdxOpt', this.name, true)" id="btOptIdx" name="btOptIdx"><xsl:value-of select="/root/gui/strings/optimize"/></button>
        <img src="{/root/gui/url}/images/loading.gif" id="waitIdxOpt" style="display:none;"/>
      </td>
		</tr>
		<tr>
			<td class="padded"><xsl:value-of select="/root/gui/strings/lucene.config.reload"/></td>
			<td>
				<button class="content" onclick="idxOperation('lucene.config.reload', 'waitIdxReload', this.name, false)" id="btReloadIdx" name="btReloadIdx"><xsl:value-of select="/root/gui/strings/reload"/></button>
				<img src="{/root/gui/url}/images/loading.gif" id="waitIdxReload" style="display:none;"/>
			</td>
		</tr>
		<xsl:if test="string(/root/gui/env/xlinkResolver/enable)='true'">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/strings/metadata.admin.index.rebuildxlinks.desc"/></td>
				<td>
					<button class="content" onclick="idxOperation('metadata.admin.index.rebuildxlinks', 'waitIdxXLnks', this.name)" id="btIdxXLnks" name="btIdxXLnks"><xsl:value-of select="/root/gui/strings/rebuildxlinks"/></button>
					<img src="{/root/gui/url}/images/loading.gif" id="waitIdxXLnks" style="display:none;"/>
				</td>
			</tr>
		</xsl:if>

		
	</xsl:template>
	
</xsl:stylesheet>
