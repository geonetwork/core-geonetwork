<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
  xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
  xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
  xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core"
  xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0">

  <xsl:include href="utility-fn.xsl"/>
  <xsl:include href="utility-tpl.xsl"/>

  <xsl:template mode="superBrief" match="mdb:MD_Metadata|*[@gco:isoType='mdb:MD_Metadata']"
                priority="2">
    <xsl:variable name="langId" select="gn-fn-iso19139:getLangId(., $lang)"/>

    <id>
      <xsl:value-of select="gn:info/id"/>
    </id>
    <uuid>
      <xsl:value-of select="gn:info/uuid"/>
    </uuid>
    <title>
      <xsl:apply-templates mode="localised"
                           select="mdb:identificationInfo/*/mri:citation/*/cit:title">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:apply-templates>
    </title>
    <abstract>
      <xsl:apply-templates mode="localised" select="mdb:identificationInfo/*/mri:abstract">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:apply-templates>
    </abstract>
  </xsl:template>


  <!-- Templates used for RSS -->
  <xsl:template name="iso19115-3.2018Brief">
    <metadata>
      <xsl:call-template name="iso19115-3.2018-brief"/>
    </metadata>
  </xsl:template>

  <xsl:template name="iso19115-3.2018-brief">
    <xsl:variable name="info" select="gn:info"/>
    <xsl:variable name="id" select="$info/id"/>
    <xsl:variable name="uuid" select="$info/uuid"/>

    <xsl:variable name="langId" select="gn-fn-iso19139:getLangId(., $lang)"/>

    <title>
      <xsl:apply-templates mode="localised"
                           select="mdb:identificationInfo/*/mri:citation/*/cit:title">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:apply-templates>
    </title>
    <abstract>
      <xsl:apply-templates mode="localised" select="mdb:identificationInfo/*/mri:abstract">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:apply-templates>
    </abstract>


    <xsl:variable name="overviews"
                  select="mdb:identificationInfo/*/mri:graphicOverview/mcc:MD_BrowseGraphic/
                                mcc:fileName/gco:CharacterString[. != '']"/>
    <xsl:for-each select="$overviews">
      <image>
        <xsl:value-of select="."/>
      </image>
    </xsl:for-each>

    <metadatacreationdate>
      <xsl:value-of select="mdb:dateInfo/*[cit:dateType/*/@codeListValue = 'revision']/cit:date/gco:DateTime"/>
    </metadatacreationdate>
  </xsl:template>
</xsl:stylesheet>
