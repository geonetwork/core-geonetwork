<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	exclude-result-prefixes="#all"
	version="2.0">

	<xsl:output method="text"/>

  <xsl:include href="iso19139-to-jsonld.xsl"/>

  <xsl:template match="/">
    <textResponse>
      <xsl:apply-templates mode="getJsonLD"
                           select="*/gmd:MD_Metadata"/>
    </textResponse>
  </xsl:template>
</xsl:stylesheet>


