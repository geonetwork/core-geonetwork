<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === Generate a table row for the harvesting entry list -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/node[@type='geonetwork' or @type='webFolder']">

		<tr id="{@id}">
			<td class="padded" align="center"><input type="checkbox"/></td>
			<td class="padded" id="node.name"><xsl:value-of select="@name"/></td>

			<!-- Type - - - - - - - - - - - - - - - - - - - - - -->

			<td class="padded">
				<xsl:if test="@type='geonetwork'">
					<xsl:value-of select="/root/strings/typeGNShort"/>
				</xsl:if>

				<xsl:if test="@type='webFolder'">
					<xsl:value-of select="/root/strings/typeWFShort"/>
				</xsl:if>
			</td>

			<!-- Status - - - - - - - - - - - - - - - - - - - - - -->

			<td class="padded" align="center">
				<xsl:choose>
					<xsl:when test="options/status = 'inactive'">
						<img id="status" src="{/root/env/url}/images/stop.png" alt="I" />
					</xsl:when>
					<xsl:when test="options/status = 'active'">
						<xsl:choose>
							<xsl:when test="info/running = 'true'">
								<img id="status" src="{/root/env/url}/images/exec.png" alt="R" />
							</xsl:when>
							<xsl:otherwise>
								<img id="status" src="{/root/env/url}/images/clock.png" alt="A" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
				</xsl:choose>
			</td>

			<!-- Errors - - - - - - - - - - - - - - - - - - - - - -->

			<td class="padded" align="center">
				<xsl:choose>
					<xsl:when test="count(error/*) = 0">
						<img src="{/root/env/url}/images/button_ok.png" alt="I" />
					</xsl:when>
					<xsl:otherwise>
						<img src="{/root/env/url}/images/important.png" alt="R" />
					</xsl:otherwise>
				</xsl:choose>
			</td>

			<!-- Every - - - - - - - - - - - - - - - - - - - - - -->
			
			<xsl:variable name="every" select="options/every"/>
			<xsl:variable name="mins"  select="$every mod 60"/>
			<xsl:variable name="hours" select="($every - $mins) div 60 mod 24"/>
			<xsl:variable name="days"  select="($every - $mins - $hours * 60) div 1440"/>
			
			<td class="padded" id="node.every"><xsl:value-of select="concat($days, ':', $hours, ':', $mins)"/></td>
			
			<!-- Last run - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded" id="node.lastRun"><xsl:value-of select="info/lastRun"/></td>

			<!-- Edit button - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded">
				<button class="content" onclick="harvesting.edit('{@id}')">
					<xsl:value-of select="/root/strings/edit"/>
				</button>
			</td>
		</tr>

	</xsl:template>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
