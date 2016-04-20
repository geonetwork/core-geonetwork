<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:variable name="dateFormat" as="xs:string"
                select="'[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]Z'"/>

  <xsl:variable name="separator" as="xs:string"
                select="'|'"/>


  <xsl:template match="/">
    <add>
      <xsl:apply-templates mode="index"/>
    </add>
  </xsl:template>


  <xsl:template match="csw:Record" mode="index">
    <xsl:variable name="identifier" as="xs:string?"
                  select="dc:identifier[. != '']"/>


    <xsl:variable name="mainLanguage" as="xs:string?"
                  select="if (normalize-space(/csw:Record/dc:language) != '')
                          then string(/csw:Record/dc:language) else 'eng'"/>

    <doc>
      <field name="metadataIdentifier">
        <xsl:value-of select="$identifier"/>
      </field>

      <field name="mainLanguage">
        <xsl:value-of select="$mainLanguage"/>
      </field>

      <field name="resourceTitle">
        <xsl:value-of select="normalize-space(dc:title/text())"/>
      </field>
      <field name="resourceAbstract">
        <xsl:value-of select="dct:abstract|dc:description"/>
      </field>
    </doc>
  </xsl:template>
</xsl:stylesheet>
