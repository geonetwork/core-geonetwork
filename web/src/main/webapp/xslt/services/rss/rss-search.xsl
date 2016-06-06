<?xml version="1.0" encoding="UTF-8" ?>
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                exclude-result-prefixes="#all">

  <xsl:output method="xml" media-type="application/rss+xml"/>

  <xsl:strip-space elements="*"/>

  <xsl:include href="rss-utils.xsl"/>


  <xsl:template match="/root">

    <rss version="2.0"
    >
      <channel>

        <title>
          <xsl:value-of
            select="concat($env/system/site/name, ' (', $env/system/site/organization, ')')"/>
        </title>
        <link>
          <xsl:value-of select="$baseURL"/>
        </link>
        <description>
          <!-- TODO : use CSW abstract here or a new setting -->
        </description>
        <language>
          <xsl:value-of select="$lang"/>
        </language>
        <copyright>
          <!-- TODO : use CSW access constraint here or a new setting -->
        </copyright>
        <category>Geographic metadata catalog</category>
        <generator>GeoNetwork opensource</generator>

        <!-- FIXME -->
        <ttl>30</ttl>

        <xsl:apply-templates mode="item" select="//rssItems/*[name() != 'summary']"/>

      </channel>
    </rss>
  </xsl:template>

</xsl:stylesheet>
