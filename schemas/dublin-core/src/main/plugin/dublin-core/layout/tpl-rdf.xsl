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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saxon="http://saxon.sf.net/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/" version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <!--
    Create reference block to metadata record and dataset to be added in dcat:Catalog usually.
  -->
  <!-- FIME : $url comes from a global variable. -->
  <xsl:template match="simpledc" mode="record-reference">
    <dcat:dataset rdf:resource="{$resourcePrefix}/datasets/{dc:identifier}"/>
    <dcat:record rdf:resource="{$resourcePrefix}/records/{dc:identifier}"/>
  </xsl:template>


  <!--
    Convert DC record to DCAT
    -->
  <xsl:template match="simpledc" mode="to-dcat">
    <!-- Catalogue records
      "A record in a data catalog, describing a single dataset."
    -->
    <dcat:CatalogRecord rdf:about="{$resourcePrefix}/records/{dc:identifier}">
      <!-- Link to a dcat:Dataset or a rdf:Description for services and feature catalogue. -->
      <foaf:primaryTopic rdf:resource="{$resourcePrefix}/datasets/{dc:identifier}"/>
    </dcat:CatalogRecord>
    <!-- Catalogue records
      "A record in a data catalog, describing a single dataset."
    -->
    <dcat:Dataset rdf:about="{$resourcePrefix}/datasets/{dc:identifier}">
      <xsl:copy-of select="dc:*|dct:*"/>
    </dcat:Dataset>
  </xsl:template>

  <xsl:template match="simpledc" mode="references"/>

  <xsl:template mode="simpledc" match="gui|request|metadata"/>

</xsl:stylesheet>
