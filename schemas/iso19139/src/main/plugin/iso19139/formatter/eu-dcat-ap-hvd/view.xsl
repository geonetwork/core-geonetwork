<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:import href="../../../iso19115-3.2018/convert/ISO19139/fromISO19139.xsl"/>
  <xsl:import href="../../../iso19115-3.2018/formatter/eu-dcat-ap-hvd/eu-dcat-ap-hvd-core.xsl"/>
  <xsl:import href="../dcat/dcat-utils.xsl"/>

  <xsl:template match="/">
    <xsl:apply-templates mode="dcat" select="root[gmd:MD_Metadata]|/gmd:MD_Metadata"/>
  </xsl:template>

  <xsl:template mode="dcat" match="*">
    <xsl:variable name="iso19115-3metadata">
      <xsl:for-each select=".">
        <xsl:call-template name="to-iso19115-3"/>
      </xsl:for-each>
    </xsl:variable>

    <rdf:RDF>
      <xsl:call-template name="create-namespaces"/>
      <xsl:apply-templates mode="iso19115-3-to-dcat"
                           select="$iso19115-3metadata"/>
    </rdf:RDF>
  </xsl:template>
</xsl:stylesheet>
