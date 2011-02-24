<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:exslt="http://exslt.org/common">

	<xsl:output
		omit-xml-declaration="no" 
		method="xml" 
		indent="yes"
		encoding="UTF-8" />
	
	<xsl:template match="/">
		<xsl:copy-of select="/root/gui/startupError/*"/>
	</xsl:template>

	<xsl:template mode="css" match="/"/>

</xsl:stylesheet>
