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

						<xsl:if test="/root/gui/services/service/@name='transfer.ownership'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/transfer.ownership"><xsl:value-of select="/root/gui/strings/transferOwnership"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/transferOwnershipDes"/></td>
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
						
						<xsl:if test="/root/gui/services/service/@name='localization'">
							<tr>
								<td class="padded"><a href="{/root/gui/locService}/localization"><xsl:value-of select="/root/gui/strings/localiz"/></a></td>
								<td class="padded"><xsl:value-of select="/root/gui/strings/localizDes"/></td>
							</tr>
						</xsl:if>
						
						
						<xsl:if test="/root/gui/services/service/@name='metadata.admin.index.rebuild'">            
							<xsl:call-template name="admin-index"/>
						</xsl:if>
						
					</xsl:variable>
					<xsl:if test="$adminServices">
						<tr>
							<td colspan="2"><b><xsl:value-of select="/root/gui/strings/admin"/></b></td>
						</tr>
						<xsl:copy-of select="$adminServices"/>
						<tr><td class="spacer"/></tr>
						<tr>
							<td class="padded"><a href="{/root/gui/locService}/test.i18n"><xsl:value-of select="/root/gui/strings/i18n"/></a></td>
							<td class="padded"><xsl:value-of select="/root/gui/strings/i18nDesc"/></td>
						</tr>
					</xsl:if>
					
					<tr>
						<td class="padded"><a href="{/root/gui/locService}/test.csw"><xsl:value-of select="/root/gui/strings/cswTest"/></a></td>
						<td class="padded"><xsl:value-of select="/root/gui/strings/cswTestDesc"/></td>
					</tr>
					
				</table>
				<p/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	
	
	<!-- ================================================================================= -->
	
	<xsl:template name="admin-index">
		
		<tr>
			<td class="padded"><xsl:value-of select="/root/gui/strings/metadata.admin.index"/></td>
			<td>
				<script language="JavaScript" type="text/javascript">   
					var msgSuccess = "<xsl:value-of select="/root/gui/strings/metadata.admin.index.success"/>";
					var msgFailed = "<xsl:value-of select="/root/gui/strings/metadata.admin.index.failed"/>";
					
					function idxRebuild(){
					  url = Env.locService + '/metadata.admin.index.rebuild';
					  $('wait').style.display = 'block';
					  $('btIdx').style.display = 'none';
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
					        $('wait').style.display = 'none';
					        $('btIdx').style.display = 'block';
					        if (resp == "true")
  					          alert (msgSuccess);
					        else
 					          alert(msgFailed);
					      },
					      onFailure: function(originalRequest){
					        $('wait').style.display = 'none';
					        $('btIdx').style.display = 'block';
					        alert(msgFailed);
					      }
					    }
					  );
					}
				</script>
				
				<form name="rebuild" accept-charset="UTF-8" action="{/root/gui/locService}/metadata.admin.index.rebuild" method="post">
					<xsl:value-of select="/root/gui/strings/metadata.admin.index.desc"/>
				</form>
				<button class="content" onclick="idxRebuild()" id="btIdx" name="btIdx"><xsl:value-of select="/root/gui/strings/rebuild"/></button>
				<img src="{/root/gui/url}/images/loading.gif" id="wait" style="display:none;"/>
			</td>
		</tr>
		
	</xsl:template>
	
</xsl:stylesheet>
