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
                xmlns:gfc="http://www.isotc211.org/2005/gfc"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:import href="../../iso19139/process/process-utility.xsl"/>

  <!-- i18n information -->
  <xsl:variable name="csv-add-values-info-loc">
    <msg id="a" xml:lang="eng">Add codelist values from CSV for column</msg>
    <msg id="a" xml:lang="fre">Ajouter les valeurs à partir d'un fichier CSV pour la colonne </msg>
    <msg id="a" xml:lang="dut">Add codelist values from CSV for column</msg>
  </xsl:variable>

  <!-- Process parameters and variables-->
  <xsl:param name="column" select="''"/>
  <xsl:param name="replaceListOfValues" select="'1'"/>
  <xsl:param name="listOfValuesAsCsv" select="''"/>
  <xsl:param name="listOfValuesSeparator" select="';'"/>



  <xsl:template name="list-add-values-from-csv">
    <suggestion process="add-values-from-csv"/>
  </xsl:template>


  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-add-values-from-csv">
    <xsl:param name="root"/>

    <xsl:for-each select="$root//gfc:carrierOfCharacteristics/*">
      <suggestion process="add-values-from-csv" id="{generate-id()}"
                  category="fcat" target="gfc:carrierOfCharacteristics">
        <name>
          <xsl:value-of select="concat(geonet:i18n($csv-add-values-info-loc, 'a', $guiLang), ' ', gfc:memberName/*)"/>
        </name>
        <operational>true</operational>
        <params>{
          "column":{"type":"string", "defaultValue":"<xsl:value-of select="gfc:memberName/*"/>"},
          "replaceListOfValues":{"type":"boolean", "defaultValue":"1"},
          "listOfValuesSeparator":{"type":"string", "defaultValue":"<xsl:value-of select="$listOfValuesSeparator"/>"},
          "listOfValuesAsCsv":{"type":"textarea", "defaultValue":"LABEL;CODE;DEFINITION"}
          }</params>
      </suggestion>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="gfc:carrierOfCharacteristics/*[gfc:memberName/* = $column]" priority="99">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="*[name() != 'gfc:listedValue']"/>

      <xsl:choose>
        <xsl:when test="$replaceListOfValues != '1' and $listOfValuesAsCsv != ''">
          <xsl:call-template name="add-values-from-csv"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="gfc:listedValue/*"/>
          <xsl:call-template name="add-values-from-csv"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="add-values-from-csv">
    <xsl:variable name="lines"
                  select="tokenize($listOfValuesAsCsv, '\n')"/>

    <xsl:for-each select="$lines[normalize-space(.) != '']">
      <xsl:variable name="line"
                    select="."/>
      <xsl:variable name="columns"
                    select="tokenize(string($line), $listOfValuesSeparator)"/>

      <gfc:listedValue>
        <gfc:FC_ListedValue>
          <gfc:label>
            <gco:CharacterString><xsl:value-of select="$columns[1]"/></gco:CharacterString>
          </gfc:label>
          <gfc:code>
            <gco:CharacterString><xsl:value-of select="$columns[2]"/></gco:CharacterString>
          </gfc:code>
          <gfc:definition>
            <gco:CharacterString><xsl:value-of select="$columns[3]"/></gco:CharacterString>
          </gfc:definition>
        </gfc:FC_ListedValue>
      </gfc:listedValue>
    </xsl:for-each>
  </xsl:template>

  <!-- Processing templates -->
  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
