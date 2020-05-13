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
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:java="java:org.fao.geonet.util.XslUtil" version="2.0"
                exclude-result-prefixes="#all">

  <xsl:import href="process-utility.xsl"/>

  <xsl:param name="uuidToRemove"/>

  <!-- i18n information -->
  <xsl:variable name="related-metadata-checker-loc">
    <msg id="a" xml:lang="eng">not found (</msg>
    <msg id="b" xml:lang="eng">). Run this task to remove it.</msg>
    <msg id="a" xml:lang="fre">non trouvé (</msg>
    <msg id="b" xml:lang="fre">). Si l'erreur persiste, corriger le lien manuellement ou exécuter
      cette action pour le supprimer.
    </msg>
    <msg id="a" xml:lang="dut"> is niet gevonden (</msg>
    <msg id="b" xml:lang="dut">). Functie verwijdert de link.</msg>
  </xsl:variable>

  <xsl:template name="list-related-metadata-checker">
    <suggestion process="related-metadata-checker"/>
  </xsl:template>

  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-related-metadata-checker">
    <xsl:param name="root"/>

    <!-- Check URL -->
    <xsl:variable name="links"
                  select="$root//gmd:parentIdentifier/gco:CharacterString"/>
    <xsl:for-each select="$links">
      <xsl:call-template name="checkUuid">
        <xsl:with-param name="uuid" select="."/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="checkUuid">
    <xsl:param name="uuid"/>
    <xsl:param name="type"/>

    <xsl:variable name="status" select="java:getIndexField($baseUrl, '_uuid', $uuid, 'en')"/>
    <!--    <xsl:message>Check:<xsl:value-of select="."/>|<xsl:value-of select="$status"/></xsl:message>
    -->
    <xsl:if test="$status=''">
      <suggestion process="related-metadata-checker" id="{generate-id()}" category="links"
                  target="all">
        <name xml:lang="en">
          <xsl:value-of select="$type"/>
          <xsl:value-of select="."/>
          <xsl:value-of select="geonet:i18n($related-metadata-checker-loc, 'a', $guiLang)"/>
          <xsl:value-of select="."/>:<xsl:value-of select="$status"/>
          <xsl:value-of select="geonet:i18n($related-metadata-checker-loc, 'b', $guiLang)"/>
        </name>
        <operational>true</operational>
        <params>{ "uuidToRemove":{"type":"string", "defaultValue":"<xsl:value-of
          select="normalize-space($uuid)"/>"}
          }
        </params>
      </suggestion>
    </xsl:if>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

  <!-- Remove the uuid. TODO : remove the parent ? -->
  <xsl:template match="*[text()=$uuidToRemove]" priority="2"/>


</xsl:stylesheet>
