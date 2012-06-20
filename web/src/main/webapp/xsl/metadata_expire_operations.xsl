<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" />
    <xsl:include href="main.xsl" />
	<xsl:template mode="script" match="/">
        <!-- javascript -->
        <script language="JavaScript1.2" type="text/javascript">
            var expOps = [];
            expOps.emailAction = '<xsl:value-of select="/root/gui/strings/metadata.expired/email.action" />';
            expOps.unpublishAction = '<xsl:value-of select="/root/gui/strings/metadata.expired/unpublish.action" />';
            expOps.emailDefault = '<xsl:value-of select="/root/defaults/email" />';
            expOps.unpublishDefault = '<xsl:value-of select="/root/defaults/unpublish" />';

            expOps.emailVal = expOps.emailDefault;
            expOps.unpublishVal = expOps.unpublishDefault;

            expOps.textChanged = function(){
                if( $('unpublish_radio').checked ){
                    expOps.unpublishVal = $('limit').getValue();
                }else{
                    expOps.emailVal = $('limit').getValue();
                }
            }

            expOps.unpublishClicked = function(){
                $('limit').setValue(expOps.unpublishVal);
                $('submitButton').update(expOps.unpublishAction);
                $('tip').update("<xsl:value-of select="/root/gui/strings/metadata.expired/unpublish.desc"/>");
            }

            expOps.emailClicked = function(){
                $('limit').setValue(expOps.emailVal);
                $('submitButton').update(expOps.emailAction);
                $('tip').update("<xsl:value-of select="/root/gui/strings/metadata.expired/email.desc"/>");
            }

            expOps.reset = function(){
                var radio = $('unpublish_radio');

	            expOps.emailVal = expOps.emailDefault;
	            expOps.unpublishVal = expOps.unpublishDefault;

                if( radio.checked ){
                    $('limit').setValue(expOps.unpublishVal);
                }else{
                    $('limit').setValue(expOps.emailVal);
                }
            }

            expOps.doSubmit = function(){
                var url = '<xsl:value-of select="/root/gui/locService"/>';
                var radio = $('unpublish_radio');
                if( radio.checked ){
                    url += '/metadata.expired.unpublish';
                }else{
                    url += '/metadata.expired.email';
                }
                url += '?limit='+$('limit').getValue();

                load( url );
            }
            expOps.init = function(){
                $('unpublish_radio').observe('click', expOps.unpublishClicked);
                $('email_radio').observe('click', expOps.emailClicked);
                if( $F('email_radio') == null){
                    expOps.unpublishClicked();
                }else{
                    expOps.emailClicked();
                }

            }
            Event.observe(window, 'load', expOps.init);
        </script>
    </xsl:template>
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title">
				<xsl:value-of select="/root/gui/strings/metadata.expired/formTitle" />
			</xsl:with-param>
			<xsl:with-param name="content">
				<table>
					<tr>
						<td style="vertical-align:middle" ><xsl:value-of select="/root/gui/strings/metadata.expired/limit" />&#160;&#160;</td>
					   <td><input type="text" id="limit" onchange="javascript:expOps.textChanged()" value="{/root/defaults/email}"/></td>
						<td style="vertical-align:middle">&#160;<xsl:value-of select="/root/gui/strings/months" /></td></tr>
	                            <tr><td/><td>
	                               <input type="radio" name="type" id="email_radio" value="email" checked="true" onclick="javascript:expOps.emailClicked"/>
	                               <xsl:value-of select="/root/gui/strings/metadata.expired/email"/>
	                            </td></tr>
	                            <tr><td/><td>
	                               <input type="radio" name="type" id="unpublish_radio" value="unpublish" onclick="javascript:expOps.unpublishClicked"/>
	                               <xsl:value-of select="/root/gui/strings/metadata.expired/unpublish"/>
	                            </td></tr>
	                            </table>
                                <br/>
                                <div id="tip"><xsl:value-of select="/root/gui/strings/metadata.expired/email.desc"/></div>
                                <div id="results"></div>
                        </xsl:with-param>

						<xsl:with-param name="buttons">
							<button onclick="javascript:expOps.doSubmit();" id="submitButton" class="content">
								<xsl:value-of select="/root/gui/strings/metadata.expired/email.action" />
							</button>
                            &#160;
                        <!--<button class="content" type="button" onclick="javascript:expOps.reset();"><xsl:value-of select="/root/gui/strings/reset"/></button>
                            &#160;
                        --><button class="content" type="button" onclick="load('{/root/gui/locService}/admin');"><xsl:value-of select="/root/gui/strings/back"/></button>
                        </xsl:with-param>
                        <xsl:with-param name="formfooter" select="'false'"/>
                    </xsl:call-template>
    </xsl:template>


</xsl:stylesheet>