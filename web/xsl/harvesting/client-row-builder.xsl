<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	
	<xsl:template match="/root/node[@type='geonetwork' or @type='webFolder']">
		<xsl:apply-templates select="." mode="row"/>
		<xsl:apply-templates select="." mode="status"/>
		<xsl:apply-templates select="." mode="error"/>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	<!-- === Generate a table row for the harvesting entry list -->
	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="row">

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
						<img id="status" src="{/root/env/url}/images/fileclose.png" alt="I" />
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
						<img id="error" src="{/root/env/url}/images/button_ok.png" alt="I" />
					</xsl:when>
					<xsl:otherwise>
						<img id="error" src="{/root/env/url}/images/important.png" alt="R" />
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
			
			<td class="padded" id="node.lastRun">
				<xsl:value-of select="substring-before(info/lastRun, 'T')"/>
				&#xA0;
				<xsl:value-of select="substring-after(info/lastRun, 'T')"/>
			</td>

			<!-- Edit button - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded">
				<button class="content" onclick="harvesting.edit('{@id}')">
					<xsl:value-of select="/root/strings/edit"/>
				</button>
			</td>
		</tr>

	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- === Generate the status tooltip for the harvesting entry list -->
	<!-- ============================================================================================= -->
	
	<xsl:template match="*" mode="status">
		<xsl:variable name="status">
			<xsl:choose>
				<xsl:when test="options/status = 'inactive'">inactive</xsl:when>
					<xsl:when test="options/status = 'active'">
						<xsl:choose>
							<xsl:when test="info/running = 'true'">running</xsl:when>
							<xsl:otherwise>active</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
				</xsl:choose>
		</xsl:variable>
		
		<status><xsl:value-of select="/root/strings/statusTip[@type=$status]"/></status>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	<!-- === Generate the error tooltip for the harvesting entry list -->
	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="error">
		<error>
			<xsl:choose>
				<xsl:when test="count(error/*) = 0">
					<xsl:apply-templates select="info" mode="result_ok"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="error" mode="result_error"/>
				</xsl:otherwise>
			</xsl:choose>
		</error>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="result_ok">
		<xsl:choose>
			<xsl:when test="count(search) != 0">
				<table>
					<tr class="tipRow">
						<td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/siteId"/> </td>
						<td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/total"/> </td>
						<td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/added"/> </td>
						<td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/updated"/> </td>
						<td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/unchanged"/> </td>
						<td class="tipHeader"> <xsl:value-of select="/root/strings/tipHeader/skipped"/> </td>
					</tr>
					<xsl:for-each select="search">
						<tr class="tipRow">
							<td class="tipCell"><b><xsl:value-of select="@siteId"/></b></td>
							<td class="tipCell"><xsl:value-of select="total"/></td>
							<td class="tipCell"><xsl:value-of select="added"/></td>
							<td class="tipCell"><xsl:value-of select="updated"/></td>
							<td class="tipCell"><xsl:value-of select="unchanged"/></td>
							<td class="tipCell"><xsl:value-of select="skipped"/></td>					
						</tr>
					</xsl:for-each>
				</table>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:value-of select="/root/strings/notRun"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template match="*" mode="result_error">
		<table>
			<tr>
				<td><b>Class</b> </td>
				<td><xsl:value-of select="class"/></td>
			</tr>
			<tr>
				<td><b>Message</b> </td>
				<td><xsl:value-of select="message"/></td>
			</tr>
			<tr>
				<td valign="top"><b>Stack</b> </td>
				<td>
					<table>
						<xsl:for-each select="stack/at">
							<tr>
								<td><xsl:value-of select="@file"/> &#xA0;</td>
								<td><i><xsl:value-of select="@line"/></i></td>
							</tr>
						</xsl:for-each>
					</table>
				</td>
			</tr>
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->

</xsl:stylesheet>
