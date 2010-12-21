<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork"
	exclude-result-prefixes="xsl geonet">

	<xsl:include href="searchform_simple_template.xsl"/>
	
	<xsl:variable name="lang" select="/root/gui/language"/>
	
	<xsl:template match="/">
		<xsl:call-template name="simple_search_panel"/>
	</xsl:template>

</xsl:stylesheet>
