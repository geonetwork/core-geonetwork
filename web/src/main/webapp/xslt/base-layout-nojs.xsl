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

<!--
  The main entry point for all user interface generated
  from XSLT.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:output omit-xml-declaration="yes" method="html" doctype-system="html" indent="yes"
              encoding="UTF-8"/>

  <xsl:include href="common/base-variables.xsl"/>
  <xsl:include href="skin/default/skin.xsl"/>
  <xsl:include href="base-layout-cssjs-loader.xsl"/>

  <xsl:template match="/">
    <html>
      <head>
        <title><!-- todo: md title / md desc / md keywords -->
          <xsl:value-of select="concat($env/system/site/name, ' - ', $env/system/site/organization)"
          />
        </title>
        <meta charset="utf-8"/>
        <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
        <meta name="apple-mobile-web-app-capable" content="yes"/>

        <meta name="description" content=""/>
        <meta name="keywords" content=""/>


        <link rel="icon" sizes="16x16 32x32 48x48" type="image/png"
              href="../../images/logos/favicon.png"/>
        <link href="rss.search?sortBy=changeDate" rel="alternate" type="application/rss+xml"
              title="{concat($env/system/site/name, ' - ', $env/system/site/organization)}"/>
        <link href="portal.opensearch" rel="search" type="application/opensearchdescription+xml"
              title="{concat($env/system/site/name, ' - ', $env/system/site/organization)}"/>

        <xsl:call-template name="css-load"/>
      </head>


      <!-- The GnCatController takes care of
      loading site information, check user login state
      and a facet search to get main site information.
      -->
      <body>
        <div class="gn-full">
          <xsl:call-template name="header"/>
          <xsl:apply-templates mode="content" select="."/>
          <xsl:call-template name="footer"/>
        </div>
      </body>
    </html>
  </xsl:template>


</xsl:stylesheet>
