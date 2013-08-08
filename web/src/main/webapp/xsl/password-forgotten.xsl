<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:import href="modal.xsl"/>
	
	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="content">
				<form id="forgottenpwd" name="forgottenpwd" accept-charset="UTF-8" action="{/root/gui/locService}/password.forgotten.submit" method="post">
					<table align="center">
						<tr>
							<th class="padded"><xsl:value-of select="/root/gui/strings/username"/></th>
							<td class="padded"><input class="content" type="text" name="username" size="20"/></td>
						</tr>
					</table>
					<input type="submit" style="display: none;" />
				</form>
			</xsl:with-param>
			<xsl:with-param name="buttons">
				<div align="center">
					<button class="content" onclick="processForgottenPwdSubmit('password.forgotten.submit')"><xsl:value-of select="/root/gui/strings/accept"/></button>
				</div>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
		
</xsl:stylesheet>
