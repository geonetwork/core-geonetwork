<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
  xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
  xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
  xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
  xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
  xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
  xmlns:gn-fn-index="http://geonetwork-opensource.org/xsl/functions/index"
  xmlns:util="java:org.fao.geonet.util.XslUtil"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all">

  <xsl:import href="common/index-utils.xsl"/>
  <xsl:include href="utility-tpl-multilingual.xsl"/>

  <xsl:template name="get-iso19115-3.2018-is-service">
    <xsl:value-of
            select="count($metadata/mdb:identificationInfo/srv:SV_ServiceIdentification) > 0"/>
  </xsl:template>

  <xsl:template name="get-iso19115-3.2018-title">
    <xsl:value-of select="($metadata/mdb:identificationInfo/*/mri:citation/*/cit:title/gco:CharacterString
                          |$metadata/mdb:contentInfo/*/mrc:featureCatalogue/*/cat:name[*/text() != '']
                          |$metadata/mdb:contentInfo/*/mrc:featureCatalogue/*/gfc:featureType/*/gfc:typeName[text() != ''])[1]
"/>
  </xsl:template>

  <xsl:template mode="get-formats-as-json" match="mdb:MD_Metadata">
    [
    <xsl:for-each select="mdb:distributionInfo/*/mrd:distributionFormat/*/mrd:formatSpecificationCitation/*/cit:title/*/text()">{
      "value": "WWW:DOWNLOAD:<xsl:value-of select="util:escapeForJson(.)"/>",
      "label": "<xsl:value-of select="util:escapeForJson(.)"/>"}
      <xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>
    ]
  </xsl:template>


  <xsl:template name="get-iso19115-3.2018-extents-as-json">[
   <xsl:for-each select="//mdb:identificationInfo/*/mri:extent
                          //gex:geographicElement/gex:EX_GeographicBoundingBox[
            number(gex:westBoundLongitude/gco:Decimal)
            and number(gex:southBoundLatitude/gco:Decimal)
            and number(gex:eastBoundLongitude/gco:Decimal)
            and number(gex:northBoundLatitude/gco:Decimal)
            and normalize-space(gex:westBoundLongitude/gco:Decimal) != ''
            and normalize-space(gex:southBoundLatitude/gco:Decimal) != ''
            and normalize-space(gex:eastBoundLongitude/gco:Decimal) != ''
            and normalize-space(gex:northBoundLatitude/gco:Decimal) != ''
            ]">
      <xsl:variable name="format" select="'#0.0000'"></xsl:variable>

      [
      <xsl:value-of select="format-number(gex:westBoundLongitude/gco:Decimal, $format)"/>,
      <xsl:value-of select="format-number(gex:southBoundLatitude/gco:Decimal, $format)"/>,
      <xsl:value-of select="format-number(gex:eastBoundLongitude/gco:Decimal, $format)"/>,
      <xsl:value-of select="format-number(gex:northBoundLatitude/gco:Decimal, $format)"/>
      ]
      <xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>
    ]
  </xsl:template>

  <xsl:template name="get-iso19115-3.2018-online-source-config">
    <xsl:param name="pattern"/>
    <config>
      <xsl:for-each select="$metadata/descendant::mrd:onLine[
        matches(
        cit:CI_OnlineResource/cit:protocol/gco:CharacterString,
        $pattern) and
        normalize-space(cit:CI_OnlineResource/cit:linkage/gco:CharacterString) != '']">

        <xsl:variable name="protocol" select="cit:CI_OnlineResource/cit:protocol/*/text()"/>

        <xsl:variable name="fileName">
          <xsl:choose>
            <xsl:when test="matches($protocol, '^DB:.*')">
              <xsl:value-of select="concat(cit:CI_OnlineResource/cit:linkage/gco:CharacterString, '#',
                cit:CI_OnlineResource/cit:name/gco:CharacterString)"/>
            </xsl:when>
            <xsl:when test="matches($protocol, '^FILE:.*')">
              <xsl:value-of select="cit:CI_OnlineResource/cit:linkage/gco:CharacterString"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="cit:CI_OnlineResource/cit:name/gco:CharacterString"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:if test="$fileName != ''">
          <resource>
            <ref><xsl:value-of select="gn:element/@ref"/></ref>
            <refParent><xsl:value-of select="gn:element/@parent"/></refParent>
            <name><xsl:value-of select="$fileName"/></name>
            <url><xsl:value-of select="cit:CI_OnlineResource/cit:linkage/gco:CharacterString"/></url>
            <title><xsl:value-of select="normalize-space($metadata/mdb:identificationInfo/*/
              mri:citation/cit:CI_Citation/cit:title/gco:CharacterString)"/></title>
            <abstract><xsl:value-of select="normalize-space($metadata/
              mdb:identificationInfo/*/mri:abstract)"/></abstract>
            <protocol><xsl:value-of select="cit:CI_OnlineResource/cit:protocol/*/text()"/></protocol>
          </resource>
        </xsl:if>
      </xsl:for-each>
    </config>
  </xsl:template>
</xsl:stylesheet>
