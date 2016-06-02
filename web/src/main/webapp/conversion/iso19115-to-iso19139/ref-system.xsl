<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns="http://www.isotc211.org/2005/gmd">

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="RefSystemTypes">

    <xsl:for-each select="RefSystem/refSysID">
      <referenceSystemIdentifier>
        <RS_Identifier>
          <xsl:apply-templates select="." mode="MdIdent"/>
        </RS_Identifier>
      </referenceSystemIdentifier>
    </xsl:for-each>

  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
