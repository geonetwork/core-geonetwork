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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:ows="http://www.opengis.net/ows"
                version="1.0"
                exclude-result-prefixes="gmd srv gco">

  <xsl:param name="displayInfo"/>
  <xsl:param name="lang"/>

  <xsl:include href="../metadata-utils.xsl"/>

  <!-- ============================================================================= -->

  <xsl:template match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">

    <xsl:variable name="info" select="geonet:info"/>
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="$lang"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="identification" select="gmd:identificationInfo/gmd:MD_DataIdentification|
      gmd:identificationInfo/*[contains(@gco:isoType, 'MD_DataIdentification')]|
      gmd:identificationInfo/srv:SV_ServiceIdentification|
      gmd:identificationInfo/*[contains(@gco:isoType, 'SV_ServiceIdentification')]"/>

    <csw:Record>

      <xsl:for-each select="gmd:fileIdentifier">
        <dc:identifier>
          <xsl:value-of select="gco:CharacterString"/>
        </dc:identifier>
      </xsl:for-each>

      <xsl:for-each select="gmd:dateStamp">
        <dc:date>
          <xsl:value-of select="gco:Date|gco:DateTime"/>
        </dc:date>
      </xsl:for-each>

      <!-- DataIdentification - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="$identification/gmd:citation/gmd:CI_Citation">
        <xsl:for-each select="gmd:title">
          <dc:title>
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </dc:title>
        </xsl:for-each>

        <!-- Type - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

        <xsl:for-each select="../../../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue">
          <dc:type>
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </dc:type>
        </xsl:for-each>

        <!-- subject -->

        <xsl:for-each
          select="../../gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[not(@gco:nilReason)]">
          <dc:subject>
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </dc:subject>
        </xsl:for-each>
        <xsl:for-each select="../../gmd:topicCategory/gmd:MD_TopicCategoryCode">
          <dc:subject>
            <xsl:value-of select="."/>
          </dc:subject><!-- TODO : translate ? -->
        </xsl:for-each>


        <!-- Distribution - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

        <xsl:for-each select="../../../../gmd:distributionInfo/gmd:MD_Distribution">
          <xsl:for-each select="gmd:distributionFormat/gmd:MD_Format/gmd:name">
            <dc:format>
              <xsl:apply-templates mode="localised" select=".">
                <xsl:with-param name="langId" select="$langId"/>
              </xsl:apply-templates>
            </dc:format>
          </xsl:for-each>
        </xsl:for-each>


        <xsl:for-each
          select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date">
          <dct:modified>
            <xsl:value-of select="."/>
          </dct:modified>
        </xsl:for-each>

        <xsl:for-each
          select="gmd:citedResponsibleParty/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='originator']/gmd:organisationName">
          <dc:creator>
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </dc:creator>
        </xsl:for-each>

        <xsl:for-each
          select="gmd:citedResponsibleParty/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='publisher']/gmd:organisationName">
          <dc:publisher>
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </dc:publisher>
        </xsl:for-each>

        <xsl:for-each
          select="gmd:citedResponsibleParty/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='author']/gmd:organisationName">
          <dc:contributor>
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </dc:contributor>
        </xsl:for-each>
      </xsl:for-each>


      <!-- abstract -->

      <xsl:for-each select="$identification/gmd:abstract">
        <dct:abstract>
          <xsl:apply-templates mode="localised" select=".">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:apply-templates>
        </dct:abstract>
        <dc:description>
          <xsl:apply-templates mode="localised" select=".">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:apply-templates>
        </dc:description>
      </xsl:for-each>

      <!-- rights -->

      <xsl:for-each select="$identification/gmd:resourceConstraints/gmd:MD_LegalConstraints|
        gmd:resourceConstraints/*[@gco:isoType='gmd:MD_LegalConstraints']">
        <xsl:for-each select="*/gmd:MD_RestrictionCode/@codeListValue">
          <dc:rights>
            <xsl:value-of select="."/>
          </dc:rights>
        </xsl:for-each>

        <xsl:for-each select="$identification/otherConstraints">
          <dc:rights>
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </dc:rights>
        </xsl:for-each>
      </xsl:for-each>

      <!-- language -->

      <xsl:for-each select="$identification/gmd:language">
        <dc:language>
          <xsl:value-of select="gco:CharacterString|gmd:LanguageCode/@codeListValue"/>
        </dc:language>
      </xsl:for-each>

      <!-- Lineage -->

      <xsl:for-each
        select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement">
        <dc:source>
          <xsl:apply-templates mode="localised" select=".">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:apply-templates>
        </dc:source>
      </xsl:for-each>

      <!-- Parent Identifier -->

      <xsl:for-each select="gmd:parentIdentifier/gco:CharacterString">
        <dc:relation>
          <xsl:value-of select="."/>
        </dc:relation>
      </xsl:for-each>


      <!-- Distribution - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution">
        <xsl:for-each select="gmd:distributionFormat/gmd:MD_Format/gmd:name">
          <dc:format>
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </dc:format>
        </xsl:for-each>
      </xsl:for-each>

      <!-- bounding box -->

      <xsl:for-each
        select="$identification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
        <xsl:variable name="rsi" select="/gmd:MD_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/
          gmd:referenceSystemIdentifier/gmd:RS_Identifier|/gmd:MD_Metadata/gmd:referenceSystemInfo/
          *[@gco:isoType='MD_ReferenceSystem']/gmd:referenceSystemIdentifier/gmd:RS_Identifier"/>
        <xsl:variable name="auth" select="$rsi/gmd:codeSpace/gco:CharacterString"/>
        <xsl:variable name="id" select="$rsi/gmd:code/gco:CharacterString"/>
        <xsl:variable name="crs" select="concat('urn:ogc:def:crs:', $auth, '::', $id)"/>

        <ows:BoundingBox>
          <xsl:attribute name="crs">
            <xsl:choose>
              <xsl:when test="$crs = 'urn:ogc:def:crs:::'">urn:ogc:def:crs:EPSG:6.6:4326</xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$crs"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>

          <ows:LowerCorner>
            <xsl:value-of
              select="concat(gmd:eastBoundLongitude/gco:Decimal, ' ', gmd:southBoundLatitude/gco:Decimal)"/>
          </ows:LowerCorner>

          <ows:UpperCorner>
            <xsl:value-of
              select="concat(gmd:westBoundLongitude/gco:Decimal, ' ', gmd:northBoundLatitude/gco:Decimal)"/>
          </ows:UpperCorner>
        </ows:BoundingBox>
      </xsl:for-each>


      <!-- Create as many URI element
                * thumbnails
                * dataset online source elements
                * as coupledResource defined for a WMS service.
                 * Get one connect point for the service
                 * Add as many layers defined in coupled resource elements.

                With this information, client could access to onlinesource defined in the metadata.

                CSW 2.0.2 ISO profil does not support dc:URI elements.
                What could be done is to add an output format supporting dclite4g
                http://wiki.osgeo.org/wiki/DCLite4G (TODO)
                -->
      <xsl:for-each select="
        gmd:identificationInfo/srv:SV_ServiceIdentification[srv:serviceType/gco:LocalName='OGC:WMS']|
        gmd:identificationInfo/*[contains(@gco:isoType, 'SV_ServiceIdentification') and srv:serviceType/gco:LocalName='OGC:WMS'] ">

        <xsl:variable name="connectPoint"
                      select="srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
        <xsl:variable name="serviceUrl">
          <xsl:choose>
            <xsl:when test="$connectPoint=''">
              <xsl:value-of
                select="../gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$connectPoint"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <dc:URI protocol="OGC:WMS-1.1.1-http-get-capabilities">
          <xsl:value-of select="$serviceUrl"/>
        </dc:URI>
        <xsl:for-each select="srv:coupledResource/srv:SV_CoupledResource">
          <xsl:if test="gco:ScopedName!=''">
            <dc:URI protocol="OGC:WMS" name="{gco:ScopedName}">
              <xsl:value-of select="$serviceUrl"/>
            </dc:URI>
          </xsl:if>
        </xsl:for-each>

      </xsl:for-each>


      <xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution">
        <xsl:for-each
          select="gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource">
          <xsl:if test="gmd:linkage">
            <dc:URI>
              <xsl:if test="gmd:protocol">
                <xsl:attribute name="protocol">
                  <xsl:value-of select="gmd:protocol/gco:CharacterString"/>
                </xsl:attribute>
              </xsl:if>

              <xsl:if test="gmd:name">
                <xsl:attribute name="name">
                  <xsl:for-each select="gmd:name">
                    <xsl:apply-templates mode="localised" select=".">
                      <xsl:with-param name="langId" select="$langId"/>
                    </xsl:apply-templates>
                  </xsl:for-each>
                </xsl:attribute>
              </xsl:if>

              <xsl:if test="gmd:description">
                <xsl:attribute name="description">
                  <xsl:for-each select="gmd:description">
                    <xsl:apply-templates mode="localised" select=".">
                      <xsl:with-param name="langId" select="$langId"/>
                    </xsl:apply-templates>
                  </xsl:for-each>
                </xsl:attribute>
              </xsl:if>

              <xsl:value-of select="gmd:linkage/gmd:URL"/>
            </dc:URI>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:for-each
        select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic">
        <xsl:variable name="fileName" select="gmd:fileName/gco:CharacterString"/>
        <xsl:variable name="fileDescr" select="gmd:fileDescription/gco:CharacterString"/>

        <xsl:if test="$fileName!=''">
          <dc:URI>
            <xsl:choose>
              <xsl:when test="contains(gmd:fileName/gco:CharacterString, '.gif')">
                <xsl:attribute name="protocol">image/gif</xsl:attribute>
              </xsl:when>
              <xsl:when test="contains(gmd:fileName/gco:CharacterString, '.png')">
                <xsl:attribute name="protocol">image/png</xsl:attribute>
              </xsl:when>
              <xsl:when test="contains(gmd:fileName/gco:CharacterString, '.jpg') or
                                        contains(gmd:fileName/gco:CharacterString, '.jpeg')">
                <xsl:attribute name="protocol">image/jpeg</xsl:attribute>
              </xsl:when>
            </xsl:choose>

            <xsl:if test="$fileDescr">
              <xsl:attribute name="name">
                <xsl:value-of select="$fileDescr"/>
              </xsl:attribute>
            </xsl:if>
            <xsl:choose>
              <xsl:when test="contains($fileName ,'://')">
                <xsl:value-of select="$fileName"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of
                  select="concat('resources.get?id=',$info/id,'&amp;fname=',$fileName,'&amp;access=public')"/>
              </xsl:otherwise>
            </xsl:choose>

          </dc:URI>
        </xsl:if>
      </xsl:for-each>

      <!-- GeoNetwork elements added when resultType is equal to results_with_summary -->
      <xsl:if test="$displayInfo = 'true'">
        <xsl:copy-of select="$info"/>
      </xsl:if>

    </csw:Record>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*">
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
