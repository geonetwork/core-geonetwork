<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<table width="100%" height="90%">
			<tr>
				<td align="center" valign="middle">
					<table cellpadding="6" >
						<tr>
							<td>
								<b><xsl:value-of select="/root/gui/error/errorText" /></b>
							</td>
						</tr>
	
					</table>
				</td>
			</tr>
		</table>
	</xsl:template>
	
</xsl:stylesheet>
