<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate the group list for the transfer ownership page  -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/group">

		<tr id="{id}">
			
			<!-- source group - - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded">
				<span>
					<xsl:value-of select="label/*[name() = /root/env/language]"/>
				</span>
			</td>
			
			<!-- target group - - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded">
				<select id="target.group" class="content" name="type" size="1"/>
			</td>
			
			<!-- target user - - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded">
				<select id="target.user" class="content" name="type" size="1"/>
			</td>
			
			<!-- operation - - - - - - - - - - - - - - - - - - - - - - -->
			
			<td class="padded">
				<!--button class="content" onclick="ownership.transfer('{id}')">
					<xsl:value-of select="/root/strings/transfer"/>
				</button-->
				
				<a href="javascript:ownership.transfer('{id}')">
					<xsl:value-of select="/root/strings/transfer"/>
				</a>
			</td>
		</tr>
			
	</xsl:template>

	<!-- ============================================================================================= -->
	
	<xsl:template match="/root/strings"/>
	<xsl:template match="/root/env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
