<xsl:stylesheet version="1.0"
                xmlns="http://www.interlis.ch/INTERLIS2.3"                
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                
     <xsl:param name="group">geocat.ch</xsl:param>
                
    <xsl:template name="header">
        <HEADERSECTION VERSION='2.3' SENDER='{$group}'>
          <MODELS/>
        </HEADERSECTION>
    </xsl:template>
</xsl:stylesheet>