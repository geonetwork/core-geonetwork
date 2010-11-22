<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


	<xsl:include href="main.xsl"/>

	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
		<script type="text/javascript" language="JavaScript">				
			function saveIndexLanguages() {
				document.indexLanguagesUpdateForm.submit();
			}					
		</script>		
	</xsl:template>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title">
				<xsl:value-of select="/root/gui/strings/useStopwords"/>
			</xsl:with-param>
			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<button class="content" onclick="goBack()"><xsl:value-of select="/root/gui/strings/back"/></button>
				&#160;
				<button class="content" onclick="saveIndexLanguages()"><xsl:value-of select="/root/gui/strings/save"/></button>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	form
	-->
	<xsl:template name="form">
		<form name="indexLanguagesUpdateForm" accept-charset="UTF-8" action="{/root/gui/locService}/index.languages.set" method="post">
			<input type="submit" style="display: none;" />
			<div id="this-is-just-to-get-the-content-left-thank-you" style="text-align:left;margin:0 auto 0 auto;">
				<table id="notifications" style="table-layout: fixed;">
					<col style="width:100px;"/>
                    <col style="width:100px;"/>
					<tbody>
					<tr>
						<td>
							<span style="text-transform: capitalize;"><xsl:value-of select="/root/gui/strings/name"/></span>
						</td>
						<td>
							<span style="text-transform: capitalize;"><xsl:value-of select="/root/gui/strings/selected"/></span>
						</td>
					</tr>
					<tr>
						<td style="margin:5px;padding:5px;">
							<xsl:text> </xsl:text>
						</td>
						<td style="margin:5px;padding:5px;">
							<xsl:text> </xsl:text>
						</td>						
					</tr>					
					<xsl:for-each select="/root/indexlanguages/indexlanguage">
						<xsl:sort select="name"/>
						<tr id="{name}-row">
							<td style="margin:5px;padding:5px;">
								<input class="content" type="hidden" id="{name}-name" name="name-{name}" value="{name}"/>
                                <label for="{name}-selected"><span style="text-transform: capitalize;"><xsl:value-of select="name"/></span></label>
							</td>
							<td style="margin:5px;padding:5px;">	
								<xsl:choose>
									<xsl:when test="selected = 'true'">
										<input class="content" type="checkbox" id="{name}-selected" name="selected-{name}" checked="checked"/>								
									</xsl:when>
									<xsl:otherwise>
										<input class="content" type="checkbox" id="{name}-selected" name="selected-{name}" />
									</xsl:otherwise>
								</xsl:choose>
							</td>
						</tr>
					</xsl:for-each>
					</tbody>
				</table>
			</div>
		</form>	
		
		<xsl:choose>
			<xsl:when test="//displayRebuildIndex">
				<div id="rebuild-index" style="text-align:left;margin:50px auto 50px auto;border:solid 1px #fff;width:50%;float:left;display:block;">
					<xsl:call-template name="rebuildIndexContents"/>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<div id="rebuild-index" style="text-align:left;margin:50px auto 50px auto;border:solid 1px #fff;width:50%;float:left;display:none;">
					<xsl:call-template name="rebuildIndexContents"/>
				</div>
			</xsl:otherwise>
		</xsl:choose>		
		
	</xsl:template>
	
	<xsl:template name="rebuildIndexContents">
			<script language="JavaScript" type="text/javascript">   
				var msgSuccess = "<xsl:value-of select="/root/gui/strings/metadata.admin.index.success"/>";
				var msgFailed = "<xsl:value-of select="/root/gui/strings/metadata.admin.index.failed"/>";
				var doYouReally = "<xsl:value-of select="/root/gui/strings/doYouReallyWantToDoThis"/>";
				
				function idxOperation(service, wait, btn){
					if (!confirm(doYouReally)) {
						return;
					 }

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
						$('rebuild-index').style.display='none';
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
			<div style="margin:10px;"><xsl:value-of select="/root/gui/strings/rebuildIndexMessage"/></div>
			<button class="content" style="margin:10px;" onclick="idxOperation('metadata.admin.index.rebuild','waitIdx', this.name);" id="btIdx" name="btIdx"><xsl:value-of select="/root/gui/strings/rebuild"/></button>
			<img src="{/root/gui/url}/images/loading.gif" id="waitIdx" style="display:none;"/>	
	</xsl:template>
	
</xsl:stylesheet>