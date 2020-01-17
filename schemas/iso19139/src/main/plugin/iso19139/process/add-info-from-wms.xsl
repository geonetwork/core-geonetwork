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
                xmlns:exslt="http://exslt.org/common" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:math="http://exslt.org/math"
                version="2.0"
                exclude-result-prefixes="srv gco gmd exslt geonet math">

  <xsl:import href="process-utility.xsl"/>

  <!-- i18n information -->
  <xsl:variable name="wms-info-loc">
    <msg id="a" xml:lang="eng">WMS service </msg>
    <msg id="b" xml:lang="eng">is described in online resource section. Run to update extent, CRS or
      graphic overview
      for this WMS service for the layer named:
    </msg>
    <msg id="a" xml:lang="fre">Le service de visualisation</msg>
    <msg id="b" xml:lang="fre">est décrit dans la section resource en ligne. Exécuter cette action
      pour mettre à jour l'étendue, les systèmes de projection
      ou les aperçus pour ce service et la couche nommée :
    </msg>
    <msg id="a" xml:lang="dut">Er is een verwijzing gevonden naar de WMS service </msg>
    <msg id="b" xml:lang="dut">. Gebruik deze functie om de dekking, de projectie of thumbnail af te leiden of bij te werken vanuit deze WMS-service voor de laag met de naam: </msg>
  </xsl:variable>

  <!-- Process parameters and variables-->
  <xsl:param name="mode" select="'process'"/>
  <xsl:param name="setExtent" select="'0'"/>
  <xsl:param name="setAndReplaceExtent" select="'0'"/>
  <xsl:param name="setCRS" select="'0'"/>
  <xsl:param name="setDynamicGraphicOverview" select="'0'"/>
  <xsl:param name="wmsServiceUrl"/>

  <xsl:variable name="setExtentMode" select="geonet:parseBoolean($setExtent)"/>
  <xsl:variable name="setAndReplaceExtentMode" select="geonet:parseBoolean($setAndReplaceExtent)"/>
  <xsl:variable name="setCRSMode" select="geonet:parseBoolean($setCRS)"/>
  <xsl:variable name="setDynamicGraphicOverviewMode"
                select="geonet:parseBoolean($setDynamicGraphicOverview)"/>


  <!-- Load the capabilities document if one oneline resource contains a protocol set to WMS
  -->
  <xsl:variable name="onlineNodes"
                select="//gmd:CI_OnlineResource[contains(gmd:protocol/gco:CharacterString, 'OGC:WMS') and normalize-space(gmd:linkage/gmd:URL)=$wmsServiceUrl]"/>
  <xsl:variable name="layerName" select="$onlineNodes/gmd:name/gco:CharacterString"/>
  <xsl:variable name="capabilitiesDoc">
    <xsl:if test="$onlineNodes">
      <xsl:copy-of select="geonet:get-wms-capabilities($wmsServiceUrl, '1.1.1')"/>
    </xsl:if>
  </xsl:variable>


  <xsl:template name="list-add-info-from-wms">
    <suggestion process="add-info-from-wms"/>
  </xsl:template>


  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-add-info-from-wms">
    <xsl:param name="root"/>

    <xsl:variable name="onlineResources"
                  select="$root//gmd:onLine/gmd:CI_OnlineResource[contains(gmd:protocol/gco:CharacterString, 'OGC:WMS')
                                            and normalize-space(gmd:linkage/gmd:URL)!='']"/>
    <xsl:variable name="srv"
                  select="$root//*[local-name(.)='SV_ServiceIdentification' or contains(@gco:isoType, 'SV_ServiceIdentification')]"/>

    <!-- Check if server is up and new value are available
     <xsl:variable name="capabilities"
      select="geonet:get-wms-capabilities(gmd:linkage/gmd:URL, '1.1.1')"/>
