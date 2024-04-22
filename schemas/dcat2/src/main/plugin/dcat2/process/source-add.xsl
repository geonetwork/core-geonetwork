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
<xsl:stylesheet
  xmlns:dct="http://purl.org/dc/terms/"
  xmlns:adms="http://www.w3.org/ns/adms#"
  xmlns:dcat="http://www.w3.org/ns/dcat#"
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:owl="http://www.w3.org/2002/07/owl#"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork"
  version="2.0"
  exclude-result-prefixes="#all">

  <!-- Source metadata record UUID -->
  <xsl:param name="sourceUuid"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template
    match="dcat:Dataset" priority="2">

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="dct:identifier|
                dct:title|
                dct:description|
                dcat:contactPoint|
                dct:issued|
                dct:modified|
                dct:publisher|
                dcat:keyword|
                dcat:theme|
                dct:accessRights|
                dct:conformsTo|
                foaf:page|
                dct:accrualPeriodicity|
                dct:hasVersion|
                dct:isVersionOf|
                dcat:landingPage|
                dct:language|
                adms:identifier|
                dct:provenance"/>
      <xsl:variable name="rdfResource"
                    select="replace(@rdf:about,'([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}){1}',$sourceUuid)"/>
      <xsl:copy-of select="dct:relation[@rdf:resource!= $rdfResource]"/>

      <dct:relation rdf:resource="{$rdfResource}"/>

      <xsl:apply-templates select="dct:source|
                dct:spatial|
                dct:temporal|
                dct:type|
                owl:versionInfo|
                adms:versionNotes|
                dcat:extension|
                dcat:distribution|
                adms:sample"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>
</xsl:stylesheet>
