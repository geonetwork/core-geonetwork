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
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:wms="http://www.opengis.net/wms"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:owsg="http://www.opengeospatial.net/ows"
                xmlns:ows11="http://www.opengis.net/ows/1.1"
                xmlns:ows2="http://www.opengis.net/ows/2.0"
                xmlns:wcs="http://www.opengis.net/wcs"
                xmlns:wps="http://www.opengeospatial.net/wps"
                xmlns:wps1="http://www.opengis.net/wps/1.0.0"
                xmlns:wps2="http://www.opengis.net/wps/2.0"
                xmlns:inspire_vs="http://inspire.ec.europa.eu/schemas/inspire_vs/1.0"
                xmlns:inspire_common="http://inspire.ec.europa.eu/schemas/common/1.0"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:math="http://exslt.org/math"
                extension-element-prefixes="saxon math"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:import href="OGCWxSGetCapabilitiesto19119/resp-party.xsl"/>

  <xsl:output indent="yes"/>

  <!-- Metadata record UUID generated. -->
  <xsl:param name="uuid"
             select="''"/>

  <!-- Layer name to process -->
  <xsl:param name="Name"
             select="''"/>

  <!--
  Editor can change the language once the record is created.

  Multilingual records injection is partially supported for the
  field title and abstract. Which means that editor can translated
  the title of a record harvested and those will be preserved.
  -->
  <xsl:param name="lang"
             select="''"/>

  <xsl:variable name="isBuildingDatasetRecord"
                select="$Name != ''"/>

  <!-- Service type is used for layer only. It allows to combine
  information from a WMS and a WFS in the same metadata record
  as far as the service URL is the same. For example this works
  for GeoServer which provides a /ows URL to access both WMS and WFS service.
  -->
  <xsl:param name="serviceType"
             select="'OGC'"/>

  <xsl:variable name="nilReasonValue"
                select="concat('synchronizedFrom', $serviceType)"/>

  <!-- Max number of coordinate system to add
    to the metadata record. Avoid to have too many CRS when
    OGC server list all epsg database. -->
  <xsl:variable name="maxCRS">21</xsl:variable>

  <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>

  <xsl:variable name="record"
                select="/root/record"/>
  <xsl:variable name="getCapabilities"
                select="/root/getCapabilities"/>
  <xsl:variable name="rootName"
                select="$getCapabilities/*/local-name()"/>
  <xsl:variable name="rootNameWithNs"
                select="$getCapabilities/*/name()"/>
  <xsl:variable name="serviceTitle"
                select="$getCapabilities/(*/ows:ServiceIdentification/ows:Title|
                       */ows11:ServiceIdentification/ows11:Title|
                       */ows2:ServiceIdentification/ows2:Title|
                       */wfs:Service/wfs:Title|
                       */wms:Service/wms:Title|
                       */Service/Title|
                       */csw:Capabilities/ows:ServiceIdentification/ows:Title|
                       */wcs:Service/wcs:label)/text()"/>
  <xsl:variable name="layerTitle"
                select="$getCapabilities/(
                                  *//wms:Layer[wms:Name=$Name]/wms:Title|
                                  *//Layer[Name=$Name]/Title|
                                  *//wcs:CoverageOfferingBrief[wcs:name=$Name]/wcs:label|
                                  *//wps2:Process[ows2:Identifier=$Name]/ows2:Title|
                                  *//wfs:FeatureType[wfs:Name=$Name]/wfs:Title)/text()"/>
  <xsl:variable name="ows">
    <xsl:choose>
      <xsl:when test="($rootName='WFS_Capabilities' and namespace-uri($getCapabilities/*)='http://www.opengis.net/wfs' and $getCapabilities/*/@version='1.1.0')
          or ($rootName='Capabilities' and namespace-uri($getCapabilities/*)='http://www.opengeospatial.net/wps')
          or ($rootName='Capabilities' and namespace-uri($getCapabilities/*)='http://www.opengis.net/wps/1.0.0')">
        true
      </xsl:when>
      <xsl:otherwise>false</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>






  <!-- XSLT to inject GetCapabilities information into an existing metadata record.

  Some assumptions are made on the input metadata record.
  Some elements MUST exist in the input document in order to trigger insertion:
  * Mandatory element:
    * fileIdentifier (to insert the UUID).
    * dateStamp (may be replaced by INSPIRE MetadataDate).
    dateStamp is also used to insert metadata contact just before it.
    * title
    * citation date used to insert INSPIRE last revision date.
    * abstract
    * DQ/scope used to insert INSPIRE conformant section after it.
  * Optional ones:
    * metadata language (may be replaced by INSPIRE ResponseLanguage)
    * fees
    * serviceType
    * service
    * MD_DigitalTransferOptions (at least an empty section should exist to trigger insertion of service URL)

  Other element inserted from the capabilities:
  * Resource contact
  * Keywords
  * Resource constraints
  * srv:extent
  * srv:containsOperations
  * srv:couplingType


  Elements inserted from the getCapabilities document are flagged with a
  nilReason attribute with value 'synchronized'. Those elements are ignore
  on next processing round.
  -->
  <xsl:template match="/">
    <xsl:apply-templates mode="copy"
                         select="/root/record/*"/>
  </xsl:template>



  <xsl:template mode="copy"
                match="gmd:fileIdentifier/gco:CharacterString"
                priority="1999">
    <xsl:copy>
      <xsl:value-of select="$uuid"/>
    </xsl:copy>
  </xsl:template>


  <!-- INSPIRE extension elements -->
  <!-- Insert dateStamp or set it to now. -->
  <xsl:template mode="copy"
                match="gmd:MD_Metadata/gmd:dateStamp/text()"
                priority="1999">
    <xsl:variable name="date"
                  select="normalize-space(//inspire_vs:ExtendedCapabilities/inspire_common:MetadataDate/text())"/>

    <xsl:value-of select="if ($date != '')
                          then $date
                          else format-dateTime(current-dateTime(),$df)"/>
  </xsl:template>




  <!-- Insert title.   -->
  <xsl:template mode="copy"
                match="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title"
                priority="1999">

    <xsl:copy>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gco:CharacterString>
        <xsl:value-of select="$serviceTitle"/>
      </gco:CharacterString>
      <xsl:copy-of select="gmd:PT_FreeText"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="copy"
                match="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title"
                priority="1999">

    <xsl:copy>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gco:CharacterString>
        <xsl:value-of select="$layerTitle"/>
      </gco:CharacterString>
      <xsl:copy-of select="gmd:PT_FreeText"/>
    </xsl:copy>
  </xsl:template>



  <!-- Insert abstract. -->
  <xsl:template mode="copy"
                match="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract"
                priority="1999">
    <xsl:copy>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gco:CharacterString>
        <xsl:value-of select="$getCapabilities/(*/ows:ServiceIdentification/ows:Abstract|
                       */ows11:ServiceIdentification/ows11:Abstract|
                       */wfs:Service/wfs:Abstract|
                       */wms:Service/wms:Abstract|
                       */Service/Abstract|
                       */csw:Capabilities/ows:ServiceIdentification/ows:Abstract|
                       */wcs:Service/wcs:description)/text()"/>
      </gco:CharacterString>
      <xsl:copy-of select="gmd:PT_FreeText"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="copy"
                match="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract"
                priority="1999">

    <xsl:copy>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gco:CharacterString>
        <xsl:value-of select="$getCapabilities//(
                                  wms:Layer[wms:Name=$Name]/wms:Abstract|
                                  Layer[Name=$Name]/Abstract|
                                  wcs:CoverageOfferingBrief[wcs:name=$Name]/wcs:description|
                                  wfs:FeatureType[wfs:Name=$Name]/wfs:Abstract|
                                  wps2:Process[ows2:Identifier=$Name]/ows2:Abstract)/text()
                                  "/>


        <xsl:variable name="processes"
                      select="$getCapabilities//wps2:Process[ows2:Identifier=$Name]/(wps2:Input|wps2:Output)"/>
        <xsl:for-each select="$processes">
          * <xsl:value-of select="concat(local-name(.), ':', ows2:Title, ', ', ows2:Abstract, '(', string-join(wps2:LiteralData/wps2:Format/@mimeType, ', '), ')')"/>
        </xsl:for-each>
      </gco:CharacterString>
      <xsl:copy-of select="gmd:PT_FreeText"/>
    </xsl:copy>
  </xsl:template>




  <!-- If GetCapabilities define a language with INSPIRE extension use it,
   if a language is defined in the template or in the record, use it,
   if a parameter is set on the harvester parameters, use it or default to eng. -->
  <xsl:template mode="copy"
              match="gmd:MD_Metadata/gmd:language"
              priority="1999">
    <xsl:copy>
      <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/">
        <xsl:variable name="currentLanguage"
                      select="*/@codeListValue"/>
        <xsl:variable name="language"
                      select="normalize-space(//inspire_vs:ExtendedCapabilities/inspire_common:ResponseLanguage/inspire_common:Language/text())"/>

        <xsl:attribute name="codeListValue"
                       select="if ($currentLanguage = '' and $language != '') then $language
                               else if ($currentLanguage != '') then $currentLanguage
                               else if ($lang != '') then $lang else 'eng'"/>
      </gmd:LanguageCode>
    </xsl:copy>
  </xsl:template>


  <!--
  Metadata contact (the one from the capabilities is added just before the datestamp which is mandatory).
  -->
  <xsl:template mode="copy"
                match="gmd:MD_Metadata/gmd:dateStamp">
    <xsl:variable name="contacts"
                  select="$getCapabilities/(*/Service/ContactInformation|
                          */wfs:Service/wfs:ContactInformation|
                          */wms:Service/wms:ContactInformation|
                          */ows:ServiceProvider|
                          */owsg:ServiceProvider|
                          */ows11:ServiceProvider)"/>
    <xsl:for-each select="$contacts">
      <gmd:contact>
        <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
        <xsl:apply-templates mode="convert"
                             select="$contacts"/>
      </gmd:contact>
    </xsl:for-each>

    <xsl:copy-of select="."/>
  </xsl:template>





  <xsl:template mode="copy"
                match="gmd:identificationInfo/*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="copy" select="gmd:citation"/>
      <xsl:apply-templates mode="copy" select="gmd:abstract"/>
      <xsl:apply-templates mode="copy" select="gmd:purpose"/>

      <!-- CSW Add queryables in purpose -->
      <xsl:if test="$rootNameWithNs = 'csw:Capabilities'">
        <gmd:purpose>
          <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
          <gco:CharacterString>
            <xsl:for-each
              select="$getCapabilities//ows:Constraint[@name='SupportedISOQueryables' or @name='AdditionalQueryables']/ows:Value">
              <xsl:value-of select="."/>
              <xsl:if test="position()!=last()">,</xsl:if>
            </xsl:for-each>
          </gco:CharacterString>
        </gmd:purpose>
      </xsl:if>

      <xsl:apply-templates mode="copy" select="gmd:credit"/>
      <xsl:apply-templates mode="copy" select="gmd:status"/>


      <!-- Insert contact. -->
      <xsl:variable name="contacts"
                    select="$getCapabilities//(ContactInformation|
                           wcs:responsibleParty|
                           wms:responsibleParty|
                           wms:Service/wms:ContactInformation|
                           ows:ServiceProvider|
                           ows11:ServiceProvider)"/>

      <xsl:for-each select="$contacts">
        <gmd:pointOfContact>
          <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
          <xsl:apply-templates mode="convert"
                               select="."/>
        </gmd:pointOfContact>
      </xsl:for-each>
      <xsl:apply-templates mode="copy" select="gmd:pointOfContact"/>



      <xsl:apply-templates mode="copy" select="gmd:resourceMaintenance"/>

      <!-- Do not copy thumbnail generated for WMS which will be updated later on. -->
      <xsl:if test="$serviceType = 'WMS'">
        <xsl:apply-templates mode="copy" select="gmd:graphicOverview[not(contains(*/gmd:fileName/gco:CharacterString, concat('attachments/', $uuid, '.png')))]"/>
      </xsl:if>
      <xsl:apply-templates mode="copy" select="gmd:resourceFormat"/>

      <!-- CSW Add output schema -->
      <xsl:if test="$rootNameWithNs = 'csw:Capabilities'">
        <xsl:for-each-group select="//ows:Parameter[@name='outputSchema']/ows:Value" group-by=".">
          <gmd:resourceFormat>
            <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
            <gmd:MD_Format>
              <gmd:name>
                <gco:CharacterString>
                  <xsl:value-of select="."/>
                </gco:CharacterString>
              </gmd:name>
              <gmd:version gco:nilReason="inapplicable">
                <gco:CharacterString/>
              </gmd:version>
            </gmd:MD_Format>
          </gmd:resourceFormat>
        </xsl:for-each-group>
      </xsl:if>



      <!-- Insert keywords. -->
      <xsl:variable name="keywords"
                    select="if ($Name != '')
                            then $getCapabilities//(
                              Layer[Name = $Name]/KeywordList|
                              wms:Layer[wms:Name = $Name]/wms:KeywordList|
                              wfs:FeatureType[wfs:Name=$Name]/ows:Keywords|
                              wcs:CoverageOfferingBrief[wcs:name=$Name]/wcs:keywords)
                            else $getCapabilities/(*/ows:ServiceIdentification/ows:Keywords|
                               */ows11:ServiceIdentification/ows11:Keywords|
                               */wms:Service/wms:KeywordList|
                               */wfs:Service/wfs:keywords|
                               */Service/KeywordList|
                               */wcs:Service/wcs:keywords|
                               *//inspire_vs:ExtendedCapabilities/inspire_common:MandatoryKeyword)"/>
      <xsl:for-each select="$keywords">
        <xsl:apply-templates mode="convert"
                             select="."/>
      </xsl:for-each>
      <xsl:apply-templates mode="copy" select="gmd:descriptiveKeywords"/>


      <xsl:apply-templates mode="copy" select="gmd:resourceSpecificUsage"/>

      <!-- Insert constraints. -->
      <xsl:variable name="constraints"
                    select="$getCapabilities/(*//wms:AccessConstraints)"/>
      <xsl:for-each select="$constraints">
        <xsl:apply-templates mode="convert"
                             select="."/>
      </xsl:for-each>

      <xsl:apply-templates mode="copy" select="gmd:resourceConstraints"/>
      <xsl:apply-templates mode="copy" select="gmd:aggregationInfo"/>


      <!-- For layers, add spatial representation type for WFS and WCS. -->
      <xsl:if test="$isBuildingDatasetRecord">
        <xsl:choose>
          <xsl:when test="//wfs:FeatureType">
            <gmd:spatialRepresentationType>
              <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
              <gmd:MD_SpatialRepresentationTypeCode
                codeList="./resources/codeList.xml#MD_SpatialRepresentationTypeCode"
                codeListValue="vector"/>
            </gmd:spatialRepresentationType>
          </xsl:when>
          <xsl:when test="//wcs:CoverageOfferingBrief">
            <gmd:spatialRepresentationType>
              <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
              <gmd:MD_SpatialRepresentationTypeCode
                codeList="./resources/codeList.xml#MD_SpatialRepresentationTypeCode"
                codeListValue="grid"/>
            </gmd:spatialRepresentationType>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates mode="copy" select="gmd:spatialRepresentationType"/>
          </xsl:otherwise>
        </xsl:choose>


        <xsl:variable name="minScale"
                      select="$getCapabilities//(
                                        Layer[Name=$Name]/MinScaleDenominator|
                                        wms:Layer[wms:Name=$Name]/wms:MinScaleDenominator)"/>
        <xsl:variable name="minScaleHint"
                      select="$getCapabilities//Layer[Name=$Name]/ScaleHint/@min"/>
        <xsl:if test="$minScale or $minScaleHint">
          <gmd:spatialResolution>
            <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
            <gmd:MD_Resolution>
              <gmd:equivalentScale>
                <gmd:MD_RepresentativeFraction>
                  <gmd:denominator>
                    <gco:Integer>
                      <xsl:value-of
                        select="if ($minScale) then $minScale else format-number(round($minScaleHint div math:sqrt(2) * 72 div 2.54 * 100), '0')"/>
                    </gco:Integer>
                  </gmd:denominator>
                </gmd:MD_RepresentativeFraction>
              </gmd:equivalentScale>
            </gmd:MD_Resolution>
          </gmd:spatialResolution>
        </xsl:if>
        <xsl:variable name="maxScale"
                      select="$getCapabilities//(
                                Layer[Name=$Name]/MaxScaleDenominator|
                                wms:Layer[wms:Name=$Name]/wms:MaxScaleDenominator)"/>
        <xsl:variable name="maxScaleHint"
                      select="$getCapabilities//Layer[Name=$Name]/ScaleHint/@max"/>
        <xsl:if test="$maxScale or $maxScaleHint">
          <gmd:spatialResolution>
            <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
            <gmd:MD_Resolution>
              <gmd:equivalentScale>
                <gmd:MD_RepresentativeFraction>
                  <gmd:denominator>
                    <gco:Integer>
                      <xsl:value-of select="if ($maxScale)
                                      then $maxScale
                                      else if ($maxScaleHint = 'Infinity')
                                        then $maxScaleHint
                                        else  format-number(round($maxScaleHint div math:sqrt(2) * 72 div 2.54 * 100), '0')"/>
                    </gco:Integer>
                  </gmd:denominator>
                </gmd:MD_RepresentativeFraction>
              </gmd:equivalentScale>
            </gmd:MD_Resolution>
          </gmd:spatialResolution>
        </xsl:if>
      </xsl:if>

      <xsl:apply-templates mode="copy" select="gmd:spatialResolution"/>
      <xsl:apply-templates mode="copy" select="gmd:language"/>
      <xsl:apply-templates mode="copy" select="gmd:characterSet"/>
      <xsl:apply-templates mode="copy" select="gmd:topicCategory"/>
      <xsl:apply-templates mode="copy" select="gmd:environmentDescription"/>

      <xsl:if test="$isBuildingDatasetRecord">
        <xsl:call-template name="build-extent">
          <xsl:with-param name="type" select="'gmd:extent'"/>
        </xsl:call-template>
      </xsl:if>

      <xsl:apply-templates mode="copy" select="gmd:extent"/>
      <xsl:apply-templates mode="copy" select="gmd:supplementalInformation"/>


      <xsl:if test="not($isBuildingDatasetRecord)">
        <xsl:apply-templates mode="copy" select="srv:serviceType"/>
        <xsl:apply-templates mode="copy" select="srv:serviceTypeVersion"/>
        <xsl:apply-templates mode="copy" select="srv:accessProperties"/>
        <xsl:apply-templates mode="copy" select="srv:restrictions"/>
        <xsl:apply-templates mode="copy" select="srv:keywords"/>


        <xsl:if test="$rootNameWithNs != 'csw:Capabilities'">
          <!-- This can not be find in CSW capabilities-->
          <xsl:call-template name="build-extent"/>
        </xsl:if>
        <xsl:apply-templates mode="copy" select="srv:extent"/>

        <xsl:apply-templates mode="copy" select="srv:coupledResource"/>
        <srv:couplingType>
          <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
          <srv:SV_CouplingType codeList="./resources/codeList.xml#SV_CouplingType">
            <xsl:attribute name="codeListValue">
              <xsl:choose>
                <xsl:when test="name(.)='wps:Capabilities' or
                                name(.)='wps1:Capabilities' or
                                name(.)='wps2:Capabilities'">loosely</xsl:when>
                <xsl:otherwise>tight</xsl:otherwise>
              </xsl:choose>
            </xsl:attribute>
          </srv:SV_CouplingType>
        </srv:couplingType>

        <xsl:call-template name="build-containsOperations"/>
        <xsl:apply-templates mode="copy" select="srv:containsOperations"/>

        <xsl:apply-templates mode="copy" select="srv:operatesOn"/>
      </xsl:if>

      <xsl:apply-templates mode="copy" select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd' and
                                     namespace-uri()!='http://www.isotc211.org/2005/srv']"/>
    </xsl:copy>
  </xsl:template>



  <!-- TODO this assume that the element exists in the input record and the element is not mandatory. -->
  <xsl:template mode="copy"
                match="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:accessProperties/gmd:MD_StandardOrderProcess/gmd:fees"
                priority="1999">
    <xsl:copy>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gco:CharacterString>
        <xsl:value-of select="$getCapabilities//*:Fees"/>
      </gco:CharacterString>
      <xsl:copy-of select="gmd:PT_FreeText"/>
    </xsl:copy>
  </xsl:template>





  <!-- Insert GetCapabilities URL.
   A transfertOptions (even empty) section MUST be set. -->
  <xsl:template mode="copy"
                match="gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions">
    <xsl:copy>
      <xsl:apply-templates mode="convert"
                           select="$getCapabilities/(.//wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource|
                         .//wfs:GetCapabilities/wfs:DCPType/wfs:HTTP/wfs:Get|
                         .//ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get|
                         .//ows11:Operation[@name='GetCapabilities']/ows11:DCP/ows11:HTTP/ows11:Get|
                         .//GetCapabilities/DCPType/HTTP/Get/OnlineResource[1]|
                         .//wcs:GetCapabilities//wcs:OnlineResource[1])"/>
      <xsl:apply-templates mode="copy"
                           select="*|@*"/>
    </xsl:copy>
  </xsl:template>



  <!-- Insert INSPIRE conformity section.
   scope is mandatory so the DQ report will be inserted after. -->
  <xsl:template mode="copy"
                match="gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope">
    <xsl:copy-of select="."/>
    <xsl:apply-templates mode="convert"
                         select="$getCapabilities//inspire_vs:ExtendedCapabilities/inspire_common:Conformity[
                            inspire_common:Degree='conformant' or
                            inspire_common:Degree='notConformant']"/>
  </xsl:template>



  <!-- Insert INSPIRE last revision date. -->
  <xsl:template mode="copy"
                match="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:date">
    <xsl:copy-of select="."/>
    <xsl:variable name="lastRevision"
                  select="$getCapabilities//inspire_vs:ExtendedCapabilities/inspire_common:TemporalReference/inspire_common:DateOfLastRevision"/>
    <xsl:for-each select="$lastRevision">
      <xsl:apply-templates mode="convert"
                           select="."/>
    </xsl:for-each>
  </xsl:template>




  <xsl:template mode="convert"
                match="Service/ContactInformation|
                       wfs:Service/wfs:ContactInformation|
                       wms:Service/wms:ContactInformation|
                       wcs:responsibleParty|
                       wms:responsibleParty|
                       ows:ServiceProvider|
                       owsg:ServiceProvider|
                       ows11:ServiceProvider">
    <gmd:CI_ResponsibleParty>
      <xsl:apply-templates select="."
                           mode="RespParty"/>
    </gmd:CI_ResponsibleParty>
  </xsl:template>


  <xsl:template mode="convert"
                match="ows:Keywords|
                       ows11:Keywords|
                       wfs:keywords|
                       KeywordList|
                       wcs:keywords">
    <gmd:descriptiveKeywords>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gmd:MD_Keywords>
        <xsl:for-each select="*">
          <gmd:keyword>
            <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
          </gmd:keyword>
        </xsl:for-each>
      </gmd:MD_Keywords>
    </gmd:descriptiveKeywords>
  </xsl:template>



  <xsl:template mode="convert"
                match="wms:KeywordList">
    <!-- Add keyword part of a vocabulary -->
    <xsl:for-each-group select="wms:Keyword[@vocabulary]" group-by="@vocabulary">
      <gmd:descriptiveKeywords>
        <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
        <gmd:MD_Keywords>
          <xsl:for-each select="../wms:Keyword[@vocabulary = current-grouping-key()]">
            <gmd:keyword>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </gmd:keyword>
          </xsl:for-each>
          <gmd:type>
            <gmd:MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode"
                                codeListValue="theme"/>
          </gmd:type>
          <xsl:if test="current-grouping-key() != ''">
            <gmd:thesaurusName>
              <gmd:CI_Citation>
                <gmd:title>
                  <gco:CharacterString>
                    <xsl:value-of select="current-grouping-key()"/>
                  </gco:CharacterString>
                </gmd:title>
                <gmd:date gco:nilReason="missing"/>
              </gmd:CI_Citation>
            </gmd:thesaurusName>
          </xsl:if>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
    </xsl:for-each-group>


    <!-- Add other WMS keywords -->
    <xsl:if test="wms:Keyword[not(@vocabulary)]">
      <gmd:descriptiveKeywords>
        <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
        <gmd:MD_Keywords>
          <xsl:for-each select="wms:Keyword[not(@vocabulary)]">
            <gmd:keyword>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </gmd:keyword>
          </xsl:for-each>
          <gmd:type>
            <gmd:MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode"
                                codeListValue="theme"/>
          </gmd:type>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
    </xsl:if>
  </xsl:template>


  <xsl:template mode="convert"
                match="inspire_common:MandatoryKeyword[@xsi:type='inspire_common:classificationOfSpatialDataService']">
    <gmd:descriptiveKeywords>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gmd:MD_Keywords>
        <xsl:for-each select="inspire_common:KeywordValue">
          <gmd:keyword>
            <gco:CharacterString>
              <xsl:value-of select="."/>
            </gco:CharacterString>
          </gmd:keyword>
        </xsl:for-each>
        <gmd:type>
          <gmd:MD_KeywordTypeCode
            codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_KeywordTypeCode"
            codeListValue="theme"/>
        </gmd:type>
        <gmd:thesaurusName>
          <gmd:CI_Citation>
            <gmd:title>
              <gco:CharacterString>INSPIRE Service taxonomy</gco:CharacterString>
            </gmd:title>
            <gmd:date>
              <gmd:CI_Date>
                <gmd:date>
                  <gco:Date>2010-04-22</gco:Date>
                </gmd:date>
                <gmd:dateType>
                  <gmd:CI_DateTypeCode
                    codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                    codeListValue="publication"/>
                </gmd:dateType>
              </gmd:CI_Date>
            </gmd:date>
          </gmd:CI_Citation>
        </gmd:thesaurusName>
      </gmd:MD_Keywords>
    </gmd:descriptiveKeywords>
  </xsl:template>



  <xsl:template mode="convert"
                match="wms:AccessConstraints">
    <gmd:resourceConstraints>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gmd:MD_LegalConstraints>
        <xsl:choose>
          <xsl:when test=". = 'copyright'
              or . = 'patent'
              or . = 'patentPending'
              or . = 'trademark'
              or . = 'license'
              or . = 'intellectualPropertyRight'
              or . = 'restricted'
              ">
            <gmd:accessConstraints>
              <gmd:MD_RestrictionCode
                codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"
                codeListValue="{.}"/>
            </gmd:accessConstraints>
          </xsl:when>
          <xsl:when test="lower-case(.) = 'none'">
            <gmd:accessConstraints>
              <gmd:MD_RestrictionCode
                codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"
                codeListValue="otherRestrictions"/>
            </gmd:accessConstraints>
            <gmd:otherConstraints>
              <gco:CharacterString>no conditions apply</gco:CharacterString>
            </gmd:otherConstraints>
          </xsl:when>
          <xsl:otherwise>
            <gmd:accessConstraints>
              <gmd:MD_RestrictionCode
                codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"
                codeListValue="otherRestrictions"/>
            </gmd:accessConstraints>
            <gmd:otherConstraints>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </gmd:otherConstraints>
          </xsl:otherwise>
        </xsl:choose>
      </gmd:MD_LegalConstraints>
    </gmd:resourceConstraints>

    <xsl:if test="lower-case(.) = 'none'">
      <gmd:resourceConstraints>
        <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
        <gmd:MD_Constraints>
          <gmd:useLimitation>
            <gco:CharacterString>no conditions apply</gco:CharacterString>
          </gmd:useLimitation>
        </gmd:MD_Constraints>
      </gmd:resourceConstraints>
    </xsl:if>

  </xsl:template>



  <xsl:template mode="convert"
                match="wms:OnlineResource|
                       OnlineResource|
                       wfs:Get|
                       ows:Get">
    <gmd:onLine>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gmd:CI_OnlineResource>
        <gmd:linkage>
          <gmd:URL><xsl:value-of select="@xlink:href|@onlineResource"/></gmd:URL>
        </gmd:linkage>

        <gmd:protocol>
          <gco:CharacterString>
            <xsl:choose>
              <xsl:when test="$rootName = ('WMT_MS_Capabilities', 'WMS_Capabilities')">OGC:WMS</xsl:when>
              <xsl:when test="$rootName = ('WFS_MS_Capabilities', 'WFS_Capabilities')">OGC:WFS</xsl:when>
              <xsl:when test="$rootName = ('WCS_Capabilities')">OGC:WCS</xsl:when>
              <xsl:when test="$rootName = ('Capabilities')">OGC:WPS</xsl:when>
              <xsl:otherwise>WWW:LINK-1.0-http--link</xsl:otherwise>
            </xsl:choose>
          </gco:CharacterString>
        </gmd:protocol>

        <xsl:if test="$isBuildingDatasetRecord">
          <gmd:name>
            <gco:CharacterString>
              <xsl:value-of select="$Name"/>
            </gco:CharacterString>
          </gmd:name>
        </xsl:if>

        <gmd:description>
          <gco:CharacterString>
            <xsl:choose>
              <xsl:when test="$isBuildingDatasetRecord">
                <xsl:value-of select="$layerTitle"/>
              </xsl:when>
              <xsl:otherwise>
                GetCapabilities URL
              </xsl:otherwise>
            </xsl:choose>
          </gco:CharacterString>
        </gmd:description>
      </gmd:CI_OnlineResource>
    </gmd:onLine>
  </xsl:template>


  <!--
  <inspire_common:Conformity>
      <inspire_common:Specification>
          <inspire_common:Title>-</inspire_common:Title>
          <inspire_common:DateOfLastRevision>2013-01-01</inspire_common:DateOfLastRevision>
      </inspire_common:Specification>
      <inspire_common:Degree>notEvaluated</inspire_common:Degree>
  </inspire_common:Conformity>
  -->
  <xsl:template mode="convert"
                match="inspire_common:Conformity">
    <gmd:report>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gmd:DQ_DomainConsistency>
        <gmd:result>
          <gmd:DQ_ConformanceResult>
            <gmd:specification>
              <gmd:CI_Citation>
                <gmd:title>
                  <gco:CharacterString>
                    <xsl:value-of
                      select="inspire_common:Specification/inspire_common:Title"/>
                  </gco:CharacterString>
                </gmd:title>
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:Date>
                        <xsl:value-of
                          select="inspire_common:Specification/inspire_common:DateOfLastRevision"/>
                      </gco:Date>
                    </gmd:date>
                    <gmd:dateType>
                      <gmd:CI_DateTypeCode
                        codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                        codeListValue="revision"/>
                    </gmd:dateType>
                  </gmd:CI_Date>
                </gmd:date>
              </gmd:CI_Citation>
            </gmd:specification>
            <!-- gmd:explanation is mandated by ISO 19115. A default value is proposed -->
            <gmd:explanation>
              <gco:CharacterString>See the referenced specification</gco:CharacterString>
            </gmd:explanation>
            <!-- the value is false instead of true if not conformant -->
            <xsl:choose>
              <xsl:when test="inspire_common:Degree='conformant'">
                <gmd:pass>
                  <gco:Boolean>true</gco:Boolean>
                </gmd:pass>
              </xsl:when>
              <xsl:when test="inspire_common:Degree='notConformant'">
                <gmd:pass>
                  <gco:Boolean>false</gco:Boolean>
                </gmd:pass>
              </xsl:when>
              <xsl:otherwise>
                <!-- Not evaluated -->
                <gmd:pass gco:nilReason="unknown">
                  <gco:Boolean/>
                </gmd:pass>
              </xsl:otherwise>
            </xsl:choose>

          </gmd:DQ_ConformanceResult>
        </gmd:result>
      </gmd:DQ_DomainConsistency>
    </gmd:report>
  </xsl:template>


  <xsl:template mode="convert"
                match="inspire_common:DateOfLastRevision">
    <gmd:date>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gmd:CI_Date>
        <gmd:date>
          <gco:Date>
            <xsl:value-of
              select="."/>
          </gco:Date>
        </gmd:date>
        <gmd:dateType>
          <gmd:CI_DateTypeCode codeList="./resources/codeList.xml#CI_DateTypeCode"
                               codeListValue="revision"/>
        </gmd:dateType>
      </gmd:CI_Date>
    </gmd:date>
  </xsl:template>




  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
          Extent in OGC spec are somehow differents !

          WCS 1.0.0
          <lonLatEnvelope srsName="WGS84(DD)">
                  <gml:pos>-130.85168 20.7052</gml:pos>
                  <gml:pos>-62.0054 54.1141</gml:pos>
          </lonLatEnvelope>

          WFS 1.1.0
          <ows:WGS84BoundingBox>
                  <ows:LowerCorner>-124.731422 24.955967</ows:LowerCorner>
                  <ows:UpperCorner>-66.969849 49.371735</ows:UpperCorner>
          </ows:WGS84BoundingBox>

          WMS 1.1.1
          <LatLonBoundingBox minx="-74.047185" miny="40.679648" maxx="-73.907005" maxy="40.882078"/>

          WMS 1.3.0
          <EX_GeographicBoundingBox>
              <westBoundLongitude>-178.9988054730254</westBoundLongitude>
              <eastBoundLongitude>179.0724773329789</eastBoundLongitude>
              <southBoundLatitude>-0.5014529001680404</southBoundLatitude>
              <northBoundLatitude>88.9987992292308</northBoundLatitude>
          </EX_GeographicBoundingBox>
          <BoundingBox CRS="EPSG:4326" minx="27.116136375774644" miny="-17.934116876940887" maxx="44.39484823803499" maxy="6.052081516030762"/>

          WPS 0.4.0 : none

          WPS 1.0.0 : none
           -->
  <xsl:template name="build-extent">
    <xsl:param name="type" select="'srv:extent'"/>

    <xsl:element name="{$type}">
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gmd:EX_Extent>
        <gmd:geographicElement>
          <gmd:EX_GeographicBoundingBox>

            <xsl:choose>
              <xsl:when test="$Name != ''">
                <xsl:choose>
                  <xsl:when test="$ows='true' or $rootName='WCS_Capabilities'">
                    <xsl:variable name="boxes">
                      <xsl:choose>
                        <xsl:when test="$ows='true'">
                          <xsl:for-each
                            select="//wfs:FeatureType[wfs:Name=$Name]/ows:WGS84BoundingBox/ows:LowerCorner">
                            <xmin>
                              <xsl:value-of select="substring-before(., ' ')"/>
                            </xmin>
                            <ymin>
                              <xsl:value-of select="substring-after(., ' ')"/>
                            </ymin>
                          </xsl:for-each>
                          <xsl:for-each
                            select="//wfs:FeatureType[wfs:Name=$Name]/ows:WGS84BoundingBox/ows:UpperCorner">
                            <xmax>
                              <xsl:value-of select="substring-before(., ' ')"/>
                            </xmax>
                            <ymax>
                              <xsl:value-of select="substring-after(., ' ')"/>
                            </ymax>
                          </xsl:for-each>
                        </xsl:when>
                        <xsl:when test="name(.)='WCS_Capabilities'">
                          <xsl:for-each
                            select="//wcs:CoverageOfferingBrief[wcs:name=$Name]/wcs:lonLatEnvelope/gml320:pos[1]">
                            <xmin>
                              <xsl:value-of select="substring-before(., ' ')"/>
                            </xmin>
                            <ymin>
                              <xsl:value-of select="substring-after(., ' ')"/>
                            </ymin>
                          </xsl:for-each>
                          <xsl:for-each
                            select="//wcs:CoverageOfferingBrief[wcs:name=$Name]/wcs:lonLatEnvelope/gml320:pos[2]">
                            <xmax>
                              <xsl:value-of select="substring-before(., ' ')"/>
                            </xmax>
                            <ymax>
                              <xsl:value-of select="substring-after(., ' ')"/>
                            </ymax>
                          </xsl:for-each>
                        </xsl:when>
                      </xsl:choose>
                    </xsl:variable>

                    <gmd:westBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="$boxes/*[name(.)='xmin']"/>
                      </gco:Decimal>
                    </gmd:westBoundLongitude>
                    <gmd:eastBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="$boxes/*[name(.)='xmax']"/>
                      </gco:Decimal>
                    </gmd:eastBoundLongitude>
                    <gmd:southBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="$boxes/*[name(.)='ymin']"/>
                      </gco:Decimal>
                    </gmd:southBoundLatitude>
                    <gmd:northBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="$boxes/*[name(.)='ymax']"/>
                      </gco:Decimal>
                    </gmd:northBoundLatitude>
                  </xsl:when>
                  <xsl:otherwise>
                    <gmd:westBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="//Layer[Name=$Name]/LatLonBoundingBox/@minx|
                      //wms:Layer[wms:Name=$Name]/wms:EX_GeographicBoundingBox/wms:westBoundLongitude|
                      //wfs:FeatureType[wfs:Name=$Name]/wfs:LatLongBoundingBox/@minx"/>
                      </gco:Decimal>
                    </gmd:westBoundLongitude>
                    <gmd:eastBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="//Layer[Name=$Name]/LatLonBoundingBox/@maxx|
                      //wms:Layer[wms:Name=$Name]/wms:EX_GeographicBoundingBox/wms:eastBoundLongitude|
                      //wfs:FeatureType[wfs:Name=$Name]/wfs:LatLongBoundingBox/@maxx"/>
                      </gco:Decimal>
                    </gmd:eastBoundLongitude>
                    <gmd:southBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="//Layer[Name=$Name]/LatLonBoundingBox/@miny|
                      //wms:Layer[wms:Name=$Name]/wms:EX_GeographicBoundingBox/wms:southBoundLatitude|
                      //wfs:FeatureType[wfs:Name=$Name]/wfs:LatLongBoundingBox/@miny"/>
                      </gco:Decimal>
                    </gmd:southBoundLatitude>
                    <gmd:northBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="//Layer[Name=$Name]/LatLonBoundingBox/@maxy|
                        //wms:Layer[wms:Name=$Name]/wms:EX_GeographicBoundingBox/wms:northBoundLatitude|
                        //wfs:FeatureType[wfs:Name=$Name]/wfs:LatLongBoundingBox/@maxy"/>
                      </gco:Decimal>
                    </gmd:northBoundLatitude>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:when test="$ows='true' or $rootName='WCS_Capabilities'">

                <xsl:variable name="boxes">
                  <xsl:choose>
                    <xsl:when test="$ows='true'">
                      <xsl:for-each select="$getCapabilities//ows:WGS84BoundingBox/ows:LowerCorner">
                        <xmin>
                          <xsl:value-of select="substring-before(., ' ')"/>
                        </xmin>
                        <ymin>
                          <xsl:value-of select="substring-after(., ' ')"/>
                        </ymin>
                      </xsl:for-each>
                      <xsl:for-each select="$getCapabilities//ows:WGS84BoundingBox/ows:UpperCorner">
                        <xmax>
                          <xsl:value-of select="substring-before(., ' ')"/>
                        </xmax>
                        <ymax>
                          <xsl:value-of select="substring-after(., ' ')"/>
                        </ymax>
                      </xsl:for-each>
                    </xsl:when>
                    <xsl:when test="$rootName='WCS_Capabilities'">
                      <xsl:for-each select="$getCapabilities//wcs:lonLatEnvelope/gml320:pos[1]">
                        <xmin>
                          <xsl:value-of select="substring-before(., ' ')"/>
                        </xmin>
                        <ymin>
                          <xsl:value-of select="substring-after(., ' ')"/>
                        </ymin>
                      </xsl:for-each>
                      <xsl:for-each select="$getCapabilities//wcs:lonLatEnvelope/gml320:pos[2]">
                        <xmax>
                          <xsl:value-of select="substring-before(., ' ')"/>
                        </xmax>
                        <ymax>
                          <xsl:value-of select="substring-after(., ' ')"/>
                        </ymax>
                      </xsl:for-each>
                    </xsl:when>
                  </xsl:choose>
                </xsl:variable>


                <gmd:westBoundLongitude>
                  <gco:Decimal>
                    <xsl:value-of select="math:min($boxes/*[name(.)='xmin'])"/>
                  </gco:Decimal>
                </gmd:westBoundLongitude>
                <gmd:eastBoundLongitude>
                  <gco:Decimal>
                    <xsl:value-of select="math:max($boxes/*[name(.)='xmax'])"/>
                  </gco:Decimal>
                </gmd:eastBoundLongitude>
                <gmd:southBoundLatitude>
                  <gco:Decimal>
                    <xsl:value-of select="math:min($boxes/*[name(.)='ymin'])"/>
                  </gco:Decimal>
                </gmd:southBoundLatitude>
                <gmd:northBoundLatitude>
                  <gco:Decimal>
                    <xsl:value-of select="math:max($boxes/*[name(.)='ymax'])"/>
                  </gco:Decimal>
                </gmd:northBoundLatitude>

              </xsl:when>
              <xsl:otherwise>

                <gmd:westBoundLongitude>
                  <gco:Decimal>
                    <xsl:value-of
                      select="math:min($getCapabilities//(wms:westBoundLongitude|//LatLonBoundingBox/@minx|//wfs:LatLongBoundingBox/@minx))"/>
                  </gco:Decimal>
                </gmd:westBoundLongitude>
                <gmd:eastBoundLongitude>
                  <gco:Decimal>
                    <xsl:value-of
                      select="math:max($getCapabilities//(wms:eastBoundLongitude|//LatLonBoundingBox/@maxx|//wfs:LatLongBoundingBox/@maxx))"/>
                  </gco:Decimal>
                </gmd:eastBoundLongitude>
                <gmd:southBoundLatitude>
                  <gco:Decimal>
                    <xsl:value-of
                      select="math:min($getCapabilities//(wms:southBoundLatitude|//LatLonBoundingBox/@miny|//wfs:LatLongBoundingBox/@miny))"/>
                  </gco:Decimal>
                </gmd:southBoundLatitude>
                <gmd:northBoundLatitude>
                  <gco:Decimal>
                    <xsl:value-of
                      select="math:max($getCapabilities//(wms:northBoundLatitude|//LatLonBoundingBox/@maxy|//wfs:LatLongBoundingBox/@maxy))"/>
                  </gco:Decimal>
                </gmd:northBoundLatitude>
              </xsl:otherwise>
            </xsl:choose>


          </gmd:EX_GeographicBoundingBox>
        </gmd:geographicElement>
      </gmd:EX_Extent>
    </xsl:element>
  </xsl:template>


  <xsl:template mode="convert"
                match="*|@*"/>



  <xsl:template mode="copy"
                match="gmd:referenceSystemInfo"
                priority="1999">
    <xsl:for-each
      select="$getCapabilities//wms:Layer[wms:Name=$Name]/wms:CRS[position() &lt; $maxCRS]|
              $getCapabilities//Layer[Name=$Name]/SRS[position() &lt; $maxCRS]">
      <gmd:referenceSystemInfo>
        <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
        <gmd:MD_ReferenceSystem>
          <gmd:referenceSystemIdentifier>
            <gmd:RS_Identifier>
              <gmd:code>
                <gco:CharacterString>
                  <xsl:value-of select="."/>
                </gco:CharacterString>
              </gmd:code>
            </gmd:RS_Identifier>
          </gmd:referenceSystemIdentifier>
        </gmd:MD_ReferenceSystem>
      </gmd:referenceSystemInfo>
    </xsl:for-each>
  </xsl:template>


  <xsl:template mode="copy"
                match="srv:serviceType">

    <srv:serviceType>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gco:LocalName codeSpace="www.w3c.org">
        <xsl:choose>
          <xsl:when test="$getCapabilities//*:ExtendedCapabilities/inspire_common:SpatialDataServiceType">
            <xsl:value-of select="$getCapabilities//*:ExtendedCapabilities/inspire_common:SpatialDataServiceType"/>
          </xsl:when>
          <xsl:when test="$rootName = ('WMT_MS_Capabilities', 'WMS_Capabilities')">OGC:WMS</xsl:when>
          <xsl:when test="$rootName = ('WFS_MS_Capabilities', 'WFS_Capabilities')">OGC:WFS</xsl:when>
          <xsl:when test="$rootName = ('WCS_Capabilities')">OGC:WCS</xsl:when>
          <xsl:when test="$rootName = ('Capabilities')">OGC:WPS</xsl:when>
          <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
        </xsl:choose>
      </gco:LocalName>
    </srv:serviceType>

    <srv:serviceTypeVersion>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gco:CharacterString>
        <xsl:value-of select='$getCapabilities/*/@version'/>
      </gco:CharacterString>
    </srv:serviceTypeVersion>
  </xsl:template>


  <xsl:template name="build-containsOperations">

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
             Operation could be OGC standard operation described in specification
             OR a specific process in a WPS. In that case, each process are described
             as one operation.
         -->

    <xsl:for-each select="$getCapabilities//(
                                Capability/Request/*|
                                wfs:Capability/wfs:Request/*|
                                wms:Capability/wms:Request/*|
                                wcs:Capability/wcs:Request/*|
                                ows:OperationsMetadata/ows:Operation|
                                ows11:OperationsMetadata/ows:Operation|
                                wps:ProcessOfferings/*|
                                wps1:ProcessOfferings/*)">
      <!-- Some services provide information about ows:ExtendedCapabilities TODO ? -->
      <srv:containsOperations>
        <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
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
          <!--  CHECKME : DCPType/SOAP ? -->
          <xsl:for-each select="DCPType/HTTP/*|wfs:DCPType/wfs:HTTP/*|wms:DCPType/wms:HTTP/*|
              wcs:DCPType/wcs:HTTP/*|ows:DCP/ows:HTTP/*|ows11:DCP/ows11:HTTP/*">
            <srv:DCP>
              <srv:DCPList codeList="./resources/codeList.xml#DCPList">
                <xsl:variable name="dcp">
                  <xsl:choose>
                    <xsl:when
                      test="name(.)='Get' or name(.)='wfs:Get' or name(.)='wms:Get' or name(.)='wcs:Get' or name(.)='ows:Get' or name(.)='ows11:Get'">
                      HTTP-GET
                    </xsl:when>
                    <xsl:when
                      test="name(.)='Post' or name(.)='wfs:Post' or name(.)='wms:Post' or name(.)='wcs:Post' or name(.)='ows:Post' or name(.)='ows11:Post'">
                      HTTP-POST
                    </xsl:when>
                    <xsl:otherwise>WebServices</xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                <xsl:attribute name="codeListValue">
                  <xsl:value-of select="$dcp"/>
                </xsl:attribute>
              </srv:DCPList>
            </srv:DCP>
          </xsl:for-each>

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
                        <xsl:value-of select="..//*[1]/OnlineResource/@xlink:href|
                          ..//*[1]/wms:OnlineResource/@xlink:href"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </gmd:URL>
                </gmd:linkage>
                <gmd:protocol>
                  <gco:CharacterString>
                    <xsl:choose>
                      <xsl:when test="$ows='true'">
                        <xsl:value-of select="ows:Value"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="."/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </gco:CharacterString>
                </gmd:protocol>
                <gmd:description>
                  <gco:CharacterString>
                    Format :
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:description>
                <gmd:function>
                  <gmd:CI_OnLineFunctionCode codeList="./resources/codeList.xml#CI_OnLineFunctionCode"
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
                  <gmd:CI_OnLineFunctionCode codeList="./resources/codeList.xml#CI_OnLineFunctionCode"
                                         codeListValue="information"/>
                </gmd:function>
              </gmd:CI_OnlineResource>
            </srv:connectPoint>
          </xsl:for-each>
        </srv:SV_OperationMetadata>
      </srv:containsOperations>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        Done by harvester after data metadata creation
        <xsl:for-each select="//Layer[count(./*[name(.)='Layer'])=0] | FeatureType[count(./*[name(.)='FeatureType'])=0] | CoverageOfferingBrief[count(./*[name(.)='CoverageOfferingBrief'])=0]">
                <srv:operatesOn>
                        <MD_DataIdentification uuidref="">
                        <xsl:value-of select="Name"/>
                        </MD_DataIdentification>
                </srv:operatesOn>
        </xsl:for-each>
        -->
  </xsl:template>


  <!-- Remove values added in the past -->
  <xsl:template mode="copy"
                match="*[@gco:nilReason = $nilReasonValue]"
                priority="999"/>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template mode="copy"
                match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"
                           mode="copy"/>
    </xsl:copy>
  </xsl:template>


  <!-- Remove geonet:* elements. -->
  <xsl:template mode="copy"
                match="geonet:*"
                priority="2"/>

</xsl:stylesheet>
