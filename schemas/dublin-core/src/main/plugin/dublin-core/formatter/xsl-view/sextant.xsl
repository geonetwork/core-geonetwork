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
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:variable name="dateFormatRegex"
                select="'(\d{4}-[01]\d-[0-3]\d.*)'"/>


  <xsl:template name="sextant-summary-view">
    <table class="table gn-sextant-view-table">
      <tr>
        <td>
          <xsl:call-template name="landingpage-label">
            <xsl:with-param name="key" select="'sxt-view-date'"/>
          </xsl:call-template>
        </td>
        <td>
          <xsl:for-each select="$metadata/dc:date">
            <dl>
              <xsl:value-of select="if (matches(., $dateFormatRegex))
                                    then format-date(xs:date(tokenize(., 'T')[1]),
                                                      '[D01]-[M01]-[Y0001]')
                                    else ."/>
            </dl>
          </xsl:for-each>
        </td>
      </tr>
      <xsl:variable name="authors"
                    select="$metadata/dc:creator"/>
      <xsl:if test="count($authors) > 0">
        <tr>
          <td>
            <xsl:call-template name="landingpage-label">
              <xsl:with-param name="key" select="'sxt-view-author'"/>
            </xsl:call-template>
          </td>
          <td>
            <xsl:for-each select="$authors">
              <dl>
                <xsl:value-of select="."/>
              </dl>
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>


      <xsl:variable name="publisher"
                    select="$metadata/dc:publisher"/>
      <xsl:if test="count($publisher) > 0">
        <tr>
          <td>
            <xsl:call-template name="landingpage-label">
              <xsl:with-param name="key" select="'sxt-view-publisher'"/>
            </xsl:call-template>
          </td>
          <td>
            <xsl:for-each select="$publisher">
              <dl>
                <xsl:value-of select="."/>
              </dl>
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>


      <tr>
        <td>
          <xsl:call-template name="landingpage-label">
            <xsl:with-param name="key" select="'sxt-view-constraints'"/>
          </xsl:call-template>
        </td>
        <td class="">
          <xsl:for-each select="$metadata/dc:rights">
            <xsl:value-of select="."/>
          </xsl:for-each>
        </td>
      </tr>


      <xsl:if test="$portalLink = ''">
        <tr id="sextant-related">
          <td>
            <xsl:call-template name="landingpage-label">
              <xsl:with-param name="key" select="'sxt-view-related'"/>
            </xsl:call-template>
          </td>
          <td>
            <div gn-related="md"
                 data-user="user"
                 data-container="#sextant-related"
                 data-types="{$sideRelated}"><xsl:comment>.</xsl:comment>
            </div>
          </td>
        </tr>
      </xsl:if>
    </table>
    <br/>
  </xsl:template>
</xsl:stylesheet>
