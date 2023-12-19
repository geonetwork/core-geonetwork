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
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:math="http://exslt.org/math"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:import href="process-utility.xsl"/>

  <!-- i18n information -->
  <xsl:variable name="wms-info-loc">
    <msg id="a" xml:lang="eng">WMS service </msg>
    <msg id="b" xml:lang="eng"> is described in online resource section. Run to update extent, CRS or
      graphic overview
      for this WMS service for the layer named:
    </msg>
    <msg id="a" xml:lang="fre">Le service de visualisation </msg>
    <msg id="b" xml:lang="fre"> est décrit dans la section resource en ligne. Exécuter cette action
      pour mettre à jour l'étendue, les systèmes de projection
      ou les aperçus pour ce service et la couche nommée :
    </msg>
    <msg id="a" xml:lang="dut">Er is een verwijzing gevonden naar de WMS service </msg>
    <msg id="b" xml:lang="dut">. Gebruik deze functie om de dekking, de projectie of thumbnail af te leiden of bij te werken vanuit deze WMS-service voor de laag met de naam: </msg>
    <msg id="connectPoint" xml:lang="eng">WMS view service</msg>
    <msg id="connectPoint" xml:lang="fre">Service de visualisation WMS</msg>
    <msg id="connectPointDesc" xml:lang="eng">Service connect point URL</msg>
    <msg id="connectPointDesc" xml:lang="fre">Adresse de connexion au service de visualisation WMS</msg>
  </xsl:variable>

  <!-- Process parameters and variables-->
  <xsl:param name="mode" select="'process'"/>
  <xsl:param name="setExtent" select="'0'"/>
  <xsl:param name="setAndReplaceExtent" select="'0'"/>
  <xsl:param name="setCRS" select="'0'"/>
  <xsl:param name="setDynamicGraphicOverview" select="'0'"/>
  <xsl:param name="setServiceConnectPoint" select="'0'"/>
  <xsl:param name="wmsServiceUrl"/>

  <xsl:variable name="maxSrs" select="21"/>

  <xsl:variable name="setExtentMode" select="geonet:parseBoolean($setExtent)"/>
  <xsl:variable name="setAndReplaceExtentMode" select="geonet:parseBoolean($setAndReplaceExtent)"/>
  <xsl:variable name="setCRSMode" select="geonet:parseBoolean($setCRS)"/>
  <xsl:variable name="setDynamicGraphicOverviewMode"
                select="geonet:parseBoolean($setDynamicGraphicOverview)"/>


  <!-- Load the capabilities document if one oneline resource contains a protocol set to WMS
  -->
  <xsl:variable name="onlineNodes"
                select="//cit:CI_OnlineResource[contains(cit:protocol/gco:CharacterString, 'OGC:WMS') and normalize-space(cit:linkage/*/text()) = $wmsServiceUrl]"/>
  <xsl:variable name="layerName" select="$onlineNodes/cit:name/gco:CharacterString"/>
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
                  select="$root//mrd:onLine/*[
                                  contains(cit:protocol/gco:CharacterString, 'OGC:WMS')
                                  and normalize-space(cit:linkage/gco:CharacterString) != '']"/>
    <xsl:variable name="srv"
                  select="count($root//*[local-name(.)='SV_ServiceIdentification' or contains(@gco:isoType, 'SV_ServiceIdentification')]) > 0"/>

    <!-- Check if server is up and new value are available
     <xsl:variable name="capabilities"
      select="geonet:get-wms-capabilities(gmd:linkage/gmd:URL, '1.1.1')"/>
