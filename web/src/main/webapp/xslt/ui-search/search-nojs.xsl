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

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- 
  Basic search interface which does not require JS.
  -->
  <xsl:import href="../base-layout.xsl"/>

  <xsl:template mode="content" match="/">
    <div class="row">
      <div class="col-lg-9">
        <form action="catalog.search.nojs">
          <fieldset>
            <input type="hidden" name="fast" value="index"/>
            <div class="form-group">
              <input type="text" name="any" class="form-control input-large gn-search-text" autofocus=""/>
            </div>
          </fieldset>
        </form>

        <xsl:if test="/root/request/*">
          <xsl:for-each select="/root/response/metadata">
            <li>
              <h2>
                <xsl:value-of select="title"/>
              </h2>
              <xsl:value-of select="abstract"/>
            </li>
          </xsl:for-each>
        </xsl:if>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>
