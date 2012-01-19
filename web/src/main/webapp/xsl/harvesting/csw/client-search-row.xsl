<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate a table that represents a search on the remote node -->
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
					<a onclick="harvesting.csw.removeSearchRow('{@id}')">
						<img style="cursor:hand; cursor:pointer" src="{/root/env/url}/images/fileclose.png" alt="Remove"/>
					</a>
					
				</td>
				<td class="padded" bgcolor="#D0E0FF"><b><xsl:value-of select="/root/strings/search"/></b></td>
			</tr>
			
			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/freeText"/></td>
				<td class="padded"><input id="csw.anytext" class="content" type="text" value="{freeText}" size="30"/></td>
			</tr>

			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/title"/></td>
				<td class="padded"><input id="csw.title" class="content" type="text" value="{title}" size="30"/></td>
			</tr>

			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/abstract"/></td>
				<td class="padded"><input id="csw.abstract" class="content" type="text" value="{abstract}" size="30"/></td>
			</tr>

			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/subject"/></td>
				<td class="padded"><input id="csw.subject" class="content" type="text" value="{subject}" size="30"/></td>
			</tr>
			
			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/minscale"/></td>
				<td class="padded"><input id="csw.minscale" class="content" type="number" value="{minscale}" size="30"/></td>
			</tr>
			
			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/maxscale"/></td>
				<td class="padded"><input id="csw.maxscale" class="content" type="number" value="{maxscale}" size="30"/></td>
			</tr>
			
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
