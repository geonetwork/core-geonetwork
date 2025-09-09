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

<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                  xmlns:xlink="http://www.w3.org/1999/xlink"
                  exclude-result-prefixes="#all">

  <xsl:output method="xml" indent="yes"/>

  <xsl:variable name="doiProtocol"
                select="'DOI'"/>

  <xsl:template match="/root">
    <xsl:apply-templates select="*[1]"/>
  </xsl:template>

  <xsl:template match="mdb:MD_Metadata/mdb:dateInfo"/>

  <!-- Remove DOI identifiers -->
  <xsl:template match="cit:identifier[
                                contains(*/mcc:code/*/text(), 'datacite.org/doi/')
                                or contains(*/mcc:code/*/text(), 'doi.org')
                                or contains(*/mcc:code/*/@xlink:href, 'doi.org')]" />

  <!-- Remove DOI links -->
  <xsl:template match="mrd:onLine[*/cit:protocol/gco:CharacterString = $doiProtocol]" />

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
