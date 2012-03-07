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
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/subtemplatesAdded"/></td>
				<td class="tipCell"><xsl:value-of select="subtemplatesAdded"/></td>
			</tr>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/subtemplatesUpdated"/></td>
				<td class="tipCell"><xsl:value-of select="subtemplatesUpdated"/></td>
			</tr>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/subtemplatesRemoved"/></td>
				<td class="tipCell"><xsl:value-of select="subtemplatesRemoved"/></td>
			</tr>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/fragmentsUnknownSchema"/></td>
				<td class="tipCell"><xsl:value-of select="fragmentsUnknownSchema"/></td>					
			</tr>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/fragmentsReturned"/></td>
				<td class="tipCell"><xsl:value-of select="fragmentsReturned"/></td>
			</tr>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/fragmentsMatched"/></td>
				<td class="tipCell"><xsl:value-of select="fragmentsMatched"/></td>
			</tr>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/recordsBuilt"/></td>
				<td class="tipCell"><xsl:value-of select="recordsBuilt"/></td>
			</tr>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/recordsUpdated"/></td>
				<td class="tipCell"><xsl:value-of select="recordsUpdated"/></td>
			</tr>
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/doesNotValidate"/></td>
				<td class="tipCell"><xsl:value-of select="doesNotValidate"/></td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