-->
    <xsl:for-each select="$onlineResources">
      <xsl:variable name="url"
                    select="normalize-space(cit:linkage/gco:CharacterString)"/>
      <suggestion process="add-info-from-wms" id="{generate-id()}" category="onlineSrc"
                  target="gex:extent">
        <name>
          <xsl:value-of select="geonet:i18n($wms-info-loc, 'a', $guiLang)"/><xsl:value-of
          select="./cit:linkage/gco:CharacterString"
        /><xsl:value-of select="geonet:i18n($wms-info-loc, 'b', $guiLang)"/><xsl:value-of
          select="./cit:name/gco:CharacterString"/>.
        </name>
        <operational>true</operational>
        <params>{
          "setExtent":{"type":"boolean", "defaultValue":"<xsl:value-of select="$setExtent"/>"},
          "setAndReplaceExtent":{"type":"boolean", "defaultValue":"<xsl:value-of
            select="$setAndReplaceExtent"/>"},
          "setCRS":{"type":"boolean", "defaultValue":"<xsl:value-of
            select="$setCRS"/>"},
          <xsl:if test="not($srv)">
            "setDynamicGraphicOverview":{"type":"boolean",
            "defaultValue":"<xsl:value-of select="$setDynamicGraphicOverview"/>"},
          </xsl:if>
          <xsl:if test="$srv and count($root//srv:containsOperations[
                      */srv:connectPoint/*/cit:linkage/*/text() = $url]) = 0">
            "setServiceConnectPoint":{"type":"boolean",
            "defaultValue":"<xsl:value-of select="$setServiceConnectPoint"/>"},
          </xsl:if>
          "wmsServiceUrl":{"type":"string", "defaultValue":"<xsl:value-of
            select="$url"/>"}
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
    match="mdb:identificationInfo/*"
    priority="2">

    <xsl:variable name="srv"
                  select="local-name(.)='SV_ServiceIdentification'
            or contains(@gco:isoType, 'SV_ServiceIdentification')"/>


    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="mri:citation"/>
      <xsl:apply-templates select="mri:abstract"/>
      <xsl:apply-templates select="mri:purpose"/>
      <xsl:apply-templates select="mri:credit"/>
      <xsl:apply-templates select="mri:status"/>
      <xsl:apply-templates select="mri:pointOfContact"/>
      <xsl:apply-templates select="mri:spatialRepresentationType"/>
      <xsl:apply-templates select="mri:spatialResolution"/>
      <xsl:apply-templates select="mri:temporalResolution"/>
      <xsl:apply-templates select="mri:topicCategory"/>

      <!-- replace or add extent. Default mode is add.
            All extent element are processed and if a geographicElement is found,
            it will be removed. Description, verticalElement and temporalElement
            are preserved.

            GeographicElement element having BoundingPolygon are preserved.
      -->
      <xsl:choose>
        <xsl:when test="$setExtentMode">
          <xsl:for-each select="mri:extent">

            <xsl:choose>
              <xsl:when
                test="*/gex:temporalElement
                      or */gex:verticalElement
                      or */gex:geographicElement[gex:EX_BoundingPolygon]">
                <xsl:copy>
                  <xsl:copy-of select="*"/>
                </xsl:copy>
              </xsl:when>
              <xsl:when test="$setAndReplaceExtentMode"/>
              <xsl:otherwise>
                <xsl:copy>
                  <xsl:copy-of select="*"/>
                </xsl:copy>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="mri:extent"/>
        </xsl:otherwise>
      </xsl:choose>

      <!-- New extent position is after existing ones. -->
      <xsl:if test="$setExtentMode">
        <xsl:for-each
          select="//mrd:onLine/*[
                    contains(cit:protocol/gco:CharacterString, 'OGC:WMS')
                    and cit:linkage/gco:CharacterString = $wmsServiceUrl]">
          <xsl:call-template name="add-extent-for-wms">
            <xsl:with-param name="srv" select="$srv"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:if>

      <xsl:apply-templates select="mri:additionalDocumentation"/>
      <xsl:apply-templates select="mri:processingLevel"/>
      <xsl:apply-templates select="mri:resourceMaintenance"/>

      <!-- graphic overview-->
      <xsl:if test="$setDynamicGraphicOverviewMode
                    and $wmsServiceUrl != ''
                    and $layerName != ''">
        <xsl:variable name="wmsBbox"
                      select="$capabilitiesDoc//Layer[Name=$layerName]/LatLonBoundingBox"/>
        <xsl:if test="$wmsBbox/@minx!=''">
          <mri:graphicOverview>
            <mcc:MD_BrowseGraphic>
              <mcc:fileName>
                <gco:CharacterString>
                  <xsl:value-of
                    select="geonet:get-wms-thumbnail-url($wmsServiceUrl, '1.1.1', $layerName,
                                concat($wmsBbox/@minx, ',', $wmsBbox/@miny, ',', $wmsBbox/@maxx, ',', $wmsBbox/@maxy))"
                  />
                </gco:CharacterString>
              </mcc:fileName>
            </mcc:MD_BrowseGraphic>
          </mri:graphicOverview>
        </xsl:if>
      </xsl:if>

      <xsl:apply-templates select="mri:graphicOverview"/>

      <xsl:apply-templates select="mri:resourceFormat"/>
      <xsl:apply-templates select="mri:descriptiveKeywords"/>
      <xsl:apply-templates select="mri:resourceSpecificUsage"/>
      <xsl:apply-templates select="mri:resourceConstraints"/>
      <xsl:apply-templates select="mri:associatedResource"/>

      <xsl:apply-templates select="mri:defaultLocale"/>
      <xsl:apply-templates select="mri:otherLocale"/>
      <xsl:apply-templates select="mri:environmentDescription"/>
      <xsl:apply-templates select="mri:supplementalInformation"/>

      <xsl:apply-templates select="srv:serviceType
                                  |srv:serviceTypeVersion
                                  |srv:accessProperties
                                  |srv:couplingType
                                  |srv:coupledResource
                                  |srv:operatedDataset
                                  |srv:profile
                                  |srv:serviceStandard
                                  |srv:containsOperations
      "/>

      <xsl:if test="$setServiceConnectPoint
                    and count(srv:containsOperations[
                      */srv:connectPoint/*/cit:linkage/*/text() = $wmsServiceUrl]) = 0">
        <srv:containsOperations>
          <srv:SV_OperationMetadata>
            <srv:operationName>
              <gco:CharacterString>GetCapabilities</gco:CharacterString>
            </srv:operationName>
            <srv:distributedComputingPlatform>
              <srv:DCPList codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#DCPList" codeListValue="WebServices"/>
            </srv:distributedComputingPlatform>
            <srv:connectPoint>
              <cit:CI_OnlineResource>
                <cit:linkage>
                  <gco:CharacterString><xsl:value-of select="$wmsServiceUrl"/></gco:CharacterString>
                </cit:linkage>
                <cit:protocol>
                  <gco:CharacterString>OGC:WMS</gco:CharacterString>
                </cit:protocol>
                <cit:name>
                  <gco:CharacterString><xsl:value-of select="geonet:i18n($wms-info-loc, 'connectPoint', $guiLang)"/></gco:CharacterString>
                </cit:name>
                <cit:description>
                  <gco:CharacterString><xsl:value-of select="geonet:i18n($wms-info-loc, 'connectPointDesc', $guiLang)"/></gco:CharacterString>
                </cit:description>
                <cit:function>
                  <cit:CI_OnLineFunctionCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_OnLineFunctionCode" codeListValue=""/>
                </cit:function>
              </cit:CI_OnlineResource>
            </srv:connectPoint>
          </srv:SV_OperationMetadata>
        </srv:containsOperations>
      </xsl:if>

      <xsl:apply-templates select="srv:operatesOn
                                  |srv:containsChain
      "/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="mdb:MD_Metadata|*[@gco:isoType='mdb:MD_Metadata']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="mdb:metadataIdentifier"/>
      <xsl:apply-templates select="mdb:defaultLocale"/>
      <xsl:apply-templates select="mdb:parentMetadata"/>
      <xsl:apply-templates select="mdb:metadataScope"/>
      <xsl:apply-templates select="mdb:contact"/>
      <xsl:apply-templates select="mdb:dateInfo"/>
      <xsl:apply-templates select="mdb:metadataStandard"/>
      <xsl:apply-templates select="mdb:metadataProfile"/>
      <xsl:apply-templates select="mdb:alternativeMetadataReference"/>
      <xsl:apply-templates select="mdb:otherLocale"/>
      <xsl:apply-templates select="mdb:metadataLinkage"/>
      <xsl:apply-templates select="mdb:spatialRepresentationInfo"/>


      <!-- Set spatial ref-->
      <xsl:choose>
        <xsl:when test="$setCRSMode">
          <xsl:for-each select="distinct-values($capabilitiesDoc//SRS)[position() &lt; $maxSrs]">
            <xsl:call-template name="RefSystemTypes">
              <xsl:with-param name="srs" select="."/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="mdb:referenceSystemInfo"/>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates select="mdb:metadataExtensionInfo"/>
      <xsl:apply-templates select="mdb:identificationInfo"/>
      <xsl:apply-templates select="mdb:contentInfo"/>
      <xsl:apply-templates select="mdb:distributionInfo"/>
      <xsl:apply-templates select="mdb:dataQualityInfo"/>
      <xsl:apply-templates select="mdb:resourceLineage"/>
      <xsl:apply-templates select="mdb:portrayalCatalogueInfo"/>
      <xsl:apply-templates select="mdb:metadataConstraints"/>
      <xsl:apply-templates select="mdb:applicationSchemaInfo"/>
      <xsl:apply-templates select="mdb:metadataMaintenance"/>
      <xsl:apply-templates select="mdb:acquisitionInformation"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="RefSystemTypes">
    <xsl:param name="srs"/>

    <mdb:referenceSystemInfo>
      <mrs:MD_ReferenceSystem>
        <mrs:referenceSystemIdentifier>
          <mcc:MD_Identifier>
            <mcc:code>
              <xsl:choose>
                <xsl:when test="starts-with($srs, 'EPSG:')">
                  <gcx:Anchor xlink:href="http://www.opengis.net/def/crs/EPSG/0/{replace($srs, 'EPSG:', '')}">
                    <xsl:value-of select="$srs"/></gcx:Anchor>
                </xsl:when>
                <xsl:otherwise>
                  <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
                </xsl:otherwise>
              </xsl:choose>
            </mcc:code>
          </mcc:MD_Identifier>
        </mrs:referenceSystemIdentifier>
      </mrs:MD_ReferenceSystem>
    </mdb:referenceSystemInfo>
  </xsl:template>


  <!-- Utility templates -->
  <xsl:template name="add-extent-for-wms">
    <xsl:param name="srv" select="false()"/>

    <xsl:variable name="layerName" select="cit:name/gco:CharacterString/text()"/>

    <xsl:choose>
      <xsl:when test="$srv">
        <xsl:variable name="minx" select="math:min($capabilitiesDoc//LatLonBoundingBox/@minx)"/>
        <xsl:variable name="maxx" select="math:max($capabilitiesDoc//LatLonBoundingBox/@maxx)"/>
        <xsl:variable name="miny" select="math:min($capabilitiesDoc//LatLonBoundingBox/@miny)"/>
        <xsl:variable name="maxy" select="math:max($capabilitiesDoc//LatLonBoundingBox/@maxy)"/>
        <mri:extent>
          <xsl:copy-of
            select="geonet:make-iso-extent(string($minx), string($miny), string($maxx), string($maxy), '')"/>
        </mri:extent>
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
      <mri:extent>
        <xsl:copy-of select="geonet:make-iso-extent(@minx, @miny, @maxx, @maxy, '')"/>
      </mri:extent>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
