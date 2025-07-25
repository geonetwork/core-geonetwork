<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	exclude-result-prefixes="#all"
	version="2.0">

  <xsl:import href="../../../iso19115-3.2018/convert/ISO19139/fromISO19139.xsl"/>
  <xsl:import href="../../../iso19115-3.2018/formatter/jsonld/iso19115-3.2018-to-jsonld.xsl"/>

	<xsl:output method="text"/>

  <xsl:variable name="metadata">
    <xsl:for-each select="/root[gmd:MD_Metadata]|/gmd:MD_Metadata">
      <xsl:call-template name="to-iso19115-3"/>
    </xsl:for-each>
  </xsl:variable>

  <xsl:template match="/">
    <textResponse>
      <xsl:apply-templates mode="getJsonLD"
                           select="$metadata"/>
    </textResponse>
  </xsl:template>
</xsl:stylesheet>


