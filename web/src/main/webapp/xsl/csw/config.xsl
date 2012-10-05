<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->

	<xsl:include href="../main.xsl"/>

	<!-- ============================================================================================= -->

	<xsl:variable name="style" select="'margin-left:50px;'"/>
	<xsl:variable name="width" select="'70px'"/>

	<!-- ============================================================================================= -->

	<xsl:template mode="script" match="/">      
		<script type="text/javascript" language="JavaScript1.2">
            var actualCswLang = "eng";

            function saveCapabilitiesInfo() {
                // Store actual values
                $('csw.title_' + actualCswLang).value      = $('csw.title').value;
                $('csw.abstract_' + actualCswLang).value   = $('csw.abstract').value;
                $('csw.fees_' + actualCswLang).value       = $('csw.fees').value;
                $('csw.accessConstraints_' + actualCswLang).value       = $('csw.accessConstraints').value

                document.cswCapabilitiesForm.submit();
            }

            function updateCswInfo(lang) {
                // Store actual values
                $('csw.title_' + actualCswLang).value      = $('csw.title').value;
                $('csw.abstract_' + actualCswLang).value   = $('csw.abstract').value;
                $('csw.fees_' + actualCswLang).value       = $('csw.fees').value;
                $('csw.accessConstraints_' + actualCswLang).value       = $('csw.accessConstraints').value;

                actualCswLang = lang;

                // Update textboxes
                $('csw.title').value      = $('csw.title_' + actualCswLang).value;
                $('csw.abstract').value   = $('csw.abstract_' + actualCswLang).value;
                $('csw.fees').value       = $('csw.fees_' + actualCswLang).value;
                $('csw.accessConstraints').value       = $('csw.accessConstraints_' + actualCswLang).value;
            }
		</script>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === page content -->
	<!-- ============================================================================================= -->

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/cswServerConfig"/>

			<xsl:with-param name="content">
				<xsl:call-template name="form"/>
			</xsl:with-param>

			<xsl:with-param name="buttons">
				<xsl:call-template name="buttons"/>
				<div align="left" style="{$style}">
				<table>
					<tr>
						<td class="padded" width="{$width}">
							<a href="test.csw">
								<xsl:value-of select="/root/gui/strings/cswTest"/>
							</a>
						</td>
						<td>
							<xsl:value-of select="/root/gui/strings/cswTestDesc"/>
						</td>
					</tr>
				</table>
				</div>
			</xsl:with-param>
			
		</xsl:call-template>
		
		
		
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="form">

		<form name="cswCapabilitiesForm" accept-charset="UTF-8" action="{/root/gui/locService}/csw.config.set" method="post">
		<div align="left" style="{$style}">
            <a href="{/root/gui/locService}/csw.customelementset.get" style="margin:20px 0px 20px 0px;display:block;outline:none;"><xsl:value-of select="/root/gui/strings/customize-elementset"/></a>

            <table>
            	<tr>
            		<td class="padded" width="{$width}"><label for="csw.enable"><xsl:value-of select="/root/gui/strings/cswServerEnable"/></label></td>
					<td class="padded"><input id="csw.enable" name="csw.enable" class="content" type="checkbox"/></td>
				</tr>
 				<tr>
 					<td class="padded"><label for="csw.metadataPublic"><xsl:value-of select="/root/gui/strings/cswServerMetadataPublic"/></label></td>
                    <td class="padded"><input id="csw.metadataPublic" name="csw.metadataPublic" class="content" type="checkbox"/></td>
                </tr>

                <tr>
                	<td class="padded"><label for="csw.contactId"><xsl:value-of select="/root/gui/strings/cswServerContact"/></label></td>
            		<td class="padded">
            			<select name="csw.contactId" id="csw.contactId">
            				<option value="-1"></option>
            				<xsl:for-each select="/root/gui/users/record">
            					<xsl:sort select="username"/>
            					<option>
            						<xsl:attribute name="value">
            							<xsl:value-of select="id"/>
            						</xsl:attribute>
            						<xsl:value-of select="username"/>
            						<xsl:text> ( </xsl:text><xsl:value-of select="surname"/>
            						<xsl:text> </xsl:text>
            						<xsl:value-of select="name"/><xsl:text> ) </xsl:text>
            					</option>
            				</xsl:for-each>
            			</select>
            		</td>
            	</tr>

            	<tr>
            		<td colspan="2">&#160;</td>
            	</tr>

                <!-- Language selector -->
                <tr>
                	<td class="padded">&#160;</td>
            		<td class="padded">
                        <select class="content" size="1" id="csw.lang"  onchange="javascript:updateCswInfo(this.value)">
						<xsl:for-each select="/root/gui/languages/record">
							<option value="{id}">
								<xsl:value-of select="name"/>
							</option>
						</xsl:for-each>
					</select>
            		</td>
            	</tr>



            	<tr>
            		<td class="padded"><label for="csw.title"><xsl:value-of select="/root/gui/strings/cswServerTitle"/></label></td>
            		<td class="padded"><input id="csw.title" class="content" type="text" value="" size="40"/></td>
            	</tr>
            	<tr>
            		<td class="padded"><label for="csw.abstract"><xsl:value-of select="/root/gui/strings/cswServerAbstract"/></label></td>
            		<td class="padded"><textarea id="csw.abstract" class="content" value="" rows="5" cols="38"/></td>
            	</tr>
            	<tr>
            		<td class="padded"><label for="csw.fees"><xsl:value-of select="/root/gui/strings/cswServerFees"/></label></td>
            		<td class="padded"><input id="csw.fees" class="content" type="text" value="" size="40"/></td>
            	</tr>
            	<tr>
            		<td class="padded"><label for="csw.accessConstraints"><xsl:value-of select="/root/gui/strings/cswServerAccessConstraints"/></label></td>
            		<td class="padded"><input id="csw.accessConstraints" class="content" type="text" value="" size="40"/></td>
            	</tr>
            	<tr>
            		<td class="padded" colspan="2">
                        <xsl:for-each select="/root/gui/languages/record">
                            <input id="csw.title_{id}" name="csw.title_{id}" class="content" type="hidden" value="" size="40"/>
                            <input id="csw.abstract_{id}" name="csw.abstract_{id}" class="content" type="hidden" value="" size="40"/>
                            <input id="csw.fees_{id}" name="csw.fees_{id}" class="content" type="hidden" value="" size="40"/>
                            <input id="csw.accessConstraints_{id}" name="csw.accessConstraints_{id}" class="content" type="hidden" value="" size="40"/>
						</xsl:for-each>
            		</td>
            	</tr>
            </table>
        </div>
        </form>
          <script>
		    <xsl:variable name="apos">'</xsl:variable>
			<xsl:variable name="escapedApos">\\'</xsl:variable>
            <xsl:for-each select="/root/response/record">
                $('csw.<xsl:value-of select="field"/>_<xsl:value-of select="langid"/>').value = '<xsl:value-of select="replace(translate(label, '&#xD;&#xA;', '\n'), $apos, $escapedApos)"/>';
            </xsl:for-each>

            	$("csw.enable").checked = <xsl:value-of select="/root/response/cswEnable"/>;
            	$("csw.metadataPublic").checked = <xsl:value-of select="/root/response/cswMetadataPublic"/>;
	             actualCswLang = $("csw.lang").value;

               // Update textboxes
                $('csw.title').value      = $('csw.title_' + actualCswLang).value;
                $('csw.abstract').value   = $('csw.abstract_' + actualCswLang).value;
                $('csw.fees').value       = $('csw.fees_' + actualCswLang).value;
                $('csw.accessConstraints').value       = $('csw.accessConstraints_' + actualCswLang).value;

                $('csw.contactId').value = '<xsl:value-of select="/root/response/cswContactId"/>';
        </script>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === Buttons -->
	<!-- ============================================================================================= -->

	<xsl:template name="buttons">
		<button class="content" onclick="load('{/root/gui/locService}/admin')">
			<xsl:value-of select="/root/gui/strings/back"/>
		</button>
		&#160;
		<button class="content" onclick="saveCapabilitiesInfo()"><xsl:value-of select="/root/gui/strings/save"/></button>
	</xsl:template>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
