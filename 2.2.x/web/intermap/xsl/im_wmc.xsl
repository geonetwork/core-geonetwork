<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:output method="xml" indent="no" encoding="UTF-8"/>
	<xsl:strip-space elements="*"/>
	<xsl:preserve-space elements="xsl:text"/>
	
	<xsl:include href="prettyprint.xsl"/>
	
	<xsl:template match="/" >		
		<xsl:apply-templates  select="//ViewContext"/> <!--  will use prettyprint templates -->		
	</xsl:template>
		
</xsl:stylesheet>
