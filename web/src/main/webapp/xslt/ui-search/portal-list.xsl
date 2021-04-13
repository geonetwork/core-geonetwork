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
                version="2.0"
                exclude-result-prefixes="#all">
  <xsl:import href="../base-layout-nojs.xsl"/>
  <xsl:import href="../common/functions-core.xsl"/>

  <xsl:template mode="content" match="/">

    <xsl:variable name="infoSeparator" select="'|'"/>

    <div class="row gn-portal"
         id="{/root/gui/systemConfig/system/site/siteId}">
      <div class="container-fluid gn-background">
        <div style="text-align:center;">
          <div class="gn-md-thumbnail">
            <a href="{$nodeUrl}">
              <img class="gn-portal-main-logo"
                   src="../../images/logos/{$env//system/site/siteId}.png"/>
            </a>
          </div>

          <a href="{$nodeUrl}">
            <h1>
              <xsl:value-of select="/root/gui/systemConfig/system/site/name"/>
            </h1>
            <h2>
              <xsl:value-of select="/root/gui/systemConfig/system/site/organization"/>
            </h2>
          </a>
        </div>
      </div>
    </div>
    <div class="container gn-info-list-blocks">
      <ul class="row gn-info-list">
        <xsl:for-each select=".//sources/record[type = 'subportal']">
          <xsl:sort select="label/*[name() = $lang]"/>
          <li>
            <xsl:variable name="portalInfo"
                          select="if (label/*[name() = $lang] != '')
                                              then label/*[name() = $lang]
                                              else name"/>
            <xsl:variable name="portalTitle"
                          select="if (contains($portalInfo, $infoSeparator))
                                  then substring-before($portalInfo, $infoSeparator)
                                  else $portalInfo"/>
            <xsl:variable name="portalDescription"
                          select="if (contains($portalInfo, $infoSeparator))
                                  then substring-after($portalInfo, $infoSeparator)
                                  else ''"/>
            <a href="../../{uuid}"
               title="{$portalInfo}">
              <section class="resultcard clearfix hasThumbnail">
                <div class="gn-md-thumbnail">
                  <div class="gn-img-thumbnail"
                        style="background-image: url(../../images/harvesting/{if (logo != '') then logo else 'blank.png'})">
                  </div>
                </div>
                <div class="title">
                  <h1>
                    <xsl:value-of select="$portalTitle"/>
                  </h1>
                  <p>
                    <xsl:value-of select="$portalDescription"/>
                  </p>
                </div>
              </section>
            </a>
          </li>
        </xsl:for-each>
      </ul>
      <br/>
    </div>
  </xsl:template>
</xsl:stylesheet>
