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

    <!-- If no search filters are defined, don't render the element -->
    <xsl:if test="count(/root/search/*) > 0">
		<table>
			<tr>
				<td>
					<a onclick="harvesting.csw.removeSearchRow('{@id}')">
						<img style="cursor:hand; cursor:pointer"
							src="{/root/env/url}/images/fileclose.png" alt="Remove"/>
					</a>

				</td>
				<td class="padded" bgcolor="#D0E0FF">
					<b>
						<xsl:value-of select="/root/strings/search"/>
					</b>
				</td>
			</tr>



			<xsl:for-each select="/root/search/*">
				<xsl:sort select="local-name()"/>
				
				<xsl:variable name="fieldId" select="concat('csw.',local-name())"/>
				<tr>
					<td/>
					<td class="padded">
						<label for="{$fieldId}"><xsl:value-of select="local-name()"/></label>
					</td>
					<td class="padded">
						<input type="text">
							<xsl:attribute name="id">
								<xsl:value-of select="$fieldId"/>
							</xsl:attribute>
							<xsl:attribute name="class">content</xsl:attribute>
							<xsl:attribute name="value">
								<xsl:value-of select="value"/>
							</xsl:attribute>
							<xsl:attribute name="size">30</xsl:attribute>
						</input>
					</td>
				</tr>
			</xsl:for-each>


		</table>
    </xsl:if>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
