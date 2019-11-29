<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd" version="2.0">

  <xsl:import href="process-utility.xsl"/>

  <!-- i18n information -->
  <xsl:variable name="scale-denominator-loc">
    <msg id="a" xml:lang="eng">The following values are not recommended for scale denominator: </msg>
    <msg id="b" xml:lang="eng">. Run this task to try to fix it (them).</msg>
    <msg id="a" xml:lang="fre">Les valeurs suivantes ne sont pas recommandées pour le dénominateur
      de l'échelle :
    </msg>
    <msg id="b" xml:lang="fre">. Exécuter cette action pour le corriger.</msg>
    <msg id="a" xml:lang="dut">De volgende waardes worden zijn niet aanbevolen in het element schaal: </msg>
    <msg id="b" xml:lang="dut">. Voer deze functie uit om het bij te werken.</msg>
  </xsl:variable>

  <xsl:template name="list-scale-denominator-formatter">
    <suggestion process="scale-denominator-formatter"/>
  </xsl:template>

  <!-- Analyze the metadata record and return available suggestion
      for that process -->
  <xsl:template name="analyze-scale-denominator-formatter">
    <xsl:param name="root"/>
    <xsl:variable name="dummyScales"
                  select="string-join($root//gmd:equivalentScale/gmd:MD_RepresentativeFraction/
                gmd:denominator[contains(gco:Integer, '/') or contains(gco:Integer, ':') or contains(gco:Integer, ' ')], ', ')"/>
    <xsl:if test="$dummyScales!=''">
      <suggestion process="scale-denominator-formatter" id="{generate-id()}"
                  category="identification" target="scale">
        <name>
          <xsl:value-of select="geonet:i18n($scale-denominator-loc, 'a', $guiLang)"/>
          <xsl:value-of select="$dummyScales"/>
          <xsl:value-of select="geonet:i18n($scale-denominator-loc, 'b', $guiLang)"/>
        </name>
        <operational>true</operational>
        <form/>
      </suggestion>
    </xsl:if>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

  <!-- Remove duplicates
  -->
  <xsl:template match="gmd:equivalentScale/gmd:MD_RepresentativeFraction/
    gmd:denominator[contains(gco:Integer, '/') or contains(gco:Integer, ':') or contains(gco:Integer, ' ')]"
                priority="2">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <gco:Integer>
        <xsl:value-of select="replace(replace(replace(gco:Integer, '1:', ''), '1/', ''), ' ', '')"/>
      </gco:Integer>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
