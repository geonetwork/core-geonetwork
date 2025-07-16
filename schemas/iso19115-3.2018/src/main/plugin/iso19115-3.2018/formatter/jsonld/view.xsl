<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  exclude-result-prefixes="#all"
  version="2.0">

  <xsl:output method="text"/>

  <xsl:include href="iso19115-3.2018-to-jsonld.xsl"/>

  <xsl:variable name="metadata" select="/root/mdb:MD_Metadata|/mdb:MD_Metadata"/>

  <xsl:template match="/">
    <textResponse>
      <xsl:apply-templates mode="getJsonLD"
                           select="$metadata"/>
    </textResponse>
  </xsl:template>
</xsl:stylesheet>


