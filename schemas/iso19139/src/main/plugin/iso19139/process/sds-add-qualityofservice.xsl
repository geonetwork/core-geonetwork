<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2016 Food and Agriculture Organization of the
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
  ~ Author: Emanuele Tajariol (etj at geo-solutions dot it)
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:import href="../../iso19139/process/process-utility.xsl"/>

  <xsl:variable name="isService"
                select="boolean(/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification)"/>

  <!-- CC2 4.5.2 Quality of Service -->
  <!-- Add gmd:DQ_ConceptualConsistency elements to express Quality of service in term of:
         * Availability
         * Performance
         * Capacity
  -->
  <xsl:template
    match="/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality[gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='service']">

    <xsl:copy>
      <xsl:apply-templates select="gmd:scope"/>
      <xsl:apply-templates select="gmd:report"/>

      <xsl:if
        test="count(gmd:report/gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/gmx:Anchor[@xlink:href='http://inspire.ec.europa.eu/metadata-codelist/Criteria/availability'])=0">
        <xsl:message>Adding missing availablility QoS</xsl:message>
        <gmd:report>
          <gmd:DQ_ConceptualConsistency>
            <gmd:nameOfMeasure>
              <gmx:Anchor
                xlink:href="http://inspire.ec.europa.eu/metadata-codelist/Criteria/availability">
                availability
              </gmx:Anchor>
            </gmd:nameOfMeasure>
            <gmd:measureIdentification>
              <gmd:MD_Identifier>
                <gmd:code>
                  <gco:CharacterString>INSPIRE_service_availability</gco:CharacterString>
                </gmd:code>
              </gmd:MD_Identifier>
            </gmd:measureIdentification>
            <gmd:result>
              <gmd:DQ_QuantitativeResult>
                <gmd:valueUnit xlink:href="http://www.opengis.net/def/uom/OGC/1.0/unity"/>
                <gmd:value>
                  <gco:Record>90</gco:Record>
                </gmd:value>
              </gmd:DQ_QuantitativeResult>
            </gmd:result>
          </gmd:DQ_ConceptualConsistency>
        </gmd:report>
      </xsl:if>

      <xsl:if
        test="count(gmd:report/gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/gmx:Anchor[@xlink:href='http://inspire.ec.europa.eu/metadata-codelist/Criteria/performance'])=0">
        <xsl:message>Adding missing performance QoS</xsl:message>
        <gmd:report>
          <gmd:DQ_ConceptualConsistency>
            <gmd:nameOfMeasure>
              <gmx:Anchor
                xlink:href="http://inspire.ec.europa.eu/metadata-codelist/Criteria/performance">
                performance
              </gmx:Anchor>
            </gmd:nameOfMeasure>
            <gmd:measureIdentification>
              <gmd:MD_Identifier>
                <gmd:code>
                  <gco:CharacterString>INSPIRE_service_performance</gco:CharacterString>
                </gmd:code>
              </gmd:MD_Identifier>
            </gmd:measureIdentification>
            <gmd:result>
              <gmd:DQ_QuantitativeResult>
                <gmd:valueUnit xlink:href=" http://www.opengis.net/def/uom/SI/second"/>
                <gmd:value>
                  <gco:Record>0.5</gco:Record>
                </gmd:value>
              </gmd:DQ_QuantitativeResult>
            </gmd:result>
          </gmd:DQ_ConceptualConsistency>
        </gmd:report>
      </xsl:if>

      <xsl:if
        test="count(gmd:report/gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/gmx:Anchor[@xlink:href='http://inspire.ec.europa.eu/metadata-codelist/Criteria/capacity'])=0">
        <xsl:message>Adding missing capacity QoS</xsl:message>
        <gmd:report>
          <gmd:DQ_ConceptualConsistency>
            <gmd:nameOfMeasure>
              <gmx:Anchor
                xlink:href="http://inspire.ec.europa.eu/metadata-codelist/Criteria/capacity">
                capacity
              </gmx:Anchor>
            </gmd:nameOfMeasure>
            <gmd:measureIdentification>
              <gmd:MD_Identifier>
                <gmd:code>
                  <gco:CharacterString>INSPIRE_service_capacity</gco:CharacterString>
                </gmd:code>
              </gmd:MD_Identifier>
            </gmd:measureIdentification>
            <gmd:result>
              <gmd:DQ_QuantitativeResult>
                <gmd:valueUnit gco:nilReason="inapplicable"/>
                <gmd:value>
                  <gco:Record>50</gco:Record>
                </gmd:value>
              </gmd:DQ_QuantitativeResult>
            </gmd:result>
          </gmd:DQ_ConceptualConsistency>
        </gmd:report>
      </xsl:if>

      <xsl:apply-templates select="gmd:lineage"/>
    </xsl:copy>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
