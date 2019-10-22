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
                version="2.0" xmlns:xls="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all">
  <xsl:import href="../base-layout-nojs.xsl"/>
  <xsl:import href="../common/functions-core.xsl"/>

  <xsl:template mode="content" match="/">
    <div class="row gn-portal"
         id="{/root/gui/systemConfig/system/site/siteId}"
         itemscope="itemscope"
         itemtype="http://schema.org/DataCatalog">
      <meta itemprop="name" content="{/root/gui/systemConfig/system/site/name}"></meta>
      <span itemprop="publisher" itemscope="itemscope" itemtype="http://schema.org/Organization">
        <meta itemprop="name" content="{/root/gui/systemConfig/system/site/organization}"></meta>
        <meta itemprop="email" content="{/root/gui/systemConfig/system/feedback/email}"></meta>
      </span>
      <meta itemprop="url" content="{$nodeUrl}search"></meta>

      <div class="col-md-6">
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
      <div class="col-md-6">
        <br/>
        <!--<div class="well">
          <br/>
        </div>-->
      </div>
    </div>
    <div class="gn-info-list-blocks">
        <ul class="row gn-info-list">
          <xsl:for-each select=".//sources/record[type = 'subportal']">
            <xsl:sort select="label/*[name() = $lang]"/>
            <li>
              <section class="resultcard clearfix hasThumbnail hasThumbnail">
                <div class="title">
                  <a href="../../{uuid}">
                    <h4><xsl:value-of select="if (label/*[name() = $lang] != '') then label/*[name() = $lang] else name"/></h4>
                  </a>
                </div>
                <div class="gn-md-thumbnail">
                  <a href="../../{uuid}">
                    <div class="gn-img-thumbnail"
                         style="background-image: url(../../images/harvesting/{if (logo != '') then logo else 'blank.png'})"><br/></div>
                  </a>
                </div>
                <div class="content">
                  <div class="abstract">
                  </div>
                  <div class="introduction"> </div>
                </div>
              </section>
            </li>
          </xsl:for-each>
        </ul>
        <br/>
    </div>
  </xsl:template>
</xsl:stylesheet>
