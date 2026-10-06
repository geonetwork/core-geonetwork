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
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:util-uuid="java:java.util.UUID"
                version="2.0" exclude-result-prefixes="#all">

  <xsl:import href="../../iso19139/process/process-utility.xsl"/>

  <!-- i18n information -->
  <xsl:variable name="add-resource-id-loc">
    <msg id="a" xml:lang="eng">Current record does not contain resource identifier. Compute a resource identifier.</msg>
    <msg id="a" xml:lang="fre">Cette fiche ne contient pas d'identifiant pour la ressource. Calculer un identifiant de ressource.</msg>
    <msg id="a" xml:lang="dut">Het huidige record bevat geen resource-ID. Bereken een bronidentificatie.</msg>
  </xsl:variable>


  <xsl:variable name="resource-id-url-prefix" select="''"/>
  <xsl:param name="useMetadataIdentifier" select="'0'"/>

  <xsl:variable name="useMetadataIdentifierMode" select="geonet:parseBoolean($useMetadataIdentifier)"/>



  <xsl:template name="list-add-resource-id">
    <suggestion process="add-resource-id"/>
  </xsl:template>


  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-add-resource-id">
    <xsl:param name="root"/>
    <xsl:variable name="hasResourceId"
                  select="count($root//gmd:identificationInfo/*/gmd:citation/
            gmd:CI_Citation/gmd:identifier/*/gmd:code[gco:CharacterString != '']) > 0"/>

    <xsl:if test="not($hasResourceId)">
      <suggestion process="add-resource-id" id="{generate-id()}" category="identification"
                  target="identification">
        <name>
          <xsl:value-of select="geonet:i18n($add-resource-id-loc, 'a', $guiLang)"/>
        </name>
        <operational>true</operational>
        <params>
          "useMetadataIdentifier":{"type":"boolean", "defaultValue":"<xsl:value-of select="$useMetadataIdentifier"/>"},
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

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

  <xsl:function name="geonet:generateCodeFromMetadataIdentifier">
    <xsl:param name="catalogUrl"/>
    <xsl:param name="nodeId"/>
    <xsl:param name="resource-id-url-prefix"/>
    <xsl:param name="metadataIdentifier"/>

    <xsl:variable name="urlWithoutLang" select="substring-before($catalogUrl, $nodeId)"/>
    <xsl:variable name="prefix"
                  select="if ($resource-id-url-prefix != '') then $resource-id-url-prefix else $urlWithoutLang"/>

    <xsl:value-of select="concat($prefix, $metadataIdentifier)"/>
  </xsl:function>


  <xsl:function name="geonet:generateRandomCode">
    <xsl:param name="resource-id-url-prefix"/>

    <xsl:variable name="resource-id-url-prefix-tmp"
                  select="util:getSettingValue('metadata/resourceIdentifierPrefix')"/>

    <xsl:variable name="resource-id-url-prefix-local"
                  select="if (ends-with($resource-id-url-prefix-tmp, '/'))
                            then $resource-id-url-prefix-tmp
                            else concat($resource-id-url-prefix-tmp, '/')"/>
    <xsl:variable name="resourceUuid" select="util-uuid:toString(util-uuid:randomUUID())"/>
    <xsl:variable name="prefix"
                  select="if ($resource-id-url-prefix != '') then $resource-id-url-prefix else $resource-id-url-prefix-local"/>

    <xsl:value-of select="concat($prefix, $resourceUuid)"/>
  </xsl:function>

  <xsl:template
    match="gmd:identificationInfo/*/gmd:citation/
        gmd:CI_Citation"
    priority="2">

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of
        select="gmd:title|
                gmd:alternateTitle|
                gmd:date|
                gmd:edition|
                gmd:editionDate"/>

      <xsl:variable name="code"
                    select="if ($useMetadataIdentifierMode)
                            then geonet:generateCodeFromMetadataIdentifier($catalogUrl, $nodeId, $resource-id-url-prefix, /*/gmd:fileIdentifier/gco:CharacterString)
                            else geonet:generateRandomCode($resource-id-url-prefix)"/>

      <xsl:copy-of
        select="gmd:identifier[gmd:MD_Identifier/gmd:code/gco:CharacterString != $code]"/>
      <gmd:identifier>
        <gmd:MD_Identifier>
          <gmd:code>
            <gco:CharacterString>
              <xsl:value-of select="$code"/>
            </gco:CharacterString>
          </gmd:code>
        </gmd:MD_Identifier>
      </gmd:identifier>

      <xsl:copy-of
        select="gmd:citedResponsibleParty|
                gmd:presentationForm|
                gmd:series|
                gmd:otherCitationDetails|
                gmd:collectiveTitle|
                gmd:ISBN|
                gmd:ISSN|
                gmd:onlineResource"/>

    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
