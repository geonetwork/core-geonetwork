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
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:gml32="http://www.opengis.net/gml/3.2"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:wcs="http://www.opengis.net/wcs"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:wms="http://www.opengis.net/wms"
                xmlns:wps="http://www.opengeospatial.net/wps"
                xmlns:wps1="http://www.opengis.net/wps/1.0.0"
                xmlns:wps2="http://www.opengis.net/wps/2.0"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:ows2="http://www.opengis.net/ows/2.0"
                xmlns:owsg="http://www.opengeospatial.net/ows"
                xmlns:ows11="http://www.opengis.net/ows/1.1"
                xmlns:inspire_vs="http://inspire.ec.europa.eu/schemas/inspire_vs/1.0"
                xmlns:inspire_common="http://inspire.ec.europa.eu/schemas/common/1.0"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:math="http://exslt.org/math"
                extension-element-prefixes="saxon math"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:import href="ogcwxs-resp-party.xsl"/>

  <xsl:output indent="yes"/>

  <!-- Metadata record UUID generated. -->
  <xsl:param name="uuid"
             select="''"/>

  <!-- Layer name to process -->
  <xsl:param name="Name"
             select="''"/>

  <!-- Service type is used for layer only. It allows to combine
  information from a WMS and a WFS in the same metadata record
  as far as the service URL is the same. For example this works
  for GeoServer which provides a /ows URL to access both WMS and WFS service.
  -->
  <xsl:param name="serviceType"
             select="'OGC'"/>

  <xsl:variable name="isBuildingDatasetRecord"
                select="$Name != ''"/>

  <xsl:variable name="nilReasonValue"
                select="concat('synchronizedFrom', $serviceType)"/>

  <!-- Max number of coordinate system to add
    to the metadata record. Avoid to have too many CRS when
    OGC server list all epsg database. -->
  <xsl:variable name="maxCRS">5</xsl:variable>

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
                       */ows2:ServiceIdentification/ows2:Title|
                       */ows11:ServiceIdentification/ows11:Title|
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
                match="mdb:metadataIdentifier/mcc:MD_Identifier/mcc:code/gco:CharacterString"
                priority="1999">
    <xsl:copy>
      <xsl:value-of select="$uuid"/>
    </xsl:copy>
  </xsl:template>


  <!-- INSPIRE extension elements -->
  <!-- Insert dateStamp or set it to now. -->
  <xsl:template mode="copy"
                match="mdb:MD_Metadata/mdb:dateInfo/cit:CI_Date[cit:dateType/*/@codeListValue = 'creation']/cit:date/gco:DateTime/text()"
                priority="1999">
    <xsl:variable name="date"
                  select="normalize-space(//inspire_vs:ExtendedCapabilities/inspire_common:MetadataDate/text())"/>

    <xsl:value-of select="if ($date != '')
                          then $date
                          else format-dateTime(current-dateTime(),$df)"/>
  </xsl:template>




  <!-- Insert title. -->
  <xsl:template mode="copy"
                match="mdb:MD_Metadata/mdb:identificationInfo/srv:SV_ServiceIdentification/mri:citation/*/cit:title"
                priority="1999">
    <xsl:copy>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gco:CharacterString>
        <xsl:value-of select="$serviceTitle"/>
      </gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="copy"
                match="mdb:MD_Metadata/mdb:identificationInfo/mri:MD_DataIdentification/mri:citation/*/cit:title"
                priority="1999">

    <xsl:copy>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gco:CharacterString>
        <xsl:value-of select="$layerTitle"/>
      </gco:CharacterString>
    </xsl:copy>
  </xsl:template>



  <!-- Insert abstract. -->
  <xsl:template mode="copy"
                match="mdb:MD_Metadata/mdb:identificationInfo/srv:SV_ServiceIdentification/mri:abstract"
                priority="1999">
    <xsl:copy>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gco:CharacterString>
        <xsl:value-of select="$getCapabilities/(*/ows:ServiceIdentification/ows:Abstract|
                         */ows11:ServiceIdentification/ows11:Abstract|
                         */ows2:ServiceIdentification/ows2:Abstract|
                         */wfs:Service/wfs:Abstract|
                         */wms:Service/wms:Abstract|
                         */Service/Abstract|
                         */csw:Capabilities/ows:ServiceIdentification/ows:Abstract|
                         */wcs:Service/wcs:description)/text()"/>
      </gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="copy"
                match="mdb:MD_Metadata/mdb:identificationInfo/*/mri:citation/*/cit:date[1]"
                priority="1999">
    <xsl:copy-of select=".[@gco:nilReason != $nilReasonValue]"/>

    <!-- Add dates from ns:AdditionalParameters if any-->
    <xsl:for-each select="$getCapabilities//ows2:AdditionalParameters/cit:date">
      <xsl:copy>
        <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
        <xsl:copy-of select="*"/>
      </xsl:copy>
    </xsl:for-each>

  </xsl:template>


  <xsl:template mode="copy"
                match="mdb:MD_Metadata/mdb:identificationInfo/mri:MD_DataIdentification/mri:abstract"
                priority="1999">

    <xsl:copy>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gco:CharacterString>
        <xsl:value-of select="$getCapabilities/(
                                  *//wms:Layer[wms:Name=$Name]/wms:Abstract|
                                  *//Layer[Name=$Name]/Abstract|
                                  *//wcs:CoverageOfferingBrief[wcs:name=$Name]/wcs:description|
                                  *//wfs:FeatureType[wfs:Name=$Name]/wfs:Abstract|
                                  *//wps2:Process[ows2:Identifier=$Name]/ows2:Abstract)/text()
                                  "/>

        <xsl:if test="$getCapabilities//wps2:Process[ows2:Identifier=$Name]">
          <xsl:text>


          </xsl:text>
          Process identifier <xsl:value-of select="$Name"/>.
          <xsl:text>


          </xsl:text>
        </xsl:if>

        <xsl:variable name="processes"
                      select="$getCapabilities//wps2:Process[ows2:Identifier=$Name]/(wps2:Input|wps2:Output)"/>
        <xsl:for-each select="$processes">
          * <xsl:value-of select="concat(local-name(.), ':', ows2:Title, ', ', ows2:Abstract, '(', string-join(wps2:LiteralData/wps2:Format/@mimeType, ', '), ')')"/><xsl:text>
