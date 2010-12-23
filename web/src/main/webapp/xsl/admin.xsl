<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="main.xsl"/>
	
	<xsl:template mode="script" match="/">
		<xsl:call-template name="jsHeader"/>
		
		<script language="JavaScript" type="text/javascript">   
        	var msgFailedAddSampleMetadata = "<xsl:value-of select="/root/gui/strings/metadata-samples-add-failed"/>";
            var msgSuccessAddSampleMetadata = "<xsl:value-of select="/root/gui/strings/metadata-samples-add-success"/>";
                          
            function allTemplate(cb) {
                if (cb == $('all')) {
                    if ($('all').checked) {
                        $('iso19139').checked = $('iso19110').checked = $('dublin-core').checked = $('fgdc-std').checked = true;
                    } else {
                        $('iso19139').checked = $('iso19110').checked = $('dublin-core').checked = $('fgdc-std').checked = false;
                    }
                } else {
                    $('all').checked = false;
                }
            }

			function addTemplate() {

                if (!(($('all').checked) ||
                      ($('iso19139').checked) ||
                      ($('iso19110').checked) ||
                      ($('dublin-core').checked) ||
                      ($('fgdc-std').checked)            
                    )) {

                    alert("<xsl:value-of select="/root/gui/strings/metadata-template-select"/>");
                    return;
                }

				var wait = 'waitTpl';
				var btn = 'tplBtn';
				$(wait).style.display = 'block';
				$(btn).style.display = 'none';
				
				var url = "metadata.templates.add.default?schema=";
				if ($('all').checked) {
				} else {
					if ($('iso19139').checked)
						url += "iso19139";
					if ($('iso19110').checked)
						url += "iso19110";
					if ($('dublin-core').checked)
					url += "dublin-core";
					if ($('fgdc-std').checked)
						url += "fgdc-std";
				}
				
				var http = new Ajax.Request(
					url, 
					{
						method: 'get', 
						parameters: null,
						onComplete: function(originalRequest){},
						onLoaded: function(originalRequest){},
						onSuccess: function(originalRequest){                                       
							// get the XML root item
							var root = originalRequest.responseXML.documentElement;
							var resp = root.getAttribute('status');
							$(wait).style.display = 'none';
							$(btn).style.display = 'block';

							if (resp == "true")
								alert ("ok");
							else
								alert(translate('error'));
						},
						onFailure: function(originalRequest){
							$(wait).style.display = 'none';
							$(btn).style.display = 'block';
							alert(msgFailed);
						}
				});
			}
            
            function addSampleData() {
				var wait = 'waitSamples';
				var btn = 'tplSamples';
				$(wait).style.display = 'block';
				$(btn).style.display = 'none';
				
				var url = "metadata.samples.add?file_type=mef&amp;uuidAction=overwrite";
				
				var http = new Ajax.Request(
					url, 
					{
						method: 'get', 
						parameters: null,
						onComplete: function(originalRequest){},
						onLoaded: function(originalRequest){},
						onSuccess: function(originalRequest){                                       
							// get the XML root item
							var root = originalRequest.responseXML.documentElement;
							var resp = root.getAttribute('status');
							$(wait).style.display = 'none';
							$(btn).style.display = 'block';

							if (resp == "true")
								alert (msgSuccessAddSampleMetadata);
							else
								alert(translate('error'));
						},
						onFailure: function(originalRequest){
							$(wait).style.display = 'none';
							$(btn).style.display = 'block';							
                            alert(msgFailedAddSampleMetadata);
						}
				});
			}
		</script>
		
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
						<xsl:if test="/root/gui/services/service/@name='metadata.add.form'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/metadata.create.form"><xsl:value-of select="/root/gui/strings/newMetadata"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/newMdDes"/></td>
							</tr>
						</xsl:if>
						
						<xsl:if test="/root/gui/services/service/@name='metadata.xmlinsert.form'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/metadata.xmlinsert.form"><xsl:value-of select="/root/gui/strings/xmlInsertTitle"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/xmlInsert"/></td>
							</tr>
						</xsl:if>
						
						<xsl:if test="/root/gui/services/service/@name='metadata.batchimport.form'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/metadata.batchimport.form"><xsl:value-of select="/root/gui/strings/batchImportTitle"/></a></td>
								<td class="padded">
									<xsl:value-of select="/root/gui/strings/batchImport"/>
								</td>
							</tr>
						</xsl:if>
						
						<xsl:if test="/root/gui/services/service/@name='metadata.searchunused.form'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/metadata.searchunused.form"><xsl:value-of select="/root/gui/strings/searchUnusedTitle"/></a></td>
								<td class="padded">
									<xsl:value-of select="/root/gui/strings/searchUnused"/>
								</td>
							</tr>
						</xsl:if>

						<tr>
							<td class="padded">
								<a href="{/root/gui/locService}/main.search?hitsPerPage=10&amp;editable=true" style="text-transform:capitalize;">
									<xsl:value-of select="/root/gui/strings/mymetadata"/>
								</a>
							</td>
							<td class="padded">
								<xsl:value-of select="/root/gui/strings/mymetadata"/>
							</td>
						</tr>						

						<xsl:if test="/root/gui/services/service/@name='transfer.ownership'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/transfer.ownership"><xsl:value-of select="/root/gui/strings/transferOwnership"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/transferOwnershipDes"/></td>
							</tr>
						</xsl:if>
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
						<xsl:if test="/root/gui/services/service/@name='metadata.templates.list'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/metadata.templates.list"><xsl:value-of select="/root/gui/strings/metadata-template-order"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/metadata-template-order-desc"/></td>
							</tr>
						</xsl:if>
						<xsl:if test="/root/gui/services/service/@name='metadata.templates.add.default'">
							<tr>
								<td class="padded"><xsl:value-of select="/root/gui/strings/metadata-template-add-default"/></td>
								<td class="padded">
									<xsl:value-of select="/root/gui/strings/metadata-template-add-default-desc"/> :
									<input type="checkbox" name="iso19139" id="iso19139" onClick="allTemplate(this);"/><label for="iso19139">iso19139/119</label>
									<input type="checkbox" name="iso19110" id="iso19110" onClick="allTemplate(this);"/><label for="iso19110">iso19110</label>
									<input type="checkbox" name="dublin-core" id="dublin-core" onClick="allTemplate(this);"/><label for="dublin-core">dublin-core</label>
									<input type="checkbox" name="fgdc-std" id="fgdc-std" onClick="allTemplate(this);"/><label for="fgdc-std">fgdc-std</label>
									<input type="checkbox" name="" id="all" onClick="allTemplate(this);"/><label for="all">all</label>
									<button class="content" onclick="addTemplate();" id="tplBtn">
										<xsl:value-of select="/root/gui/strings/metadata-template-add-default"/>
									</button>
									<img src="{/root/gui/url}/images/loading.gif" id="waitTpl" style="display:none;"/>									
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
						<xsl:if test="/root/gui/services/service/@name='user.pwupdate'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/user.pwedit?id={/root/gui/session/userId}"><xsl:value-of select="/root/gui/strings/userPw"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/userPwDes"/></td>
							</tr>
						</xsl:if>
						<xsl:if test="/root/gui/services/service/@name='user.infoupdate'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/user.infoedit?id={/root/gui/session/userId}"><xsl:value-of select="/root/gui/strings/userInfo"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/userInfoDes"/></td>
							</tr>
						</xsl:if>
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
						
						<xsl:if test="/root/gui/services/service/@name='category.update' and /root/gui/config/category/admin">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/category.list"><xsl:value-of select="/root/gui/strings/categoryManagement"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/categoryManDes"/></td>
							</tr>
						</xsl:if>
						
						<xsl:if test="/root/gui/services/service/@name='thesaurus.admin'">
							<tr>
								<td class="padded">
									<a href="{/root/gui/locService}/thesaurus.admin">
									<xsl:value-of select="/root/gui/strings/thesaurus/management"/>
									</a>
								</td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/thesaurus/manDes"/></td>
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
						
						<xsl:if test="/root/gui/services/service/@name='notifications.list'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/notifications.list"><xsl:value-of select="/root/gui/strings/notifications"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/remoteUpdates"/></td>
							</tr>
						</xsl:if>

						<xsl:if test="/root/gui/services/service/@name='localization'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/localization"><xsl:value-of select="/root/gui/strings/localiz"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/localizDes"/></td>
							</tr>
						</xsl:if>

						<xsl:if test="/root/gui/services/service/@name='index.languages.get'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/index.languages.get"><xsl:value-of select="/root/gui/strings/indexLanguages"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/indexLanguagesDes"/></td>
							</tr>
						</xsl:if>						
						
                        <xsl:if test="/root/gui/services/service/@name='csw.config.get'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/csw.config.get"><xsl:value-of select="/root/gui/strings/cswServer"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/cswServerDes"/></td>
							</tr>
						</xsl:if>

						<xsl:if test="/root/gui/services/service/@name='metadata.admin.index.rebuild' and /root/gui/services/service/@name='metadata.admin.index.optimize'">            
							<xsl:call-template name="admin-index"/>
						</xsl:if>
						
					</xsl:variable>
					<xsl:if test="normalize-space($adminServices)">
						<tr>
							<td colspan="2"><b><xsl:value-of select="/root/gui/strings/admin"/></b></td>
						</tr>
						<xsl:copy-of select="$adminServices"/>
						<tr><td class="spacer"/></tr>
                        
                        <xsl:if test="/root/gui/services/service/@name='test.i18n'">
						<tr>
							<td class="padded"><a href="{/root/gui/locService}/test.i18n"><xsl:value-of select="/root/gui/strings/i18n"/></a></td>
							<td class="padded"><xsl:value-of select="/root/gui/strings/i18nDesc"/></td>
						</tr>
                        </xsl:if>
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
                                <button class="content" onclick="addSampleData();" id="tplSamples">                                   
                                    <xsl:value-of select="/root/gui/strings/metadata-samples-add"/>
                                </button>
                                <img src="{/root/gui/url}/images/loading.gif" id="waitSamples" style="display:none;"/>									
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
				<script language="JavaScript" type="text/javascript">   
					var msgSuccess = "<xsl:value-of select="/root/gui/strings/metadata.admin.index.success"/>";
					var msgFailed = "<xsl:value-of select="/root/gui/strings/metadata.admin.index.failed"/>";
					var doYouReally = "<xsl:value-of select="/root/gui/strings/doYouReallyWantToDoThis"/>";
					
					function idxOperation(service, wait, btn){
						if (!confirm(doYouReally)) return;

						var url = Env.locService + '/' + service;
					  $(wait).style.display = 'block';
					  $(btn).style.display = 'none';
					  var http = new Ajax.Request(
					    url, 
					    {
					      method: 'get', 
					      parameters: null,
					      onComplete: function(originalRequest){},
					      onLoaded: function(originalRequest){},
					      onSuccess: function(originalRequest){                                       
					        // get the XML root item
   					        var root = originalRequest.responseXML.documentElement;
					
					        var resp = root.getElementsByTagName('status')[0].firstChild.nodeValue;
					        $(wait).style.display = 'none';
					        $(btn).style.display = 'block';
					        if (resp == "true")
  					          alert (msgSuccess);
					        else
 					          alert(msgFailed);
					      },
					      onFailure: function(originalRequest){
					        $(wait).style.display = 'none';
					        $(btn).style.display = 'block';
					        alert(msgFailed);
					      }
					    }
					  );
					}
				</script>
			</td>
			<td>
				<button class="content" onclick="idxOperation('metadata.admin.index.rebuild','waitIdx', this.name)" id="btIdx" name="btIdx"><xsl:value-of select="/root/gui/strings/rebuild"/></button>
				<img src="{/root/gui/url}/images/loading.gif" id="waitIdx" style="display:none;"/>
			</td>
		</tr>
		<tr>
      <td class="padded"><xsl:value-of select="/root/gui/strings/metadata.admin.index.optimize.desc"/></td>
      <td>
        <button class="content" onclick="idxOperation('metadata.admin.index.optimize', 'waitIdxOpt', this.name)" id="btOptIdx" name="btOptIdx"><xsl:value-of select="/root/gui/strings/optimize"/></button>
        <img src="{/root/gui/url}/images/loading.gif" id="waitIdxOpt" style="display:none;"/>
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
