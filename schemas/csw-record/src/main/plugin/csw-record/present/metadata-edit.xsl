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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
>

  <xsl:include href="metadata-view.xsl"/>

  <!-- main template - the way into processing csw-record which is
         published as dublic-core -->
  <xsl:template name="csw-record">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="embedded"/>

    <xsl:apply-templates mode="dublin-core" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="embedded" select="$embedded"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- CompleteTab template - csw-record just calls completeTab from
         metadata-utils.xsl -->
  <xsl:template name="csw-recordCompleteTab">
    <xsl:param name="tabLink"/>

    <xsl:call-template name="mainTab">
      <xsl:with-param name="title" select="/root/gui/strings/completeTab"/>
      <xsl:with-param name="default">metadata</xsl:with-param>
      <xsl:with-param name="menu">
        <item label="metadata">metadata</item>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- Brief template - csw-record just calls Brief from
         dublin-core -->
  <xsl:template name="csw-recordBrief">
    <xsl:call-template name="dublin-coreBrief"/>
  </xsl:template>

</xsl:stylesheet>
