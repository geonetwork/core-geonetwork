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
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:template match="/root">
    <xsl:choose>
      <xsl:when test="descKeys/keyword">
        <xsl:apply-templates select="descKeys/keyword"/>
      </xsl:when>
      <xsl:otherwise>
        <keyword/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="keyword">
    <xsl:param name="ancestor">
      <null></null>
    </xsl:param>

    <xsl:variable name="iso3code" select="//descKeys/keyword[1]/defaultLang"/>
    <keyword>
      <xsl:variable name="contents">
        <xsl:copy-of select="uri"/>
        <prefLabel>
          <xsl:for-each select="value[. != '']">
            <xsl:element name="{util:threeCharLangCode(@lang|@language)}">
              <xsl:value-of select="text()"/>
            </xsl:element>
            <xsl:element name="{lower-case(substring(@lang|@language, 1, 2))}">
              <xsl:value-of select="text()"/>
            </xsl:element>
          </xsl:for-each>
        </prefLabel>
        <thesaurus>
          <xsl:value-of select="//request/thesaurus"/>
        </thesaurus>
      </xsl:variable>

      <xsl:copy-of select="$contents"/>

      <xsl:if test="not($ancestor/null)">
        <ancestor>
          <xsl:copy-of select="$ancestor"/>
        </ancestor>
      </xsl:if>

      <xsl:if test="count(../broader/*)>0">
        <broader>
          <xsl:apply-templates select="../broader/keyword">
            <xsl:with-param name="ancestor" select="$contents"/>
          </xsl:apply-templates>
        </broader>
      </xsl:if>
      <xsl:if test="count(../narrower/*)>0">
        <narrower>
          <xsl:apply-templates select="../narrower/keyword">
            <xsl:with-param name="ancestor" select="$contents"/>
          </xsl:apply-templates>
        </narrower>
      </xsl:if>
      <xsl:if test="count(../related/*)>0">
        <related>
          <xsl:apply-templates select="../related/keyword">
            <xsl:with-param name="ancestor" select="$contents"/>
          </xsl:apply-templates>
        </related>
      </xsl:if>
    </keyword>
  </xsl:template>

</xsl:stylesheet>
