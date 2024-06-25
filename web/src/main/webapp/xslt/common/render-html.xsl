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

  <xsl:param name="cssClass" select="''"/>

  <xsl:include href="../base-layout-cssjs-loader.xsl"/>
  <xsl:include href="../skin/default/skin.xsl"/>


  <xsl:template name="render-html">
    <xsl:param name="content"/>
    <xsl:param name="title"
               select="/root/gui/systemConfig/settings/system/site/name"/>
    <xsl:param name="description"
               select="/root/gui/systemConfig/settings/strings/mainpage2"/>
    <xsl:param name="thumbnail"
               select="concat(/root/gui/url,'/images/logos/favicon.png')"/>
    <xsl:param name="type"
               select="'dataset'"/>
    <xsl:param name="meta" required="no" as="node()*"/>


    <html ng-app="{$angularModule}" lang="{$lang2chars}" id="ng-app">
      <head>
        <title><xsl:value-of select="if($title != '')
                  then $title
                  else /root/gui/systemConfig/settings/system/site/name"/></title>
        <base href="{$nodeUrl}eng/catalog.search"/>
        <meta charset="utf-8"/>

        <xsl:copy-of select="$meta"/>

        <meta name="viewport" content="initial-scale=1.0"/>
        <meta name="apple-mobile-web-app-capable" content="yes"/>

        <meta name="description" content="{normalize-space($description)}"/>
        <meta name="keywords" content=""/>

        <meta property="og:title" content="{$title}" />
        <meta property="og:description" content="{normalize-space($description)}" />
        <meta property="og:site_name" content="{/root/gui/systemConfig/settings/system/site/name}" />
        <meta property="og:image" content="{$thumbnail}" />

        <meta name="twitter:card" content="summary" />
        <meta name="twitter:image" content="{$thumbnail}" />
        <meta name="twitter:title" content="{$title}" />
        <meta name="twitter:description" content="{normalize-space($description)}" />
        <meta name="twitter:site" content="{/root/gui/systemConfig/settings/system/site/name}" />

        <xsl:if test="/root/info/record/uuid">
          <link rel="canonical" href="{$nodeUrl}api/records/{/root/info/record/uuid}" />
        </xsl:if>
        <link rel="icon" sizes="16x16 32x32 48x48" type="image/png"
              href="{/root/gui/url}/images/logos/favicon.png"/>
        <link href="{$nodeUrl}eng/rss.search?sortBy=changeDate"
              rel="alternate"
              type="application/rss+xml"
              title="{$title}"/>
        <link href="{$nodeUrl}eng/portal.opensearch"
              rel="search"
              type="application/opensearchdescription+xml"
              title="{$title}"/>

        <xsl:call-template name="css-load-nojs"/>

      </head>

      <body class="gn-nojs {$cssClass}">
        <div class="gn-full">
          <xsl:call-template name="header"/>
          <div class="container" role="main">
            <xsl:copy-of select="$content"/>
          </div>
          <xsl:call-template name="footer"/>
        </div>

        <xsl:call-template name="webAnalytics"/>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
