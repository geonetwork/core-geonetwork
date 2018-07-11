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
                xmlns:atom="http://www.w3.org/2005/Atom"
                xmlns:georss="http://www.georss.org/georss"
                xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
                xmlns:inspire_dls="http://inspire.ec.europa.eu/schemas/inspire_dls/1.0"
                exclude-result-prefixes="#all" version="1.0">

  <xsl:output omit-xml-declaration="yes" method="html" doctype-system="html" indent="yes"
              encoding="UTF-8"/>

  <!-- Catalog settings -->
  <xsl:variable name="env" select="/root/gui/systemConfig"/>

  <xsl:template match="/root">
    <html>
      <head>
        <head>
          <title>
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

          <style>
            html {
              font-family: sans-serif;
              font-size: 0.9em;
            }

            div.feed {
              border: 1px solid #000;
              margin: 5px;
              background-color: #ededed;
            }

            .feed-title {
              font-weight: bold;
            }

            .column-title {
              float: left;
              width: 15%;
              font-weight: bold;
            }

            .column-content {
              float: left;
              width: 85%;
            }

            .row {
              padding: 5px;
            }

            /* Clear floats after the columns */
            .row:after {
              content: "";
              display: table;
              clear: both;
            }
          </style>
        </head>


      </head>
      <body>
        <h1><xsl:value-of select="/root/gui/strings/atom/results/header" /></h1>

        <xsl:if test="count(feeds/atom:feed) = 0">
          <p><xsl:value-of select="/root/gui/strings/atom/results/noresults" /></p>
        </xsl:if>


        <xsl:for-each select="feeds/atom:feed">
          <div class="feed">
            <div class="row">
              <div class="column-title">
                <span><xsl:value-of select="/root/gui/strings/atom/results/title" /></span>
              </div>
              <div class="column-content">
                <span class="feed-title"><xsl:value-of select="atom:title" /></span>
              </div>
            </div>

            <div class="row">
              <div class="column-title">
                <span><xsl:value-of select="/root/gui/strings/atom/results/content" /></span>
              </div>
              <div class="column-content">
                <span><xsl:value-of select="atom:content" /></span>
              </div>
            </div>

            <div class="row">
              <div class="column-title">
                <span><xsl:value-of select="/root/gui/strings/atom/results/id" /></span>
              </div>
              <div class="column-content">
                <xsl:choose>
                  <xsl:when test="starts-with(atom:id, 'http')">
                    <a href="{atom:id}"><xsl:value-of select="atom:id" /></a>
                  </xsl:when>
                  <xsl:otherwise>
                    <span><xsl:value-of select="atom:id" /></span>
                  </xsl:otherwise>
                </xsl:choose>
               </div>
            </div>

            <div class="row">
              <div class="column-title">
                <span><xsl:value-of select="/root/gui/strings/atom/results/rights" /></span>
              </div>
              <div class="column-content">
                <span><xsl:value-of select="atom:rights" /></span>
              </div>
            </div>

            <xsl:if test="atom:link[@rel='describedby']">
              <div class="row">
                <div class="column-title">
                  <span><xsl:value-of select="/root/gui/strings/atom/results/describedby" /></span>
                </div>
                <div class="column-content">
                  <a href="{atom:link[@rel='describedby']/@href}"><xsl:value-of select="atom:link[@rel='describedby']/@href" /></a>
                </div>
              </div>
            </xsl:if>

            <div class="row">
              <div class="column-title">
                <span><xsl:value-of select="/root/gui/strings/atom/results/author" /></span>
              </div>
              <div class="column-content">
                <span><xsl:value-of select="atom:author/atom:name" />
                  <xsl:if test="atom:author/atom:email">
                  - <a href="{atom:author/atom:email}"><xsl:value-of select="atom:author/atom:email" /></a>
                  </xsl:if>
                </span>
              </div>
            </div>

            <div class="row">
              <div class="column-title">
                <span><xsl:value-of select="/root/gui/strings/atom/results/updated" /></span>
              </div>
              <div class="column-content">
                <span><xsl:value-of select="atom:updated" /></span>
              </div>
            </div>
          </div>
        </xsl:for-each>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
