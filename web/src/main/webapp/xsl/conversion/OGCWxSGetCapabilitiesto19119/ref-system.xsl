<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns="http://www.isotc211.org/2005/gmd"
>

  <!-- ============================================================================= -->

  <xsl:template name="RefSystemTypes">
    <xsl:param name="srs"/>
    <referenceSystemIdentifier>
      <RS_Identifier>
        <code>
          <gco:CharacterString>
            <xsl:value-of select="$srs"/>
          </gco:CharacterString>
        </code>
      </RS_Identifier>
    </referenceSystemIdentifier>
  </xsl:template>


  <!-- ============================================================================= -->

</xsl:stylesheet>
