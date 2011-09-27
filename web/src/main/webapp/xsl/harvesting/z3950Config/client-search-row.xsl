<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate a table that represents a search on the local node -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/search">
		<div id="{@id}">
			<p/>
			<xsl:apply-templates select="." mode="data"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->
		
	<xsl:template match="*" mode="data">
		<table>
			<tr>
				<td>
					<a onclick="harvesting.z3950Config.removeSearchRow('{@id}')">
						<img style="cursor:hand; cursor:pointer" src="{/root/env/url}/images/fileclose.png" alt="Remove"/>
					</a>
					
				</td>
				<td class="padded" bgcolor="#D0E0FF" colspan="2"><b><xsl:value-of select="/root/strings/criteria"/></b></td>
			</tr>
			
			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/freeText"/></td>
				<td class="padded"><input id="z3950Config.text" class="content" type="text" value="{freeText}" size="30"/></td>
			</tr>

			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/title"/></td>
				<td class="padded"><input id="z3950Config.title" class="content" type="text" value="{title}" size="30"/></td>
			</tr>

			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/abstract"/></td>
				<td class="padded"><input id="z3950Config.abstract" class="content" type="text" value="{abstract}" size="30"/></td>
			</tr>

			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/keywords"/></td>
				<td class="padded"><input id="z3950Config.keywords" class="content" type="text" value="{keywords}" size="30"/></td>
			</tr>

			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/category"/></td>
				<td class="padded"><input id="z3950Config.category" class="content" type="text" value="z3950Servers" disabled="disabled"/></td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->
	
	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
