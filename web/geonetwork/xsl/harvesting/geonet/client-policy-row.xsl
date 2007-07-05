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
				<form>
					<!-- copy -->
	
					<div>
						<input name="gn.copyPolicy" type="radio" value="copy">
							<xsl:if test="@policy = 'copy'">
								<xsl:attribute name="checked" select="'on'"/>
							</xsl:if>
						</input>
						<xsl:value-of select="/root/strings/policy/copy"/>
					</div>
					
					<!-- copy to intranet or create and copy -->
	
					<div>
						<xsl:choose>
							<xsl:when test="@name = 'all'">
								<input name="gn.copyPolicy" type="radio" value="copyToIntranet">
									<xsl:if test="@policy = 'copyToIntranet'">
										<xsl:attribute name="checked" select="'on'"/>
									</xsl:if>
								</input>
								<xsl:value-of select="/root/strings/policy/copyToIntranet"/>
							</xsl:when>
		
							<xsl:otherwise>
								<input name="gn.copyPolicy" type="radio" value="createAndCopy">
									<xsl:if test="@policy = 'createAndCopy'">
										<xsl:attribute name="checked" select="'on'"/>
									</xsl:if>
								</input>
								<xsl:value-of select="/root/strings/policy/createAndCopy"/>
							</xsl:otherwise>
						</xsl:choose>
					</div>
					
					<!-- don't copy -->
	
					<div>
						<input name="gn.copyPolicy" type="radio" value="dontCopy">
							<xsl:if test="@policy = 'dontCopy'">
								<xsl:attribute name="checked" select="'on'"/>
							</xsl:if>
						</input>
						<xsl:value-of select="/root/strings/policy/dontCopy"/>
					</div>
				</form>
			</td>			
		</tr>

	</xsl:template>

	
	<!-- ============================================================================================= -->

</xsl:stylesheet>
