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
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                extension-element-prefixes="saxon"
                version="2.0" exclude-result-prefixes="#all">

  <xsl:output name="default-serialize-mode" indent="no"
              omit-xml-declaration="yes"/>

  <xsl:variable name="isExtentSubtemplate"
                select="count(/root/gmd:EX_Extent) = 1"/>

  <xsl:template match="/root">
    <xsl:apply-templates select="gmd:*|*[@gco:isoType]"/>
  </xsl:template>


  <!-- On a subtemplate of type extent having a polygon
  defined, computed the bounding box from the polygon
  removing any previous bounding boxes. -->
  <xsl:template match="gmd:EX_Extent
                            [gmd:geographicElement/*/gmd:polygon/(gml:*|gml320:*)]
                            [$isExtentSubtemplate]">
    <xsl:variable name="polygons"
                  select="gmd:geographicElement/gmd:EX_BoundingPolygon/
                            gmd:polygon"/>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="gmd:description"/>
      <xsl:apply-templates select="gmd:geographicElement[gmd:EX_GeographicDescription]"/>

      <!-- Replace bounding box by the one computed from gmd:polygons -->
      <xsl:apply-templates mode="compute-bbox-for-polygon"
                           select="gmd:geographicElement/gmd:EX_BoundingPolygon/gmd:polygon"/>

      <xsl:apply-templates select="gmd:geographicElement[gmd:EX_BoundingPolygon]"/>
      <xsl:apply-templates select="gmd:temporalElement[gmd:EX_TemporalExtent]"/>
      <xsl:apply-templates select="gmd:temporalElement[gmd:EX_SpatialTemporalExtent]"/>
      <xsl:apply-templates select="gmd:verticalElement"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:EX_SpatialTemporalExtent
                            [gmd:spatialExtent/*/gmd:polygon/(gml:*|gml320:*)]
                            [$isExtentSubtemplate]">
    <xsl:variable name="polygons"
                  select="*/gmd:EX_BoundingPolygon/
                            gmd:polygon"/>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="gmd:extent"/>
      <xsl:apply-templates select="gmd:spatialExtent[gmd:EX_GeographicDescription]"/>

      <!-- Replace bounding box by the one computed from gmd:polygons -->
      <xsl:apply-templates mode="compute-bbox-for-polygon"
                           select="gmd:spatialExtent/gmd:EX_BoundingPolygon/gmd:polygon"/>

      <xsl:apply-templates select="gmd:spatialExtent[gmd:EX_BoundingPolygon]"/>
    </xsl:copy>
  </xsl:template>



  <xsl:template mode="compute-bbox-for-polygon"
                match="gmd:polygon">

    <xsl:variable name="polygonWithNs">
      <xsl:apply-templates select="./(gml:*|gml320:*)"/>
    </xsl:variable>
    <xsl:variable name="bbox"
                  select="java:geomToBbox(
                                    saxon:serialize($polygonWithNs,
                                    'default-serialize-mode'))"/>
    <xsl:if test="$bbox != ''">
      <xsl:variable name="bboxCoordinates"
                    select="tokenize($bbox, '\|')"/>

      <gmd:geographicElement>
        <gmd:EX_GeographicBoundingBox>
          <gmd:westBoundLongitude>
            <gco:Decimal><xsl:value-of select="$bboxCoordinates[1]"/></gco:Decimal>
          </gmd:westBoundLongitude>
          <gmd:eastBoundLongitude>
            <gco:Decimal><xsl:value-of select="$bboxCoordinates[3]"/></gco:Decimal>
          </gmd:eastBoundLongitude>
          <gmd:southBoundLatitude>
            <gco:Decimal><xsl:value-of select="$bboxCoordinates[2]"/></gco:Decimal>
          </gmd:southBoundLatitude>
          <gmd:northBoundLatitude>
            <gco:Decimal><xsl:value-of select="$bboxCoordinates[4]"/></gco:Decimal>
          </gmd:northBoundLatitude>
        </gmd:EX_GeographicBoundingBox>
      </gmd:geographicElement>
    </xsl:if>
  </xsl:template>




  <!-- Add gmd:id attribute to all gml elements which required one. -->
  <xsl:template match="gml:MultiSurface[not(@gml:id)]|gml:Polygon[not(@gml:id)]">
    <xsl:copy>
      <xsl:attribute name="gml:id">
        <xsl:value-of select="generate-id(.)"/>
      </xsl:attribute>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@gml:id">
    <xsl:choose>
      <xsl:when test="normalize-space(.)=''">
        <xsl:attribute name="gml:id">
          <xsl:value-of select="generate-id(.)"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="gml320:MultiSurface[not(@gml320:id)]|gml320:Polygon[not(@gml320:id)]">
    <xsl:copy>
      <xsl:attribute name="gml320:id">
        <xsl:value-of select="generate-id(.)"/>
      </xsl:attribute>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@gml320:id">
    <xsl:choose>
      <xsl:when test="normalize-space(.)=''">
        <xsl:attribute name="gml320:id">
          <xsl:value-of select="generate-id(.)"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <xsl:template name="correct_ns_prefix">
    <xsl:param name="element"/>
    <xsl:param name="prefix"/>
    <xsl:choose>
      <xsl:when test="local-name($element)=name($element) and $prefix != '' ">
        <xsl:element name="{$prefix}:{local-name($element)}">
          <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="gmd:*">
    <xsl:call-template name="correct_ns_prefix">
      <xsl:with-param name="element" select="."/>
      <xsl:with-param name="prefix" select="'gmd'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="gco:*">
    <xsl:call-template name="correct_ns_prefix">
      <xsl:with-param name="element" select="."/>
      <xsl:with-param name="prefix" select="'gco'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="gml:*">
    <xsl:call-template name="correct_ns_prefix">
      <xsl:with-param name="element" select="."/>
      <xsl:with-param name="prefix" select="'gml'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="gml320:*">
    <xsl:call-template name="correct_ns_prefix">
      <xsl:with-param name="element" select="."/>
      <xsl:with-param name="prefix" select="'gml320'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
