<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                exclude-result-prefixes="#all"
                version="2.0">
  <xsl:import href="../../iso19139/process/process-utility.xsl"/>
  <xsl:import href="../../iso19139/layout/utility-vacuum.xsl"/>

  <!-- i18n information -->
  <xsl:variable name="vacuum-loc">
    <msg id="a" xml:lang="eng">Remove empty elements from this record.</msg>
    <msg id="a" xml:lang="fre">Supprimer les éléments vides de cette fiche.</msg>
  </xsl:variable>

  <xsl:template name="list-vacuum">
    <suggestion process="vacuum"/>
  </xsl:template>

  <xsl:template name="analyze-vacuum">
    <xsl:param name="root"/>

    <suggestion process="vacuum" id="{generate-id()}" category="metadata" target="metadata">
      <name><xsl:value-of select="geonet:i18n($vacuum-loc, 'a', $guiLang)"/></name>
      <operational>true</operational>
    </suggestion>
  </xsl:template>

  <xsl:template match="/">
        <xsl:copy-of select="gn-fn-iso19139:vacuum(.)"/>
    </xsl:template>
</xsl:stylesheet>
