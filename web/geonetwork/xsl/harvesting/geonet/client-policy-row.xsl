<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate a table row for the harvesting's webdav privilege list -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/group">

		<tr id="gn.group.{@name}" class="policyGroup">
			<td class="padded"><xsl:value-of select="@name"/></td>
			
			<td class="padded">
				<select id="gn.copyPolicy" class="content" size="1">
					<!-- copy -->
	
					<option value="copy">
							<xsl:if test="@policy = 'copy'">
								<xsl:attribute name="selected">on</xsl:attribute>
							</xsl:if>
						<xsl:value-of select="/root/strings/policy/copy"/>
					</option>
					
					<!-- copy to intranet or create and copy -->
	
						<xsl:choose>
							<xsl:when test="@name = 'all'">
								<option value="copyToIntranet">
									<xsl:if test="@policy = 'copyToIntranet'">
										<xsl:attribute name="selected">on</xsl:attribute>
									</xsl:if>
									<xsl:value-of select="/root/strings/policy/copyToIntranet"/>
								</option>
							</xsl:when>
		
							<xsl:otherwise>
								<option value="createAndCopy">
									<xsl:if test="@policy = 'createAndCopy'">
										<xsl:attribute name="selected">on</xsl:attribute>
									</xsl:if>
									<xsl:value-of select="/root/strings/policy/createAndCopy"/>
								</option>
							</xsl:otherwise>
						</xsl:choose>
					
					<!-- don't copy -->
	
					<option value="dontCopy">
						<xsl:if test="@policy = 'dontCopy'">
							<xsl:attribute name="selected">on</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="/root/strings/policy/dontCopy"/>
					</option>
				</select>
			</td>			
		</tr>

	</xsl:template>

	
	<!-- ============================================================================================= -->
	
	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
