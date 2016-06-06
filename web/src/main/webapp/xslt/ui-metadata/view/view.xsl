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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:import href="../../common/base-variables-metadata.xsl"/>

  <xsl:import href="../../common/functions-metadata.xsl"/>
  <!-- Add template utility-->

  <xsl:import href="../../common/profiles-loader.xsl"/>

  <xsl:import href="../form-builder.xsl"/>

  <xsl:output omit-xml-declaration="yes" method="html" doctype-public="html" indent="yes"
              encoding="UTF-8"/>

  <xsl:template match="/">
    <article class="gn-metadata-view">
      <xsl:call-template name="scroll-spy-nav-bar"/>

      <!-- Dispatch to profile mode -->
      <xsl:variable name="profileTemplate" select="concat('render-',$schema)"/>
      <saxon:call-template name="{$profileTemplate}">
        <xsl:with-param name="base" select="$metadata"/>
      </saxon:call-template>

    </article>
  </xsl:template>

</xsl:stylesheet>
