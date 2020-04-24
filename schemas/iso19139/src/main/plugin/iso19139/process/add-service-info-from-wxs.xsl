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
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:wcs="http://www.opengis.net/wcs"
                xmlns:wms="http://www.opengis.net/wms"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:owsg="http://www.opengeospatial.net/ows"
                xmlns:ows11="http://www.opengis.net/ows/1.1"
                xmlns:wps="http://www.opengeospatial.net/wps"
                xmlns:wps1="http://www.opengis.net/wps/1.0.0"
                version="2.0"
                exclude-result-prefixes="srv gco gmd exslt geonet wms wfs wcs ows owsg ows11 wps wps1 xlink">

  <xsl:import href="process-utility.xsl"/>

  <!-- i18n information -->
  <xsl:variable name="wxs-info-loc">
    <msg id="a" xml:lang="eng">OGC WMS or WFS service</msg>
    <msg id="b" xml:lang="eng">is described in online resource section. Run this process to add
      operations information
    </msg>
    <msg id="a" xml:lang="fre">Le service WMS ou WFS</msg>
    <msg id="b" xml:lang="fre">est décrit dans la section resource en ligne. Exécuter cette action
      pour ajouter ou remplacer les informations relatives aux opérations
    </msg>
    <msg id="a" xml:lang="dut">Er is een verwijzing gevonden naar de WMS of WFS service </msg>
    <msg id="b" xml:lang="dut">. Voer deze functie uit om operationele informatie toe te voegen of bij te werken. </msg>
  </xsl:variable>

  <!-- Process parameters and variables-->
  <xsl:param name="setAndReplaceOperations" select="'0'"/>
  <xsl:param name="wxsServiceUrl"/>

  <xsl:variable name="setAndReplaceOperationsMode"
                select="geonet:parseBoolean($setAndReplaceOperations)"/>


  <!-- Load the capabilities document if one oneline resource contains a protocol set to WMS or WFS
    Check if containsOperation element is already defined
  -->
  <xsl:variable name="wxsOnlineNodes"
                select="//gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine//gmd:CI_OnlineResource[(contains(gmd:protocol/gco:CharacterString, 'OGC:WMS')
    or contains(gmd:protocol/gco:CharacterString, 'OGC:WFS')) and gmd:linkage/gmd:URL = $wxsServiceUrl]"/>
  <xsl:variable name="wxsProtocol" select="$wxsOnlineNodes/gmd:protocol/gco:CharacterString"/>

  <xsl:variable name="alreadyContainsOp" select="count(//srv:containsOperations[
      srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL=$wxsServiceUrl])"/>

  <xsl:variable name="wxsCapabilitiesDoc">
    <xsl:if test="$wxsOnlineNodes and $alreadyContainsOp = 0">
      <xsl:choose>
        <xsl:when test="contains($wxsProtocol, 'WMS')">
          <xsl:copy-of select="geonet:get-wxs-capabilities($wxsServiceUrl, 'WMS', '1.3.0')"/>
        </xsl:when>
        <xsl:when test="contains($wxsProtocol, 'WFS')">
          <xsl:copy-of select="geonet:get-wxs-capabilities($wxsServiceUrl, 'WFS', '1.1.0')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:message>process:add-service-info-from-wxs: Unsupported protocol.</xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:variable>


  <xsl:template name="list-add-service-info-from-wxs">
    <suggestion process="add-service-info-from-wxs"/>
  </xsl:template>


  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-add-service-info-from-wxs">
    <xsl:param name="root"/>

    <xsl:variable name="srv"
                  select="$root//*[local-name(.)='SV_ServiceIdentification' or contains(@gco:isoType, 'SV_ServiceIdentification')]"/>

    <xsl:variable name="onlineResources"
                  select="$root//gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[(contains(gmd:protocol/gco:CharacterString, 'OGC:WMS')
                                            or contains(gmd:protocol/gco:CharacterString, 'OGC:WFS'))
                                            and normalize-space(gmd:linkage/gmd:URL)!='']"/>

    <xsl:if test="$srv"><!-- Only apply to service metadata-->
      <xsl:for-each select="$onlineResources">
        <suggestion process="add-service-info-from-wxs" id="{generate-id()}-service"
                    category="onlineSrc" target="srv:containsOperations">
          <name>
            <xsl:value-of select="geonet:i18n($wxs-info-loc, 'a', $guiLang)"/><xsl:value-of
            select="./gmd:linkage/gmd:URL"
          /><xsl:value-of select="geonet:i18n($wxs-info-loc, 'b', $guiLang)"/>.
          </name>
          <operational>true</operational>
          <params>{ "setAndReplaceOperations":{"type":"boolean", "defaultValue":"<xsl:value-of
            select="$setAndReplaceOperations"/>"},
            "wxsServiceUrl":{"type":"string", "defaultValue":"<xsl:value-of
              select="normalize-space(gmd:linkage/gmd:URL)"/>"}
            }
          </params>
        </suggestion>
      </xsl:for-each>
    </xsl:if>
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

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <!-- Copy all elements from AbstractMD_IdentificationType-->
      <xsl:copy-of
        select="gmd:*"/>


      <!-- Service -->
      <xsl:copy-of
        select="srv:serviceType|
                srv:serviceTypeVersion|
                srv:accessProperties|
                srv:restrictions|
                srv:keywords|
                srv:extent|
                srv:coupledResource|
                srv:couplingType
                "/>
      <!-- Adding contains operation info -->
      <xsl:if test="not($setAndReplaceOperationsMode)">
        <xsl:copy-of select="srv:containsOperations"/>
      </xsl:if>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        Operation could be OGC standard operation described in specification
        OR a specific process in a WPS. In that case, each process are described
        as one operation.

      TODO : WPS not supported yet
      -->

      <xsl:variable name="ows">
        <xsl:choose>
          <xsl:when test="(local-name($wxsCapabilitiesDoc/.)='WFS_Capabilities' and namespace-uri($wxsCapabilitiesDoc/.)='http://www.opengis.net/wfs'
            and $wxsCapabilitiesDoc/./@version='1.1.0')
            or (local-name($wxsCapabilitiesDoc/.)='Capabilities' and namespace-uri($wxsCapabilitiesDoc/.)='http://www.opengeospatial.net/wps')
            or (local-name($wxsCapabilitiesDoc/.)='Capabilities' and namespace-uri($wxsCapabilitiesDoc/.)='http://www.opengis.net/wps/1.0.0')">
            true
          </xsl:when>
          <xsl:otherwise>false</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:for-each select="$wxsCapabilitiesDoc//Request/*|
        $wxsCapabilitiesDoc//wfs:Request/*|
        $wxsCapabilitiesDoc//wms:Request/*|
        $wxsCapabilitiesDoc//wcs:Request/*|
        $wxsCapabilitiesDoc//ows:OperationsMetadata/ows:Operation|
        $wxsCapabilitiesDoc//ows11:OperationsMetadata/ows:Operation|
        $wxsCapabilitiesDoc//wps:ProcessOfferings/*|
        $wxsCapabilitiesDoc//wps1:ProcessOfferings/*">
        <!-- Some services provide information about ows:ExtendedCapabilities TODO ? -->

        <srv:containsOperations>
          <srv:SV_OperationMetadata>
            <srv:operationName>
              <gco:CharacterString>
                <xsl:choose>
                  <xsl:when test="name(.)='wps:Process'">WPS Process:
                    <xsl:value-of select="ows:Title|ows11:Title"/>
                  </xsl:when>
                  <xsl:when test="$ows='true'">
                    <xsl:value-of select="@name"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="name(.)"/>
                  </xsl:otherwise>
                </xsl:choose>
              </gco:CharacterString>
            </srv:operationName>
            <!--  CHECKME : DCPType/SOAP ?
            <xsl:for-each select="DCPType/HTTP/*|wfs:DCPType/wfs:HTTP/*|wms:DCPType/wms:HTTP/*|
            wcs:DCPType/wcs:HTTP/*|ows:DCP/ows:HTTP/*|ows11:DCP/ows11:HTTP/*"> -->
            <srv:DCP>
              <srv:DCPList
                codeList="http://www.isotc211.org/2005/iso19119/resources/Codelist/gmxCodelists.xml#DCPList"
                codeListValue="WebServices"/>
            </srv:DCP>
            <!--</xsl:for-each>-->

            <xsl:if test="name(.)='wps:Process' or name(.)='wps11:ProcessOfferings'">
              <srv:operationDescription>
                <gco:CharacterString>
                  <xsl:value-of select="ows:Abstract|ows11:Title"/>
                </gco:CharacterString>
              </srv:operationDescription>
              <srv:invocationName>
                <gco:CharacterString>
                  <xsl:value-of select="ows:Identifier|ows11:Identifier"/>
                </gco:CharacterString>
              </srv:invocationName>
            </xsl:if>

            <xsl:for-each
              select="Format|wms:Format|ows:Parameter[@name='AcceptFormats' or @name='outputFormat']">
              <srv:connectPoint>
                <gmd:CI_OnlineResource>
                  <gmd:linkage>
                    <gmd:URL>
                      <xsl:choose>
                        <xsl:when test="$ows='true'">
                          <xsl:value-of
                            select="..//ows:Get[1]/@xlink:href"/><!-- FIXME supposed at least one Get -->
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of
                            select="..//*[1]/OnlineResource/@xlink:href|..//*[1]/wms:OnlineResource/@xlink:href"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </gmd:URL>
                  </gmd:linkage>
                  <gmd:protocol>
                    <gco:CharacterString>
                      <xsl:value-of select="$wxsProtocol"/>
                    </gco:CharacterString>
                  </gmd:protocol>
                  <gmd:description>
                    <gco:CharacterString>
                      Format :
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </gmd:description>
                  <gmd:function>
                    <gmd:CI_OnLineFunctionCode
                      codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_OnLineFunctionCode"
                      codeListValue="information"/>
                  </gmd:function>
                </gmd:CI_OnlineResource>
              </srv:connectPoint>
            </xsl:for-each>


            <!-- Some Operations in WFS 1.0.0 have no ResultFormat no CI_OnlineResource created
              WCS has no output format
            -->
            <xsl:for-each select="wfs:ResultFormat/*">
              <srv:connectPoint>
                <gmd:CI_OnlineResource>
                  <gmd:linkage>
                    <gmd:URL>
                      <xsl:value-of select="../..//wfs:Get[1]/@onlineResource"/>
                    </gmd:URL>
                  </gmd:linkage>
                  <gmd:protocol>
                    <gco:CharacterString>
                      <xsl:value-of select="name(.)"/>
                    </gco:CharacterString>
                  </gmd:protocol>
                  <gmd:function>
                    <gmd:CI_OnLineFunctionCode
                      codeList="./resources/codeList.xml#CI_OnLineFunctionCode"
                      codeListValue="information"/>
                  </gmd:function>
                </gmd:CI_OnlineResource>
              </srv:connectPoint>
            </xsl:for-each>
          </srv:SV_OperationMetadata>
        </srv:containsOperations>
      </xsl:for-each>

      <!-- End of service -->
      <xsl:copy-of select="srv:operatesOn"/>

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

</xsl:stylesheet>
