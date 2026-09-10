<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:dct="http://purl.org/dc/terms/"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:util="java:org.fao.geonet.util.XslUtil"
  xmlns:schema-org-fn="http://geonetwork-opensource.org/xsl/functions/schema-org"
  exclude-result-prefixes="#all"
  version="2.0">

  <xsl:import href="../../../iso19115-3.2018/formatter/jsonld/jsonld-utils.xsl"/>

  <xsl:output method="text"/>

  <xsl:variable name="baseUrl"
             select="util:getSettingValue('nodeUrl')"/>
  <xsl:variable name="catalogueName"
                select="util:getSettingValue('system/site/name')"/>

  <xsl:template match="/">
    <textResponse>
      <xsl:for-each select="/root/simpledc">
        <xsl:variable name="recordUri"
                      select="concat($baseUrl, 'api/records/', dc:identifier)"/>
        {
          "@context": "http://schema.org/",
          "@type": "schema:CreativeWork",
          "@id": <xsl:value-of select="schema-org-fn:toJsonText($recordUri)"/>,
          "includedInDataCatalog": [{
          "url": <xsl:value-of select="schema-org-fn:toJsonText($baseUrl)"/>,
          "name": <xsl:value-of select="schema-org-fn:toJsonText($catalogueName)"/>
          }],
          "inLanguage": <xsl:value-of select="schema-org-fn:toJsonText(dc:language)"/>,
          "name": <xsl:value-of select="schema-org-fn:toJsonText(dc:title)"/>,
          "description": <xsl:value-of select="schema-org-fn:toJsonText(dc:description)"/>
        }
      </xsl:for-each>
    </textResponse>
  </xsl:template>
</xsl:stylesheet>


