<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate the tooltips for the editor elements -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/element">
		<div>
			<xsl:choose>
				<xsl:when test="@error">
					<font color="#C00000"><xsl:value-of select="concat(/root/strings/error, ' : ', @error)"/></font>
				</xsl:when>
				
				<xsl:otherwise>
					<b><xsl:value-of select="label"/></b>
					<div style="height:6px;"/>
					<xsl:value-of select="description"/>
					
					<div style="height:6px;"/>
					<xsl:choose>
						<xsl:when test="condition">
							<font color="#C00000"><xsl:value-of select="condition"/></font>
						</xsl:when>
						
						<xsl:otherwise>
							<!-- if the condition is missing, the element could still be mandatory -->
							<!-- so, we cannot say that it is optional -->
							<!--font color="#008000"><xsl:value-of select="/root/strings/optional"/></font-->
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->
	
	<xsl:template match="strings"/>
	
	<!-- ============================================================================================= -->

</xsl:stylesheet>
