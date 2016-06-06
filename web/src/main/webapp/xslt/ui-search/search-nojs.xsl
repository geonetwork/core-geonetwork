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
            <input type="text" name="any" id="fldAny"
                   class="form-control input-large gn-search-text" autofocus=""/>
          </div>
          <div class="form-group">
            <input type="submit" class="btn btn-primary" value="Search"/>
          </div>
          <input type="hidden" name="fast" value="index"/>
        </form>
      </div>
    </div>


    <xsl:if test="/root/request/*">

      <div class="row" style="padding-bottom:20px">
        <div class="col-xs-12">
          From
          <b>
            <xsl:value-of select="/root/response/@from"/>
          </b>
          to
          <b>
            <xsl:value-of select="/root/response/@to"/>
          </b>
          out of
          <b>
            <xsl:value-of select="/root/response/summary/@count"/>
          </b>
          results.
        </div>
      </div>

      <xsl:for-each select="/root/response/metadata">
        <div class="row" style="padding-bottom:20px;">
          <div class="col-xs-10">
            <a href="../../metadata/{*[name()='geonet:info']/uuid}">
              <xsl:value-of select="title|defaultTitle"/>
            </a>
            <br/>
            <xsl:value-of select="abstract"/>
          </div>
        </div>
      </xsl:for-each>
    </xsl:if>


    <div class="row" style="background-color:#999;color:white;padding:40px">
      <h2>Browse by topic</h2>
      <xsl:for-each select="root/response/summary/topicCats/topicCat">
        <div class="col-xs-12 col-sm-6 col-lg-4" style="padding:15px">
          <a style="color:white" href="catalog.search.nojs?fast=index&amp;any={@name}">
            <xsl:value-of select="@label"/> (<xsl:value-of select="@count"/>)
          </a>
        </div>
      </xsl:for-each>
    </div>


  </xsl:template>

</xsl:stylesheet>
