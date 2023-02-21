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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                extension-element-prefixes="saxon"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                xmlns:mime="java:org.fao.geonet.util.MimeTypeFinder"
                xmlns:gn="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all">

  <xsl:output name="default-serialize-mode" indent="no"
              omit-xml-declaration="yes"/>

  <xsl:variable name="isExtentSubtemplate"
                select="count(/root/gex:EX_Extent) = 1"/>

  <xsl:template match="/root">
    <xsl:apply-templates select="gex:*|mrs:*|mdb:*|cit:*|dqm:*|cit:*|mcc:*|mrc:*|mrd:*|mco:*|mdq:*"/>
  </xsl:template>


  <!-- On a subtemplate of type extent having a polygon
  defined, computed the bounding box from the polygon
  removing any previous bounding boxes. -->
  <xsl:template match="gex:EX_Extent
                            [gex:geographicElement/*/gex:polygon/gml:*]
                            [$isExtentSubtemplate]">
    <xsl:variable name="polygons"
                  select="gex:geographicElement/gex:EX_BoundingPolygon/
                            gex:polygon"/>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="gex:description"/>
      <xsl:apply-templates select="gex:geographicElement[gex:EX_GeographicDescription]"/>

      <!-- Replace bounding box by the one computed from gex:polygons -->
      <xsl:apply-templates mode="compute-bbox-for-polygon"
                           select="gex:geographicElement/gex:EX_BoundingPolygon/gex:polygon"/>

      <xsl:apply-templates select="gex:geographicElement[gex:EX_BoundingPolygon]"/>
      <xsl:apply-templates select="gex:temporalElement[gex:EX_TemporalExtent]"/>
      <xsl:apply-templates select="gex:temporalElement[gex:EX_SpatialTemporalExtent]"/>
      <xsl:apply-templates select="gex:verticalElement"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gex:EX_SpatialTemporalExtent
                            [gex:spatialExtent/*/gex:polygon/gml:*]
                            [$isExtentSubtemplate]">
    <xsl:variable name="polygons"
                  select="*/gex:EX_BoundingPolygon/
                            gex:polygon"/>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="gex:extent"/>
      <xsl:apply-templates select="gex:spatialExtent[gex:EX_GeographicDescription]"/>

      <!-- Replace bounding box by the one computed from gex:polygons -->
      <xsl:apply-templates mode="compute-bbox-for-polygon"
                           select="gex:spatialExtent/gex:EX_BoundingPolygon/gex:polygon"/>

      <xsl:apply-templates select="gex:spatialExtent[gex:EX_BoundingPolygon]"/>
    </xsl:copy>
  </xsl:template>



  <xsl:template mode="compute-bbox-for-polygon"
                match="gex:polygon">
    <xsl:variable name="bbox"
                  select="java:geomToBbox(saxon:serialize(./gml:*, 'default-serialize-mode'))"/>
    <xsl:if test="$bbox != ''">
      <xsl:variable name="bboxCoordinates"
                    select="tokenize($bbox, '\|')"/>

      <gex:geographicElement>
        <gex:EX_GeographicBoundingBox>
          <gex:westBoundLongitude>
            <gco:Decimal><xsl:value-of select="$bboxCoordinates[1]"/></gco:Decimal>
          </gex:westBoundLongitude>
          <gex:eastBoundLongitude>
            <gco:Decimal><xsl:value-of select="$bboxCoordinates[3]"/></gco:Decimal>
          </gex:eastBoundLongitude>
          <gex:southBoundLatitude>
            <gco:Decimal><xsl:value-of select="$bboxCoordinates[2]"/></gco:Decimal>
          </gex:southBoundLatitude>
          <gex:northBoundLatitude>
            <gco:Decimal><xsl:value-of select="$bboxCoordinates[4]"/></gco:Decimal>
          </gex:northBoundLatitude>
        </gex:EX_GeographicBoundingBox>
      </gex:geographicElement>
    </xsl:if>
  </xsl:template>


  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
