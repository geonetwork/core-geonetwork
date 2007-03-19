<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:include href="main.xsl"/>
	<xsl:include href="thesaurus-util.xsl"/>
	<!--
	additional scripts
	-->
	<xsl:template mode="script" match="/">
		<!-- javascript -->
		<script language="JavaScript1.2" type="text/javascript">
			var locService= "<xsl:value-of select="/root/gui/locService"/>";
			var locUrl    = "<xsl:value-of select="/root/gui/locUrl"/>";
			var url       = "<xsl:value-of select="/root/gui/url"/>";
			var foundWords = '<xsl:value-of select="/root/gui/strings/foundWords"/>';
			var pages = '<xsl:value-of select="/root/gui/strings/pages"/>';
			var selection = '<xsl:value-of select="/root/gui/strings/selection"/>';
			var sort = '<xsl:value-of select="/root/gui/strings/sort"/>';
			var label = '<xsl:value-of select="/root/gui/strings/label"/>';
			var definition = '<xsl:value-of select="/root/gui/strings/definition"/>';
		</script>
		<script type="text/javascript" src="{/root/gui/url}/scripts/geonetwork.js" language="JavaScript"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/geonetwork-ajax.js" language="JavaScript"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/form_check.js" language="JavaScript"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/prototype.js" language="JavaScript"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/keywordsearching.js" language="JavaScript"/>
		<script type="text/javascript" src="{/root/gui/url}/scripts/scriptaculous/scriptaculous.js?load=effects,controls"/>
		<script language="JavaScript1.2" type="text/javascript">
		
			mode = '<xsl:value-of select="/root/response/mode"/>';
			
			function checkSearchSubmit()
			{	
				if($F('thesaSelected')=='true'){
				<xsl:variable name="thesaName" select="/root/response/selectedThesaurus"/>
			<xsl:if test="$thesaName!=''">
				<xsl:variable name="thesaOk" select="count(/root/response/thesaurusList/directory/thesaurus[@value=$thesaName])"/>
				<xsl:choose>
					<xsl:when test="$thesaOk=0">
						alert('<xsl:value-of select="/root/gui/strings/thesaurus/unknown"/> : <xsl:value-of select="$thesaName"/>.');	
						return false;				
					</xsl:when>
					<xsl:otherwise>
						return true;
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
				} else{				
					return true;
				}
			}
					
			function doSearchSubmit()
			{
				if (checkSearchSubmit())
				{		
					ksearching.search(document.simplesearch);
				}
			}		

		function removeKeyword(){			
			if (confirm('<xsl:value-of select="/root/gui/strings/thesaurus/keywordConfirm"/>')){
				ksearching.deleteKeyword(document.simplesearch);
			}
		}	
		function refresh(){					
				doSearchSubmit();
				msgWindow.close();	
			}		
			
	
		</script>
	</xsl:template>
	<!--
	page content
	-->
	<xsl:template name="content">
		<table width="100%" height="100%">
			<tr>
				<td>
					<xsl:variable name="mode" select="/root/response/mode"/>
					<xsl:call-template name="formLayout">
						<xsl:with-param name="title">
							<xsl:choose>
								<xsl:when test="$mode='edit'">
									<xsl:value-of select="/root/gui/strings/editionThesaurus"/>&#160;<xsl:value-of select="/root/response/selectedThesaurus"/>
								</xsl:when>
								<xsl:when test="$mode='consult'">
									<xsl:value-of select="/root/gui/strings/consultationThesaurus"/>&#160;<xsl:value-of select="/root/response/selectedThesaurus"/>
								</xsl:when>
							</xsl:choose>			
						</xsl:with-param>
						<xsl:with-param name="content">
							<xsl:call-template name="form"/>
						</xsl:with-param>
						<xsl:with-param name="buttons">
							<button onclick="javascript:doSearchSubmit();" class="content">
								<xsl:value-of select="/root/gui/strings/search"/>
							</button>
    				&#160;
						<xsl:if test="$mode='edit'">	
							<xsl:call-template name="buttonAdd">
								<xsl:with-param name="mode" select="$mode"/>
								<xsl:with-param name="thesaurus" select="/root/response/selectedThesaurus"/>
							</xsl:call-template>
		    				&#160;
						</xsl:if>
						<button class="content" type="button" onclick="load('{/root/gui/locService}/thesaurus.admin');"><xsl:value-of select="/root/gui/strings/back"/></button>
						</xsl:with-param>
						<xsl:with-param name="formfooter" select="'false'"/>
					</xsl:call-template>
				</td>
			</tr>
			<tr>
				<td height="100%">
					<xsl:call-template name="formLayout">
						<xsl:with-param name="content">
							<div id="divResults" style="display:none"/>
						</xsl:with-param>
					</xsl:call-template>
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template name="form">
		<form method="get" action="javascript:doSearchSubmit();" name="simplesearch">
			<input value="200" type="hidden" id="nbResults" name="nbResults"/>
			<input type="hidden" value="true" id="thesaSelected"/>
			<input type="hidden" id="pThesauri">
				<xsl:attribute name="value"><xsl:value-of select="/root/response/selectedThesaurus"/></xsl:attribute>
			</input>
			<table align="center">
				<tr>
					<td class="padded-content">
						<table>
							<tr>
								<td class="padded-content" colspan="2">
									<table>
										<tr>
											<td>
												<xsl:value-of select="/root/gui/strings/keywords"/>
											</td>
											<td class="padded-content">
												<input value="" size="30" name="pKeyword" id="pKeyword" class="content" autocomplete="off"></input>
												<div id="keywordList" class="keywordList"></div>
												<script type="text/javascript">
													  document.simplesearch.pKeyword.focus();

													  new Ajax.Autocompleter('pKeyword', 'keywordList', 'xml.search.keywords?pMode=search&amp;pNewSearch=true&amp;pThesauri=<xsl:value-of select="/root/response/selectedThesaurus"/>&amp;nbResults=&amp;pTypeSearch=1&amp;',{method:'get', paramName: 'pKeyword'});
												</script>
											</td>
										
											<td class="padded-content">
												<input type="radio" id="pTypeSearch" name="pNameTypeSearch" value="0"/>
											</td>
											<td class="commonText">
												<xsl:value-of select="/root/gui/strings/startsWith"/>
											</td>
											<td class="padded-content">
												<input type="radio" id="pTypeSearch" name="pNameTypeSearch" value="1">
												<xsl:attribute name="checked"/>
												</input>
											</td>
											<td class="commonText">
												<xsl:value-of select="/root/gui/strings/contains"/>
											</td>
											<td class="padded-content">
												<input type="radio" id="pTypeSearch" name="pNameTypeSearch" value="2"/>
											</td>
											<td class="commonText">
												<xsl:value-of select="/root/gui/strings/exactTerm"/>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>
</xsl:stylesheet>
