<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate the result tooltip for the harvesting entry list -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/node">
		<xsl:choose>
			<xsl:when test="info/result">
				<xsl:apply-templates select="info/result"/>
			</xsl:when>
			
			<xsl:otherwise>
				<span><xsl:value-of select="/root/strings/notRun"/></span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="result">
		<table>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/total"/></td>
				<td class="tipCell"><xsl:value-of select="total"/></td>
			</tr>

			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/added"/></td>
				<td class="tipCell"><xsl:value-of select="added"/></td>
			</tr>

			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/removed"/></td>
				<td class="tipCell"><xsl:value-of select="removed"/></td>
			</tr>

			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/updated"/></td>
				<td class="tipCell"><xsl:value-of select="updated"/></td>
			</tr>

			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/unchanged"/></td>
				<td class="tipCell"><xsl:value-of select="unchanged"/></td>
			</tr>

			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/unknownSchema"/></td>
				<td class="tipCell"><xsl:value-of select="unknownSchema"/></td>					
			</tr>
			
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/duplicatedResource"/></td>
				<td class="tipCell"><xsl:value-of select="duplicatedResource"/></td>
			</tr>
			
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/unretrievable"/></td>
				<td class="tipCell"><xsl:value-of select="unretrievable"/></td>					
			</tr>
            <tr class="tipRow">
                <td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/doesNotValidate"/> </td>
                <td class="tipCell"><xsl:value-of select="doesNotValidate"/></td>
            </tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
