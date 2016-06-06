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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                exclude-result-prefixes="util xsi tds nc">

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <xsl:include href="thredds/utils.xsl"/>

  <!--
        This xslt transforms netcdf metadata to ISO19139 metadata that can be used by the thredds harvester to generate iso metadata from thredds catalogs.

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

  <!-- === Get file name from location (bit after final '/') === -->

  <xsl:variable name="name" select="replace(/root/nc:netcdf/@location,'^.*/','')"/>

  <!-- === Add fragments === -->

  <xsl:template match="/root">
    <records>
      <xsl:for-each select="nc:netcdf[1]">
        <xsl:variable name="authority" select="nc:attribute[@name='authority']/@value"/>
        <xsl:variable name="id" select="nc:attribute[@name='id']/@value"/>
        <xsl:variable name="uuid">
          <xsl:if test="$authority and $id">
            <xsl:value-of select="concat($authority,':')"/>
          </xsl:if>
          <xsl:value-of select="$id"/>
        </xsl:variable>

        <record uuid="{$uuid}">

          <!-- Metadata creation date [Mandatory] -->

          <fragment id="thredds.metadata.creation" uuid="{util:toString(util:randomUUID())}"
                    title="{concat($name,'_metadata_creation')}">
            <gmd:dateStamp>
              <gco:DateTime>
                <xsl:variable name="creationDate"
                              select="nc:attribute[@name='date_created']/@value"/>

                <xsl:choose>
                  <xsl:when test="$creationDate!=''">
                    <!-- Use resource creation date if available -->
                    <xsl:call-template name="getThreddsDateAsUTC">
                      <xsl:with-param name="sourceDate" select="$creationDate"/>
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
                    title="{concat($name,'_title')}">
            <gmd:title>
              <gco:CharacterString>
                <xsl:choose>
                  <xsl:when test="nc:attribute[@name='title']/@value != ''">
                    <xsl:value-of select="nc:attribute[@name='title']/@value"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="@title"/>
                  </xsl:otherwise>
                </xsl:choose>
              </gco:CharacterString>
            </gmd:title>
          </fragment>

          <!-- Resource dates [0..3] -->

          <replacementGroup id="thredds.resource.dates">

            <!-- creation date -->

            <xsl:variable name="creationDate" select="nc:attribute[@name='date_created']/@value"/>

            <xsl:if test="$creationDate!=''">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($name,'_resource_creation')}">
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:DateTime>
                        <xsl:call-template name="getThreddsDateAsUTC">
                          <xsl:with-param name="sourceDate" select="$creationDate"/>
                        </xsl:call-template>
                      </gco:DateTime>
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

            <xsl:variable name="modifiedDate" select="nc:attribute[@name='date_modified']/@value"/>

            <xsl:if test="$modifiedDate!=''">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($name,'_resource_revision')}">
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:DateTime>
                        <xsl:call-template name="getThreddsDateAsUTC">
                          <xsl:with-param name="sourceDate" select="$modifiedDate"/>
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

            <!-- Publication date [0..1] -->

            <xsl:variable name="issuedDate" select="nc:attribute[@name='date_issued']/@value"/>

            <xsl:if test="$issuedDate!=''">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($name,'_resource_published')}">
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:DateTime>
                        <xsl:call-template name="getThreddsDateAsUTC">
                          <xsl:with-param name="sourceDate" select="$issuedDate"/>
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

            <xsl:variable name="publisher_name"
                          select="nc:attribute[@name='publisher_name']/@value"/>
            <xsl:variable name="publisher_url" select="nc:attribute[@name='publisher_url']/@value"/>
            <xsl:variable name="publisher_email"
                          select="nc:attribute[@name='publisher_email']/@value"/>

            <xsl:if test="$publisher_name!=''">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($name,'_publisher')}">
                <gmd:citedResponsibleParty>
                  <gmd:CI_ResponsibleParty>
                    <gmd:individualName>
                      <gco:CharacterString>
                        <xsl:value-of select="$publisher_name"/>
                      </gco:CharacterString>
                    </gmd:individualName>
                    <xsl:if test="$publisher_url!='' or $publisher_email!=''">
                      <gmd:contactInfo>
                        <gmd:CI_Contact>
                          <xsl:if test="$publisher_email!=''">
                            <gmd:address>
                              <gmd:CI_Address>
                                <gmd:electronicMailAddress>
                                  <gco:CharacterString>
                                    <xsl:value-of select="$publisher_email"/>
                                  </gco:CharacterString>
                                </gmd:electronicMailAddress>
                              </gmd:CI_Address>
                            </gmd:address>
                          </xsl:if>
                          <xsl:if test="$publisher_url!=''">
                            <gmd:onlineResource>
                              <gmd:CI_OnlineResource>
                                <gmd:linkage>
                                  <gmd:URL>
                                    <xsl:value-of select="$publisher_url"/>
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
            </xsl:if>

            <!-- Originators [0..N] -->

            <xsl:variable name="creator_name" select="nc:attribute[@name='creator_name']/@value"/>
            <xsl:variable name="creator_url" select="nc:attribute[@name='creator_url']/@value"/>
            <xsl:variable name="creator_email" select="nc:attribute[@name='creator_email']/@value"/>
            <xsl:variable name="institution" select="nc:attribute[@name='institution']/@value"/>

            <xsl:if test="$creator_name!='' or $institution!=''">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($name,'_originator')}">
                <gmd:citedResponsibleParty>
                  <gmd:CI_ResponsibleParty>
                    <xsl:if test="$creator_name!=''">
                      <gmd:individualName>
                        <gco:CharacterString>
                          <xsl:value-of select="$creator_name"/>
                        </gco:CharacterString>
                      </gmd:individualName>
                    </xsl:if>
                    <xsl:if test="$institution!=''">
                      <gmd:organisationName>
                        <gco:CharacterString>
                          <xsl:value-of select="$institution"/>
                        </gco:CharacterString>
                      </gmd:organisationName>
                    </xsl:if>
                    <xsl:if test="$creator_url!='' or $creator_email!=''">
                      <gmd:contactInfo>
                        <gmd:CI_Contact>
                          <xsl:if test="$creator_email!=''">
                            <gmd:address>
                              <gmd:CI_Address>
                                <gmd:electronicMailAddress>
                                  <gco:CharacterString>
                                    <xsl:value-of select="$creator_email"/>
                                  </gco:CharacterString>
                                </gmd:electronicMailAddress>
                              </gmd:CI_Address>
                            </gmd:address>
                          </xsl:if>
                          <xsl:if test="$creator_url!=''">
                            <gmd:onlineResource>
                              <gmd:CI_OnlineResource>
                                <gmd:linkage>
                                  <gmd:URL>
                                    <xsl:value-of select="$creator_url"/>
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
            </xsl:if>

            <!-- contributors [0..1] -->

            <xsl:variable name="contributor_name"
                          select="nc:attribute[@name='contributor_name']/@value"/>
            <xsl:variable name="contributor_role"
                          select="nc:attribute[@name='contributor_role']/@value"/>

            <xsl:if test="$contributor_name!=''">
              <fragment uuid="{util:toString(util:randomUUID())}" title="{concat($name,'_author')}">
                <gmd:citedResponsibleParty>
                  <gmd:CI_ResponsibleParty>
                    <gmd:individualName>
                      <gco:CharacterString>
                        <xsl:value-of select="$contributor_name"/>
                      </gco:CharacterString>
                    </gmd:individualName>
                    <gmd:role>
                      <xsl:choose>
                        <xsl:when test="$contributor_role='PI'">
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
            </xsl:if>
          </replacementGroup>

          <!-- Abstract [1]-->

          <xsl:variable name="summary" select="nc:attribute[@name='summary']/@value"/>

          <fragment id="thredds.abstract" uuid="{util:toString(util:randomUUID())}"
                    title="{concat($name,'_abstract')}">
            <gmd:abstract>
              <gco:CharacterString>
                <xsl:choose>
                  <xsl:when test="$summary!=''">
                    <xsl:value-of select="$summary"/>
                  </xsl:when>
                  <xsl:otherwise>NetCDF dataset</xsl:otherwise>
                </xsl:choose>
              </gco:CharacterString>
            </gmd:abstract>
          </fragment>

          <!-- Credit [0..1] -->

          <xsl:variable name="acknowledgment" select="nc:attribute[@name='acknowledgment']/@value"/>

          <replacementGroup id="thredds.credit">
            <xsl:if test="$acknowledgment!=''">
              <fragment uuid="{util:toString(util:randomUUID())}" title="{concat($name,'_credit')}">
                <gmd:credit>
                  <gco:CharacterString>
                    <xsl:value-of select="$acknowledgment"/>
                  </gco:CharacterString>
                </gmd:credit>
              </fragment>
            </xsl:if>
          </replacementGroup>

          <!-- Keywords [0..N] -->

          <xsl:variable name="keywords" select="nc:attribute[@name='keywords']/@value"/>
          <xsl:variable name="keywords_vocabulary"
                        select="nc:attribute[@name='keywords_vocabulary']/@value"/>

          <!-- Keywords [0..N] -->

          <replacementGroup id="thredds.keywords">
            <xsl:if test="$keywords!=''">
              <xsl:call-template name="descriptive-keywords">
                <xsl:with-param name="vocabulary" select="$keywords_vocabulary"/>
                <xsl:with-param name="keywords" select="tokenize($keywords,',')"/>
              </xsl:call-template>
            </xsl:if>
          </replacementGroup>

          <!-- Use constraints [0..1] -->

          <xsl:variable name="license" select="nc:attribute[@name='license']/@value"/>

          <replacementGroup id="thredds.use.constraints">
            <xsl:if test="$license!=''">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($name,'_use_constraints')}">
                <gmd:resourceConstraints>
                  <gmd:MD_LegalConstraints>
                    <gmd:useConstraints>
                      <gmd:MD_RestrictionCode
                        codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_RestrictionCode"
                        codeListValue="otherRestrictions"/>
                    </gmd:useConstraints>
                    <gmd:otherConstraints>
                      <gco:CharacterString>
                        <xsl:value-of select="$license"/>
                      </gco:CharacterString>
                    </gmd:otherConstraints>
                  </gmd:MD_LegalConstraints>
                </gmd:resourceConstraints>
              </fragment>
            </xsl:if>
          </replacementGroup>

          <!-- Aggregation info (Project) [0-1] -->

          <xsl:variable name="project" select="nc:attribute[@name='project']/@value"/>

          <replacementGroup id="thredds.project">
            <xsl:if test="$project!=''">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($name,'_project')}">
                <gmd:aggregationInfo>
                  <gmd:MD_AggregateInformation>
                    <gmd:aggregateDataSetName>
                      <gmd:CI_Citation>
                        <gmd:title>
                          <gco:CharacterString>
                            <xsl:value-of select="$project"/>
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
            </xsl:if>
          </replacementGroup>

          <!-- Extent [Mandatory for dataset] -->

          <fragment id="thredds.extent" uuid="{util:toString(util:randomUUID())}"
                    title="{concat($name,'_extent')}">
            <gmd:extent>
              <gmd:EX_Extent>

                <!-- geographic extent [0..1] -->

                <xsl:variable name="geospatial_lat_min"
                              select="nc:attribute[@name='geospatial_lat_min']/@value"/>
                <xsl:variable name="geospatial_lat_max"
                              select="nc:attribute[@name='geospatial_lat_max']/@value"/>
                <xsl:variable name="geospatial_lon_min"
                              select="nc:attribute[@name='geospatial_lon_min']/@value"/>
                <xsl:variable name="geospatial_lon_max"
                              select="nc:attribute[@name='geospatial_lon_max']/@value"/>

                <xsl:if
                  test="$geospatial_lat_min!='' or $geospatial_lat_max!='' or $geospatial_lon_min!='' or $geospatial_lon_max!=''">
                  <!-- TODO: check lat/lon units -->
                  <gmd:geographicElement>
                    <gmd:EX_GeographicBoundingBox>
                      <gmd:westBoundLongitude>
                        <gco:Decimal>
                          <xsl:value-of select="$geospatial_lon_min"/>
                        </gco:Decimal>
                      </gmd:westBoundLongitude>
                      <gmd:eastBoundLongitude>
                        <gco:Decimal>
                          <xsl:value-of select="$geospatial_lon_max"/>
                        </gco:Decimal>
                      </gmd:eastBoundLongitude>
                      <gmd:southBoundLatitude>
                        <gco:Decimal>
                          <xsl:value-of select="$geospatial_lat_min"/>
                        </gco:Decimal>
                      </gmd:southBoundLatitude>
                      <gmd:northBoundLatitude>
                        <gco:Decimal>
                          <xsl:value-of select="$geospatial_lat_max"/>
                        </gco:Decimal>
                      </gmd:northBoundLatitude>
                    </gmd:EX_GeographicBoundingBox>
                  </gmd:geographicElement>
                </xsl:if>

                <!-- temporal extent [0..1] -->

                <xsl:variable name="time_coverage_start"
                              select="nc:attribute[@name='time_coverage_start']/@value"/>
                <xsl:variable name="time_coverage_end"
                              select="nc:attribute[@name='time_coverage_end']/@value"/>
                <xsl:variable name="time_coverage_duration"
                              select="nc:attribute[@name='time_coverage_duration']/@value"/>
                <xsl:variable name="time_coverage_resolution"
                              select="nc:attribute[@name='time_coverage_resolution']/@value"/>

                <xsl:if test="$time_coverage_start!='' or $time_coverage_end!=''">
                  <xsl:variable name="dateRange">
                    <xsl:call-template name="resolveThreddsDateRange">
                      <xsl:with-param name="start" select="$time_coverage_start"/>
                      <xsl:with-param name="end" select="$time_coverage_end"/>
                      <xsl:with-param name="duration" select="$time_coverage_duration"/>
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
                </xsl:if>

                <!-- vertical extent [0-1] -->

                <xsl:variable name="geospatial_vertical_min"
                              select="nc:attribute[@name='geospatial_vertical_min']/@value"/>
                <xsl:variable name="geospatial_vertical_max"
                              select="nc:attribute[@name='geospatial_vertical_max']/@value"/>

                <xsl:if test="$geospatial_vertical_min!='' or $geospatial_vertical_max!=''">
                  <gmd:verticalElement>
                    <gmd:EX_VerticalExtent>
                      <gmd:minimumValue>
                        <gco:Real>
                          <xsl:value-of select="$geospatial_vertical_min"/>
                        </gco:Real>
                      </gmd:minimumValue>
                      <gmd:maximumValue>
                        <gco:Real>
                          <xsl:value-of select="$geospatial_vertical_max"/>
                        </gco:Real>
                      </gmd:maximumValue>
                    </gmd:EX_VerticalExtent>
                  </gmd:verticalElement>
                </xsl:if>

              </gmd:EX_Extent>
            </gmd:extent>
          </fragment>

          <!-- Supplemental information [0..1] -->

          <xsl:variable name="comment" select="nc:attribute[@name='comment']/@value"/>

          <replacementGroup id="thredds.supplemental">
            <xsl:if test="$comment!=''">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($name,'_supplemental')}">
                <gmd:supplementalInformation>
                  <gco:CharacterString>
                    <xsl:value-of select="$comment"/>
                  </gco:CharacterString>
                </gmd:supplementalInformation>
              </fragment>
            </xsl:if>
          </replacementGroup>

          <!-- Online resources [2..N] -->

          <replacementGroup id="thredds.online.resources">
            <xsl:variable name="catalogUrl" select="replace(/root/catalogUri,'.xml','.html')"/>

            <!-- Links to services -->

            <xsl:for-each select="//tds:serviceName|//@serviceName">
              <xsl:variable name="serviceName" select="string(.)"/>
              <xsl:apply-templates select="/root/tds:catalog//tds:service[@name=$serviceName]"
                                   mode="transfer-options"/>
            </xsl:for-each>

            <!-- Link to thredds catalog for dataset -->

            <fragment uuid="{util:toString(util:randomUUID())}"
                      title="{concat($name,'_dataset_link')}">
              <gmd:onLine>
                <gmd:CI_OnlineResource>
                  <gmd:linkage>
                    <gmd:URL>
                      <xsl:value-of
                        select="concat($catalogUrl,'?dataset=',/root/tds:catalog/tds:dataset/@ID)"/>
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
                      title="{concat($name,'_dataset_link')}">
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
                    <gco:CharacterString>THREDDS CATALOG</gco:CharacterString>
                  </gmd:description>
                </gmd:CI_OnlineResource>
              </gmd:onLine>
            </fragment>

          </replacementGroup>

          <!-- Lineage -->

          <xsl:variable name="history" select="nc:attribute[@name='history']/@value"/>

          <replacementGroup id="thredds.data.quality">
            <xsl:if test="$history!=''">
              <fragment uuid="{util:toString(util:randomUUID())}"
                        title="{concat($name,'_history')}">
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
                            <xsl:value-of select="$history"/>
                          </gco:CharacterString>
                        </gmd:statement>
                      </gmd:LI_Lineage>
                    </gmd:lineage>
                  </gmd:DQ_DataQuality>
                </gmd:dataQualityInfo>
              </fragment>
            </xsl:if>

            <xsl:variable name="processing_level"
                          select="nc:attribute[@name='processing_level']/@value"/>

            <xsl:if test="$processing_level!=''">
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
                          <xsl:value-of select="$processing_level"/>
                        </gco:CharacterString>
                      </gmd:statement>
                    </gmd:LI_Lineage>
                  </gmd:lineage>
                </gmd:DQ_DataQuality>
              </gmd:dataQualityInfo>
            </xsl:if>
          </replacementGroup>

        </record>
      </xsl:for-each>
    </records>
  </xsl:template>

  <!-- === Descriptive Keywords === -->

  <xsl:template name="descriptive-keywords">
    <xsl:param name="vocabulary"/>
    <xsl:param name="keywords"/>

    <fragment uuid="{util:toString(util:randomUUID())}" title="{concat($name,'_keywords')}">
      <gmd:descriptiveKeywords>
        <gmd:MD_Keywords>
          <xsl:for-each select="$keywords">
            <gmd:keyword>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </gmd:keyword>
          </xsl:for-each>
          <xsl:if test="$vocabulary!=''">
            <xsl:call-template name="thesaurus">
              <xsl:with-param name="name" select="$vocabulary"/>
            </xsl:call-template>
          </xsl:if>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
    </fragment>
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

        <fragment uuid="{util:toString(util:randomUUID())}" title="{concat($name,'_service_url')}">
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
