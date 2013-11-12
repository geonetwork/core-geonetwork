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
			
			
			
		<xsl:for-each select="/root/search/*">
			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="local-name()"/></td>
				 <td class="padded">
		<input type="text" >
			<xsl:attribute name="id">
				<xsl:value-of select="concat('csw.',local-name())" />
			</xsl:attribute>
			<xsl:attribute name="class">content</xsl:attribute>
			<xsl:attribute name="value"></xsl:attribute>
			<xsl:attribute name="size">30</xsl:attribute>
		</input>
					</td> 
			</tr>
		</xsl:for-each>

						
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
