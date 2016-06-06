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

<xsl:stylesheet xmlns:tds="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
                xmlns:nc="http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:util="java:java.util.UUID"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                exclude-result-prefixes="util xs xsi tds nc">

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <xsl:include href="thredds/utils.xsl"/>

  <!--
        This xslt transforms thredds/ncml metadata to ISO19139 metadata that can be used by the thredds harvester to generate iso metadata from thredds catalogs.

        Transformation based on:

            thredds.catalog.dl.DIFWriter in the netcdf4.0 java library
            http://www.unidata.ucar.edu/projects/THREDDS/tech/catalog/InvCatalogSpec.html
            http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html
            https://www.nosc.noaa.gov/dmc/swg/wiki/index.php?title=NetCDF_Attribute_Convention_for_Dataset_Discovery

        Requires netcdf java library on class path (e.g. netcdf4.0.jar) for netcdf date transformation routines

    -->

  <!-- === Default rule - do nothing/match children === -->

  <xsl:template match="@*|node()">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- === Add fragments  === -->

  <xsl:variable name="datasetName" select="/root/tds:catalog/tds:dataset/@name"/>

  <xsl:template match="/root">
    <records>
      <xsl:for-each select="tds:catalog/tds:dataset[1 and $datasetName!='latest.xml']">
        <xsl:variable name="authority"
                      select="tds:authority|tds:metadata//tds:authority|@authority"/>
        <xsl:variable name="id" select="@ID"/>
        <xsl:variable name="uuid">
          <xsl:if test="$authority[1] and $id">
            <xsl:value-of select="concat($authority[1],':')"/>
          </xsl:if>
          <xsl:value-of select="$id"/>
        </xsl:variable>

        <record uuid="{$uuid}">

          <!-- Metadata creation date [Mandatory] -->

          <fragment id="thredds.metadata.creation" uuid="{util:toString(util:randomUUID())}"
                    title="{concat($datasetName,'_metadata_creation')}">
            <gmd:dateStamp>
              <gco:DateTime>
                <xsl:variable name="creationDate"
                              select="tds:date[@type='created']|tds:metadata//tds:date[@type='created']"/>

                <xsl:choose>
                  <xsl:when test="$creationDate[1]!=''">
                    <!-- Use resource creation date if available -->
                    <xsl:call-template name="getThreddsDateAsUTC">
                      <xsl:with-param name="sourceDate" select="$creationDate[1]"/>
                    </xsl:call-template>
                  </xsl:when>
                  <xsl:otherwise>
                    <!-- Otherwise use curent date -->
                    <xsl:call-template name="getUtcDateTime">
                      <xsl:with-param name="dateTime" select="current-dateTime()"/>
                    </xsl:call-template>
                  </xsl:otherwise>
                </xsl:choose>
              </gco:DateTime>
            </gmd:dateStamp>
          </fragment>

          <!-- Metadata title [Mandatory] -->

          <fragment id="thredds.title" uuid="{util:toString(util:randomUUID())}"
                    title="{concat($datasetName,'_title')}">
            <gmd:title>
              <gco:CharacterString>
                <xsl:choose>
                  <xsl:when test="normalize-space(@urlPath)!=''">
                    <xsl:value-of select="@urlPath"/>
                  </xsl:when>
                  <xsl:when test="normalize-space(@ID)!=''">
                    <xsl:value-of select="@ID"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="@name"/>
                  </xsl:otherwise>
                </xsl:choose>
              </gco:CharacterString>
            </gmd:title>
          </fragment>


          <!-- Resource dates [0..3] -->

          <replacementGroup id="thredds.resource.dates">

            <!-- creation date -->

            <xsl:variable name="creationDate"
                          select="tds:date[@type='created']|tds:metadata//tds:date[@type='created']"/>

            <xsl:if test="$creationDate[1]!=''">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($datasetName,'_resource_creation')}">
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <xsl:call-template name="getThreddsDateAsUTC">
                        <xsl:with-param name="sourceDate" select="$creationDate[1]"/>
                      </xsl:call-template>
                    </gmd:date>
                    <gmd:dateType>
                      <gmd:CI_DateTypeCode
                        codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode"
                        codeListValue="creation"/>
                    </gmd:dateType>
                  </gmd:CI_Date>
                </gmd:date>
              </fragment>
            </xsl:if>

            <!-- Revision date [0..1] -->

            <xsl:variable name="modifiedDate"
                          select="tds:date[@type='modified']|tds:metadata//tds:date[@type='modified']"/>

            <xsl:if test="$modifiedDate[1]!=''">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($datasetName,'_resource_revision')}">
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:DateTime>
                        <xsl:call-template name="getThreddsDateAsUTC">
                          <xsl:with-param name="sourceDate" select="$modifiedDate[1]"/>
                        </xsl:call-template>
                      </gco:DateTime>
                    </gmd:date>
                    <gmd:dateType>
                      <gmd:CI_DateTypeCode
                        codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode"
                        codeListValue="revision"/>
                    </gmd:dateType>
                  </gmd:CI_Date>
                </gmd:date>
              </fragment>
            </xsl:if>

            <!-- published date -->

            <xsl:variable name="issuedDate"
                          select="tds:date[@type='issued']|tds:metadata//tds:date[@type='issued']"/>

            <xsl:if test="$issuedDate[1]!=''">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($datasetName,'_resource_published')}">
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:DateTime>
                        <xsl:call-template name="getThreddsDateAsUTC">
                          <xsl:with-param name="sourceDate" select="$issuedDate[1]"/>
                        </xsl:call-template>
                      </gco:DateTime>
                    </gmd:date>
                    <gmd:dateType>
                      <gmd:CI_DateTypeCode
                        codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode"
                        codeListValue="publication"/>
                    </gmd:dateType>
                  </gmd:CI_Date>
                </gmd:date>
              </fragment>
            </xsl:if>
          </replacementGroup>

          <!-- Responsible Parties [0..N] -->

          <replacementGroup id="thredds.responsible.parties">

            <!-- Publishers [0..N] -->

            <xsl:for-each select="tds:publisher|tds:metadata//tds:publisher">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($datasetName,'_publisher')}">
                <gmd:citedResponsibleParty>
                  <gmd:CI_ResponsibleParty>
                    <gmd:individualName>
                      <gco:CharacterString>
                        <xsl:value-of select="tds:name"/>
                      </gco:CharacterString>
                    </gmd:individualName>
                    <xsl:if test="tds:contact/@name!='' or tds:contact/@url!=''">
                      <gmd:contactInfo>
                        <gmd:CI_Contact>
                          <xsl:if test="tds:contact/@name!=''">
                            <gmd:address>
                              <gmd:CI_Address>
                                <gmd:electronicMailAddress>
                                  <gco:CharacterString>
                                    <xsl:value-of select="tds:contact/@email"/>
                                  </gco:CharacterString>
                                </gmd:electronicMailAddress>
                              </gmd:CI_Address>
                            </gmd:address>
                          </xsl:if>
                          <xsl:if test="tds:contact/@url!=''">
                            <gmd:onlineResource>
                              <gmd:CI_OnlineResource>
                                <gmd:linkage>
                                  <gmd:URL>
                                    <xsl:value-of select="tds:contact/@url"/>
                                  </gmd:URL>
                                </gmd:linkage>
                                <gmd:protocol>
                                  <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                                </gmd:protocol>
                              </gmd:CI_OnlineResource>
                            </gmd:onlineResource>
                          </xsl:if>
                        </gmd:CI_Contact>
                      </gmd:contactInfo>
                    </xsl:if>
                    <gmd:role>
                      <gmd:CI_RoleCode
                        codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode"
                        codeListValue="publisher"/>
                    </gmd:role>
                  </gmd:CI_ResponsibleParty>
                </gmd:citedResponsibleParty>
              </fragment>
            </xsl:for-each>

            <!-- Originators [0..N] -->

            <xsl:for-each select="tds:creator|tds:metadata//tds:creator">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($datasetName,'_originator')}">
                <gmd:citedResponsibleParty>
                  <gmd:CI_ResponsibleParty>
                    <gmd:individualName>
                      <gco:CharacterString>
                        <xsl:value-of select="tds:name"/>
                      </gco:CharacterString>
                    </gmd:individualName>
                    <xsl:if test="tds:contact/@name!='' or tds:contact/@url!=''">
                      <gmd:contactInfo>
                        <gmd:CI_Contact>
                          <xsl:if test="tds:contact/@name!=''">
                            <gmd:address>
                              <gmd:CI_Address>
                                <gmd:electronicMailAddress>
                                  <gco:CharacterString>
                                    <xsl:value-of select="tds:contact/@email"/>
                                  </gco:CharacterString>
                                </gmd:electronicMailAddress>
                              </gmd:CI_Address>
                            </gmd:address>
                          </xsl:if>
                          <xsl:if test="tds:contact/@url!=''">
                            <gmd:onlineResource>
                              <gmd:CI_OnlineResource>
                                <gmd:linkage>
                                  <gmd:URL>
                                    <xsl:value-of select="tds:contact/@url"/>
                                  </gmd:URL>
                                </gmd:linkage>
                                <gmd:protocol>
                                  <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                                </gmd:protocol>
                              </gmd:CI_OnlineResource>
                            </gmd:onlineResource>
                          </xsl:if>
                        </gmd:CI_Contact>
                      </gmd:contactInfo>
                    </xsl:if>
                    <gmd:role>
                      <gmd:CI_RoleCode
                        codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode"
                        codeListValue="originator"/>
                    </gmd:role>
                  </gmd:CI_ResponsibleParty>
                </gmd:citedResponsibleParty>
              </fragment>
            </xsl:for-each>

            <!-- contributors [0..N] -->

            <xsl:for-each select="tds:contributor|tds:metadata//tds:contributor">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($datasetName,'_author')}">
                <gmd:citedResponsibleParty>
                  <gmd:CI_ResponsibleParty>
                    <gmd:individualName>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </gmd:individualName>
                    <gmd:role>
                      <xsl:choose>
                        <xsl:when test="@role='PI'">
                          <gmd:CI_RoleCode
                            codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode"
                            codeListValue="principalInvestigator"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <gmd:CI_RoleCode
                            codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode"
                            codeListValue="author"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </gmd:role>
                  </gmd:CI_ResponsibleParty>
                </gmd:citedResponsibleParty>
              </fragment>
            </xsl:for-each>
          </replacementGroup>

          <!-- Abstract [Mandatory]]-->

          <fragment id="thredds.abstract" uuid="{util:toString(util:randomUUID())}"
                    title="{concat($datasetName,'_abstract')}">
            <gmd:abstract>
              <gco:CharacterString>
                <xsl:choose>
                  <xsl:when
                    test="tds:documentation[@type='summary']|tds:metadata//tds:documentation[@type='summary']">
                    <xsl:for-each
                      select="tds:documentation[@type='summary']|tds:metadata//tds:documentation[@type='summary']">
                      <xsl:value-of select="."/>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>Thredds dataset</xsl:otherwise>
                </xsl:choose>
              </gco:CharacterString>
            </gmd:abstract>
          </fragment>

          <!-- Credit [0..N]-->

          <replacementGroup id="thredds.credit">
            <xsl:for-each
              select="tds:documentation[@type='funding']|tds:metadata//tds:documentation[@type='funding']">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($datasetName,'_credit')}">
                <gmd:credit>
                  <gco:CharacterString>
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:credit>
              </fragment>
            </xsl:for-each>
          </replacementGroup>

          <!-- Keywords [0..N] -->

          <replacementGroup id="thredds.keywords">
            <!-- Add keywords with no vocabulary specified -->
            <xsl:if
              test="tds:keyword[not(@vocabulary)]|tds:metadata//tds:keyword[not(@vocabulary)]">
              <xsl:call-template name="descriptive-keywords">
                <xsl:with-param name="keywords"
                                select="tds:keyword[not(@vocabulary)]|tds:metadata//tds:keyword[not(@vocabulary)]"/>
              </xsl:call-template>
            </xsl:if>

            <!-- Add keywords belonging to a vocabulary -->
            <xsl:for-each-group select="tds:keyword|tds:metadata//tds:keyword"
                                group-by="@vocabulary">
              <xsl:call-template name="descriptive-keywords">
                <xsl:with-param name="vocabulary" select="current-grouping-key()"/>
                <xsl:with-param name="keywords" select="current-group()"/>
              </xsl:call-template>
            </xsl:for-each-group>
          </replacementGroup>

          <!-- Use constraints [0..1] -->

          <replacementGroup id="thredds.use.constraints">
            <xsl:for-each
              select="tds:documentation[@type='rights']|tds:metadata//tds:documentation[@type='rights']">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($datasetName,'_use_constraints')}">
                <gmd:MD_LegalConstraints>
                  <gmd:useConstraints>
                    <gmd:MD_RestrictionCode
                      codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_RestrictionCode"
                      codeListValue="otherRestrictions"/>
                  </gmd:useConstraints>
                  <gmd:otherConstraints>
                    <gco:CharacterString>
                      <xsl:value-of select="."/>
                    </gco:CharacterString>
                  </gmd:otherConstraints>
                </gmd:MD_LegalConstraints>
              </fragment>
            </xsl:for-each>
          </replacementGroup>

          <!-- Aggregation info (Project) [0-N] -->

          <replacementGroup id="thredds.project">
            <xsl:for-each select="tds:project|tds:metadata//tds:project">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($datasetName,'_project')}">
                <gmd:aggregationInfo>
                  <gmd:MD_AggregateInformation>
                    <gmd:aggregateDataSetName>
                      <gmd:CI_Citation>
                        <gmd:title>
                          <gco:CharacterString>
                            <xsl:value-of select="."/>
                          </gco:CharacterString>
                        </gmd:title>
                        <gmd:date>
                          <gmd:CI_Date>
                            <gmd:dateType>
                              <gmd:CI_DateTypeCode
                                codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode"
                                codeListValue=""/>
                            </gmd:dateType>
                          </gmd:CI_Date>
                        </gmd:date>
                      </gmd:CI_Citation>
                    </gmd:aggregateDataSetName>
                    <gmd:associationType>
                      <gmd:DS_AssociationTypeCode
                        codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#DS_AssociationTypeCode"
                        codeListValue="largerWorkCitation">largerWorkCitation
                      </gmd:DS_AssociationTypeCode>
                    </gmd:associationType>
                    <gmd:initiativeType>
                      <gmd:DS_InitiativeTypeCode
                        codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#DS_InitiativeTypeCode"
                        codeListValue="project">project
                      </gmd:DS_InitiativeTypeCode>
                    </gmd:initiativeType>
                  </gmd:MD_AggregateInformation>
                </gmd:aggregationInfo>
              </fragment>
            </xsl:for-each>
          </replacementGroup>

          <!-- Extent [Mandatory for dataset] -->

          <fragment id="thredds.extent" uuid="{util:toString(util:randomUUID())}"
                    title="{concat($datasetName,'_extent')}">
            <gmd:extent>
              <gmd:EX_Extent>

                <!-- geographic extent [0..1] -->

                <xsl:for-each select="tds:geospatialCoverage|tds:metadata//tds:geospatialCoverage">
                  <!-- TODO: support lat/long in units other than deegrees -->
                  <xsl:if
                    test="tds:northsouth/tds:units='degrees_north' and tds:eastwest/tds:units='degrees_east'">
                    <gmd:geographicElement>
                      <gmd:EX_GeographicBoundingBox>
                        <gmd:westBoundLongitude>
                          <gco:Decimal>
                            <xsl:value-of select="tds:eastwest/tds:start"/>
                          </gco:Decimal>
                        </gmd:westBoundLongitude>
                        <gmd:eastBoundLongitude>
                          <gco:Decimal>
                            <xsl:value-of select="tds:eastwest/tds:start + tds:eastwest/tds:size"/>
                          </gco:Decimal>
                        </gmd:eastBoundLongitude>
                        <gmd:southBoundLatitude>
                          <gco:Decimal>
                            <xsl:value-of select="tds:northsouth/tds:start"/>
                          </gco:Decimal>
                        </gmd:southBoundLatitude>
                        <gmd:northBoundLatitude>
                          <gco:Decimal>
                            <xsl:value-of
                              select="tds:northsouth/tds:start + tds:northsouth/tds:size"/>
                          </gco:Decimal>
                        </gmd:northBoundLatitude>
                      </gmd:EX_GeographicBoundingBox>
                    </gmd:geographicElement>
                  </xsl:if>
                </xsl:for-each>

                <!-- temporal extent [0..1] -->

                <xsl:for-each select="tds:timeCoverage|tds:metadata//tds:timeCoverage">
                  <xsl:variable name="dateRange">
                    <xsl:call-template name="resolveThreddsDateRange">
                      <xsl:with-param name="start" select="tds:start"/>
                      <xsl:with-param name="end" select="tds:end"/>
                      <xsl:with-param name="duration" select="tds:duration"/>
                    </xsl:call-template>
                  </xsl:variable>

                  <gmd:temporalElement>
                    <gmd:EX_TemporalExtent>
                      <gmd:extent>
                        <gml:TimePeriod gml:id="{generate-id(.)}">
                          <gml:beginPosition>
                            <xsl:value-of select="$dateRange/startDate"/>
                          </gml:beginPosition>
                          <gml:endPosition>
                            <xsl:value-of select="$dateRange/endDate"/>
                          </gml:endPosition>
                        </gml:TimePeriod>
                      </gmd:extent>
                    </gmd:EX_TemporalExtent>
                  </gmd:temporalElement>
                </xsl:for-each>

                <!-- vertical extent [0-1] -->

                <xsl:for-each
                  select="tds:geospatialCoverage/tds:updown|tds:metadata//tds:geospatialCoverage/tds:updown">
                  <gmd:verticalElement>
                    <gmd:EX_VerticalExtent>
                      <gmd:minimumValue>
                        <gco:Real>
                          <xsl:value-of select="tds:start"/>
                        </gco:Real>
                      </gmd:minimumValue>
                      <gmd:maximumValue>
                        <gco:Real>
                          <xsl:value-of select="tds:start+tds:size"/>
                        </gco:Real>
                      </gmd:maximumValue>
                    </gmd:EX_VerticalExtent>
                  </gmd:verticalElement>
                </xsl:for-each>

              </gmd:EX_Extent>
            </gmd:extent>
          </fragment>

          <!-- Supplemental information [0..1] -->

          <replacementGroup id="thredds.supplemental">
            <xsl:if
              test="tds:documentation[not(@type)]|tds:metadata//tds:documentation[not(@type)]">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($datasetName,'_supplemental')}">
                <gmd:supplementalInformation>
                  <gco:CharacterString>
                    <xsl:for-each
                      select="tds:documentation[not(@type)]|tds:metadata//tds:documentation[not(@type)]">
                      <xsl:value-of select="."/>
                    </xsl:for-each>
                  </gco:CharacterString>
                </gmd:supplementalInformation>
              </fragment>
            </xsl:if>
          </replacementGroup>

          <!-- Online resources [2..N] -->

          <replacementGroup id="thredds.online.resources">
            <xsl:variable name="catalogUrl" select="replace(/root/catalogUri,'.xml','.html')"/>

            <!-- Links to services if an atomic dataset -->

            <xsl:if test="not(tds:dataset)">
              <xsl:for-each select="tds:serviceName|tds:metadata//tds:serviceName|@serviceName">
                <xsl:variable name="serviceName" select="string(.)"/>
                <xsl:apply-templates select="/root/tds:catalog//tds:service[@name=$serviceName]"
                                     mode="transfer-options"/>
              </xsl:for-each>
            </xsl:if>

            <!-- Link to thredds catalog for dataset -->

            <fragment uuid="{util:toString(util:randomUUID())}"
                      title="{concat($datasetName,'_dataset_link')}">
              <gmd:onLine>
                <gmd:CI_OnlineResource>
                  <gmd:linkage>
                    <gmd:URL>
                      <xsl:value-of select="concat($catalogUrl,'?dataset=',@ID)"/>
                    </gmd:URL>
                  </gmd:linkage>
                  <gmd:protocol>
                    <gco:CharacterString>WWW:LINK-1.0-http--downloaddata</gco:CharacterString>
                  </gmd:protocol>
                  <gmd:description>
                    <gco:CharacterString>THREDDS Metadata</gco:CharacterString>
                  </gmd:description>
                </gmd:CI_OnlineResource>
              </gmd:onLine>
            </fragment>

            <!-- Link to thredds catalog containing dataset -->

            <fragment uuid="{util:toString(util:randomUUID())}"
                      title="{concat($datasetName,'_catalog_link')}">
              <gmd:onLine>
                <gmd:CI_OnlineResource>
                  <gmd:linkage>
                    <gmd:URL>
                      <xsl:value-of select="$catalogUrl"/>
                    </gmd:URL>
                  </gmd:linkage>
                  <gmd:protocol>
                    <gco:CharacterString>WWW:LINK-1.0-http--downloaddata</gco:CharacterString>
                  </gmd:protocol>
                  <gmd:description>
                    <gco:CharacterString>THREDDS Catalog</gco:CharacterString>
                  </gmd:description>
                </gmd:CI_OnlineResource>
              </gmd:onLine>
            </fragment>

          </replacementGroup>

          <!-- Lineage [0..N] -->

          <replacementGroup id="thredds.data.quality">
            <xsl:for-each
              select="tds:documentation[@type='history']|tds:metadata//tds:documentation[@type='history']">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($datasetName,'_history')}">
                <gmd:dataQualityInfo>
                  <gmd:DQ_DataQuality>
                    <gmd:scope>
                      <gmd:DQ_Scope>
                        <gmd:level>
                          <gmd:MD_ScopeCode
                            codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_ScopeCode"
                            codeListValue="dataset">dataset
                          </gmd:MD_ScopeCode>
                        </gmd:level>
                      </gmd:DQ_Scope>
                    </gmd:scope>
                    <gmd:lineage>
                      <gmd:LI_Lineage>
                        <gmd:statement>
                          <gco:CharacterString>
                            <xsl:value-of select="."/>
                          </gco:CharacterString>
                        </gmd:statement>
                      </gmd:LI_Lineage>
                    </gmd:lineage>
                  </gmd:DQ_DataQuality>
                </gmd:dataQualityInfo>
              </fragment>
            </xsl:for-each>

            <xsl:for-each
              select="tds:documentation[@type='processing_level']|tds:metadata//tds:documentation[@type='processing_level']">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($datasetName,'_processing_level')}">
                <gmd:dataQualityInfo>
                  <gmd:DQ_DataQuality>
                    <gmd:scope>
                      <gmd:DQ_Scope>
                        <gmd:level>
                          <gmd:MD_ScopeCode
                            codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_ScopeCode"
                            codeListValue="dataset">dataset
                          </gmd:MD_ScopeCode>
                        </gmd:level>
                      </gmd:DQ_Scope>
                    </gmd:scope>
                    <gmd:lineage>
                      <gmd:LI_Lineage>
                        <gmd:statement>
                          <gco:CharacterString>
                            <xsl:value-of select="."/>
                          </gco:CharacterString>
                        </gmd:statement>
                      </gmd:LI_Lineage>
                    </gmd:lineage>
                  </gmd:DQ_DataQuality>
                </gmd:dataQualityInfo>
              </fragment>
            </xsl:for-each>
          </replacementGroup>

        </record>
      </xsl:for-each>
    </records>
  </xsl:template>

  <!-- === Descriptive Keywords === -->

  <xsl:template name="descriptive-keywords">
    <xsl:param name="vocabulary"/>
    <xsl:param name="keywords"/>

    <fragment uuid="{util:toString(util:randomUUID())}" title="{concat($datasetName,'_keywords')}">
      <gmd:descriptiveKeywords>
        <gmd:MD_Keywords>
          <xsl:apply-templates mode="keywords" select="$keywords"/>
          <xsl:if test="$vocabulary">
            <xsl:call-template name="thesaurus">
              <xsl:with-param name="name" select="$vocabulary"/>
            </xsl:call-template>
          </xsl:if>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
    </fragment>
  </xsl:template>

  <!-- === Keyword === -->

  <xsl:template match="tds:keyword" mode="keywords">
    <gmd:keyword>
      <gco:CharacterString>
        <xsl:value-of select="."/>
      </gco:CharacterString>
    </gmd:keyword>
  </xsl:template>

  <!-- === Thesaurus === -->

  <xsl:template name="thesaurus">
    <xsl:param name="name"/>

    <gmd:thesaurusName>
      <gmd:CI_Citation>
        <gmd:title>
          <gco:CharacterString>
            <xsl:value-of select="$name"/>
          </gco:CharacterString>
        </gmd:title>
        <gmd:date>
          <gmd:CI_Date>
            <gmd:date>
              <gco:Date></gco:Date>
            </gmd:date>
            <gmd:dateType>
              <gmd:CI_DateTypeCode
                codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode"
                codeListValue=""/>
            </gmd:dateType>
          </gmd:CI_Date>
        </gmd:date>
      </gmd:CI_Citation>
    </gmd:thesaurusName>
  </xsl:template>

  <!-- === Online resources for Transfer options === -->

  <xsl:template match="tds:service" mode="transfer-options">
    <xsl:choose>
      <xsl:when test="@serviceType='Compound'">
        <xsl:apply-templates mode="transfer-options"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="catalogPath" select="replace(/root/catalogUri,'^http://[^/]*','')"/>
        <xsl:variable name="catalogHost" select="substring-before(/root/catalogUri,$catalogPath)"/>
        <xsl:variable name="baseUrl" select="@base"/>
        <xsl:variable name="urlPath" select="/root/tds:catalog/tds:dataset/@urlPath"/>

        <xsl:variable name="service-suffix">
          <xsl:if test="@serviceType='OPENDAP'">.html</xsl:if>
        </xsl:variable>

        <xsl:variable name="protocol">
          <xsl:choose>
            <xsl:when test="@serviceType='WMS'">OGC:WMS</xsl:when>
            <xsl:when test="@serviceType='WCS'">OGC:WCS</xsl:when>
            <xsl:otherwise>WWW:LINK-1.0-http--downloaddata</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <fragment uuid="{util:toString(util:randomUUID())}"
                  title="{concat($datasetName,'_service_url')}">
          <gmd:onLine>
            <gmd:CI_OnlineResource>
              <gmd:linkage>
                <gmd:URL>
                  <xsl:value-of select="concat($catalogHost,$baseUrl,$urlPath,$service-suffix)"/>
                </gmd:URL>
              </gmd:linkage>
              <gmd:protocol>
                <gco:CharacterString>
                  <xsl:value-of select="$protocol"/>
                </gco:CharacterString>
              </gmd:protocol>
              <gmd:name gco:nilReason="missing">
                <gco:CharacterString/>
              </gmd:name>
              <gmd:description>
                <gco:CharacterString>Data available via
                  <xsl:value-of select="@serviceType"/>
                </gco:CharacterString>
              </gmd:description>
            </gmd:CI_OnlineResource>
          </gmd:onLine>
        </fragment>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
