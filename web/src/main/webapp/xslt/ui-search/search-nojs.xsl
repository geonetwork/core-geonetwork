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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
  <!--
  Basic search interface which does not require JS.
  -->
  <xsl:import href="../base-layout-nojs.xsl"/>



  <xsl:template mode="content" match="/">

    <div class="row" style="padding-bottom:20px">
      <div class="col-md-push-3 col-md-6">
        <form action="catalog.search.nojs" class="form-inline">
          <div class="form-group">
            <input type="text"
                   name="any"
                   id="fldAny"
                   value="{/root/request/any}"
                   class="form-control input-large gn-search-text"
                   autofocus=""/>
          </div>
          <div class="form-group">
            <input type="submit" class="btn btn-primary" value="Search"/>
          </div>
          <input type="hidden" name="fast" value="index"/>
        </form>
      </div>
    </div>

    <!-- TODO: Display info about the filter.
    eg. siteLogo if source filter is active, gravatar if contact filter is used. -->

    <!-- Display field stats only -->
    <xsl:variable name="fieldStats" select="/root/search/params/*[. = '']/name()"/>
    <xsl:for-each select="/root/search/response/summary[1]/dimension[@name = $fieldStats and category]">
      <h1><xsl:value-of select="@label"/></h1>

      <xsl:variable name="field" select="@name"/>
      <ul>
        <xsl:for-each select="category">
          <li>
            <a href="?{$field}={@value}"><xsl:value-of select="@label"/> (<xsl:value-of select="@count"/>)</a>
          </li>
        </xsl:for-each>
      </ul>
    </xsl:for-each>

    <!---->
    <textarea cols="100" rows="30">
      <xsl:copy-of select="."/>
    </textarea>


    <xsl:if test="count($fieldStats) = 0">
      <div class="row">
        <div class="col-md-3">
          <xsl:for-each select="/root/search/response[1]/summary">
            <xsl:for-each select="dimension[category]">
              <h1><xsl:value-of select="@label"/></h1>

              <xsl:variable name="field" select="@name"/>
              <ul>
                <xsl:for-each select="category">
                  <li>
                    <a href="?{$field}={@value}"><xsl:value-of select="@label"/> (<xsl:value-of select="@count"/>)</a>
                  </li>
                </xsl:for-each>
              </ul>
            </xsl:for-each>
          </xsl:for-each>
        </div>
        <div class="col-md-9">
          <xsl:for-each select="/root/search/response[@from]">

            <div class="row" style="padding-bottom:20px">
              <div class="col-xs-12">
                From
                <b>
                  <xsl:value-of select="@from"/>
                </b>
                to
                <b>
                  <xsl:value-of select="@to"/>
                </b>
                out of
                <b>
                  <xsl:value-of select="@count"/>
                </b>
                results.
              </div>
            </div>

            <xsl:for-each select="metadata">
              <div class="row" style="padding-bottom:20px;">
                <div class="col-xs-10">
                  <a href="api/records/{*[name()='geonet:info']/uuid}">
                    <xsl:value-of select="title|defaultTitle"/>
                  </a>
                  <br/>
                  <xsl:value-of select="abstract"/>
                </div>
              </div>
            </xsl:for-each>
          </xsl:for-each>
        </div>
      </div>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
