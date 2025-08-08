<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
  xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
	exclude-result-prefixes="#all"
	version="2.0">

  <xsl:import href="iso19139-to-jsonld.xsl"/>

	<xsl:output method="text"/>

  <xsl:template match="/">
    <textResponse>
      <xsl:for-each select="root/gmd:MD_Metadata">
        <xsl:call-template name="iso19139toJsonLD">
          <xsl:with-param name="record" select="."/>
          <xsl:with-param name="lang" select="$lang"/>
        </xsl:call-template>
      </xsl:for-each>
    </textResponse>
  </xsl:template>
</xsl:stylesheet>





