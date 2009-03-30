<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <!--
    page content
    -->
    <xsl:template match="/">
        <font class="error"><xsl:value-of select="/root/gui/error/message"/></font>
    </xsl:template>
    
</xsl:stylesheet>
