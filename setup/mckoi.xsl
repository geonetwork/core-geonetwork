<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- === set activator ======================================= -->
	
	<xsl:template match="resource[@enabled='true']">
		<resource enabled="true">
			<xsl:apply-templates select="*"/>
			<activator class="org.fao.geonet.activators.McKoiActivator">
				<configFile>WEB-INF/db/db.conf</configFile>
			</activator>
		</resource>
	</xsl:template>
	
	<!-- === element copying =========================================== -->
	
	<xsl:template match="@*|node()">
		 <xsl:copy>
			  <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>

</xsl:stylesheet>
