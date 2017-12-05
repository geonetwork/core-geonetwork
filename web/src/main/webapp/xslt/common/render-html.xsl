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

  <xsl:include href="../base-layout-cssjs-loader.xsl"/>
  <xsl:include href="../skin/default/skin.xsl"/>

  <xsl:output omit-xml-declaration="yes"
              method="html"
              doctype-system="html"
              indent="yes"
              encoding="UTF-8"/>

  <xsl:template name="render-html">
    <xsl:param name="content"/>
    <xsl:param name="title"
               select="''"/>
    <xsl:param name="description"
               select="''"/>

    <html>
      <head>
        <title><xsl:value-of select="$title"/></title>
        <meta charset="utf-8"/>
        <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
        <meta name="apple-mobile-web-app-capable" content="yes"/>

        <meta name="description" content="{normalize-space($description)}"/>
        <meta name="keywords" content=""/>


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

        <xsl:call-template name="css-load"/>
      </head>

      <body>
        <div class="gn-full">
          <xsl:call-template name="header"/>
          <div class="container">
            <xsl:copy-of select="$content"/>
          </div>
          <xsl:call-template name="footer"/>
        </div>

        <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
        <script src="http://code.jquery.com/jquery-1.12.4.min.js"
                integrity="sha256-ZosEbRLbNQzLpnKIkEdrPv7lOy9C27hHQ+Xp8a4MxAQ="
                crossorigin="anonymous">
          &#160;
        </script>
      
        <!-- Latest compiled and minified JavaScript -->
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" 
                integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
                crossorigin="anonymous">
          &#160;
        </script>
      
        <script type="text/javascript">
          // attach click to tab
          $('.nav-tabs-advanced a').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
          });
          // hide empty tab     
          $('.nav-tabs-advanced a').each(function() {
      
            var tabLink = $(this).attr('href');
      
            if (tabLink) {
              if ($(tabLink).length === 0) {
                $(this).parent().hide();
              }
            }
          });
          // show the first tab
          $('.nav-tabs-advanced a:first').tab('show');
        </script>
        <xsl:call-template name="css-load"/>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