</xsl:text>
        </xsl:for-each>
      </gco:CharacterString>
    </xsl:copy>
  </xsl:template>




  <xsl:template mode="copy"
                match="mdb:MD_Metadata/mdb:defaultLocale/*/lan:language"
                priority="1999">

    <xsl:variable name="language"
                  select="normalize-space(//inspire_vs:ExtendedCapabilities/inspire_common:ResponseLanguage/inspire_common:Language/text())"/>

    <xsl:copy>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <lan:LanguageCode>
        <xsl:copy-of select="@codeList"/>
        <xsl:attribute name="codeListValue"
                       select="if ($language != '') then $language else 'eng'"/>
      </lan:LanguageCode>
    </xsl:copy>
  </xsl:template>


  <!--
  Metadata contact (the one from the capabilities is added just after the metadataScope which is mandatory).
  -->
  <xsl:template mode="copy"
                match="mdb:MD_Metadata/mdb:metadataScope">
    <xsl:copy-of select="."/>


    <xsl:variable name="contacts"
                  select="$getCapabilities/(*/Service/ContactInformation|
                          */wfs:Service/wfs:ContactInformation|
                          */wms:Service/wms:ContactInformation|
                          */ows:ServiceProvider|
                          */ows2:ServiceProvider|
                          */owsg:ServiceProvider|
                          */ows11:ServiceProvider)"/>
    <xsl:for-each select="$contacts">
      <mdb:contact>
        <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
        <xsl:apply-templates mode="convert"
                             select="$contacts"/>
      </mdb:contact>
    </xsl:for-each>

  </xsl:template>





  <xsl:template mode="copy"
                match="mdb:identificationInfo/*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="copy" select="mri:citation"/>
      <xsl:apply-templates mode="copy" select="mri:abstract"/>
      <xsl:apply-templates mode="copy" select="mri:purpose"/>

      <!-- CSW Add queryables in purpose -->
      <xsl:if test="$rootNameWithNs = 'csw:Capabilities'">
        <mri:purpose>
          <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
          <gco:CharacterString>
            <xsl:for-each
              select="$getCapabilities//ows:Constraint[@name='SupportedISOQueryables' or @name='AdditionalQueryables']/ows:Value">
              <xsl:value-of select="."/>
              <xsl:if test="position()!=last()">,</xsl:if>
            </xsl:for-each>
          </gco:CharacterString>
        </mri:purpose>
      </xsl:if>

      <xsl:apply-templates mode="copy" select="mri:credit"/>
      <xsl:apply-templates mode="copy" select="mri:status"/>


      <!-- Insert contact. -->
      <xsl:variable name="contacts"
                    select="$getCapabilities//(ContactInformation|
                           wcs:responsibleParty|
                           wms:responsibleParty|
                           wms:Service/wms:ContactInformation|
                           ows:ServiceProvider|
                           ows11:ServiceProvider)"/>



      <xsl:for-each select="$contacts">
        <mri:pointOfContact>
          <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
          <xsl:apply-templates mode="convert"
                               select="."/>
        </mri:pointOfContact>
      </xsl:for-each>
      <xsl:apply-templates mode="copy" select="mri:pointOfContact[@gco:nilReason != $nilReasonValue]"/>

      <!-- Add contact from ns:AdditionalParameters if any-->
      <xsl:for-each select="$getCapabilities//ows2:AdditionalParameters/mri:pointOfContact">
        <xsl:copy>
          <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
          <xsl:copy-of select="*"/>
        </xsl:copy>
      </xsl:for-each>

      <!-- For layers, add spatial representation type for WFS and WCS. -->
      <xsl:if test="$isBuildingDatasetRecord">
        <xsl:choose>
          <xsl:when test="//wfs:FeatureType">
            <mri:spatialRepresentationType>
              <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
              <mcc:MD_SpatialRepresentationTypeCode
                codeList="./resources/codeList.xml#MD_SpatialRepresentationTypeCode"
                codeListValue="vector"/>
            </mri:spatialRepresentationType>
          </xsl:when>
          <xsl:when test="//wcs:CoverageOfferingBrief">
            <mri:spatialRepresentationType>
              <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
              <mccMD_SpatialRepresentationTypeCode
                codeList="./resources/codeList.xml#MD_SpatialRepresentationTypeCode"
                codeListValue="grid"/>
            </mri:spatialRepresentationType>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates mode="copy" select="mri:spatialRepresentationType"/>
          </xsl:otherwise>
        </xsl:choose>


        <xsl:variable name="minScale"
                      select="$getCapabilities//(
                                        Layer[Name=$Name]/MinScaleDenominator|
                                        wms:Layer[wms:Name=$Name]/wms:MinScaleDenominator)"/>
        <xsl:variable name="minScaleHint"
                      select="$getCapabilities//Layer[Name=$Name]/ScaleHint/@min"/>
        <xsl:if test="$minScale or $minScaleHint">
          <mri:spatialResolution>
            <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
            <mri:MD_Resolution>
              <mri:equivalentScale>
                <mri:MD_RepresentativeFraction>
                  <mri:denominator>
                    <gco:Integer>
                      <xsl:value-of
                        select="if ($minScale) then $minScale else format-number(round($minScaleHint div math:sqrt(2) * 72 div 2.54 * 100), '0')"/>
                    </gco:Integer>
                  </mri:denominator>
                </mri:MD_RepresentativeFraction>
              </mri:equivalentScale>
            </mri:MD_Resolution>
          </mri:spatialResolution>
        </xsl:if>
        <xsl:variable name="maxScale"
                      select="$getCapabilities//(
                                Layer[Name=$Name]/MaxScaleDenominator|
                                wms:Layer[wms:Name=$Name]/wms:MaxScaleDenominator)"/>
        <xsl:variable name="maxScaleHint"
                      select="$getCapabilities//Layer[Name=$Name]/ScaleHint/@max"/>
        <xsl:if test="$maxScale or $maxScaleHint">
          <mri:spatialResolution>
            <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
            <mri:MD_Resolution>
              <mri:equivalentScale>
                <mri:MD_RepresentativeFraction>
                  <mri:denominator>
                    <gco:Integer>
                      <xsl:value-of select="if ($maxScale)
                                      then $maxScale
                                      else if ($maxScaleHint = 'Infinity')
                                        then $maxScaleHint
                                        else  format-number(round($maxScaleHint div math:sqrt(2) * 72 div 2.54 * 100), '0')"/>
                    </gco:Integer>
                  </mri:denominator>
                </mri:MD_RepresentativeFraction>
              </mri:equivalentScale>
            </mri:MD_Resolution>
          </mri:spatialResolution>
        </xsl:if>
      </xsl:if>

      <xsl:apply-templates mode="copy" select="mri:spatialResolution"/>
      <xsl:apply-templates mode="copy" select="mri:temporalResolution"/>
      <xsl:apply-templates mode="copy" select="mri:topicCategory"/>
      <xsl:apply-templates mode="copy" select="mri:extent"/>


      <xsl:if test="$rootNameWithNs != 'csw:Capabilities'">
        <!-- This can not be find in CSW capabilities-->
        <xsl:call-template name="build-extent"/>
      </xsl:if>

      <xsl:apply-templates mode="copy" select="mri:additionalDocumentation"/>
      <xsl:apply-templates mode="copy" select="mri:processingLevel"/>

      <xsl:apply-templates mode="copy" select="mri:resourceMaintenance"/>
      <xsl:if test="$serviceType = 'WMS'">
        <xsl:apply-templates mode="copy" select="mri:graphicOverview[not(contains(*/mcc:fileName/gco:CharacterString, concat('attachments/', $uuid, '.png')))]"/>
      </xsl:if>
      <xsl:apply-templates mode="copy" select="mri:resourceFormat"/>


      <!-- CSW Add output schema -->
      <xsl:if test="$rootNameWithNs = 'csw:Capabilities'">
        <xsl:for-each-group select="//ows:Parameter[@name='outputSchema']/ows:Value" group-by=".">
          <mri:resourceFormat>
            <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
            <mrd:MD_Format>
              <mrd:formatSpecificationCitation>
                <cit:CI_Citation>
                  <cit:title>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </cit:title>
                </cit:CI_Citation>
              </mrd:formatSpecificationCitation>
            </mrd:MD_Format>
          </mri:resourceFormat>
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
                               */ows2:ServiceIdentification/ows2:Keywords|
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
      <xsl:apply-templates mode="copy" select="mri:descriptiveKeywords"/>


      <xsl:apply-templates mode="copy" select="mri:resourceSpecificUsage"/>

      <!-- Insert constraints. -->
      <xsl:variable name="constraints"
                    select="$getCapabilities/(*//wms:AccessConstraints)"/>
      <xsl:for-each select="$constraints">
        <xsl:apply-templates mode="convert"
                             select="."/>
      </xsl:for-each>

      <xsl:apply-templates mode="copy" select="mri:resourceConstraints"/>
      <xsl:apply-templates mode="copy" select="mri:associatedResource"/>
      <xsl:apply-templates mode="copy" select="mri:defaultLocale"/>
      <xsl:apply-templates mode="copy" select="mri:otherLocale"/>
      <xsl:apply-templates mode="copy" select="mri:environmentDescription"/>
      <xsl:apply-templates mode="copy" select="mri:supplementalInformation"/>


      <xsl:if test="not($isBuildingDatasetRecord)">
        <xsl:apply-templates mode="copy" select="srv:serviceType"/>
        <xsl:apply-templates mode="copy" select="srv:serviceTypeVersion"/>
        <xsl:apply-templates mode="copy" select="srv:accessProperties"/>
        <xsl:apply-templates mode="copy" select="srv:restrictions"/>
        <xsl:apply-templates mode="copy" select="srv:keywords"/>

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
    </xsl:copy>
  </xsl:template>



  <!-- TODO this assume that the element exists in the input record and the element is not mandatory. -->
  <xsl:template mode="copy"
                match="mdb:MD_Metadata/mdb:identificationInfo/srv:SV_ServiceIdentification/srv:accessProperties/mrd:MD_StandardOrderProcess/mrd:fees"
                priority="1999">
    <xsl:copy>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gco:CharacterString>
        <xsl:value-of select="$getCapabilities//*:Fees"/>
      </gco:CharacterString>
    </xsl:copy>
  </xsl:template>





  <!-- Insert GetCapabilities URL.
   A transfertOptions (even empty) section MUST be set. -->
  <xsl:template mode="copy"
                match="mdb:MD_Metadata/mdb:distributionInfo/mrd:MD_Distribution/mrd:transferOptions/mrd:MD_DigitalTransferOptions">
    <xsl:copy>
      <xsl:apply-templates mode="convert"
                           select="$getCapabilities//(wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource|
                         wfs:GetCapabilities/wfs:DCPType/wfs:HTTP/wfs:Get|
                         ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get|
                         ows11:Operation[@name='GetCapabilities']/ows11:DCP/ows11:HTTP/ows11:Get|
                         GetCapabilities/DCPType/HTTP/Get/OnlineResource[1]|
                         wcs:GetCapabilities//wcs:OnlineResource[1])"/>
      <xsl:apply-templates mode="copy"
                           select="*|@*"/>
    </xsl:copy>
  </xsl:template>



  <!-- Insert INSPIRE conformity section.
   scope is mandatory so the DQ report will be inserted after. -->
  <xsl:template mode="copy"
                match="mdb:MD_Metadata/mdb:dataQualityInfo/mdq:DQ_DataQuality/mdq:scope">
    <xsl:copy-of select="."/>
    <xsl:apply-templates mode="convert"
                         select="$getCapabilities//inspire_vs:ExtendedCapabilities/inspire_common:Conformity[
                            inspire_common:Degree='conformant' or
                            inspire_common:Degree='notConformant']"/>
  </xsl:template>



  <!-- Insert INSPIRE last revision date. -->
  <xsl:template mode="copy"
                match="mdb:MD_Metadata/mdb:identificationInfo/srv:SV_ServiceIdentification/mri:citation/*/cit:date">
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
                       ows2:ServiceProvider|
                       owsg:ServiceProvider|
                       ows11:ServiceProvider">
    <cit:CI_Responsibility>
      <xsl:apply-templates select="."
                           mode="RespParty"/>
    </cit:CI_Responsibility>
  </xsl:template>


  <xsl:template mode="convert"
                match="ows:Keywords|
                       ows2:Keywords|
                       ows11:Keywords|
                       wfs:keywords|
                       KeywordList|
                       wcs:keywords">
    <mri:descriptiveKeywords>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <mri:MD_Keywords>
        <xsl:for-each select="*">
          <mri:keyword>
            <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
          </mri:keyword>
        </xsl:for-each>
      </mri:MD_Keywords>
    </mri:descriptiveKeywords>
  </xsl:template>



  <xsl:template mode="convert"
                match="wms:KeywordList">
    <!-- Add keyword part of a vocabulary -->
    <xsl:for-each-group select="wms:Keyword[@vocabulary]" group-by="@vocabulary">
      <mri:descriptiveKeywords>
        <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
        <mri:MD_Keywords>
          <xsl:for-each select="../wms:Keyword[@vocabulary = current-grouping-key()]">
            <mri:keyword>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </mri:keyword>
          </xsl:for-each>
          <mri:type>
            <mri:MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode"
                                    codeListValue="theme"/>
          </mri:type>
          <xsl:if test="current-grouping-key() != ''">
            <mri:thesaurusName>
              <cit:CI_Citation>
                <cit:title>
                  <gco:CharacterString>
                    <xsl:value-of select="current-grouping-key()"/>
                  </gco:CharacterString>
                </cit:title>
                <cit:date gco:nilReason="missing"/>
              </cit:CI_Citation>
            </mri:thesaurusName>
          </xsl:if>
        </mri:MD_Keywords>
      </mri:descriptiveKeywords>
    </xsl:for-each-group>


    <!-- Add other WMS keywords -->
    <xsl:if test="wms:Keyword[not(@vocabulary)]">
      <mri:descriptiveKeywords>
        <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
        <mri:MD_Keywords>
          <xsl:for-each select="wms:Keyword[not(@vocabulary)]">
            <mri:keyword>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </mri:keyword>
          </xsl:for-each>
          <mri:type>
            <mri:MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode"
                                    codeListValue="theme"/>
          </mri:type>
        </mri:MD_Keywords>
      </mri:descriptiveKeywords>
    </xsl:if>
  </xsl:template>


  <xsl:template mode="convert"
                match="inspire_common:MandatoryKeyword[@xsi:type='inspire_common:classificationOfSpatialDataService']">
    <mri:descriptiveKeywords>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <mri:MD_Keywords>
        <xsl:for-each select="inspire_common:KeywordValue">
          <mri:keyword>
            <gco:CharacterString>
              <xsl:value-of select="."/>
            </gco:CharacterString>
          </mri:keyword>
        </xsl:for-each>
        <mri:type>
          <mri:MD_KeywordTypeCode
            codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_KeywordTypeCode"
            codeListValue="theme"/>
        </mri:type>
        <mri:thesaurusName>
          <cit:CI_Citation>
            <cit:title>
              <gco:CharacterString>INSPIRE Service taxonomy</gco:CharacterString>
            </cit:title>
            <cit:date>
              <cit:CI_Date>
                <cit:date>
                  <gco:Date>2010-04-22</gco:Date>
                </cit:date>
                <cit:dateType>
                  <cit:CI_DateTypeCode
                    codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                    codeListValue="publication"/>
                </cit:dateType>
              </cit:CI_Date>
            </cit:date>
          </cit:CI_Citation>
        </mri:thesaurusName>
      </mri:MD_Keywords>
    </mri:descriptiveKeywords>
  </xsl:template>



  <xsl:template mode="convert"
                match="wms:AccessConstraints">
    <mri:resourceConstraints>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <mco:MD_LegalConstraints>
        <xsl:choose>
          <xsl:when test=". = 'copyright'
              or . = 'patent'
              or . = 'patentPending'
              or . = 'trademark'
              or . = 'license'
              or . = 'intellectualPropertyRight'
              or . = 'restricted'
              ">
            <mco:accessConstraints>
              <mco:MD_RestrictionCode
                codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"
                codeListValue="{.}"/>
            </mco:accessConstraints>
          </xsl:when>
          <xsl:when test="lower-case(.) = 'none'">
            <mco:accessConstraints>
              <mco:MD_RestrictionCode
                codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"
                codeListValue="otherRestrictions"/>
            </mco:accessConstraints>
            <mco:otherConstraints>
              <gco:CharacterString>no conditions apply</gco:CharacterString>
            </mco:otherConstraints>
          </xsl:when>
          <xsl:otherwise>
            <mco:accessConstraints>
              <mco:MD_RestrictionCode
                codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"
                codeListValue="otherRestrictions"/>
            </mco:accessConstraints>
            <mco:otherConstraints>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </mco:otherConstraints>
          </xsl:otherwise>
        </xsl:choose>
      </mco:MD_LegalConstraints>
    </mri:resourceConstraints>

    <xsl:if test="lower-case(.) = 'none'">
      <mri:resourceConstraints>
        <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
        <mco:MD_Constraints>
          <mco:useLimitation>
            <gco:CharacterString>no conditions apply</gco:CharacterString>
          </mco:useLimitation>
        </mco:MD_Constraints>
      </mri:resourceConstraints>
    </xsl:if>

  </xsl:template>



  <xsl:template mode="convert"
                match="wms:OnlineResource|
                       OnlineResource|
                       wfs:Get|
                       ows:Get">
    <mrd:onLine>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <cit:CI_OnlineResource>
        <cit:linkage>
          <gco:CharacterString><xsl:value-of select="@xlink:href|@onlineResource"/></gco:CharacterString>
        </cit:linkage>
        <xsl:if test="$isBuildingDatasetRecord">
          <cit:name>
            <gco:CharacterString>
              <xsl:value-of select="$Name"/>
            </gco:CharacterString>
          </cit:name>
        </xsl:if>
        <cit:protocol>
          <gco:CharacterString>
            <xsl:choose>
              <xsl:when test="$rootName = ('WMT_MS_Capabilities', 'WMS_Capabilities')">OGC:WMS</xsl:when>
              <xsl:when test="$rootName = ('WFS_MS_Capabilities', 'WFS_Capabilities')">OGC:WFS</xsl:when>
              <xsl:when test="$rootName = ('WCS_Capabilities')">OGC:WCS</xsl:when>
              <xsl:when test="$rootName = ('Capabilities')">OGC:WPS</xsl:when>
              <xsl:otherwise>WWW:LINK-1.0-http--link</xsl:otherwise>
            </xsl:choose>
          </gco:CharacterString>
        </cit:protocol>

        <cit:description>
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
        </cit:description>
      </cit:CI_OnlineResource>
    </mrd:onLine>
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
    <mdq:report>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <mdq:DQ_DomainConsistency>
        <mdq:result>
          <mdq:DQ_ConformanceResult>
            <mdq:specification>
              <cit:CI_Citation>
                <cit:title>
                  <gco:CharacterString>
                    <xsl:value-of
                      select="inspire_common:Specification/inspire_common:Title"/>
                  </gco:CharacterString>
                </cit:title>
                <cit:date>
                  <cit:CI_Date>
                    <cit:date>
                      <gco:Date>
                        <xsl:value-of
                          select="inspire_common:Specification/inspire_common:DateOfLastRevision"/>
                      </gco:Date>
                    </cit:date>
                    <cit:dateType>
                      <cit:CI_DateTypeCode
                        codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                        codeListValue="revision"/>
                    </cit:dateType>
                  </cit:CI_Date>
                </cit:date>
              </cit:CI_Citation>
            </mdq:specification>
            <!-- mdq:explanation is mandated by ISO 19115. A default value is proposed -->
            <mdq:explanation>
              <gco:CharacterString>See the referenced specification</gco:CharacterString>
            </mdq:explanation>
            <!-- the value is false instead of true if not conformant -->
            <xsl:choose>
              <xsl:when test="inspire_common:Degree='conformant'">
                <mdq:pass>
                  <gco:Boolean>true</gco:Boolean>
                </mdq:pass>
              </xsl:when>
              <xsl:when test="inspire_common:Degree='notConformant'">
                <mdq:pass>
                  <gco:Boolean>false</gco:Boolean>
                </mdq:pass>
              </xsl:when>
              <xsl:otherwise>
                <!-- Not evaluated -->
                <mdq:pass gco:nilReason="unknown">
                  <gco:Boolean/>
                </mdq:pass>
              </xsl:otherwise>
            </xsl:choose>

          </mdq:DQ_ConformanceResult>
        </mdq:result>
      </mdq:DQ_DomainConsistency>
    </mdq:report>
  </xsl:template>


  <xsl:template mode="convert"
                match="inspire_common:DateOfLastRevision">
    <cit:date>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <cit:CI_Date>
        <cit:date>
          <gco:Date>
            <xsl:value-of
              select="."/>
          </gco:Date>
        </cit:date>
        <cit:dateType>
          <cit:CI_DateTypeCode codeList="./resources/codeList.xml#CI_DateTypeCode"
                               codeListValue="revision"/>
        </cit:dateType>
      </cit:CI_Date>
    </cit:date>
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

    <mri:extent>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gex:EX_Extent>
        <gex:geographicElement>
          <gex:EX_GeographicBoundingBox>

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
                            select="//wcs:CoverageOfferingBrief[wcs:name=$Name]/wcs:lonLatEnvelope/gml:pos[1]">
                            <xmin>
                              <xsl:value-of select="substring-before(., ' ')"/>
                            </xmin>
                            <ymin>
                              <xsl:value-of select="substring-after(., ' ')"/>
                            </ymin>
                          </xsl:for-each>
                          <xsl:for-each
                            select="//wcs:CoverageOfferingBrief[wcs:name=$Name]/wcs:lonLatEnvelope/gml:pos[2]">
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

                    <gex:westBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="$boxes/*[name(.)='xmin']"/>
                      </gco:Decimal>
                    </gex:westBoundLongitude>
                    <gex:eastBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="$boxes/*[name(.)='xmax']"/>
                      </gco:Decimal>
                    </gex:eastBoundLongitude>
                    <gex:southBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="$boxes/*[name(.)='ymin']"/>
                      </gco:Decimal>
                    </gex:southBoundLatitude>
                    <gex:northBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="$boxes/*[name(.)='ymax']"/>
                      </gco:Decimal>
                    </gex:northBoundLatitude>
                  </xsl:when>
                  <xsl:otherwise>
                    <gex:westBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="//Layer[Name=$Name]/LatLonBoundingBox/@minx|
                      //wms:Layer[wms:Name=$Name]/wms:EX_GeographicBoundingBox/wms:westBoundLongitude|
                      //wfs:FeatureType[wfs:Name=$Name]/wfs:LatLongBoundingBox/@minx"/>
                      </gco:Decimal>
                    </gex:westBoundLongitude>
                    <gex:eastBoundLongitude>
                      <gco:Decimal>
                        <xsl:value-of select="//Layer[Name=$Name]/LatLonBoundingBox/@maxx|
                      //wms:Layer[wms:Name=$Name]/wms:EX_GeographicBoundingBox/wms:eastBoundLongitude|
                      //wfs:FeatureType[wfs:Name=$Name]/wfs:LatLongBoundingBox/@maxx"/>
                      </gco:Decimal>
                    </gex:eastBoundLongitude>
                    <gex:southBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="//Layer[Name=$Name]/LatLonBoundingBox/@miny|
                      //wms:Layer[wms:Name=$Name]/wms:EX_GeographicBoundingBox/wms:southBoundLatitude|
                      //wfs:FeatureType[wfs:Name=$Name]/wfs:LatLongBoundingBox/@miny"/>
                      </gco:Decimal>
                    </gex:southBoundLatitude>
                    <gex:northBoundLatitude>
                      <gco:Decimal>
                        <xsl:value-of select="//Layer[Name=$Name]/LatLonBoundingBox/@maxy|
                        //wms:Layer[wms:Name=$Name]/wms:EX_GeographicBoundingBox/wms:northBoundLatitude|
                        //wfs:FeatureType[wfs:Name=$Name]/wfs:LatLongBoundingBox/@maxy"/>
                      </gco:Decimal>
                    </gex:northBoundLatitude>
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
                      <xsl:for-each select="$getCapabilities//wcs:lonLatEnvelope/gml:pos[1]">
                        <xmin>
                          <xsl:value-of select="substring-before(., ' ')"/>
                        </xmin>
                        <ymin>
                          <xsl:value-of select="substring-after(., ' ')"/>
                        </ymin>
                      </xsl:for-each>
                      <xsl:for-each select="$getCapabilities//wcs:lonLatEnvelope/gml:pos[2]">
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


                <gex:westBoundLongitude>
                  <gco:Decimal>
                    <xsl:value-of select="math:min($boxes/*[name(.)='xmin'])"/>
                  </gco:Decimal>
                </gex:westBoundLongitude>
                <gex:eastBoundLongitude>
                  <gco:Decimal>
                    <xsl:value-of select="math:max($boxes/*[name(.)='xmax'])"/>
                  </gco:Decimal>
                </gex:eastBoundLongitude>
                <gex:southBoundLatitude>
                  <gco:Decimal>
                    <xsl:value-of select="math:min($boxes/*[name(.)='ymin'])"/>
                  </gco:Decimal>
                </gex:southBoundLatitude>
                <gex:northBoundLatitude>
                  <gco:Decimal>
                    <xsl:value-of select="math:max($boxes/*[name(.)='ymax'])"/>
                  </gco:Decimal>
                </gex:northBoundLatitude>

              </xsl:when>
              <xsl:otherwise>

                <gex:westBoundLongitude>
                  <gco:Decimal>
                    <xsl:value-of
                      select="math:min($getCapabilities//(wms:westBoundLongitude|//LatLonBoundingBox/@minx|//wfs:LatLongBoundingBox/@minx))"/>
                  </gco:Decimal>
                </gex:westBoundLongitude>
                <gex:eastBoundLongitude>
                  <gco:Decimal>
                    <xsl:value-of
                      select="math:max($getCapabilities//(wms:eastBoundLongitude|//LatLonBoundingBox/@maxx|//wfs:LatLongBoundingBox/@maxx))"/>
                  </gco:Decimal>
                </gex:eastBoundLongitude>
                <gex:southBoundLatitude>
                  <gco:Decimal>
                    <xsl:value-of
                      select="math:min($getCapabilities//(wms:southBoundLatitude|//LatLonBoundingBox/@miny|//wfs:LatLongBoundingBox/@miny))"/>
                  </gco:Decimal>
                </gex:southBoundLatitude>
                <gex:northBoundLatitude>
                  <gco:Decimal>
                    <xsl:value-of
                      select="math:max($getCapabilities//(wms:northBoundLatitude|//LatLonBoundingBox/@maxy|//wfs:LatLongBoundingBox/@maxy))"/>
                  </gco:Decimal>
                </gex:northBoundLatitude>
              </xsl:otherwise>
            </xsl:choose>


          </gex:EX_GeographicBoundingBox>
        </gex:geographicElement>
      </gex:EX_Extent>
    </mri:extent>
  </xsl:template>


  <xsl:template mode="convert"
                match="*|@*"/>



  <xsl:template mode="copy"
                match="mdb:referenceSystemInfo[1]"
                priority="1999">
    <xsl:for-each
      select="distinct-values(
                  $getCapabilities//wms:Layer[wms:Name=$Name]/wms:CRS[position() &lt; $maxCRS]|
                  $getCapabilities//Layer[Name=$Name]/SRS[position() &lt; $maxCRS])">
      <mdb:referenceSystemInfo>
        <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
        <mrs:MD_ReferenceSystem>
          <mrs:referenceSystemIdentifier>
            <mcc:RS_Identifier>
              <mcc:code>
                <gco:CharacterString>
                  <xsl:value-of select="."/>
                </gco:CharacterString>
              </mcc:code>
            </mcc:RS_Identifier>
          </mrs:referenceSystemIdentifier>
        </mrs:MD_ReferenceSystem>
      </mdb:referenceSystemInfo>
    </xsl:for-each>
  </xsl:template>


  <xsl:template mode="copy"
                match="srv:serviceType"
                priority="1999">

    <srv:serviceType>
      <xsl:attribute name="gco:nilReason" select="$nilReasonValue"/>
      <gco:ScopedName codeSpace="https://github.com/OSGeo/Cat-Interop">
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
      </gco:ScopedName>
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
            <srv:distributedComputingPlatform>
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
            </srv:distributedComputingPlatform>
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
              <cit:CI_OnlineResource>
                <cit:linkage>
                  <gco:CharacterString>
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
                  </gco:CharacterString>
                </cit:linkage>
                <cit:protocol>
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
                </cit:protocol>
                <cit:description>
                  <gco:CharacterString>
                    Format :
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </cit:description>
                <cit:function>
                  <cit:CI_OnLineFunctionCode codeList="./resources/codeList.xml#CI_OnLineFunctionCode"
                                             codeListValue="information"/>
                </cit:function>
              </cit:CI_OnlineResource>
            </srv:connectPoint>
          </xsl:for-each>


          <!-- Some Operations in WFS 1.0.0 have no ResultFormat no CI_OnlineResource created
                            WCS has no output format
                    -->
          <xsl:for-each select="wfs:ResultFormat/*">
            <srv:connectPoint>
              <cit:CI_OnlineResource>
                <cit:linkage>
                  <gco:CharacterString>
                    <xsl:value-of select="../..//wfs:Get[1]/@onlineResource"/>
                  </gco:CharacterString>
                </cit:linkage>
                <cit:protocol>
                  <gco:CharacterString>
                    <xsl:value-of select="name(.)"/>
                  </gco:CharacterString>
                </cit:protocol>
                <cit:function>
                  <cit:CI_OnLineFunctionCode codeList="./resources/codeList.xml#CI_OnLineFunctionCode"
                                             codeListValue="information"/>
                </cit:function>
              </cit:CI_OnlineResource>
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
