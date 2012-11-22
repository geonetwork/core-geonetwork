<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:gfc="http://www.isotc211.org/2005/gfc" 
	xmlns:gmx="http://www.isotc211.org/2005/gmx"
    xmlns:gco="http://www.isotc211.org/2005/gco" 
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
	
	<xsl:template mode="permitMarkup" match="gfc:scope | gfc:description | gfc:definition">
		<xsl:value-of select="true()" />
	</xsl:template>
</xsl:stylesheet>