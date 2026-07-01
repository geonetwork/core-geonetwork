<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
Stylesheet used to add a publication date of a metadata record.
-->
<xsl:stylesheet xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- The publication date  -->
  <xsl:param name="publicationDate"/>

  <xsl:template match="gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="gmd:title" />
      <xsl:apply-templates select="gmd:alternateTitle" />

      <xsl:choose>
        <xsl:when test="string($publicationDate)">
          <gmd:date>
            <gmd:CI_Date>
              <gmd:date>
                <gco:DateTime>
                  <xsl:value-of select="$publicationDate"/>
                </gco:DateTime>
              </gmd:date>
              <gmd:dateType>
                <gmd:CI_DateTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode"
                                     codeListValue="publication">
                </gmd:CI_DateTypeCode>
              </gmd:dateType>
            </gmd:CI_Date>
          </gmd:date>

          <xsl:apply-templates select="gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue != 'publication']" />
        </xsl:when>

        <xsl:otherwise>
          <xsl:apply-templates select="gmd:date" />
        </xsl:otherwise>
      </xsl:choose>


      <xsl:apply-templates select="gmd:edition" />
      <xsl:apply-templates select="gmd:editionDate" />
      <xsl:apply-templates select="gmd:identifier" />
      <xsl:apply-templates select="gmd:citedResponsibleParty" />
      <xsl:apply-templates select="gmd:presentationForm" />
      <xsl:apply-templates select="gmd:series" />
      <xsl:apply-templates select="gmd:otherCitationDetails" />
      <xsl:apply-templates select="gmd:collectiveTitle" />
      <xsl:apply-templates select="gmd:ISBN" />
    </xsl:copy>
  </xsl:template>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template
    match="geonet:*"
    priority="2"/>
</xsl:stylesheet>
