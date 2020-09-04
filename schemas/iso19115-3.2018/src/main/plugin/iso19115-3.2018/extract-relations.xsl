<?xml version="1.0" encoding="UTF-8"?>
<!--
  Create a simple XML tree for relation description.
  <relations>
    <relation type="related|services|children">
      + super-brief representation.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:gmx="http://standards.iso.org/iso/19115/-3/gmx"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  exclude-result-prefixes="#all" >


    <!-- Convert an element gco:CharacterString
    to the GN localized string structure -->
    <xsl:template mode="get-iso19115-3.2018-localized-string" match="*">
      <xsl:variable name="mainLanguage"
                    select="ancestor::mdb:MD_Metadata/mdb:defaultLocale/*/lan:language/*/@codeListValue"/>

        <xsl:for-each select="gco:CharacterString|
                          lan:PT_FreeText/*/lan:LocalisedCharacterString">
            <xsl:variable name="localeId"
                          select="substring-after(@locale, '#')"/>
            <value lang="{if (@locale)
                  then ancestor::mdb:MD_Metadata/mdb:otherLocale/*[@id = $localeId]/lan:language/*/@codeListValue
                  else if ($mainLanguage) then $mainLanguage else $lang}">
                <xsl:value-of select="."/>
            </value>
        </xsl:for-each>
    </xsl:template>



  <!-- Relation contained in the metadata record has to be returned
  It could be document or thumbnails
  -->
  <xsl:template mode="relation" match="metadata[mdb:MD_Metadata or *[contains(@gco:isoType, 'MD_Metadata')]]" priority="99">

    <thumbnails>
      <xsl:for-each select="*/descendant::*[name(.) = 'mri:graphicOverview']/*">
        <item>
          <id><xsl:value-of select="mcc:fileName/gco:CharacterString"/></id>
          <url>
              <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                    select="mcc:fileName"/>
          </url>
          <title>
              <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                   select="mcc:fileDescription"/>
          </title>
          <type>thumbnail</type>
        </item>
      </xsl:for-each>
    </thumbnails>

    <onlines>
      <xsl:for-each select="*/descendant::*[
                            local-name() = 'portrayalCatalogueCitation'
                            ]/*[cit:onlineResource/*/cit:linkage/gco:CharacterString != '']">
        <item>
          <id><xsl:value-of select="cit:onlineResource/cit:CI_OnlineResource/cit:linkage/gco:CharacterString"/></id>
          <url>
            <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                 select="cit:onlineResource/*/cit:linkage"/>
          </url>
          <title>
            <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                 select="cit:title"/>
          </title>
          <type>legend</type>
        </item>
      </xsl:for-each>

      <xsl:for-each select="*/descendant::*[
                            local-name() = 'additionalDocumentation' or
                            local-name() = 'specification' or
                            local-name() = 'reportReference'
                            ]/*[cit:onlineResource/*/cit:linkage/gco:CharacterString != '']">
        <item>
          <id><xsl:value-of select="cit:onlineResource/cit:CI_OnlineResource/cit:linkage/gco:CharacterString"/></id>
          <url>
            <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                 select="cit:onlineResource/*/cit:linkage"/>
          </url>
          <title>
             <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                  select="cit:title"/>
          </title>
          <type>dq-report</type>
         </item>
      </xsl:for-each>

      <xsl:for-each select="*/descendant::*[
                            local-name() = 'onLine'
                            ]/*[cit:linkage/gco:CharacterString != '']">
        <item>
          <id><xsl:value-of select="cit:linkage/gco:CharacterString"/></id>
          <title>
              <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                   select="cit:name"/>
          </title>
          <url>
              <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                   select="cit:linkage"/>
          </url>
          <function><xsl:value-of select="cit:function/*/@codeListValue"/></function>
          <applicationProfile><xsl:value-of select="cit:applicationProfile/gco:CharacterString"/></applicationProfile>
          <description>
              <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                   select="cit:description"/>
          </description>
          <protocol><xsl:value-of select="cit:protocol/gco:CharacterString"/></protocol>
          <type>onlinesrc</type>
        </item>
      </xsl:for-each>

      <xsl:for-each select="*/descendant::*[
                            local-name() = 'featureCatalogueCitation'
                            ]/*[cit:onlineResource/*/cit:linkage/gco:CharacterString != '']">
        <item>
          <id><xsl:value-of select="cit:onlineResource/cit:CI_OnlineResource/cit:linkage/gco:CharacterString"/></id>
          <url>
            <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                 select="cit:onlineResource/*/cit:linkage"/>
          </url>
          <title>
            <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                               select="cit:title"/>
          </title>
          <type>fcats</type>
        </item>
      </xsl:for-each>
    </onlines>
  </xsl:template>
</xsl:stylesheet>
