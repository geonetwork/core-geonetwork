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

<xsl:stylesheet version="1.0"
		xmlns:dc="http://purl.org/dc/elements/1.1/"    
		xmlns:ows="http://www.opengis.net/ows"
		xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:geonet="http://www.fao.org/geonetwork"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- This stylesheet produces iso19135 metadata in XML format -->
  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />

  <!-- Metadata is passed under /root XPath -->
  <xsl:template match="/root">
    <!-- Export csw-record XML (just a copy) -->
    <xsl:apply-templates select="csw:Record|csw:SummaryRecord|csw:BriefRecord"/>
  </xsl:template>

	<!-- Delete any GeoNetwork specific elements -->
  <xsl:template match="geonet:*"/> 

  <!-- Copy everything else -->
  <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()[name(self::*)!='geonet:info']"/>
      </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
