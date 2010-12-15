<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate the error tooltip for the harvesting entry list -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/node">
		<xsl:choose>
			<xsl:when test="count(info/search) != 0">
				<table>
					<tr class="tipRow">
						<td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/siteId"/> </td>
						<td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/total"/> </td>
						<td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/added"/> </td>
						<td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/updated"/> </td>
						<td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/unchanged"/> </td>
						<td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/skipped"/> </td>
                        <td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/doesNotValidate"/> </td>
					</tr>
					<xsl:for-each select="info/search">
						<tr class="tipRow">
							<td class="tipCell"><b><xsl:value-of select="@siteId"/></b></td>
							<td class="tipCell"><xsl:value-of select="total"/></td>
							<td class="tipCell"><xsl:value-of select="added"/></td>
							<td class="tipCell"><xsl:value-of select="updated"/></td>
							<td class="tipCell"><xsl:value-of select="unchanged"/></td>
							<td class="tipCell"><xsl:value-of select="skipped"/></td>					
                            <td class="tipCell"><xsl:value-of select="doesNotValidate"/></td>					
						</tr>
					</xsl:for-each>
				</table>
			</xsl:when>
			
			<xsl:otherwise>
				<span><xsl:value-of select="/root/strings/notRun"/></span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
