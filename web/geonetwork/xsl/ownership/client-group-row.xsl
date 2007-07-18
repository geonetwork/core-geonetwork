<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate the group list for the transfer ownership page  -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/group">

		<tr id="group.{id}">
			
			<!-- source group - - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded">
				<span id="{id}">
					<xsl:value-of select="label/*[name() = /root/env/language]"/>
				</span>
			</td>
			
			<!-- target group - - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded">
				<select id="source.user" class="content" name="type" size="1"/>
			</td>
			
			<!-- target user - - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded">
				<select id="source.user" class="content" name="type" size="1"/>
			</td>
			
			<!-- operation - - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded">
				<button class="content" onclick="ownership.transfer('group.{id}')">
					<xsl:value-of select="/root/strings/transfer"/>
				</button>
			</td>
		</tr>
			
	</xsl:template>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
