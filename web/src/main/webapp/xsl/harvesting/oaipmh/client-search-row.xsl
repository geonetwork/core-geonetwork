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
			<!-- Remove button - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<tr>
				<td>
					<img id="{@id}.oai.remove" style="cursor:pointer;" src="{/root/env/url}/images/fileclose.png" />					
				</td>
				<td class="padded" bgcolor="#D0E0FF" colspan="4"><b><xsl:value-of select="/root/strings/criteria"/></b></td>
			</tr>
			
			<!-- From field - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/from"/></td>
				<td class="padded">
				    <div id="{@id}.oai.from" class="cal"></div>
				    <input id="{@id}.oai.from_cal" type="hidden" value="{from}"/>
				    </td>
			</tr>

			<!-- Until field - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/until"/></td>
				<td class="padded">
                    <div id="{@id}.oai.until" class="cal"></div>
				    <input id="{@id}.oai.until_cal" type="hidden" value="{until}"/></td>
			</tr>

			<!-- Set dropdown - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/set"/></td>
				<td class="padded">
					<input id="{@id}.oai.set" class="content" type="text" value="{set}"/>
				</td>
				<td class="padded" colspan="2" id="{@id}.oai.sets.cols" style="display:none;">
					<xsl:value-of select="/root/strings/returnedSets"/>
					&#160;
					<select id="{@id}.oai.set_select" class="content" size="1" onchange="$('{@id}.oai.set').value = this.options[this.selectedIndex].value;"/>
				</td>
			</tr>

			<!-- Prefix dropdown - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/prefix"/></td>
				<td class="padded">
					<input id="{@id}.oai.prefix" class="content" type="text" value="{prefix}"/>
				</td>
				<td class="padded" colspan="2" id="{@id}.oai.prefixes.cols" style="display:none;">
					<xsl:value-of select="/root/strings/returnedPrefixes"/>
					&#160;
					<select id="{@id}.oai.prefix_select" class="content" size="1" onchange="$('{@id}.oai.prefix').value = this.options[this.selectedIndex].value;"/>
				</td>
			</tr>

			<!-- Stylesheet dropdown - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<!-- COMMENTED OUT : we need to find a better management of metadata conversion -->
			
			<tr>
				<td/>
				<td class="padded"><!--xsl:value-of select="/root/strings/stylesheet"/--></td>
				<td class="padded" colspan="3">
					<select id="{@id}.oai.stylesheet" class="content" size="1" style="display:none">
						<option value=""/>
					</select>
				</td>
			</tr>
			
		</table>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
