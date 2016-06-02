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
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:java="java:org.fao.geonet.util.XslUtil" version="2.0">

  <!--
        Template for INSPIRE tab
        http://inspire.jrc.ec.europa.eu/reports/ImplementingRules/metadata/MD_IR_and_ISO_20090218.pdf
    -->
  <xsl:template name="inspiretabs">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="dataset"/>

    <xsl:for-each
      select="gmd:identificationInfo/*[namespace-uri() != 'http://www.fao.org/geonetwork']">

      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title"
                        select="/root/gui/strings/inspireSection/identification/title"/>
        <xsl:with-param name="id"
                        select="generate-id(/root/gui/strings/inspireSection/identification/title)"/>
        <xsl:with-param name="content">

          <xsl:apply-templates mode="elementEP"
                               select="gmd:citation/gmd:CI_Citation/gmd:title|
                                        gmd:CI_Citation/geonet:child[string(@name)='title']">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>

          <xsl:apply-templates mode="elementEP"
                               select="
            gmd:abstract|
            geonet:child[string(@name)='abstract']">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>

          <xsl:apply-templates mode="elementEP"
                               select="
            ../../gmd:hierarchyLevel|
            ../../geonet:child[string(@name)='hierarchyLevel']
            ">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>


          <!-- Service info-->
          <xsl:if test="not($dataset)">
            <xsl:apply-templates mode="elementEP"
                                 select="
              srv:couplingType|
              geonet:child[string(@name)='couplingType']">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>


            <xsl:call-template name="complexElementGuiWrapper">
              <xsl:with-param name="title"
                              select="/root/gui/iso19139/element[@name='srv:operatesOn']/label"/>
              <xsl:with-param name="content">
                <xsl:apply-templates mode="elementEP"
                                     select="
                  srv:operatesOn|
                  geonet:child[string(@name)='operatesOn']">
                  <xsl:with-param name="schema" select="$schema"/>
                  <xsl:with-param name="edit" select="$edit"/>
                </xsl:apply-templates>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:if>


          <!-- Distribution section -->

          <xsl:apply-templates mode="complexElement"
                               select="
            ../../gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>
          <xsl:if test="not(../../gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat)">
            <xsl:apply-templates mode="elementEP"
                                 select="
              ../../gmd:distributionInfo/gmd:MD_Distribution/geonet:child[string(@name)='distributionFormat']">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:if>

          <xsl:apply-templates mode="complexElement"
                               select="
            ../../gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>
          <xsl:if test="not(../../gmd:distributionInfo)">
            <xsl:apply-templates mode="elementEP"
                                 select="
              ../../geonet:child[string(@name)='distributionInfo']">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:if>
          <xsl:if
            test="not(../../gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine)">
            <xsl:apply-templates mode="elementEP"
                                 select="
              ../../gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/geonet:child[string(@name)='onLine']">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:if>


          <!-- Resource id -->
          <xsl:apply-templates mode="complexElement"
                               select="
            gmd:citation/gmd:CI_Citation/gmd:identifier
            ">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>
          <xsl:if test="not(gmd:citation/gmd:CI_Citation/gmd:identifier)">
            <xsl:apply-templates mode="elementEP"
                                 select="
              gmd:citation/gmd:CI_Citation/geonet:child[string(@name)='identifier']
              ">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:if>

          <!-- Language -->
          <xsl:apply-templates mode="elementEP"
                               select="
            gmd:language
            ">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>

          <xsl:if test="not(gmd:language)">
            <xsl:apply-templates mode="elementEP"
                                 select="
            geonet:child[string(@name)='language']
            ">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:if>

          <xsl:apply-templates mode="elementEP"
                               select="
            gmd:characterSet
            ">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>

          <xsl:if test="not(gmd:characterSet)">
            <xsl:apply-templates mode="elementEP"
                                 select="
              geonet:child[string(@name)='characterSet']
              ">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:if>
        </xsl:with-param>
      </xsl:call-template>


      <!--  Classification of spatial data and services -->
      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title"
                        select="/root/gui/strings/inspireSection/classification/title"/>
        <xsl:with-param name="id"
                        select="generate-id(/root/gui/strings/inspireSection/classification/title)"/>
        <xsl:with-param name="content">

          <xsl:apply-templates mode="complexElement"
                               select="
            gmd:topicCategory
            ">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>

          <xsl:apply-templates mode="elementEP"
                               select="
            geonet:child[string(@name)='topicCategory']
            ">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>

          <!-- Service info-->
          <xsl:apply-templates mode="complexElement"
                               select="
              srv:serviceType|
              geonet:child[string(@name)='serviceType']">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>


        </xsl:with-param>
      </xsl:call-template>


      <!--  Keywords -->
      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title"
                        select="/root/gui/strings/inspireSection/keywords/title"/>
        <xsl:with-param name="id"
                        select="generate-id(/root/gui/strings/inspireSection/keywords/title)"/>
        <xsl:with-param name="content">

          <xsl:apply-templates mode="elementEP"
                               select="
            gmd:descriptiveKeywords
            ">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>
          <xsl:if test="not(gmd:descriptiveKeywords)">
            <xsl:apply-templates mode="elementEP"
                                 select="
              geonet:child[string(@name)='descriptiveKeywords']
              ">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:if>

        </xsl:with-param>
      </xsl:call-template>

      <!-- Extent information -->
      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title"
                        select="/root/gui/strings/inspireSection/geoloc/title"/>
        <xsl:with-param name="id"
                        select="generate-id(/root/gui/strings/inspireSection/geoloc/title)"/>
        <xsl:with-param name="content">

          <xsl:for-each select="*:extent/gmd:EX_Extent">
            <xsl:apply-templates mode="elementEP"
                                 select="
              gmd:description
              ">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>

            <xsl:apply-templates mode="complexElement"
                                 select="
              gmd:geographicElement|gmd:verticalElement
              ">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>

            <xsl:apply-templates mode="elementEP"
                                 select="
              geonet:child[string(@name)='geographicElement']">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:for-each>

          <xsl:if test="not(*:extent)">
            <xsl:apply-templates mode="elementEP"
                                 select="
              geonet:child[string(@name)='extent']">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:if>
        </xsl:with-param>
      </xsl:call-template>

      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title"
                        select="/root/gui/strings/inspireSection/temporal/title"/>
        <xsl:with-param name="id"
                        select="generate-id(/root/gui/strings/inspireSection/temporal/title)"/>
        <xsl:with-param name="content">

          <xsl:apply-templates mode="complexElement"
                               select="
            gmd:citation/gmd:CI_Citation/gmd:date|
            gmd:citation/geonet:child[string(@name)='date']
            ">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>
          <!-- temporal extent -->

          <xsl:for-each select="*:extent/gmd:EX_Extent">
            <xsl:apply-templates mode="complexElement"
                                 select="
              gmd:temporalElement
              ">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>


            <xsl:apply-templates mode="elementEP"
                                 select="
              geonet:child[string(@name)='temporalElement']">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:for-each>

        </xsl:with-param>
      </xsl:call-template>


      <!-- Reference system info -->
      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title"
                        select="/root/gui/schemas/iso19139/labels/element[@name='gmd:referenceSystemInfo']/label"/>
        <xsl:with-param name="id"
                        select="generate-id(/root/gui/schemas/iso19139/labels/element[@name='gmd:referenceSystemInfo']/label)"/>
        <xsl:with-param name="content">

          <xsl:for-each select="../../gmd:referenceSystemInfo/gmd:MD_ReferenceSystem">
            <xsl:apply-templates mode="elementEP" select="gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code
              |gmd:referenceSystemIdentifier/gmd:RS_Identifier/geonet:child[string(@name)='code']
              ">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:for-each>

        </xsl:with-param>
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit" select="$edit"/>
      </xsl:call-template>


      <!-- Quality and validity  -->
      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title"
                        select="/root/gui/strings/inspireSection/quality/title"/>
        <xsl:with-param name="id"
                        select="generate-id(/root/gui/strings/inspireSection/quality/title)"/>
        <xsl:with-param name="content">


          <!-- Display lineage only for datasets -->
          <xsl:if test="$dataset">
            <xsl:apply-templates mode="complexElement"
                                 select="../../gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>

            <xsl:if
              test="not(../../gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage)">
              <xsl:apply-templates mode="elementEP"
                                   select="../../gmd:dataQualityInfo/gmd:DQ_DataQuality/geonet:child[string(@name)='lineage']">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit" select="$edit"/>
              </xsl:apply-templates>
            </xsl:if>
          </xsl:if>


          <!-- Resolution information -->
          <xsl:apply-templates mode="complexElement"
                               select="
            gmd:spatialResolution">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>
          <xsl:if test="not(gmd:spatialResolution)">
            <xsl:apply-templates mode="elementEP"
                                 select="
              geonet:child[string(@name)='spatialResolution']">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:if>
        </xsl:with-param>
      </xsl:call-template>


      <!-- Conformity  -->
      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title"
                        select="/root/gui/strings/inspireSection/conformity/title"/>
        <xsl:with-param name="id"
                        select="generate-id(/root/gui/strings/inspireSection/conformity/title)"/>
        <xsl:with-param name="content">

          <xsl:apply-templates mode="complexElement"
                               select="../../gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report[gmd:DQ_DomainConsistency]">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>

          <xsl:if
            test="not(../../gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult)">
            <xsl:apply-templates mode="elementEP"
                                 select="
              ../../gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/geonet:child[string(@name)='result']
              ">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:if>
        </xsl:with-param>
      </xsl:call-template>

      <!-- Constraint  -->
      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title"
                        select="/root/gui/strings/inspireSection/constraint/title"/>
        <xsl:with-param name="id"
                        select="generate-id(/root/gui/strings/inspireSection/constraint/title)"/>
        <xsl:with-param name="content">
          <xsl:variable name="schematitle"
                        select="string(/root/gui/iso19139/element[@name='gmd:resourceConstraints']/label)"/>
          <xsl:call-template name="complexElementGuiWrapper">
            <xsl:with-param name="title" select="$schematitle"/>
            <xsl:with-param name="content">
              <xsl:apply-templates mode="complexElement"
                                   select="
                gmd:resourceConstraints">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit" select="$edit"/>
              </xsl:apply-templates>

              <xsl:apply-templates mode="elementEP"
                                   select="
                geonet:child[string(@name)='resourceConstraints']
                ">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit" select="$edit"/>
              </xsl:apply-templates>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>

      <!-- Organisation  -->
      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title"
                        select="/root/gui/strings/inspireSection/org/title"/>
        <xsl:with-param name="id"
                        select="generate-id(/root/gui/strings/inspireSection/org/title)"/>
        <xsl:with-param name="content">

          <xsl:apply-templates mode="elementEP"
                               select="
            gmd:pointOfContact
            ">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
          </xsl:apply-templates>
          <xsl:if test="not(gmd:pointOfContact)">
            <xsl:apply-templates mode="elementEP"
                                 select="
              geonet:child[string(@name)='pointOfContact']
              ">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:if>
        </xsl:with-param>
      </xsl:call-template>

      <!-- Metadata  -->
      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title"
                        select="/root/gui/strings/inspireSection/metadata/title"/>
        <xsl:with-param name="id"
                        select="generate-id(/root/gui/strings/inspireSection/metadata/title)"/>
        <xsl:with-param name="content">

          <xsl:call-template name="complexElementGuiWrapper">
            <xsl:with-param name="title"
                            select="string(/root/gui/*[name(.)=$schema]/element[@name='gmd:MD_Metadata']/label)"/>
            <xsl:with-param name="content">
              <xsl:apply-templates mode="elementEP"
                                   select="
                  ../../gmd:fileIdentifier|
                  ../../gmd:language|
                  ../../gmd:metadataStandardName|
                  ../../gmd:metadataStandardVersion|
                  ../../geonet:child[string(@name)='language']|
                  ../../gmd:characterSet|
                  ../../geonet:child[string(@name)='characterSet']|
                  ../../gmd:contact|
                  ../../geonet:child[string(@name)='contact']|
                  ../../gmd:dateStamp|
                  ../../geonet:child[string(@name)='dateStamp']
                  ">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit" select="$edit"/>
              </xsl:apply-templates>
            </xsl:with-param>
          </xsl:call-template>


        </xsl:with-param>
      </xsl:call-template>
    </xsl:for-each>

  </xsl:template>
</xsl:stylesheet>
