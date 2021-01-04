<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
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
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:exslt="http://exslt.org/common"
                exclude-result-prefixes="#all">
  <xsl:import href="metadata-view.xsl"/>

  <xsl:template name="iso19115-3.2018CompleteTab"/>

  <!-- main template - the way into processing iso19139 -->
  <xsl:template name="metadata-iso19115-3.2018">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="embedded"/>

    <xsl:apply-templates mode="iso19115-3.2018" select="." >
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="embedded" select="$embedded" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template name="iso19115-3.2018GetIsoLanguage" mode="iso19115-3.2018GetIsoLanguage" match="*">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="value"/>
    <xsl:param name="ref"/>

    <xsl:variable name="lang" select="/root/gui/language"/>

    <xsl:choose>
      <xsl:when test="$edit=true()">
        <select class="md" name="{$ref}" size="1">
          <option name=""/>

          <xsl:for-each select="/root/gui/isoLang/record">
            <xsl:sort select="label/child::*[name() = $lang]"/>
            <option value="{code}">
              <xsl:if test="code = $value">
                <xsl:attribute name="selected"/>
              </xsl:if>
              <xsl:value-of select="label/child::*[name() = $lang]"/>
            </option>
          </xsl:for-each>
        </select>
      </xsl:when>

      <xsl:otherwise>
        <xsl:value-of select="/root/gui/isoLang/record[code=$value]/label/child::*[name() = $lang]"/>

        <!-- In view mode display other languages from gmd:locale of gmd:MD_Metadata element
        FIXME
        <xsl:if test="../gmd:locale or ../../gmd:locale">
          <xsl:text> (</xsl:text><xsl:value-of
            select="string(/root/gui/schemas/iso19139/labels/element[@name='gmd:locale' and not(@context)]/label)"/>
          <xsl:text>:</xsl:text>
          <xsl:for-each select="../gmd:locale|../../gmd:locale">
            <xsl:variable name="c" select="gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue"/>
            <xsl:value-of select="/root/gui/isoLang/record[code=$c]/label/child::*[name() = $lang]"/>
            <xsl:if test="position()!=last()">,</xsl:if>
          </xsl:for-each>
          <xsl:text>)</xsl:text>
        </xsl:if>-->

      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
