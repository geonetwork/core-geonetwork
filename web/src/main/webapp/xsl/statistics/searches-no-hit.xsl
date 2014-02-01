<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
    	<xsl:value-of select="root/gui/search/record/nohit"/>/<xsl:value-of select="root/gui/search/record/totalcount"/>
    	&#160;<xsl:value-of select="/root/gui/strings/stat.totalsearches"/>
    </xsl:template>
</xsl:stylesheet>
