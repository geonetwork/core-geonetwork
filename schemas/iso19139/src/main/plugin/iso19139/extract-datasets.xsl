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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="2.0">
  <xsl:template match="gmd:MD_Metadata">
    <datasets>
      <xsl:for-each select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:operatesOn">
        <dataset>
          <xsl:choose>
            <xsl:when test="@uuidref!=''">
              <xsl:value-of select="@uuidref"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- works only for csw requests for now -->
              <xsl:value-of select="tokenize(tokenize(string(@xlink:href),'id=')[2],'&amp;')[1]"/>
            </xsl:otherwise>
          </xsl:choose>
        </dataset>
      </xsl:for-each>
    </datasets>
  </xsl:template>
</xsl:stylesheet>