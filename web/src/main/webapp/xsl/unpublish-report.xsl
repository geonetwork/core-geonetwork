<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="text" indent="no" media-type="text/csv"></xsl:output>

	<xsl:template match="/">
		<xsl:text>"UUID","Changing Entity","Valid","Published","Change Date","Change Time","Failed Rules","Failed Assertions"</xsl:text>
		<xsl:apply-templates select="/root/report/allElements/record"/> 
	</xsl:template>
	<xsl:template match="record">
"<xsl:value-of select="uuid"/>","<xsl:value-of select="entity"/>","<xsl:value-of select="validated"/>","<xsl:value-of select="published"/>","<xsl:value-of select="changedate"/>","<xsl:value-of select="changetime"/>","<xsl:value-of select="failurerule"/>","<xsl:value-of select="failurereasons"/>"</xsl:template>

</xsl:stylesheet>
