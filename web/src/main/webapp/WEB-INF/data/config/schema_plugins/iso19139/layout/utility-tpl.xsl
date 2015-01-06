<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
  xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all">

  <xsl:include href="utility-tpl-multilingual.xsl"/>


  <xsl:template name="get-iso19139-extents-as-json">[
    <xsl:for-each select="//gmd:geographicElement/gmd:EX_GeographicBoundingBox">
      <xsl:variable name="format" select="'#0.0000'"></xsl:variable>

      <xsl:if test="number(gmd:westBoundLongitude/gco:Decimal)
            and number(gmd:southBoundLatitude/gco:Decimal)
            and number(gmd:eastBoundLongitude/gco:Decimal)
            and number(gmd:northBoundLatitude/gco:Decimal)
            ">
        [
          <xsl:value-of select="format-number(gmd:westBoundLongitude/gco:Decimal, $format)"/>,
          <xsl:value-of select="format-number(gmd:southBoundLatitude/gco:Decimal, $format)"/>,
          <xsl:value-of select="format-number(gmd:eastBoundLongitude/gco:Decimal, $format)"/>,
          <xsl:value-of select="format-number(gmd:northBoundLatitude/gco:Decimal, $format)"/>
        ]
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:if>
    </xsl:for-each>
    ]
  </xsl:template>

  <xsl:template name="get-iso19139-online-source-config">
    <xsl:param name="pattern"/>
    <config>
      <xsl:for-each select="$metadata/descendant::gmd:onLine[
        matches(
        gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString,
        $pattern) and
        normalize-space(gmd:CI_OnlineResource/gmd:linkage/gmd:URL) != '']">
        <xsl:variable name="protocol" select="gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString"/>
        <xsl:variable name="fileName">
          <xsl:choose>
            <xsl:when test="matches($protocol, '^DB:.*')">
              <xsl:value-of select="concat(gmd:CI_OnlineResource/gmd:linkage/gmd:URL, '#', 
                gmd:CI_OnlineResource/gmd:name/gco:CharacterString)"/>
            </xsl:when>
            <xsl:when test="matches($protocol, '^FILE:.*')">
              <xsl:value-of select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="gmd:CI_OnlineResource/gmd:name/gmx:MimeFileType|
                gmd:CI_OnlineResource/gmd:name/gco:CharacterString"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:if test="$fileName != ''">
          <resource>
            <ref><xsl:value-of select="gn:element/@ref"/></ref>
            <refParent><xsl:value-of select="gn:element/@parent"/></refParent>
            <name><xsl:value-of select="$fileName"/></name>
            <url><xsl:value-of select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/></url>
            <title><xsl:value-of select="normalize-space($metadata/gmd:identificationInfo/*/
              gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString)"/></title>
            <abstract><xsl:value-of select="normalize-space($metadata/
              gmd:identificationInfo/*/gmd:abstract)"/></abstract>
            <protocol><xsl:value-of select="gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString"/></protocol>
          </resource>
        </xsl:if>
      </xsl:for-each>
    </config>
  </xsl:template>
</xsl:stylesheet>
