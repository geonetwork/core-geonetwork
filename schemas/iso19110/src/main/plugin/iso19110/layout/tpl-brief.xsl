<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gn="http://www.fao.org/geonetwork" xmlns:gfc="http://www.isotc211.org/2005/gfc"
  xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gco="http://www.isotc211.org/2005/gco"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="xs" version="2.0">

  <!-- ===================================================================== -->
  <!-- === iso19110 brief formatting === -->
  <!-- ===================================================================== -->

  <xsl:template mode="superBrief" match="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType">
    <xsl:variable name="uuid" select="gn:info/uuid"/>
    <id>
      <xsl:value-of select="gn:info/id"/>
    </id>
    <uuid>
      <xsl:value-of select="$uuid"/>
    </uuid>
    <xsl:if test="gmx:name|gfc:name|gfc:typeName">
      <title>
        <xsl:value-of
          select="gmx:name/gco:CharacterString|gfc:name/gco:CharacterString|gfc:typeName/gco:LocalName"
        />
      </title>
    </xsl:if>
  </xsl:template>

  <xsl:template name="iso19110Brief">
    <metadata>
      <xsl:variable name="id" select="gn:info/id"/>
      <xsl:variable name="uuid" select="gn:info/uuid"/>

      <xsl:if test="gmx:name or gfc:name">
        <title>
          <xsl:value-of select="gmx:name/gco:CharacterString|gfc:name/gco:CharacterString"/>
        </title>
      </xsl:if>

      <xsl:if test="gmx:scope or gfc:scope">
        <abstract>
          <xsl:value-of select="gmx:scope/gco:CharacterString|gfc:scope/gco:CharacterString"/>
        </abstract>
      </xsl:if>

      <gn:info>
        <xsl:copy-of select="gn:info/*"/>
        <category internal="true">featureCatalogue</category>
      </gn:info>
    </metadata>
  </xsl:template>
</xsl:stylesheet>
