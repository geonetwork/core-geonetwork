<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
  xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
  exclude-result-prefixes="#all"
  version="2.0">

  <xsl:import href="../../../iso19115-3.2018/convert/ISO19139/fromISO19139.xsl"/>
  <xsl:import href="../../../iso19115-3.2018/formatter/jsonld/iso19115-3.2018-to-jsonld.xsl"/>

  <xsl:output method="text"/>

  <xsl:template name="iso19139toJsonLD" as="xs:string*">
    <xsl:param name="record" as="node()"/>
    <xsl:param name="lang" as="xs:string?"/>

        <xsl:variable name="iso19115-3metadata" as="node()">
          <xsl:for-each select="$record">
            <xsl:call-template name="to-iso19115-3"/>
          </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="defaultLanguage"
                      select="(($iso19115-3metadata/mdb:defaultLocale/*/lan:language/*/@codeListValue, 'eng')[. != ''])[1]"/>

        <xsl:variable name="requestedLanguageExist"
                      select="$lang != ''
                          and count($iso19115-3metadata/mdb:otherLocale/*[lan:language/*/@codeListValue = $lang]/@id) > 0"/>

        <xsl:variable name="requestedLanguage"
                      select="if ($requestedLanguageExist)
                          then $lang
                          else $defaultLanguage"/>

        <xsl:call-template name="iso19115-3.2018toJsonLD">
          <xsl:with-param name="record" select="$iso19115-3metadata"/>
          <xsl:with-param name="requestedLanguage" select="$requestedLanguage"/>
        </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>





