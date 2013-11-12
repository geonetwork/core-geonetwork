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
					<a onclick="harvesting.geonet.removeSearchRow('{@id}')">
						<img style="cursor:hand; cursor:pointer" src="{/root/env/url}/images/fileclose.png" alt="Remove"/>
					</a>
					
				</td>
				<td class="padded" bgcolor="#D0E0FF" colspan="2"><b><xsl:value-of select="/root/strings/criteria"/></b></td>
			</tr>
			
			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/freeText"/></td>
				<td class="padded"><input id="gn.text" class="content" type="text" value="{freeText}" size="30"/></td>
			</tr>

			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/title"/></td>
				<td class="padded"><input id="gn.title" class="content" type="text" value="{title}" size="30"/></td>
			</tr>

			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/abstract"/></td>
				<td class="padded"><input id="gn.abstract" class="content" type="text" value="{abstract}" size="30"/></td>
			</tr>

			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/keywords"/></td>
				<td class="padded"><input id="gn.keywords" class="content" type="text" value="{keywords}" size="30"/></td>
			</tr>
			
			<tr>
				<td/>
				<td class="padded"><input id="gn.anyField" class="content" type="text" value="{anyField}" size="30"/></td>
				<td class="padded"><input id="gn.anyValue" class="content" type="text" value="{anyValue}" size="30"/></td>
			</tr>
			
			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/digital"/></td>
				<td class="padded">
					<input id="gn.digital" class="content" type="checkbox" value="" size="30">
						<xsl:if test="digital = 'true'">
							<xsl:attribute name="checked"/>
						</xsl:if>
					</input>
				</td>
			</tr>

			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/hardcopy"/></td>
				<td class="padded">
					<input id="gn.hardcopy" class="content" type="checkbox" value="" size="30">
						<xsl:if test="hardcopy = 'true'">
							<xsl:attribute name="checked"/>
						</xsl:if>
					</input>
				</td>
			</tr>
			
			<tr>
				<td/>
				<td class="padded"><xsl:value-of select="/root/strings/source"/></td>
				<td class="padded">
					<select id="gn.source" class="content" size="1">
						<option value=""/>
						<xsl:choose>
							<xsl:when test="sources">
								<xsl:for-each select="sources/source">
									<option value="{@uuid}"><xsl:value-of select="@name"/></option>
								</xsl:for-each>
							</xsl:when>
							
							<xsl:otherwise>
								<xsl:if test="string(source/uuid) != ''">
									<option value="{source/uuid}" selected="on">
										<xsl:value-of select="source/name"/>
									</option>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>
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
