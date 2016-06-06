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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                version="2.0" exclude-result-prefixes="xsl geonet">


  <xsl:include href="../../common/profiles-loader-tpl-brief.xsl"/>
  <xsl:include href="metadata-fop.xsl"/>

  <!--
    Start FOP layout
  -->
  <xsl:template match="/root">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <xsl:call-template name="fop-master"/>

      <fo:page-sequence master-reference="simpleA4" initial-page-number="1">

        <xsl:call-template name="fop-footer"/>

        <fo:flow flow-name="xsl-region-body">

          <!-- Banner level -->
          <xsl:call-template name="banner"/>


          <fo:block font-size="{$font-size}">

            <fo:table width="100%" table-layout="fixed">
              <fo:table-column column-width="1.8cm"/>
              <fo:table-column column-width="18.2cm"/>
              <fo:table-body>
                <fo:table-row height="8mm">
                  <fo:table-cell display-align="center" number-columns-spanned="2">
                    <fo:block text-align="center" color="{$font-color}">
                      <xsl:value-of select="/root/response/summary/@count"/>
                      <xsl:text> </xsl:text>
                      <xsl:value-of select="/root/gui/strings/ress"/>
                      <xsl:if test="/root/response/summary/@count &gt; 1">s</xsl:if>
                    </fo:block>
                  </fo:table-cell>
                </fo:table-row>

                <xsl:call-template name="fo">
                  <xsl:with-param name="res" select="/root/response"/>
                </xsl:call-template>

              </fo:table-body>
            </fo:table>
          </fo:block>

          <fo:block id="terminator"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <!-- ============================================================== -->

  <xsl:template match="geonet:info/title" mode="strip"/>

  <xsl:template match="@*|node()" mode="strip">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="strip"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
