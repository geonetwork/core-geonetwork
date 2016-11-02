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

<xsl:stylesheet xmlns:j2e="http://java.sun.com/xml/ns/javaee" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0" exclude-result-prefixes="j2e xsl">

  <xsl:output encoding="UTF-8" indent="yes"
              media-type="text/xml"/>

  <xsl:param name="nodeId"/>
  <xsl:param name="host"/>

  <!-- Add the servlet mapping to the new node -->
  <xsl:template match="/j2e:web-app" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:message>Add servlet mapping for <xsl:value-of select="$nodeId"/></xsl:message>
      <xsl:apply-templates select="*"/>

      <xsl:if
        test="count(//j2e:servlet-mapping/j2e:url-pattern[text() = concat('/', $nodeId, '/*')]) = 0">
        <xsl:element name="servlet-mapping" namespace="http://java.sun.com/xml/ns/javaee">
          <xsl:element name="servlet-name" namespace="http://java.sun.com/xml/ns/javaee">spring</xsl:element>
          <xsl:element name="url-pattern" namespace="http://java.sun.com/xml/ns/javaee">/<xsl:value-of select="$nodeId"/>/*</xsl:element>
        </xsl:element>
      </xsl:if>
    </xsl:copy>
  </xsl:template>

  <!-- Update trustedHost -->
  <xsl:template match="j2e:filter[j2e:filter-name = 'springSecurityFilterChain']/j2e:init-param[j2e:param-name = 'trustedHost']" priority="10">
    <xsl:message>Update trustedHost for <xsl:value-of select="$host"/></xsl:message>
    <xsl:copy>
      <xsl:copy-of select="j2e:param-name" />
            
      <xsl:choose>
        <xsl:when test="not(string(j2e:param-value))">
          <xsl:element name="param-value" namespace="http://java.sun.com/xml/ns/javaee"><xsl:value-of select="$host" /></xsl:element>          
        </xsl:when>
        <xsl:when test="contains(concat(j2e:param-value, ','), concat($host, ','))">
          <xsl:copy-of select="j2e:param-value" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:element name="param-value" namespace="http://java.sun.com/xml/ns/javaee"><xsl:value-of select="concat(j2e:param-value, ',', $host)" /></xsl:element>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*|*|text()|comment()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|comment()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