-->
    <xsl:for-each select="$onlineResources">
      <suggestion process="add-info-from-wms" id="{generate-id()}" category="onlineSrc"
                  target="gmd:extent">
        <name>
          <xsl:value-of select="geonet:i18n($wms-info-loc, 'a', $guiLang)"/><xsl:value-of
          select="./gmd:linkage/gmd:URL"
        /><xsl:value-of select="geonet:i18n($wms-info-loc, 'b', $guiLang)"/><xsl:value-of
          select="./gmd:name/gco:CharacterString"/>.
        </name>
        <operational>true</operational>
        <params>{"setExtent":{"type":"boolean", "defaultValue":"<xsl:value-of select="$setExtent"/>"},
          "setAndReplaceExtent":{"type":"boolean", "defaultValue":"<xsl:value-of
            select="$setAndReplaceExtent"/>"}, "setCRS":{"type":"boolean", "defaultValue":"<xsl:value-of
            select="$setCRS"/>"},
          <xsl:if test="not($srv)">
            "setDynamicGraphicOverview":{"type":"boolean",
            "defaultValue":"<xsl:value-of select="$setDynamicGraphicOverview"/>"},
          </xsl:if>
          "wmsServiceUrl":{"type":"string", "defaultValue":"<xsl:value-of
            select="normalize-space(gmd:linkage/gmd:URL)"/>"}
          }
        </params>
      </suggestion>
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


  <!-- Here set extent and graphicOverview -->
  <xsl:template
    match="gmd:identificationInfo/*"
    priority="2">

    <xsl:variable name="srv"
                  select="local-name(.)='SV_ServiceIdentification'
            or contains(@gco:isoType, 'SV_ServiceIdentification')"/>


    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <!-- Copy all elements from AbstractMD_IdentificationType-->
      <xsl:copy-of
        select="gmd:citation|
        gmd:abstract|
        gmd:purpose|
        gmd:credit|
        gmd:status|
        gmd:pointOfContact|
        gmd:resourceMaintenance|
        gmd:graphicOverview
        "/>
      <!-- graphic overview-->
      <xsl:if test="$setDynamicGraphicOverviewMode and $wmsServiceUrl!='' and $layerName!=''">
        <xsl:variable name="wmsBbox"
                      select="$capabilitiesDoc//Layer[Name=$layerName]/LatLonBoundingBox"/>
        <xsl:if test="$wmsBbox/@minx!=''">
          <gmd:graphicOverview>
            <gmd:MD_BrowseGraphic>
              <gmd:fileName>
                <gco:CharacterString>

                  <xsl:value-of
                    select="geonet:get-wms-thumbnail-url($wmsServiceUrl, '1.1.1', $layerName,
                                concat($wmsBbox/@minx, ',', $wmsBbox/@miny, ',', $wmsBbox/@maxx, ',', $wmsBbox/@maxy))"
                  />
                </gco:CharacterString>
              </gmd:fileName>
              <gmd:fileDescription>
                <gco:CharacterString>
                  <xsl:value-of select="$layerName"/>
                </gco:CharacterString>
              </gmd:fileDescription>
            </gmd:MD_BrowseGraphic>
          </gmd:graphicOverview>
        </xsl:if>
      </xsl:if>

      <xsl:copy-of
        select="gmd:resourceFormat|
                gmd:descriptiveKeywords|
                gmd:resourceSpecificUsage|
                gmd:resourceConstraints|
                gmd:aggregationInfo
                "/>

      <!-- Data -->
      <xsl:copy-of
        select="gmd:spatialRepresentationType|
                gmd:spatialResolution|
                gmd:language|
                gmd:characterSet|
                gmd:topicCategory|
                gmd:environmentDescription
                "/>

      <!-- Service -->
      <xsl:copy-of
        select="srv:serviceType|
                srv:serviceTypeVersion|
                srv:accessProperties|
                srv:restrictions|
                srv:keywords
                "/>

      <!-- Keep existing extent and compute
            from WMS service -->

      <!-- replace or add extent. Default mode is add.
            All extent element are processed and if a geographicElement is found,
            it will be removed. Description, verticalElement and temporalElement
            are preserved.

            GeographicElement element having BoundingPolygon are preserved.
      -->
      <xsl:choose>
        <xsl:when test="$setExtentMode">
          <xsl:for-each select="srv:extent|gmd:extent">

            <xsl:choose>
              <xsl:when
                test="gmd:EX_Extent/gmd:temporalElement or gmd:EX_Extent/gmd:verticalElement
                or gmd:EX_Extent/gmd:geographicElement[gmd:EX_BoundingPolygon]">
                <xsl:copy>
                  <xsl:copy-of select="gmd:EX_Extent"/>
                </xsl:copy>
              </xsl:when>
              <xsl:when test="$setAndReplaceExtentMode"/>
              <xsl:otherwise>
                <xsl:copy>
                  <xsl:copy-of select="gmd:EX_Extent"/>
                </xsl:copy>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="srv:extent|gmd:extent"/>
        </xsl:otherwise>
      </xsl:choose>

      <!-- New extent position is after existing ones. -->
      <xsl:if test="$setExtentMode">
        <xsl:for-each
          select="//gmd:onLine/gmd:CI_OnlineResource[contains(gmd:protocol/gco:CharacterString, 'OGC:WMS') and gmd:linkage/gmd:URL=$wmsServiceUrl]">
          <xsl:call-template name="add-extent-for-wms">
            <xsl:with-param name="srv" select="$srv"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:if>

      <!-- End of data -->
      <xsl:copy-of select="gmd:supplementalInformation"/>

      <!-- End of service -->
      <xsl:copy-of select="srv:coupledResource|
                srv:couplingType|
                srv:containsOperations|
                srv:operatesOn
                "/>

      <!-- Note: When applying this stylesheet
            to an ISO profil having a new substitute for
            MD_Identification, profil specific element copy.
            -->
      <xsl:for-each
        select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd'
              and namespace-uri()!='http://www.isotc211.org/2005/srv']">
        <xsl:copy-of select="."/>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
    <xsl:copy>
      <xsl:copy-of
        select="gmd:fileIdentifier
        |gmd:language
        |gmd:characterSet
        |gmd:parentIdentifier
        |gmd:hierarchyLevel
        |gmd:hierarchyLevelName
        |gmd:contact
        |gmd:dateStamp
        |gmd:metadataStandardName
        |gmd:metadataStandardVersion
        |gmd:dataSetURI
        |gmd:locale
        |gmd:spatialRepresentationInfo
        "/>

      <!-- Set spatial ref-->
      <xsl:if test="$setCRSMode and $capabilitiesDoc//SRS">
        <xsl:for-each-group select="$capabilitiesDoc//SRS" group-by=".">
          <gmd:referenceSystemInfo>
            <gmd:MD_ReferenceSystem>
              <xsl:call-template name="RefSystemTypes">
                <xsl:with-param name="srs" select="current-grouping-key()"/>
              </xsl:call-template>
            </gmd:MD_ReferenceSystem>
          </gmd:referenceSystemInfo>
        </xsl:for-each-group>
      </xsl:if>

      <xsl:copy-of select="gmd:metadataExtensionInfo
        "/>

      <xsl:apply-templates select="gmd:identificationInfo"/>

      <xsl:copy-of
        select="gmd:contentInfo
        |gmd:distributionInfo
        |gmd:dataQualityInfo
        |gmd:portrayalCatalogueInfo
        |gmd:metadataConstraints
        |gmd:applicationSchemaInfo
        |gmd:metadataMaintenance
        |gmd:series
        |gmd:describes
        |gmd:propertyType
        |gmd:featureType
        |gmd:featureAttribute
        "/>


      <xsl:for-each
        select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd' and namespace-uri()!='http://www.fao.org/geonetwork']">
        <xsl:copy-of select="."/>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="RefSystemTypes">
    <xsl:param name="srs"/>
    <gmd:referenceSystemIdentifier>
      <gmd:RS_Identifier>
        <gmd:code>
          <gco:CharacterString>
            <xsl:value-of select="$srs"/>
          </gco:CharacterString>
        </gmd:code>
      </gmd:RS_Identifier>
    </gmd:referenceSystemIdentifier>
  </xsl:template>


  <!-- Utility templates -->
  <xsl:template name="add-extent-for-wms">
    <xsl:param name="srv" select="false()"/>
    <xsl:param name="status" select="false()"/>

    <xsl:variable name="layerName" select="gmd:name/gco:CharacterString/text()"/>

    <xsl:choose>
      <xsl:when test="$srv">
        <xsl:variable name="minx" select="math:min($capabilitiesDoc//LatLonBoundingBox/@minx)"/>
        <xsl:variable name="maxx" select="math:max($capabilitiesDoc//LatLonBoundingBox/@maxx)"/>
        <xsl:variable name="miny" select="math:min($capabilitiesDoc//LatLonBoundingBox/@miny)"/>
        <xsl:variable name="maxy" select="math:max($capabilitiesDoc//LatLonBoundingBox/@maxy)"/>
        <srv:extent>
          <xsl:copy-of
            select="geonet:make-iso-extent(string($minx), string($miny), string($maxx), string($maxy), '')"/>
        </srv:extent>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="$capabilitiesDoc//Layer[Name=$layerName]"
                             mode="create-bbox-for-wms"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


  <!-- Create a bounding box -->
  <xsl:template mode="create-bbox-for-wms" match="Layer">
    <xsl:param name="srv" select="false()"/>

    <xsl:for-each select="LatLonBoundingBox">
      <gmd:extent>
        <xsl:copy-of select="geonet:make-iso-extent(@minx, @miny, @maxx, @maxy, '')"/>
      </gmd:extent>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
