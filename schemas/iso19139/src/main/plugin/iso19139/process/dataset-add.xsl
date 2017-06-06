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

<!--
Stylesheet used to update metadata for a service and
attached it to the metadata for data.
-->
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:param name="uuidref"/>
  <xsl:param name="scopedName"/>
  <xsl:param name="siteUrl"/>
  <xsl:param name="protocol" select="'OGC:WMS-1.1.1-http-get-map'"/>
  <xsl:param name="url"/>
  <xsl:param name="desc"/>

  <xsl:template match="/gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of
        select="gmd:fileIdentifier|
                gmd:language|
                gmd:characterSet|
                gmd:parentIdentifier|
                gmd:hierarchyLevel|
                gmd:hierarchyLevelName|
                gmd:contact|
                gmd:dateStamp|
                gmd:metadataStandardName|
                gmd:metadataStandardVersion|
                gmd:dataSetURI|
                gmd:locale|
                gmd:spatialRepresentationInfo|
                gmd:referenceSystemInfo|
                gmd:metadataExtensionInfo"/>

      <!-- Check current metadata is a service metadata record
            And add the link to the dataset -->
      <xsl:choose>
        <xsl:when
          test="gmd:identificationInfo/srv:SV_ServiceIdentification|
                gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']">
          <gmd:identificationInfo>
            <srv:SV_ServiceIdentification>
              <xsl:copy-of
                select="gmd:identificationInfo/*/gmd:citation|
                        gmd:identificationInfo/*/gmd:abstract|
                        gmd:identificationInfo/*/gmd:purpose|
                        gmd:identificationInfo/*/gmd:credit|
                        gmd:identificationInfo/*/gmd:statut|
                        gmd:identificationInfo/*/gmd:pointOfContact|
                        gmd:identificationInfo/*/gmd:resourceMaintenance|
                        gmd:identificationInfo/*/gmd:graphicOverview|
                        gmd:identificationInfo/*/gmd:resourceFormat|
                        gmd:identificationInfo/*/gmd:descriptiveKeywords|
                        gmd:identificationInfo/*/gmd:resourceSpecificUsage|
                        gmd:identificationInfo/*/gmd:resourceConstraints|
                        gmd:identificationInfo/*/gmd:aggregationInfo|
                        gmd:identificationInfo/*/srv:serviceType|
                        gmd:identificationInfo/*/srv:serviceTypeVersion|
                        gmd:identificationInfo/*/srv:accessProperties|
                        gmd:identificationInfo/*/srv:restrictions|
                        gmd:identificationInfo/*/srv:keywords|
                        gmd:identificationInfo/*/srv:extent"/>


              <!-- Handle SV_CoupledResource -->
              <xsl:variable name="coupledResource">
                <xsl:if test="$scopedName != ''">
                  <xsl:for-each select="tokenize($scopedName, ',')">
                    <srv:coupledResource>
                      <srv:SV_CoupledResource>
                        <srv:operationName>
                          <gco:CharacterString>GetCapabilities</gco:CharacterString>
                        </srv:operationName>
                        <srv:identifier>
                          <gco:CharacterString>
                            <xsl:value-of select="$uuidref"/>
                          </gco:CharacterString>
                        </srv:identifier>
                        <gco:ScopedName>
                          <xsl:value-of select="."/>
                        </gco:ScopedName>
                      </srv:SV_CoupledResource>
                    </srv:coupledResource>
                  </xsl:for-each>
                </xsl:if>
                <xsl:if test="not($scopedName)">
                  <srv:coupledResource>
                    <srv:SV_CoupledResource>
                      <srv:operationName>
                        <gco:CharacterString>GetCapabilities</gco:CharacterString>
                      </srv:operationName>
                      <srv:identifier>
                        <gco:CharacterString>
                          <xsl:value-of select="$uuidref"/>
                        </gco:CharacterString>
                      </srv:identifier>
                    </srv:SV_CoupledResource>
                  </srv:coupledResource>
                </xsl:if>
              </xsl:variable>

              <xsl:choose>
                <xsl:when
                  test="gmd:identificationInfo/*/srv:coupledResource">
                  <xsl:for-each
                    select="gmd:identificationInfo/*/srv:coupledResource">
                    <!-- Avoid duplicate SV_CoupledResource elements -->
                    <xsl:choose>
                      <xsl:when
                        test="srv:SV_CoupledResource/srv:identifier/gco:CharacterString!=$uuidref">
                        <xsl:copy-of select="."/>
                      </xsl:when>
                    </xsl:choose>
                    <xsl:if test="position()=last()">
                      <xsl:copy-of select="$coupledResource"/>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:if test="$uuidref and $uuidref != ''">
                    <xsl:copy-of select="$coupledResource"/>
                  </xsl:if>
                </xsl:otherwise>

              </xsl:choose>


              <xsl:copy-of
                select="gmd:identificationInfo/*/srv:couplingType|
                        gmd:identificationInfo/*/srv:containsOperations|
                        gmd:identificationInfo/*/srv:operatesOn[@uuidref!=$uuidref]"/>

              <!-- Handle operatesOn

              // TODO : it looks like the dataset identifier and not the
              // metadata UUID should be set in the operatesOn element of
              // the service metadata record.
              -->
              <srv:operatesOn uuidref="{$uuidref}"
                              xlink:href="{$siteUrl}csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;id={$uuidref}"/>

              <xsl:apply-templates select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd' and
                                             namespace-uri()!='http://www.isotc211.org/2005/srv']"/>
            </srv:SV_ServiceIdentification>
          </gmd:identificationInfo>
        </xsl:when>
        <xsl:otherwise>
          <!-- Probably a dataset metadata record -->
          <xsl:copy-of select="gmd:identificationInfo"/>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:copy-of select="gmd:contentInfo"/>


      <xsl:choose>
        <xsl:when
          test="gmd:identificationInfo/srv:SV_ServiceIdentification|
                gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']">
          <xsl:copy-of select="gmd:distributionInfo"/>
        </xsl:when>
        <!-- In a dataset add a link in the distribution section -->
        <xsl:otherwise>
          <!-- TODO we could check if online resource already exists before adding information -->
          <gmd:distributionInfo>
            <gmd:MD_Distribution>
              <xsl:copy-of
                select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat"/>
              <xsl:copy-of
                select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor"/>
              <gmd:transferOptions>
                <gmd:MD_DigitalTransferOptions>
                  <xsl:copy-of
                    select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions/gmd:unitsOfDistribution"/>
                  <xsl:copy-of
                    select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions/gmd:transferSize"/>
                  <xsl:copy-of
                    select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions/gmd:onLine"/>

                  <xsl:for-each select="tokenize($scopedName, ',')">
                    <gmd:onLine>
                      <gmd:CI_OnlineResource>
                        <gmd:linkage>
                          <gmd:URL>
                            <xsl:value-of select="$url"/>
                          </gmd:URL>
                        </gmd:linkage>
                        <gmd:protocol>
                          <gco:CharacterString>
                            <xsl:value-of select="$protocol"/>
                          </gco:CharacterString>
                        </gmd:protocol>
                        <gmd:name>
                          <gco:CharacterString>
                            <xsl:value-of select="."/>
                          </gco:CharacterString>
                        </gmd:name>
                        <gmd:description>
                          <gco:CharacterString>
                            <xsl:value-of select="."/>
                          </gco:CharacterString>
                        </gmd:description>
                      </gmd:CI_OnlineResource>
                    </gmd:onLine>
                  </xsl:for-each>
                  <xsl:copy-of
                    select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions/gmd:offLine"
                  />
                </gmd:MD_DigitalTransferOptions>
              </gmd:transferOptions>
              <xsl:copy-of
                select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions[position() > 1]"
              />
            </gmd:MD_Distribution>

          </gmd:distributionInfo>
        </xsl:otherwise>
      </xsl:choose>


      <xsl:copy-of
        select="gmd:dataQualityInfo|
                gmd:portrayalCatalogueInfo|
                gmd:metadataConstraints|
                gmd:applicationSchemaInfo|
                gmd:metadataMaintenance|
                gmd:series|
                gmd:describes|
                gmd:propertyType|
                gmd:featureType|
                gmd:featureAttribute"/>

      <xsl:apply-templates select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd' and
                                     namespace-uri()!='http://www.isotc211.org/2005/srv']"/>

    </xsl:copy>
  </xsl:template>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
