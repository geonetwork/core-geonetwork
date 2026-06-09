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
  xmlns:saxon="http://saxon.sf.net/"
  extension-element-prefixes="saxon"
  version="2.0">


  <!-- Register here the list of process for the schema-->
  <xsl:include href="process/collection-updater.xsl"/>
  <xsl:include href="process/keywords-comma-exploder.xsl"/>
  <xsl:include href="process/scale-denominator-formatter.xsl"/>
  <xsl:include href="process/add-extent-from-geokeywords.xsl"/>
  <xsl:include href="process/add-info-from-wms.xsl"/>
  <xsl:include href="process/add-service-info-from-wxs.xsl"/>
  <xsl:include href="process/linked-data-checker.xsl"/>
  <xsl:include href="process/related-metadata-checker.xsl"/>
  <xsl:include href="process/add-resource-id.xsl"/>
  <xsl:include href="process/add-date-for-status.xsl"/>
  <!--<xsl:include href="process/vacuum.xsl"/>-->
  <!--  Disabled by default because related to INSPIRE only
    <xsl:include href="process/inspire-add-conformity.xsl"/>
    <xsl:include href="process/inspire-add-dq-toporeport.xsl"/>
    <xsl:include href="process/inspire-themes-and-topiccategory.xsl"/>
  -->

  <xsl:variable name="processes">
    <p>collection-updater</p>
    <p>keywords-comma-exploder</p>
    <p>scale-denominator-formatter</p>
    <p>add-extent-from-geokeywords</p>
    <p>add-info-from-wms</p>
    <p>add-service-info-from-wxs</p>
    <p>linked-data-checker</p>
    <p>related-metadata-checker</p>
    <p>add-resource-id</p>
    <p>add-date-for-status</p>
    <!--<p>vacuum</p>-->
    <!--  Disabled by default because related to INSPIRE only
        <p>inspire-add-conformity</p>
        <p>inspire-add-dq-toporeport</p>
        <p>inspire-themes-and-topiccategory</p>
    -->
  </xsl:variable>

  <xsl:param name="action" select="'list'"/>
  <xsl:param name="process" select="''"/>

  <!-- Analyze or process -->
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$action= 'list' or $action = 'analyze'">
        <xsl:variable name="root" select="/"/>

        <suggestions>
          <!-- Filter process if user ask for a specific one. If not loop over all. -->
          <xsl:for-each select="if ($process='') then $processes/p else $processes/p[.=$process]">
            <xsl:variable name="tplName" select="concat($action, '-',.)"/>
            <saxon:call-template name="{$tplName}">
              <xsl:with-param name="root" select="$root"/>
              <xsl:fallback>
                <xsl:message>Fall back as no saxon:call-template exists</xsl:message>
              </xsl:fallback>
            </saxon:call-template>
          </xsl:for-each>
        </suggestions>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
