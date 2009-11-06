<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate the error tooltip for the harvesting entry list -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root">
		<xsl:apply-templates select="*"/>
	</xsl:template>

	<xsl:template match="error">
		<table>
			<tr>
				<td><b>Class</b> </td>
				<td><xsl:value-of select="class"/></td>
			</tr>
			<tr>
				<td><b>Message</b> </td>
				<td><xsl:value-of select="message"/></td>
			</tr>
			<tr>
				<td valign="top"><b>Stack</b> </td>
				<td>
					<table>
						<xsl:for-each select="stack/at">
							<tr>
								<td><xsl:value-of select="@file"/> &#xA0;</td>
								<td><i><xsl:value-of select="@line"/></i></td>
							</tr>
						</xsl:for-each>
					</table>
				</td>
			</tr>
			<xsl:if test="object">
				<tr>
					<td><b>Info</b> </td>
					<td><xsl:value-of select="object"/></td>
				</tr>
			</xsl:if>
		</table>
	</xsl:template>

	<!-- swallow the rest of the elements -->
	<xsl:template match="@*|node()">
		<xsl:apply-templates select="*"/>
	</xsl:template>
		
	<!-- ============================================================================================= -->

</xsl:stylesheet>
