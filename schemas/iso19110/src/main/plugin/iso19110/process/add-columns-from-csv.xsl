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
  <xsl:variable name="csv-info-loc">
    <msg id="a" xml:lang="eng">Add columns from CSV</msg>
    <msg id="a" xml:lang="fre">Ajouter les colonnes à partir d'un fichier CSV</msg>
    <msg id="a" xml:lang="dut">Add columns from CSV</msg>
  </xsl:variable>

  <!-- Process parameters and variables-->
  <xsl:param name="replaceColumns" select="'1'"/>
  <xsl:param name="columnListAsCsv" select="''"/>
  <xsl:param name="columnListSeparator" select="';'"/>


  <xsl:template name="list-add-columns-from-csv">
    <suggestion process="add-columns-from-csv"/>
  </xsl:template>


  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-add-columns-from-csv">
    <xsl:param name="root"/>

    <suggestion process="add-columns-from-csv" id="{generate-id()}"
                category="fcat" target="gfc:FC_FeatureType">
      <name>
        <xsl:value-of select="geonet:i18n($csv-info-loc, 'a', $guiLang)"/>
      </name>
      <operational>true</operational>
      <params>{
        "replaceColumns":{"type":"boolean", "defaultValue":"1"},
        "columnListSeparator":{"type":"string", "defaultValue":"<xsl:value-of select="$columnListSeparator"/>"},
        "columnListAsCsv":{"type":"textarea", "defaultValue":"SHORT_NAME;DESCRIPTION;TYPE;CARDINALITY_MIN;CARDINALITY_MAX"}
      }</params>
    </suggestion>
  </xsl:template>

  <xsl:template match="gfc:FC_FeatureType" priority="99">

    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="*[name() != 'gfc:carrierOfCharacteristics']"/>

      <xsl:if test="$replaceColumns != '1'">
        <xsl:apply-templates select="gfc:carrierOfCharacteristics"/>
      </xsl:if>

      <xsl:variable name="lines"
                    select="tokenize($columnListAsCsv, '\n')"/>

      <xsl:for-each select="$lines[normalize-space(.) != '']">
        <xsl:variable name="line"
                      select="."/>
        <xsl:variable name="columns"
                      select="tokenize(string($line), $columnListSeparator)"/>

        <gfc:carrierOfCharacteristics>
          <gfc:FC_FeatureAttribute>
            <gfc:featureType/>
            <xsl:if test="$columns[1]">
              <gfc:memberName>
                <gco:LocalName><xsl:value-of select="$columns[1]"/></gco:LocalName>
              </gfc:memberName>
            </xsl:if>
            <xsl:if test="$columns[2]">
              <gfc:definition>
                <gco:CharacterString><xsl:value-of select="$columns[2]"/></gco:CharacterString>
              </gfc:definition>
            </xsl:if>
            <xsl:if test="$columns[4]">
            <gfc:cardinality>
              <gco:Multiplicity>
                <gco:range>
                  <gco:MultiplicityRange>
                    <gco:lower>
                      <gco:Integer><xsl:value-of select="$columns[4]"/></gco:Integer>
                    </gco:lower>
                    <gco:upper>
                      <xsl:choose>
                        <xsl:when test="$columns[5] != ''">
                          <gco:Integer><xsl:value-of select="$columns[5]"/></gco:Integer>
                        </xsl:when>
                        <xsl:otherwise>
                          <gco:UnlimitedInteger xsi:nil="true" isInfinite="true" />
                        </xsl:otherwise>
                      </xsl:choose>
                    </gco:upper>
                  </gco:MultiplicityRange>
                </gco:range>
              </gco:Multiplicity>
            </gfc:cardinality>
            </xsl:if>
            <xsl:if test="$columns[3]">
            <gfc:valueType>
              <gco:TypeName>
                <gco:aName>
                  <gco:CharacterString><xsl:value-of select="$columns[3]"/></gco:CharacterString>
                </gco:aName>
              </gco:TypeName>
            </gfc:valueType>
            </xsl:if>
          </gfc:FC_FeatureAttribute>
        </gfc:carrierOfCharacteristics>
      </xsl:for-each>
    </xsl:copy>

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
