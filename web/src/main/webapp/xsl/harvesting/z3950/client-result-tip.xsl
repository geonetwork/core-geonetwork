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
				<table>
					<xsl:for-each select="info/result/*">
						<xsl:apply-templates select="."/>
					</xsl:for-each>
				</table>
			</xsl:when>
			
			<xsl:otherwise>
				<span><xsl:value-of select="/root/strings/notRun"/></span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="stats">
			<tr class="tipRow">
				<td class="tipHeader" colspan="2"><xsl:value-of select="@server"/></td>
			</tr>
			<xsl:apply-templates select="*"/>
			<tr class="tipRow">
				<td class="dots" colspan="2"/>
			</tr>
	</xsl:template>

	<xsl:template match="total">
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/total"/></td>
				<td class="tipCell"><xsl:value-of select="string(.)"/></td>
			</tr>
	</xsl:template>

	<xsl:template match="added">
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/added"/></td>
				<td class="tipCell"><xsl:value-of select="string(.)"/></td>
			</tr>
	</xsl:template>

	<xsl:template match="removed">
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/removed"/></td>
				<td class="tipCell"><xsl:value-of select="string(.)"/></td>
			</tr>
	</xsl:template>

	<xsl:template match="updated">
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/updated"/></td>
				<td class="tipCell"><xsl:value-of select="string(.)"/></td>
			</tr>
	</xsl:template>

	<xsl:template match="unchanged">
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/unchanged"/></td>
				<td class="tipCell"><xsl:value-of select="string(.)"/></td>
			</tr>
	</xsl:template>

	<xsl:template match="unknownSchema">
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/unknownSchema"/></td>
				<td class="tipCell"><xsl:value-of select="string(.)"/></td>					
			</tr>
	</xsl:template>

	<xsl:template match="unretrievable">
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/unretrievable"/></td>
				<td class="tipCell"><xsl:value-of select="string(.)"/></td>					
			</tr>
	</xsl:template>

	<xsl:template match="badFormat">
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/badFormat"/></td>
				<td class="tipCell"><xsl:value-of select="string(.)"/></td>					
			</tr>
	</xsl:template>

	<xsl:template match="doesNotValidate">
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/doesNotValidate"/></td>
				<td class="tipCell"><xsl:value-of select="string(.)"/></td>					
			</tr>
	</xsl:template>

	<xsl:template match="couldNotInsert">
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="/root/strings/tipHeader/couldNotInsert"/></td>
				<td class="tipCell"><xsl:value-of select="string(.)"/></td>					
			</tr>
	</xsl:template>

	<xsl:template match="server">
			<tr class="tipRow">
				<td class="tipHeader"><xsl:value-of select="substring-before(string(.),'!!')"/></td>
				<td class="tipCell"><xsl:value-of select="substring-after(string(.),'!!')"/></td>
			</tr>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->
	
	<!-- ============================================================================================= -->

</xsl:stylesheet>
