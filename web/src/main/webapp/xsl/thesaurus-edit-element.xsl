<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:include href="main.xsl" />
    <xsl:include href="thesaurus-util.xsl"/>
    <xsl:include href="translate-widget.xsl" />



<!--additional scripts-->
<xsl:template mode="script" match="/">

<script type="text/javascript" src="{/root/gui/url}/scripts/translation_edit.js"/>
<script src="{/root/gui/url}/scripts/mapfishIntegration/ext-small.js" type="text/javascript"/>
<script type="text/javascript" language="JavaScript1.2">

Ext.onReady(function() {editI18n.init('prefLab', '<xsl:value-of select="/root/gui/language"/>');});

refreshParent = function(){
	if(window.opener)
	{
		window.opener.location.href="javascript:refresh();";
	}
}

function checkForm(form){
	// TODO : Check lat long in world
	if (checkString(form.newid,"<xsl:value-of select="/root/gui/strings/code"/>",false)==false){
		return false
	};

	<xsl:if test="string(/root/response/thesaType)='place'">
	if (checkString(form.west,"<xsl:value-of select="/root/gui/strings/westLon"/>",false)==false){
		return false;
	};
	if(isSignedFloat(form.west.value)==false){
		warnInvalid(form.west,"Invalid <xsl:value-of select="/root/gui/strings/westLon"/> coordinates");
		return false;
	}
	if (checkString(form.east,"<xsl:value-of select="/root/gui/strings/eastLon"/>",false)==false){
		return false;
	};
	if(isSignedFloat(form.east.value)==false){
		warnInvalid(form.east,"Invalid <xsl:value-of select="/root/gui/strings/eastLon"/> coordinates");
		return false;
	}
	if (checkString(form.south,"<xsl:value-of select="/root/gui/strings/southLat"/>",false)==false){
		return false;
	};
	if(isSignedFloat(form.south.value)==false){
		warnInvalid(form.south,"Invalid <xsl:value-of select="/root/gui/strings/southLat"/> coordinates");
		return false;
	}
	if (checkString(form.north,"<xsl:value-of select="/root/gui/strings/northLat"/>",false)==false){
		return false;
	};
	if(isSignedFloat(form.north.value)==false){
		warnInvalid(form.north,"Invalid <xsl:value-of select="/root/gui/strings/northLat"/> coordinates");
		return false;
	}
	</xsl:if>
	return true;
}

function submitKeywordForm(){
	var form = $('keywordForm');
	if (checkForm(form)){
        var url = form.action;
        var titi = Form.serialize('keywordForm');

        var http = new Ajax.Request(
                url,
                {
                    method: 'post',
                    postBody: titi,
					onSuccess: parseResponse,
					onFailure: function(originalRequest){
									alert('Error : ' + originalRequest.responseText);
								}
				});
	}
}

function parseResponse(t){
	var xmlobject = t.responseXML;
	if (xmlobject.getElementsByTagName("error").length > 0){
		alert('Error : ' + xmlobject.getElementsByTagName("message")[0].firstChild.nodeValue);
	} else if(window.opener)
	{
		window.opener.location.href="javascript:refresh();";
	}
}

</script>
</xsl:template>



