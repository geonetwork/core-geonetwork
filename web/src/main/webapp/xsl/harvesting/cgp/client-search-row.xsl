<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate a table that represents a search on the remote node -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/search">
		<div id="cgp.search">
			<p/>
			<xsl:apply-templates select="." mode="data"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="data">
		<table>
			<tr>
				<td>
					<a onclick="harvesting.cgp.removeSearch()">
						<img style="cursor:hand; cursor:pointer" src="{/root/env/url}/images/fileclose.png" alt="Remove"/>
					</a>

				</td>
				<td class="padded" bgcolor="#D0E0FF"><b><xsl:value-of select="/root/strings/search"/></b></td>
			</tr>

			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/freeText"/></td>
				<td class="padded"><input id="{@id}.cgp.anytext" class="content" type="text" value="{freeText}" size="30"/></td>
			</tr>

            <!-- From field - - - - - - - - - - - - - - - - - - - - - - - - - -->
            
            <tr>
                <td/>
                <td class="padded"><xsl:value-of select="/root/strings/from"/></td>
                <td class="padded">
                    <div id="{@id}.cgp.from" class="cal"></div>
                    <input id="{@id}.cgp.from_cal" type="hidden" value="{from}"/>
                    </td>
            </tr>

            <!-- Until field - - - - - - - - - - - - - - - - - - - - - - - - - -->
            
            <tr>
                <td/>
                <td class="padded"><xsl:value-of select="/root/strings/until"/></td>
                <td class="padded">
                    <div id="{@id}.cgp.until" class="cal"></div>
                    <input id="{@id}.cgp.until_cal" type="hidden" value="{until}"/></td>
            </tr>

			<!-- BBox fields - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<tr>
				<td/>
				<!-- <td class="padded" style="vertical-align:middle"><xsl:value-of select="/root/strings/extent"/></td> -->
				<td class="padded" colspan="3">
					<table>
						<tr>
							<td></td>
							<td style="text-align:center"><xsl:value-of select="/root/strings/latNorth"/></td>
							<td></td>
						</tr>
						<tr>
							<td></td>
							<td><input id="{@id}.cgp.latnorth" class="content" type="text" value="{latNorth}" size="12"/></td>
							<td></td>
						</tr>
						<tr>
							<td><xsl:value-of select="/root/strings/lonWest"/><input id="{@id}.cgp.lonwest" class="content" type="text" value="{lonWest}" size="12"/></td>
							<td style="text-align:center"><xsl:value-of select="/root/strings/extent"/></td>
							<td><input id="{@id}.cgp.loneast" class="content" type="text" value="{lonEast}" size="12"/><xsl:value-of select="/root/strings/lonEast"/></td>
						</tr>
						<tr>
							<td></td>
							<td><input id="{@id}.cgp.latsouth" class="content" type="text" value="{latSouth}" size="12"/></td>
							<td></td>
						</tr>
						<tr>
							<td></td>
							<td style="text-align:center"><xsl:value-of select="/root/strings/latSouth"/></td>
							<td></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
