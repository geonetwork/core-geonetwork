<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                exclude-result-prefixes="#all">


  <!-- Convert an element gco:CharacterString
  to the GN localized string structure -->
  <xsl:template mode="get-iso19115-3.2018-localized-string" match="*">
    <xsl:param name="defaultLanguage" select="'eng'" as="xs:string?"/>

    <xsl:variable name="mainLanguage"
                  select="ancestor::mdb:MD_Metadata/mdb:defaultLocale/*/lan:language/*/@codeListValue"/>

    <xsl:for-each select="gco:CharacterString|gcx:Anchor|gcx:MimeFileType|
                          lan:PT_FreeText/*/lan:LocalisedCharacterString">
      <xsl:variable name="localeId"
                    select="substring-after(@locale, '#')"/>
      <value lang="{if (@locale)
                    then ancestor::mdb:MD_Metadata/mdb:otherLocale/*[@id = $localeId]/lan:language/*/@codeListValue
                    else if ($mainLanguage)
                    then $mainLanguage
                    else $defaultLanguage}">
        <xsl:value-of select="."/>
      </value>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="collect-distribution-links">

    <xsl:for-each select="*/descendant::*[
                            local-name() = 'onLine'
                            ]/*[cit:linkage/gco:CharacterString != '']">
      <item>
        <id>
          <xsl:value-of select="cit:linkage/gco:CharacterString"/>
        </id>
        <title>
          <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                               select="cit:name"/>
        </title>
        <url>
          <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                               select="cit:linkage"/>
        </url>
        <function>
          <xsl:value-of select="cit:function/*/@codeListValue"/>
        </function>
        <applicationProfile>
          <xsl:value-of select="cit:applicationProfile/gco:CharacterString"/>
        </applicationProfile>
        <description>
          <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                               select="cit:description"/>
        </description>
        <protocol>
          <xsl:value-of select="cit:protocol/*/text()"/>
        </protocol>
        <mimeType>
          <xsl:value-of select="if (*/gcx:MimeFileType)
                                then */gcx:MimeFileType/@type
                                else if (starts-with(cit:protocol/gco:CharacterString, 'WWW:DOWNLOAD:'))
                                then replace(cit:protocol/gco:CharacterString, 'WWW:DOWNLOAD:', '')
                                else ''"/>
        </mimeType>
        <type>onlinesrc</type>
      </item>
    </xsl:for-each>
  </xsl:template>


  <xsl:variable name="documentsConfig" as="node()*">
    <doc protocol="WWW:LINK" function="legend" type="legend">
      <element>portrayalCatalogueCitation</element>
    </doc>
    <doc protocol="WWW:LINK" function="featureCatalogue" type="fcats">
      <element>featureCatalogueCitation</element>
    </doc>
    <doc protocol="WWW:LINK" function="dataQualityReport" type="dq-report">
      <element>additionalDocumentation</element>
      <element>specification</element>
      <element>reportReference</element>
    </doc>
  </xsl:variable>

  <!--
  Collecting links in the metadata records. This is used during
  indexing (using forIndexing=true to return the complete element
  (which could be multilingual and will be indexed with translations)
  and in extract-relations.xsl which return only the current API call language.
  -->
  <xsl:template name="collect-documents">
    <xsl:param name="forIndexing" select="false()" as="xs:boolean"/>

    <xsl:variable name="root" select="."/>
    <xsl:for-each select="$documentsConfig">
      <xsl:variable name="docType"
                    select="current()"/>
      <xsl:for-each select="$root/descendant::*[
                              local-name() = $docType/element/text()
                              ]/*/cit:onlineResource/*[cit:linkage/gco:CharacterString != '']">
        <item>
          <id>
            <xsl:value-of select="cit:linkage/gco:CharacterString"/>
          </id>
          <url>
            <xsl:choose>
              <xsl:when test="$forIndexing">
                <xsl:copy-of select="cit:linkage"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                     select="cit:linkage"/>
              </xsl:otherwise>
            </xsl:choose>
          </url>
          <title>
            <xsl:variable name="name"
                          select="if (cit:name) then cit:name else ../../cit:title"/>
            <xsl:choose>
              <xsl:when test="$forIndexing">
                <xsl:copy-of select="$name"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                     select="$name"/>
              </xsl:otherwise>
            </xsl:choose>
          </title>
          <description>
            <xsl:variable name="desc"
                          select="if (cit:description)
                                        then cit:description
                                        else ../../../../mdq:abstract"/>
            <xsl:choose>
              <xsl:when test="$forIndexing">
                <xsl:copy-of select="$desc"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                     select="$desc"/>
              </xsl:otherwise>
            </xsl:choose>
          </description>
          <protocol><xsl:value-of select="$docType/@protocol"/></protocol>
          <function><xsl:value-of select="$docType/@function"/></function>
          <type><xsl:value-of select="$docType/@type"/></type>
          <xsl:if test="$forIndexing and ../../../@gco:nilReason">
            <nilReason><xsl:value-of select="../../../@gco:nilReason"/></nilReason>
          </xsl:if>
        </item>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