<!--
page content
-->
<xsl:template name="content">
	<xsl:variable name="mode" select="/root/request/mode"/>

	<div align="center">
		<table><tr valign="top"><td>
		<form action="{/root/gui/locService}/thesaurus.addelement" name="keywordForm" id="keywordForm">
			<xsl:attribute name="action">
			 <xsl:value-of select="concat(/root/gui/locService,'/geocat.thesaurus.updateelement')"/>
			</xsl:attribute>

			<table width="60%" border="0">
					<tr>
					<td colspan="3" class="padded" valign="bottom">
					<table border="0" width="100%">
					   <tr>
							<td class="green-content" width="40%">::<xsl:value-of select="/root/gui/strings/thesaurus/keywordDescription"/>
						</td>
						<td class="content" valign="bottom" width="100%">
						   <table width="100%">
							  <tr>
								 <td class="dots"></td>
							  </tr>
							</table>
						</td>
					  </tr>
					</table>
              </td>
            </tr>
				<tr>
				<td class="dots">&#160;
              </td>
					<td class="padded-content" align="center">
						<table width="100%" border="0">

			<input type="hidden" name="ref" id="ref" value="{/root/response/thesaurus}"/>
			<input type="hidden" name="refType" id="ref" value="{/root/response/thesaType}"/>
			<input type="hidden" name="namespace" id="namespace" value="{/root/response/nsCode}"/>
			<input type="hidden" name="oldid" id="oldid">
				<xsl:attribute name="value"><xsl:value-of select="/root/response/relCode"/></xsl:attribute>
			</input>
						<tr>
                        <th class="md" valign="top" width="10%">
                          <xsl:value-of select="/root/gui/strings/identifier"/> &#160;
                        </th>
                        <td class="padded" valign="top">
							<xsl:choose>
								<xsl:when test="$mode='consult'">
										<xsl:value-of select="/root/response/relCode"/>
								</xsl:when>
								<xsl:otherwise>
									<input class="md" type="text" name="newid" id="newid">
										<xsl:attribute name="value"><xsl:value-of select="/root/response/relCode"/></xsl:attribute>
									</input>
								</xsl:otherwise>
							</xsl:choose>
                        </td>
                     </tr>
                     <tr>
                        <th class="md" valign="top" width="10%">
                          <xsl:value-of select="/root/gui/strings/label"/> &#160;
                        </th>
							<xsl:choose>
								<xsl:when test="$mode='consult'">
		                          <td class="padded" valign="top">
										<xsl:value-of select="/root/response/prefLab"/> (<xsl:value-of select="/root/gui/strings/child::*[name() = /root/gui/language]"/>)
  							      </td>
								</xsl:when>
								<xsl:otherwise>
                                  <td class="padded" valign="top">
                        			<xsl:call-template name="translationWidgetInputs">
				                        <xsl:with-param name="key" select="'prefLab'"/>
				                        <xsl:with-param name="root" select="/root/response/prefLab"/>
				                   </xsl:call-template>
				                    </td><td>
				                   <xsl:call-template name="translationWidgetSelect">
				                        <xsl:with-param name="key" select="'prefLab'"/>
				                   </xsl:call-template>  
				                   
				                      
									<!--<input class="md" name="prefLab" id="prefLab" type="text">
									   <xsl:attribute name="value"><xsl:value-of select="/root/response/prefLab"/></xsl:attribute>
									</input>&#160;

									<select class="md" name="lang" id="lang" type="text">
									   <xsl:for-each select="/root/gui/languages/record">
										   <xsl:variable name="langId" select="id"/>
										   <xsl:if test="$langId = /root/gui/language">

										    TODO : Add multilingual thesaurus editing. Actually, Geonetwork provide only
										   editing in the current GUI interface language 
										   <option>
                                                <xsl:attribute name="selected">selected</xsl:attribute>
                                                <xsl:attribute name="value"><xsl:value-of select="substring($langId, 1, 2)"/></xsl:attribute>
                                                <xsl:value-of select="name"/>
                                           </option>
										   </xsl:if>
									   </xsl:for-each>
								   </select>-->
                                  </td>
								</xsl:otherwise>
							</xsl:choose>

                     </tr>
                     <tr>
                        <th class="md" valign="top" width="15%">
                           <xsl:value-of select="/root/gui/strings/definition"/> &#160;
                        </th>
                        <td class="padded" valign="top">
							<xsl:choose>
								<xsl:when test="$mode='consult'">
										<xsl:value-of select="/root/response/definition"/>
								</xsl:when>
								<xsl:otherwise>
								   <textarea class="md" rows="6" cols="40" name="definition">
									   <xsl:value-of select="/root/response/definition"/>
								   </textarea>
								</xsl:otherwise>
							</xsl:choose>
                        </td>
                     </tr>


				<!-- bbox -->
				<xsl:if test="string(/root/response/thesaType)='place'">
					<tr>
                        <td>&#160;</td>
                        <td class="padded">
                          <table width="100%">
                             <tr>
                               <td>&#160;</td>
                               <td class="green-content" width="40%"> <xsl:value-of select="/root/gui/strings/geoBndBox"/>
                               </td>
                               <td class="content" valign="bottom" width="100%">
                                 <table width="100%">
                                   <tr><td class="dots"></td></tr>
                                 </table>
                               </td>
                               <td >&#160;</td>
                             </tr>
                             <tr>
                                <td class="dots">&#160;</td>
                                <td colspan="2" class="padded-content" align="center">
                                  <table width="100%">
                                     <tr>
										<td></td>
										<td><xsl:value-of select="/root/gui/strings/northbc"/><input size="5" value="" name="north" class="md" type="text">
												<xsl:attribute name="value"><xsl:value-of select="/root/response/north"/></xsl:attribute>
											</input></td>
                                        <td>
                                        </td>
                                     </tr>
                                     <tr>
										<td><xsl:value-of select="/root/gui/strings/westbc"/><input size="5" value="" name="west" class="md" type="text">
                                                <xsl:attribute name="value"><xsl:value-of select="/root/response/west"/></xsl:attribute>
                                            </input>
                                        </td><td></td>
                                        <td><xsl:value-of select="/root/gui/strings/eastbc"/><input size="5" value="" name="east" class="md" type="text">
											<xsl:attribute name="value"><xsl:value-of select="/root/response/east"/></xsl:attribute>
											</input></td>

                                     </tr>
                                     <tr>
										<td></td>
										<td><xsl:value-of select="/root/gui/strings/southbc"/><input size="5" value="" name="south" class="md" type="text">
												<xsl:attribute name="value"><xsl:value-of select="/root/response/south"/></xsl:attribute>
											</input></td>
                                        <td>
                                        </td>
                                     </tr>
							     </table>
                                </td>
                                <td class="dots">&#160;</td>
                              </tr>
                              <tr>
                                 <td colspan="3" class="dots"></td>
                              </tr>


                          </table>
                        </td>
                     </tr>
					</xsl:if>
                    </table>
              </td>
              <td class="dots">&#160;</td>
            </tr>
            <tr>
              <td colspan="3" class="dots"></td>
            </tr>
            <tr>
              <td colspan="3">&#160;</td>
            </tr>
            <tr>
               <td colspan="3" align="center">
				 <xsl:choose>
					<xsl:when test="$mode='consult'">
					</xsl:when>
					<xsl:otherwise>
						<button onclick="javascript:submitKeywordForm();" class="content" type="button"><xsl:value-of select="/root/gui/strings/submit"/></button>
					</xsl:otherwise>
				</xsl:choose>
                    &#160;
                  <button onclick="javascript:window.close();" class="content" type="button"><xsl:value-of select="/root/gui/strings/close"/></button>
				</td>
            </tr>
        </table>
 		</form>
		</td><td>
				<table>
				 <tr>
						<th class="md" valign="top" width="15%">
						</th>
						<td colspan="2">
							<xsl:if test="$mode!='edit'">
								<xsl:call-template name="legend">
									<xsl:with-param name="keywords" select="/root/response"/>
									<xsl:with-param name="mode">legend</xsl:with-param>
									<xsl:with-param name="thesaurus" select="/root/response/thesaurus"/>
									<xsl:with-param name="url" select="/root/gui/url"/>
								</xsl:call-template>
							</xsl:if>
						</td>
					</tr>
				</table>
		</td></tr></table>
		</div>
	</xsl:template>
</xsl:stylesheet>
