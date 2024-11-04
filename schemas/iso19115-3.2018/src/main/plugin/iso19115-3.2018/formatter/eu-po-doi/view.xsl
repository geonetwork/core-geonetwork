<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
<!-- Conversion from ISO19139 to European Union Publication Office DOI
http://ra.publications.europa.eu/schema/OP/DOIMetadata/1.0/OP_DOIMetadata_1.0.xsd

     To retrieve a record:
     http://localhost:8080/geonetwork/srv/api/records/ff8d8cd6-c753-4581-99a3-af23fe4c996b/formatters/eu-po-doi?output=xml
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:eu="http://ra.publications.europa.eu/schema/doidata/1.0"
                xmlns:grant_id="http://www.crossref.org/grant_id/0.1.1"
                xmlns:datacite="http://datacite.org/schema/kernel-4"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:import href="base.xsl"/>
  <xsl:import href="../datacite/view.xsl"/>

  <xsl:output method="xml"
              indent="yes"/>

  <xsl:template match="/">
    <xsl:call-template name="eu-po-doi-message">
      <xsl:with-param name="dataciteResource">
        <resource
          xmlns="http://datacite.org/schema/kernel-4">
          <xsl:apply-templates select="$metadata"
                               mode="toDatacite"/>
        </resource>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>
