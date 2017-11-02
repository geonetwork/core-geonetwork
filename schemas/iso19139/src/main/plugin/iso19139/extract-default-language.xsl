<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                version="1.0">

  <xsl:template match="gmd:MD_Metadata">
    <language>
      <xsl:value-of select="gmd:language/gmd:LanguageCode/@codeListValue"/>
    </language>
  </xsl:template>

</xsl:stylesheet>
