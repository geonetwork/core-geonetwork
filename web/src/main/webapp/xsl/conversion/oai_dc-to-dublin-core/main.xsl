<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
                version="1.0"
                exclude-result-prefixes="oai_dc">

  <!-- ============================================================================= -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <!-- ============================================================================= -->

  <xsl:template match="oai_dc:dc">
    <simpledc>
      <xsl:copy-of select="*"/>
    </simpledc>
  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
