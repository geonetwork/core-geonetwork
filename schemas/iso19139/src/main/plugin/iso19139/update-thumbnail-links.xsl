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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                version="1.0">

  <!-- ================================================================= -->

  <xsl:template match="/root">
    <xsl:apply-templates select="gmd:MD_Metadata|*[contains(@gco:isoType, 'MD_Metadata')]"/>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="gmd:graphicOverview">
    <xsl:variable name="metadataId"
                  select="/root/*/gmd:fileIdentifier/gco:CharacterString/text()"/>

    <xsl:variable name="fileName"
                  select="gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString/text()"/>

    <xsl:choose>
      <!-- Thumbnail points to GeoNetwork 3.2+ node -->
      <xsl:when test="contains($fileName, '/attachments/')">
        <xsl:choose>
          <xsl:when test="starts-with($fileName, /root/env/url)">
            <!-- same GN node -->
            <xsl:copy>
              <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
          </xsl:when>
          <xsl:otherwise>
            <!-- other GN node: update the url -->
            <gmd:graphicOverview>
              <gmd:MD_BrowseGraphic>
                <gmd:fileName>
                  <gco:CharacterString>
                    <xsl:value-of select="concat(
              /root/env/url,
              'api/records/', $metadataId, '/attachments/',
              substring-after($fileName, '/attachments/'))"/>
                  </gco:CharacterString>
                </gmd:fileName>

                <xsl:apply-templates select="gmd:MD_BrowseGraphic/gmd:fileDescription" />
                <xsl:apply-templates select="gmd:MD_BrowseGraphic/gmd:fileType" />
              </gmd:MD_BrowseGraphic>
            </gmd:graphicOverview>

          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <!-- Thumbnail only contains the file name: imported from Geonetwork export previous to 3.2 -->
      <xsl:when test="not(starts-with($fileName, 'http'))">
        <gmd:graphicOverview>
          <gmd:MD_BrowseGraphic>
            <gmd:fileName>
              <gco:CharacterString>
                <xsl:value-of select="concat(
              /root/env/url,
              'api/records/', $metadataId, '/attachments/',
              $fileName)"/>
              </gco:CharacterString>
            </gmd:fileName>

            <xsl:apply-templates select="gmd:MD_BrowseGraphic/gmd:fileDescription" />
            <xsl:apply-templates select="gmd:MD_BrowseGraphic/gmd:fileType" />
          </gmd:MD_BrowseGraphic>
        </gmd:graphicOverview>

      </xsl:when>

      <!-- Other cases: copy as defined, for example pointing to external websites -->
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

</xsl:stylesheet>
