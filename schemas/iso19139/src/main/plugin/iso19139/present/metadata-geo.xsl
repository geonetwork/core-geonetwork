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

<xsl:stylesheet xmlns:java="java:org.fao.geonet.util.XslUtil" xmlns:math="http://exslt.org/math"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:exslt="http://exslt.org/common" version="1.0"
                exclude-result-prefixes="gmd gco gml gts srv gmx xlink exslt geonet java math">

  <xsl:template mode="iso19139" match="gmd:EX_BoundingPolygon" priority="20">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:apply-templates mode="iso19139" select="gmd:extentTypeCode">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:apply-templates>

    <xsl:apply-templates mode="elementEP" select="geonet:child">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:apply-templates>

    <xsl:apply-templates mode="iso19139" select="gmd:polygon">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Create an hidden input field which store
  the GML geometry for editing on the client side.

  The input is prefixed by "_X" in order to process
  XML in DataManager.
  -->
  <xsl:template mode="iso19139" match="gmd:polygon" priority="20">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:variable name="targetId" select="geonet:element/@ref"/>
    <xsl:variable name="geometry">
      <xsl:apply-templates mode="editXMLElement"/>
    </xsl:variable>

    <xsl:apply-templates mode="complexElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="content">
        <!-- TODO : hide this -->
        <textarea style="display:none;" id="_X{$targetId}" name="_X{$targetId}" rows="5" cols="40">
          <xsl:value-of select="string($geometry)"/>
        </textarea>
        <td class="padded" style="width:100%;">
          <xsl:variable name="ts" select="string(@ts)"/>
          <xsl:variable name="cs" select="string(@cs)"/>
          <xsl:variable name="wktCoords">
            <xsl:apply-templates mode="gml" select="*"/>
          </xsl:variable>
          <xsl:variable name="geom">POLYGON(<xsl:value-of
            select="java:replace(string($wktCoords), '\),$', ')')"/>)
          </xsl:variable>
          <xsl:call-template name="showMap">
            <xsl:with-param name="edit" select="$edit"/>
            <xsl:with-param name="mode" select="'polygon'"/>
            <xsl:with-param name="coords" select="$geom"/>
            <xsl:with-param name="targetPolygon" select="$targetId"/>
            <xsl:with-param name="eltRef" select="$targetId"/>
          </xsl:call-template>
        </td>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="gml" match="gml:coordinates">
    <xsl:variable name="ts" select="string(@ts)"/>
    <xsl:variable name="cs" select="string(@cs)"/>(<xsl:value-of
    select="java:takeUntil(java:toWktCoords(string(.),$ts,$cs), ';\Z')"/>),
  </xsl:template>
  <xsl:template mode="gml" match="gml:posList">(<xsl:value-of
    select="java:takeUntil(java:posListToWktCoords(string(.), string(@dimension)), ';\Z')"/>),
  </xsl:template>
  <xsl:template mode="gml" match="text()"/>

  <!-- Compute global bbox of current metadata record -->
  <xsl:template name="iso19139-global-bbox">
    <xsl:param name="separator" select="','"/>
    <xsl:if test="//gmd:EX_GeographicBoundingBox">
      <xsl:value-of
        select="math:min(//gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal)"/>
      <xsl:value-of select="$separator"/>
      <xsl:value-of
        select="math:min(//gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal)"/>
      <xsl:value-of select="$separator"/>
      <xsl:value-of
        select="math:max(//gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal)"/>
      <xsl:value-of select="$separator"/>
      <xsl:value-of
        select="math:max(//gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal)"/>
    </xsl:if>
  </xsl:template>

  <!-- Do not allow multiple polygons in same extent. -->
  <xsl:template mode="elementEP"
                match="geonet:child[@name='polygon' and @prefix='gmd' and preceding-sibling::gmd:polygon]"
                priority="20"/>


  <!-- ============================================================================= -->
  <xsl:template mode="iso19139" match="gmd:EX_GeographicBoundingBox" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <!-- regions combobox -->
    <xsl:variable name="places">
      <xsl:if test="$edit=true() and /root/gui/regions/record">
        <xsl:variable name="ref" select="geonet:element/@ref"/>
        <xsl:variable name="keyword" select="string(.)"/>

        <xsl:variable name="selection"
                      select="concat(gmd:westBoundLongitude/gco:Decimal,';',gmd:eastBoundLongitude/gco:Decimal,';',gmd:southBoundLatitude/gco:Decimal,';',gmd:northBoundLatitude/gco:Decimal)"/>
        <xsl:variable name="lang" select="/root/gui/language"/>

        <select name="place" size="1"
                onChange="javascript:setRegion('{gmd:westBoundLongitude/gco:Decimal/geonet:element/@ref}', '{gmd:eastBoundLongitude/gco:Decimal/geonet:element/@ref}', '{gmd:southBoundLatitude/gco:Decimal/geonet:element/@ref}', '{gmd:northBoundLatitude/gco:Decimal/geonet:element/@ref}', this.options[this.selectedIndex], {geonet:element/@ref}, '{../../gmd:description/gco:CharacterString/geonet:element/@ref}')"
                class="md">
          <option value=""/>
          <xsl:for-each select="/root/gui/regions/record">
            <xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>

            <xsl:variable name="value" select="concat(west,',',east,',',south,',',north)"/>
            <option value="{$value}">
              <xsl:if test="$value=$selection">
                <xsl:attribute name="selected"/>
              </xsl:if>
              <xsl:value-of select="label/child::*[name() = $lang]"/>
            </option>
          </xsl:for-each>
        </select>
      </xsl:if>
    </xsl:variable>

    <xsl:apply-templates mode="iso19139" select="gmd:extentTypeCode">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:apply-templates>

    <xsl:apply-templates mode="elementEP" select="geonet:child">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:apply-templates>

    <xsl:variable name="geoBox">
      <xsl:call-template name="geoBoxGUI">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit" select="$edit"/>
        <xsl:with-param name="id" select="geonet:element/@ref"/>
        <xsl:with-param name="sEl" select="gmd:southBoundLatitude"/>
        <xsl:with-param name="nEl" select="gmd:northBoundLatitude"/>
        <xsl:with-param name="eEl" select="gmd:eastBoundLongitude"/>
        <xsl:with-param name="wEl" select="gmd:westBoundLongitude"/>
        <xsl:with-param name="sValue" select="gmd:southBoundLatitude/gco:Decimal/text()"/>
        <xsl:with-param name="nValue" select="gmd:northBoundLatitude/gco:Decimal/text()"/>
        <xsl:with-param name="eValue" select="gmd:eastBoundLongitude/gco:Decimal/text()"/>
        <xsl:with-param name="wValue" select="gmd:westBoundLongitude/gco:Decimal/text()"/>
        <xsl:with-param name="sId" select="gmd:southBoundLatitude/gco:Decimal/geonet:element/@ref"/>
        <xsl:with-param name="nId" select="gmd:northBoundLatitude/gco:Decimal/geonet:element/@ref"/>
        <xsl:with-param name="eId" select="gmd:eastBoundLongitude/gco:Decimal/geonet:element/@ref"/>
        <xsl:with-param name="wId" select="gmd:westBoundLongitude/gco:Decimal/geonet:element/@ref"/>
        <xsl:with-param name="descId"
                        select="../../gmd:description/gco:CharacterString/geonet:element/@ref"/>
        <xsl:with-param name="places" select="$places"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:apply-templates mode="complexElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="content">
        <tr>
          <td align="center">
            <xsl:copy-of select="$geoBox"/>
          </td>
        </tr>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>


</xsl:stylesheet>
