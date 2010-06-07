<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml"/>

	<xsl:template match="/">
		<xsl:apply-templates select="/root/response"/>
	</xsl:template>
		
	<xsl:template match="response">
		<table>
			<tr>
				<td style="padding-right:5px"><b>Title</b></td>
				<td><xsl:value-of select="./title"/></td>				
			</tr>
			<tr>
				<td style="padding-right:5px"><b>Abstract</b></td>
				<td><xsl:value-of select="./abstract"/></td>
			</tr>

			<xsl:if test="./legendURL">
				<tr>
					<td style="padding-right:5px"><b>Legend</b></td>
					<td>	
						<img src="{./legendURL}"  title="{/root/gui/strings/legend}"/>
					</td>
				</tr>
			</xsl:if>
			
			<xsl:if test="./info">
				<tr>
					<td style="padding-right:5px"><b><xsl:value-of select="./info/@type"/></b></td>
					<td><xsl:value-of select="./info"/></td>
				</tr>
			</xsl:if>				
									
		</table>			
	</xsl:template>
	
</xsl:stylesheet>
