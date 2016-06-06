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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gfc="http://www.isotc211.org/2005/gfc"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="1.0"
                exclude-result-prefixes="gco gfc gmx">

  <xsl:param name="displayInfo"/>

  <!-- ============================================================================= -->

  <xsl:template
    match="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType|*[@gco:isoType='gfc:FC_FeatureCatalogue']">

    <xsl:variable name="info" select="geonet:info"/>
    <xsl:variable name="identification" select="."/>


    <csw:BriefRecord>

      <dc:identifier>
        <xsl:value-of select="@uuid"/>
      </dc:identifier>

      <!-- DataIdentification -->
      <xsl:for-each
        select="gmx:name/gco:CharacterString|gfc:name/gco:CharacterString|gfc:typeName/gco:LocalName">
        <dc:title>
          <xsl:value-of select="."/>
        </dc:title>
      </xsl:for-each>

      <dc:type>FeatureCatalogue</dc:type>

      <!-- GeoNetwork elements added when resultType is equal to results_with_summary -->
      <xsl:if test="$displayInfo = 'true'">
        <xsl:copy-of select="$info"/>
      </xsl:if>

    </csw:BriefRecord>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*">
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
